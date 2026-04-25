<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'
import { useResourceList } from '@/composables/useResourceList'
import EmptyState from '@/components/EmptyState.vue'
import L from 'leaflet'
import {
  createMap, addMarker, addRoute, clusterOptions, fitBounds,
  attachRouteVisibilityHandler,
} from '@/composables/useDeliveraMap'
import { MAP_DEFAULT_CENTER, MAP_DEFAULT_ZOOM_REGION } from '@/constants/map'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const router = useRouter()
const route = useRoute()
const api = useApi()
const auth = useAuthStore()
const { load: loadConfig, statusSeverity } = useAppConfig()
const { items: orders, loading, error } = useResourceList('/orders')

const successMsg = ref(route.query.created ? t('orders.created', { reference: route.query.created }) : '')
const filterStatus = ref('ALL')
const filterPriority = ref('ALL')
const filterText = ref('')

const mapEl = ref(null)
let map = null
let clusterGroup = null
let routeEntries = []
let mapToken = 0

const statusOptions = computed(() => [
  { label: t('orders.filterAll'), value: 'ALL' },
  { label: t('orders.status.PENDING'), value: 'PENDING' },
  { label: t('orders.status.IN_TRANSIT'), value: 'IN_TRANSIT' },
  { label: t('orders.status.DELIVERED'), value: 'DELIVERED' },
  { label: t('orders.status.CANCELLED'), value: 'CANCELLED' },
])

const priorityOptions = computed(() => [
  { label: t('orders.filterAll'), value: 'ALL' },
  { label: t('orders.priority.HIGH'), value: 'HIGH' },
  { label: t('orders.priority.NORMAL'), value: 'NORMAL' },
  { label: t('orders.priority.LOW'), value: 'LOW' },
])

const filtered = computed(() => {
  return orders.value.filter(o => {
    if (filterStatus.value !== 'ALL' && o.status !== filterStatus.value) return false
    if (filterPriority.value !== 'ALL' && o.priority !== filterPriority.value) return false
    if (filterText.value) {
      const q = filterText.value.toLowerCase()
      const matches = o.reference.toLowerCase().includes(q)
        || (o.originName || '').toLowerCase().includes(q)
        || (o.destinationName || '').toLowerCase().includes(q)
        || (o.recipientEmail || '').toLowerCase().includes(q)
        || (o.recipientName || '').toLowerCase().includes(q)
      if (!matches) return false
    }
    return true
  })
})

// Solo pedidos pendientes con coords origen (para el mapa)
const mappableOrders = computed(() =>
  filtered.value.filter(o =>
    (o.status === 'PENDING' || o.status === 'IN_TRANSIT') &&
    o.originLat && o.originLon
  )
)

function isOwnCompany(companyId) {
  return companyId && auth.companyId && companyId === auth.companyId
}

function originKind(o) {
  return isOwnCompany(o.originCompanyId) ? 'OWN_UNIT' : 'OTHER_UNIT'
}

function originNavigateTo(o) {
  return isOwnCompany(o.originCompanyId) ? `/units/${o.originId}` : null
}

function destinationKind(o) {
  if (o.destinationId) {
    return isOwnCompany(o.destinationCompanyId) ? 'OWN_UNIT' : 'OTHER_UNIT'
  }
  return 'CUSTOMER'
}

function destinationNavigateTo(o) {
  if (o.destinationId && isOwnCompany(o.destinationCompanyId)) {
    return `/units/${o.destinationId}`
  }
  if (!o.destinationId && o.loyalUserId) return `/loyal-users/${o.loyalUserId}`
  return null
}

function destinationTitle(o) {
  return o.destinationName || o.recipientName || o.recipientEmail || ''
}

function initMap() {
  if (!mapEl.value) return
  if (map) { map.remove(); map = null }
  map = createMap(mapEl.value)
  map.setView(MAP_DEFAULT_CENTER, MAP_DEFAULT_ZOOM_REGION)
  updateMapOrders()
}

function clearRoutes() {
  routeEntries.forEach(e => { if (map && e?.layer) map.removeLayer(e.layer) })
  routeEntries = []
}

async function updateMapOrders() {
  if (!map) return
  const token = ++mapToken
  if (clusterGroup) { map.removeLayer(clusterGroup); clusterGroup = null }
  clearRoutes()

  const items = mappableOrders.value
  if (items.length === 0) return

  clusterGroup = L.markerClusterGroup(clusterOptions())
  const bounds = []
  const markerByKey = new Map()

  function markerFor(key, opts) {
    if (markerByKey.has(key)) return markerByKey.get(key)
    const mk = addMarker(map, opts)
    markerByKey.set(key, mk)
    clusterGroup.addLayer(mk)
    return mk
  }

  items.forEach(o => {
    const oLat = parseFloat(o.originLat)
    const oLon = parseFloat(o.originLon)

    const oKind = originKind(o)
    const oNav = originNavigateTo(o)
    markerFor('u:' + o.originId, {
      id: o.originId, lat: oLat, lon: oLon, kind: oKind,
      title: o.originName, subtitle: t('units.detail'),
      actionLabel: oNav ? t('units.detail') : null,
      navigateTo: oNav, router,
    })
    bounds.push([oLat, oLon])

    if (o.destinationLat != null && o.destinationLon != null) {
      const dLat = parseFloat(o.destinationLat)
      const dLon = parseFloat(o.destinationLon)
      const kind = destinationKind(o)
      const key = o.destinationId ? 'u:' + o.destinationId : (o.loyalUserId ? 'l:' + o.loyalUserId : `c:${dLat},${dLon},${o.recipientEmail || ''}`)
      markerFor(key, {
        id: key, lat: dLat, lon: dLon, kind,
        title: destinationTitle(o),
        subtitle: kind === 'CUSTOMER' ? t('orders.recipientName') : t('units.detail'),
        actionLabel: kind === 'OWN_UNIT' ? t('units.detail') : (kind === 'CUSTOMER' && o.loyalUserId ? t('loyalUsers.detail') : null),
        navigateTo: destinationNavigateTo(o), router,
      })
      bounds.push([dLat, dLon])
    }
  })

  map.addLayer(clusterGroup)
  fitBounds(map, bounds)

  // Instalamos el handler una vez que tenemos el clusterGroup.
  attachRouteVisibilityHandler(map, clusterGroup, () => routeEntries)

  // Rutas (todas las que tienen coords completas)
  const routeable = items.filter(o => o.destinationLat != null && o.destinationLon != null)
  for (const o of routeable) {
    if (token !== mapToken || !map) return
    const originKey = 'u:' + o.originId
    const destKey = o.destinationId ? 'u:' + o.destinationId : (o.loyalUserId ? 'l:' + o.loyalUserId : `c:${o.destinationLat},${o.destinationLon},${o.recipientEmail || ''}`)
    const entry = await addRoute(map, {
      orderId: o.id,
      origin: { lat: parseFloat(o.originLat), lon: parseFloat(o.originLon) },
      dest:   { lat: parseFloat(o.destinationLat), lon: parseFloat(o.destinationLon) },
      popupTitle: o.reference,
      popupSubtitle: `${o.originName} → ${destinationTitle(o)}`,
      actionLabel: t('orders.viewDetail'),
      router,
      originMarker: markerByKey.get(originKey) || null,
      destMarker: markerByKey.get(destKey) || null,
      status: o.status,
      currentLocation: o.currentLat != null && o.currentLon != null
        ? { lat: parseFloat(o.currentLat), lon: parseFloat(o.currentLon) } : null,
    })
    if (token !== mapToken) { if (entry?.layer && map) map.removeLayer(entry.layer); return }
    routeEntries.push(entry)
    // Traer rutas al frente para que queden por encima de los tiles.
    entry.layer?.bringToFront?.()
  }
}

// Debounce: al teclear en el filtro evitamos relanzar updateMapOrders
// (que hace peticiones OSRM por ruta) en cada pulsación.
let filterDebounce = null
watch(filtered, () => {
  if (!map) return
  clearTimeout(filterDebounce)
  filterDebounce = setTimeout(() => updateMapOrders(), 250)
})
onUnmounted(() => clearTimeout(filterDebounce))

async function deleteOrder(e, id) {
  e.stopPropagation()
  if (!confirm(t('orders.deleteConfirm'))) return
  const res = await api.del(`/orders/${id}`)
  if (res.ok) orders.value = orders.value.filter(o => o.id !== id)
}

onMounted(async () => {
  loadConfig()
  await nextTick()
  initMap()
})

onUnmounted(() => { if (map) { map.remove(); map = null } })

watch(orders, async () => {
  await nextTick()
  if (map) updateMapOrders()
  else initMap()
})
</script>

<template>
  <div class="orders-page card-full">
    <div class="orders-split">
      <!-- Panel izquierdo: lista -->
      <div class="orders-list-panel">
        <div class="list-header">
          <h1>{{ t('orders.listTitle') }}</h1>
          <div class="header-actions">
            <PButton
              v-if="auth.canCreateOrders"
              :label="t('orders.new')"
              icon="pi pi-plus"
              @click="router.push('/orders/new')"
            />
          </div>
        </div>

        <div class="filters-wrapper">
          <span class="filters-label">{{ t('common.filters') }}</span>
          <div class="filters-box">
            <div class="filter-row">
              <label class="filter-group-label">{{ t('orders.reference') }}</label>
              <input
                v-model="filterText"
                :placeholder="t('orders.reference') + '...'"
                class="filter-search"
                type="text"
              />
            </div>
            <div class="filter-row">
              <label class="filter-group-label">{{ t('orders.statusLabel') }}</label>
              <PSelect
                v-model="filterStatus"
                :options="statusOptions"
                option-label="label"
                option-value="value"
                class="filter-select"
              />
              <label class="filter-group-label">{{ t('orders.priority.label') }}</label>
              <PSelect
                v-model="filterPriority"
                :options="priorityOptions"
                option-label="label"
                option-value="value"
                class="filter-select"
              />
            </div>
          </div>
        </div>

        <PMessage v-if="successMsg" severity="success" :closable="false">{{ successMsg }}</PMessage>
        <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

        <div class="list-scroll">
        <DataTable
          :value="filtered"
          :loading="loading"
          paginator
          :rows="10"
          striped-rows
          row-hover
          @row-click="e => router.push(`/orders/${e.data.id}`)"
        >
          <template #empty>
            <EmptyState icon="pi-send" :message="t('orders.empty')">
              <PButton
                v-if="auth.canCreateOrders"
                :label="t('orders.new')"
                icon="pi pi-plus"
                size="small"
                @click="router.push('/orders/new')"
              />
            </EmptyState>
          </template>
          <Column field="reference" :header="t('orders.reference')" style="width:150px;font-weight:600" />
          <Column :header="t('orders.route')">
            <template #body="{ data }">
              <span>{{ data.originName }}</span>
              <i class="pi pi-arrow-right" style="margin:0 6px;font-size:11px;color:#94a3b8" />
              <span v-if="data.destinationName">{{ data.destinationName }}</span>
              <span v-else class="recipient-label">{{ data.recipientName || data.recipientEmail }}</span>
            </template>
          </Column>
          <Column :header="t('orders.statusLabel')" style="width:130px">
            <template #body="{ data }">
              <PTag
                :value="t(`orders.status.${data.status}`)"
                :severity="statusSeverity[data.status] ?? 'secondary'"
              />
            </template>
          </Column>
          <Column :header="t('orders.date')" style="width:110px">
            <template #body="{ data }">{{ formatDate(data.createdAt) }}</template>
          </Column>
          <Column v-if="auth.isCompanyAdmin" style="width:48px;padding:0">
            <template #body="{ data }">
              <button class="delete-btn" @click="deleteOrder($event, data.id)" :title="t('common.delete')">
                <i class="pi pi-times" />
              </button>
            </template>
          </Column>
        </DataTable>
        </div>
      </div>

      <!-- Panel derecho: mapa -->
      <div class="orders-map-panel">
        <div ref="mapEl" class="orders-map-el" />
        <div v-if="mappableOrders.length === 0 && !loading" class="map-no-coords">
          {{ t('orders.noMapOrders') }}
        </div>
        <!-- Leyenda -->
        <div v-if="mappableOrders.length > 0" class="map-legend">
          <span class="legend-item"><span class="legend-dot" style="background:#7c3aed"></span>{{ t('map.legend.ownUnit') }}</span>
          <span class="legend-item"><span class="legend-dot" style="background:#a78bfa"></span>{{ t('map.legend.otherUnit') }}</span>
          <span class="legend-item"><span class="legend-line legend-line--solid"></span>{{ t('map.legend.routeSolid') }}</span>
          <span class="legend-item"><span class="legend-line legend-line--dashed"></span>{{ t('map.legend.routeDashed') }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped src="./OrdersView.css"></style>

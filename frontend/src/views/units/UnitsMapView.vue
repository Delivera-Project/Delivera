<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({ iconUrl: markerIcon, iconRetinaUrl: markerIcon2x, shadowUrl: markerShadow })

const { t } = useI18n()
const api = useApi()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const unitsWithCoords = ref(0)
const mapEl = ref(null)
let map = null

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await api.get('/units')
    if (!res.ok) { error.value = t('error.connection'); return }
    const units = await res.json()
    const mapped = units.filter(u => u.latitude != null && u.longitude != null)
    unitsWithCoords.value = mapped.length

    map = L.map(mapEl.value)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map)

    if (mapped.length === 0) {
      map.setView([40.4168, -3.7038], 6)
    } else {
      const bounds = []
      mapped.forEach(u => {
        const lat = parseFloat(u.latitude)
        const lng = parseFloat(u.longitude)
        const marker = L.marker([lat, lng]).addTo(map)
        marker.bindPopup(`
          <strong>${u.name}</strong><br>
          <span>${t('units.' + u.type)}</span><br>
          ${u.address ? `<small>${u.address}</small><br>` : ''}
          <a href="/units/${u.id}" class="map-popup-link">${t('units.detail')}</a>
        `)
        bounds.push([lat, lng])
      })
      map.fitBounds(bounds, { padding: [40, 40] })
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  if (map) { map.remove(); map = null }
})
</script>

<template>
  <div class="card card-wide">
    <div class="list-header">
      <h1>{{ t('units.mapTitle') }}</h1>
      <PButton
        :label="t('units.listView')"
        icon="pi pi-list"
        severity="secondary"
        @click="router.push('/units')"
      />
    </div>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <div v-if="loading" class="map-loading">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <div v-show="!loading && !error" class="map-container">
      <div ref="mapEl" class="map-el" data-testid="units-map" />
      <p v-if="!loading && unitsWithCoords === 0" class="map-no-coords">
        {{ t('units.noCoords') }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.map-container { position: relative; }
.map-el { height: 520px; border-radius: 10px; border: 1px solid #e2e8f0; }
.map-loading { display: flex; justify-content: center; align-items: center; height: 200px; color: #94a3b8; }
.map-no-coords { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; padding: 12px 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.1); color: #64748b; font-size: 14px; pointer-events: none; }
</style>

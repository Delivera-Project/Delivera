<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'
import { useAppConfig } from '@/composables/useAppConfig'
import { useGeolocation } from '@/composables/useGeolocation'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const route = useRoute()
const router = useRouter()
const api = useApi()
const { load: loadConfig, statusSeverity } = useAppConfig()

const loyalUser = ref(null)
const orders = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const editingAddress = ref(false)
const addrForm = ref({ address: '', latitude: null, longitude: null })
const { locating, getPosition } = useGeolocation()
const saving = ref(false)

function startEditAddress() {
  addrForm.value.address = loyalUser.value?.address || ''
  addrForm.value.latitude = loyalUser.value?.latitude ?? null
  addrForm.value.longitude = loyalUser.value?.longitude ?? null
  editingAddress.value = true
  error.value = ''
  success.value = ''
}

async function captureLocation() {
  try {
    const { lat, lon } = await getPosition()
    addrForm.value.latitude = lat
    addrForm.value.longitude = lon
  } catch {
    error.value = t('profile.locationDenied')
  }
}

async function saveAddress() {
  saving.value = true
  error.value = ''
  try {
    const res = await api.put(`/loyal-users/${route.params.id}/address`, {
      email: loyalUser.value.email,
      address: addrForm.value.address || null,
      latitude: addrForm.value.latitude,
      longitude: addrForm.value.longitude,
    })
    if (res.ok) {
      loyalUser.value = await res.json()
      editingAddress.value = false
      success.value = t('profile.updated')
    } else {
      const data = await res.json().catch(() => ({}))
      error.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  loadConfig()
  loading.value = true
  try {
    const [luRes, ordersRes] = await Promise.all([
      api.get(`/loyal-users`),
      api.get(`/loyal-users/${route.params.id}/orders`),
    ])
    if (ordersRes.ok) orders.value = await ordersRes.json()
    if (luRes.ok) {
      const list = await luRes.json()
      loyalUser.value = list.find(l => l.id === route.params.id) || null
    }
    // Si no encontramos el fidelizado mostrar error en lugar de la ficha vacía
    if (!loyalUser.value) {
      error.value = t('loyalUsers.notFound')
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="surface-card card-wide">
    <PButton
      type="button"
      text
      severity="secondary"
      icon="pi pi-arrow-left"
      class="detail-back-btn"
      @click="router.push('/loyal-users')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else>
      <div class="lu-header">
        <div class="lu-avatar"><i class="pi pi-user" /></div>
        <div>
          <h1>{{ loyalUser?.email }}</h1>
          <PTag
            v-if="loyalUser"
            :value="loyalUser.registered ? t('loyalUsers.registered') : t('loyalUsers.notRegistered')"
            :severity="loyalUser?.registered ? 'success' : 'secondary'"
          />
        </div>
      </div>

      <PMessage v-if="success" severity="success" :closable="false" class="mb-3">{{ success }}</PMessage>

      <div class="addr-block">
        <div class="addr-header">
          <h3 class="section-title">{{ t('fields.address') }}</h3>
          <PButton v-if="!editingAddress" type="button" severity="secondary" outlined size="small" icon="pi pi-pencil" :label="t('profile.edit')" @click="startEditAddress" />
        </div>
        <div v-if="!editingAddress">
          <div class="addr-value">{{ loyalUser?.address || t('fields.empty') }}</div>
          <small v-if="loyalUser?.latitude" class="field-hint">{{ loyalUser.latitude }}, {{ loyalUser.longitude }}</small>
        </div>
        <div v-else class="addr-edit">
          <InputText v-model="addrForm.address" :placeholder="t('fields.addressPlaceholder')" maxlength="500" fluid />
          <div class="addr-actions">
            <PButton type="button" :label="t('profile.useCurrentLocation')" icon="pi pi-map-marker" severity="secondary" outlined size="small" :loading="locating" @click="captureLocation" />
            <small v-if="addrForm.latitude" class="field-hint">{{ addrForm.latitude }}, {{ addrForm.longitude }}</small>
          </div>
          <div class="addr-buttons">
            <PButton type="button" :label="t('profile.save')" icon="pi pi-check" size="small" :loading="saving" @click="saveAddress" />
            <PButton type="button" :label="t('profile.cancel')" icon="pi pi-times" severity="secondary" outlined size="small" @click="editingAddress = false" />
          </div>
        </div>
      </div>

      <h3 class="section-title">{{ t('loyalUsers.orders') }}</h3>

      <DataTable :value="orders" striped-rows row-hover @row-click="e => router.push(`/orders/${e.data.id}`)">
        <template #empty>
          <EmptyState icon="pi-send" :message="t('loyalUsers.ordersEmpty')" />
        </template>
        <Column field="reference" :header="t('orders.reference')" style="font-weight:600;width:170px" />
        <Column :header="t('orders.statusLabel')" style="width:130px">
          <template #body="{ data }">
            <PTag :value="t(`orders.status.${data.status}`)" :severity="statusSeverity[data.status]" />
          </template>
        </Column>
        <Column :header="t('orders.date')" style="width:120px">
          <template #body="{ data }">
            {{ formatDate(data.createdAt) }}
          </template>
        </Column>
      </DataTable>
    </template>
  </div>
</template>

<style scoped src="./LoyalUserDetailView.css"></style>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const route = useRoute()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()

const unit = ref(null)
const loading = ref(false)
const error = ref('')
const workerActionError = ref('')

const isAdmin = computed(() => auth.role === 'COMPANY_ADMIN')

async function load() {
  loading.value = true
  try {
    const res = await api.get(`/units/${route.params.id}`)
    if (res.ok) unit.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

async function unassignWorker(workerId) {
  workerActionError.value = ''
  try {
    const res = await api.del(`/units/${route.params.id}/workers/${workerId}`)
    if (res.ok) unit.value = await res.json()
    else { const d = await res.json(); workerActionError.value = api.translateError(d, 'error.saveFailed') }
  } catch { workerActionError.value = t('error.connection') }
}

onMounted(load)
</script>

<template>
  <div class="card card-wide">
    <PButton
      type="button"
      text
      severity="secondary"
      icon="pi pi-arrow-left"
      class="detail-back-btn"
      @click="router.push('/units')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else-if="unit">
      <h1 class="detail-title">{{ unit.name }}</h1>

      <div class="detail-badges">
        <div class="badge-group">
          <span class="info-label">{{ t('fields.type') }}</span>
          <PTag :value="t(`units.${unit.type}`)" />
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item info-item--full">
          <span class="info-label">{{ t('fields.address') }}</span>
          <span class="info-value">{{ unit.address || '—' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">Coordenadas</span>
          <span class="info-value">
            {{ (unit.latitude && unit.longitude) ? `${unit.latitude}, ${unit.longitude}` : t('units.noCoords') }}
          </span>
        </div>
        <div class="info-item">
          <span class="info-label">{{ t('orders.date') }}</span>
          <span class="info-value">{{ formatDate(unit.createdAt) ?? '—' }}</span>
        </div>
      </div>

      <!-- Trabajadores asignados -->
      <div v-if="isAdmin" class="workers-section">
        <h3>{{ t('units.workers') }}</h3>
        <PMessage v-if="workerActionError" severity="error" :closable="false" class="form-message">{{ workerActionError }}</PMessage>
        <div v-if="unit.workers && unit.workers.length" class="worker-list">
          <div v-for="w in unit.workers" :key="w.id" class="worker-row">
            <span class="worker-info">
              <span class="worker-name">{{ w.firstName }} {{ w.lastName }}</span>
              <span class="worker-email">{{ w.email }}</span>
            </span>
            <PTag :value="t('workers.roles.' + w.role)" severity="info" />
            <PButton icon="pi pi-times" text severity="danger" size="small" @click="unassignWorker(w.id)" />
          </div>
        </div>
        <p v-else class="empty-workers">{{ t('units.noWorkers') }}</p>
        <RouterLink :to="`/units/${unit.id}/assign-workers`">
          <PButton :label="t('units.assignWorkers')" icon="pi pi-plus" severity="secondary" size="small" class="assign-btn" />
        </RouterLink>
      </div>
    </template>
  </div>
</template>

<style scoped>
.card { text-align: left; }
.info-grid { border-top: 1px solid #f1f5f9; border-bottom: none; }
.workers-section { margin-top: 28px; padding-top: 20px; border-top: 1px solid #f1f5f9; }
.workers-section h3 { font-size: 14px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.04em; margin: 0 0 16px; }
.worker-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 12px; }
.worker-row { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; }
.worker-info { flex: 1; min-width: 0; }
.worker-name { display: block; font-size: 13px; font-weight: 500; color: #1e293b; }
.worker-email { display: block; font-size: 12px; color: #94a3b8; }
.empty-workers { font-size: 13px; color: #94a3b8; margin: 0 0 12px; }
.assign-btn { margin-top: 4px; }
</style>

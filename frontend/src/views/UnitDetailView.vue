<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const api = useApi()

const unit = ref(null)
const loading = ref(false)
const error = ref('')

onMounted(async () => {
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
})
</script>

<template>
  <div class="card card-wide">
    <PButton
      type="button"
      text
      severity="secondary"
      icon="pi pi-arrow-left"
      class="back-btn"
      @click="router.push('/units')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else-if="unit">
      <h1 class="detail-name">{{ unit.name }}</h1>

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
          <span class="info-value">{{ unit.createdAt ? new Date(unit.createdAt).toLocaleDateString() : '—' }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.card { text-align: left; }
.back-btn { margin-bottom: 16px; }
.loading-state { display: flex; justify-content: center; padding: 60px; color: #94a3b8; }

.detail-name {
  margin: 0 0 16px;
  font-size: 22px;
  font-weight: 700;
  text-align: center;
}

.detail-badges {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 24px;
  margin-bottom: 20px;
}

.badge-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 40px;
  padding: 20px 0 24px;
  border-top: 1px solid #f1f5f9;
}

.info-item--full { grid-column: 1 / -1; }
.info-item { display: flex; flex-direction: column; gap: 5px; }

.info-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #94a3b8;
}

.info-value { font-size: 15px; font-weight: 500; color: #1e293b; }
</style>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDate } = useFormatDate()
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
    </template>
  </div>
</template>

<style scoped>
.card { text-align: left; }
.info-grid { border-top: 1px solid #f1f5f9; border-bottom: none; }
</style>

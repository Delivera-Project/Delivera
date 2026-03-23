<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppConfig } from '@/composables/useAppConfig'
import { fetchPublicOrder } from '@/composables/useApi'
import TimelineList from '@/components/TimelineList.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { load: loadConfig, statusSeverity } = useAppConfig()

const order = ref(null)
const loading = ref(false)
const error = ref('')

async function fetchOrder() {
  const reference = route.query.q
  if (!reference) { error.value = t('tracking.notFound'); return }
  loading.value = true
  try {
    order.value = await fetchPublicOrder(reference)
  } catch (e) {
    error.value = e.message === 'not_found' ? t('tracking.notFound') : t('error.connection')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadConfig(); fetchOrder() })
</script>

<template>
  <div class="card card-wide">
    <PButton
      type="button"
      text
      severity="secondary"
      icon="pi pi-arrow-left"
      class="back-btn"
      @click="router.push('/my-orders')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else-if="order">
      <div class="detail-summary">
        <span class="detail-ref">{{ order.reference }}</span>
        <PTag :value="t(`orders.status.${order.status}`)" :severity="statusSeverity[order.status]" />
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">{{ t('tracking.company') }}</span>
          <span class="info-value">{{ order.companyName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">{{ t('tracking.origin') }}</span>
          <span class="info-value">{{ order.originName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">{{ t('tracking.destination') }}</span>
          <span class="info-value">{{ order.destinationName || order.recipientName || order.recipientEmail || '—' }}</span>
        </div>
      </div>

      <h3 class="timeline-heading">{{ t('orders.timeline') }}</h3>
      <TimelineList :events="order.events ?? []" />
    </template>
  </div>
</template>

<style scoped>
.card { text-align: left; }
.back-btn { margin-bottom: 16px; }
.loading-state { display: flex; justify-content: center; padding: 60px; color: #94a3b8; }

.detail-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.detail-ref { font-size: 20px; font-weight: 700; color: #1e293b; }

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 32px;
  background: #f8fafc;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 24px;
}
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #94a3b8; }
.info-value { font-size: 14px; font-weight: 500; color: #1e293b; }

.timeline-heading { margin: 0 0 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; color: #94a3b8; }
</style>

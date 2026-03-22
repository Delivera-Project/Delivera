<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDateTime } = useFormatDate()
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
    const res = await fetch(
      `${import.meta.env.VITE_API_URL}/api/v1/orders/public/search?reference=${encodeURIComponent(reference)}`
    )
    if (res.ok) order.value = await res.json()
    else error.value = t('tracking.notFound')
  } catch {
    error.value = t('error.connection')
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
      <div v-if="!order.events?.length" class="timeline-empty">
        <i class="pi pi-clock" />
        <span>{{ t('tracking.noEvents') }}</span>
      </div>
      <div v-else class="timeline">
        <div v-for="ev in order.events" :key="ev.id" class="timeline-item">
          <div class="timeline-dot" :class="`dot-${ev.status.toLowerCase()}`" />
          <div class="timeline-content">
            <div class="timeline-status">
              <PTag :value="t(`orders.status.${ev.status}`)" :severity="statusSeverity[ev.status]" size="small" />
              <span class="timeline-date">{{ formatDateTime(ev.createdAt) }}</span>
            </div>
            <div v-if="ev.note" class="timeline-note">{{ ev.note }}</div>
          </div>
        </div>
      </div>
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
.timeline-empty { display: flex; align-items: center; gap: 8px; color: #94a3b8; font-size: 13px; }
.timeline { display: flex; flex-direction: column; }
.timeline-item { display: flex; gap: 14px; position: relative; padding-bottom: 20px; }
.timeline-item:last-child { padding-bottom: 0; }
.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: #e2e8f0;
}
.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  border: 2px solid currentColor;
}
.dot-pending { color: #f59e0b; background: #fef3c7; }
.dot-in_transit { color: #3b82f6; background: #dbeafe; }
.dot-delivered { color: #10b981; background: #d1fae5; }
.dot-cancelled { color: #ef4444; background: #fee2e2; }
.timeline-content { flex: 1; }
.timeline-status { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.timeline-date { font-size: 12px; color: #94a3b8; }
.timeline-note { font-size: 13px; color: #475569; }
</style>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDateTime } = useFormatDate()
const route = useRoute()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()
const { load: loadConfig, statusSeverity, prioritySeverity, getNextStatuses } = useAppConfig()

const order = ref(null)
const loading = ref(false)
const error = ref('')
const updating = ref(false)
const updateError = ref('')
const updateSuccess = ref('')
const newStatus = ref('')
const statusNote = ref('')
const activeTab = ref(0)

const viteEnvBase = `${import.meta.env.VITE_APP_URL || globalThis.location?.origin || ''}`

async function copyTrackingUrl(token) {
  try {
    await navigator.clipboard.writeText(`${viteEnvBase}/track/${token}`)
  } catch {
    // clipboard not available (non-secure context or permission denied)
  }
}

const availableNextStatuses = computed(() => order.value ? getNextStatuses(order.value.status) : [])
const canUpdateStatus = computed(() => ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'].includes(auth.role))

onMounted(async () => {
  loadConfig()
  loading.value = true
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    if (res.ok) order.value = await res.json()
    else error.value = t('error.ORDER_NOT_FOUND')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
})

async function submitStatusUpdate() {
  if (!newStatus.value) return
  updating.value = true
  updateError.value = ''
  updateSuccess.value = ''
  try {
    const res = await api.patch(`/orders/${order.value.id}/status`, {
      status: newStatus.value,
      note: statusNote.value || null,
    })
    if (res.ok) {
      order.value = await res.json()
      updateSuccess.value = t('orders.statusUpdated')
      newStatus.value = ''
      statusNote.value = ''
    } else {
      const data = await res.json()
      updateError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    updateError.value = t('error.connection')
  } finally {
    updating.value = false
  }
}
</script>

<template>
  <div class="card card-wide">
    <PButton
      type="button"
      text
      severity="secondary"
      icon="pi pi-arrow-left"
      class="back-btn"
      @click="router.push('/orders')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else-if="order">
      <h1 class="detail-reference">{{ order.reference }}</h1>

      <div class="detail-badges">
        <div class="badge-group">
          <span class="info-label">{{ t('orders.statusLabel') }}</span>
          <PTag :value="t(`orders.status.${order.status}`)" :severity="statusSeverity[order.status]" />
        </div>
        <div class="badge-group">
          <span class="info-label">{{ t('orders.priorityLabel') }}</span>
          <PTag :value="t(`orders.priority.${order.priority}`)" :severity="prioritySeverity[order.priority]" />
        </div>
      </div>

      <PTabs v-model:value="activeTab">
        <PTabList>
          <PTab :value="0">{{ t('orders.details') }}</PTab>
          <PTab :value="1">{{ t('orders.timeline') }}</PTab>
        </PTabList>

        <PTabPanels>
          <PTabPanel :value="0">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">{{ t('orders.origin') }}</span>
                <span class="info-value">{{ order.originName }}</span>
                <span class="info-sub">{{ order.originCompanyName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ order.destinationName ? t('orders.destination') : t('tracking.recipient') }}</span>
                <span class="info-value">{{ order.destinationName || order.recipientName || order.recipientEmail }}</span>
                <span v-if="order.destinationCompanyName" class="info-sub">{{ order.destinationCompanyName }}</span>
                <span v-else-if="order.recipientName && order.recipientEmail" class="info-sub">{{ order.recipientEmail }}</span>
              </div>
              <div class="info-item info-item--full">
                <span class="info-label">{{ t('orders.date') }}</span>
                <span class="info-value">{{ formatDateTime(order.createdAt) }}</span>
              </div>
              <div v-if="order.notes" class="info-item info-item--full">
                <span class="info-label">{{ t('orders.notes') }}</span>
                <span class="info-value info-notes">{{ order.notes }}</span>
              </div>
            </div>

            <!-- Tracking URL: solo para B2C con token y sin reclamar -->
            <div v-if="order.trackingToken && !order.claimed" class="tracking-section">
              <div class="tracking-header">
                <i class="pi pi-link" />
                <div>
                  <div class="tracking-label">{{ t('orders.trackingUrl') }}</div>
                  <div class="tracking-hint">{{ t('orders.trackingUrlHint') }}</div>
                </div>
              </div>
              <div class="tracking-url-row">
                <code class="tracking-url">{{ `${viteEnvBase}/track/${order.trackingToken}` }}</code>
                <PButton
                  icon="pi pi-copy"
                  text
                  severity="secondary"
                  size="small"
                  @click="copyTrackingUrl(order.trackingToken)"
                />
              </div>
            </div>
            <!-- Ya reclamado -->
            <div v-else-if="order.trackingToken && order.claimed" class="claimed-section">
              <i class="pi pi-check-circle" />
              <div>
                <div class="claimed-label">{{ t('orders.claimed') }}</div>
                <div class="claimed-hint">{{ t('orders.claimedHint') }}</div>
              </div>
            </div>
          </PTabPanel>

          <PTabPanel :value="1">
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
                  <div v-if="ev.authorEmail" class="timeline-author">{{ ev.authorEmail }}</div>
                </div>
              </div>
            </div>

            <div v-if="canUpdateStatus && availableNextStatuses.length" class="status-section">
              <div class="timeline-divider" />
              <p class="status-section-title">{{ t('orders.updateStatus') }}</p>
              <div class="status-form">
                <div class="status-field">
                  <label class="info-label">{{ t('orders.newStatus') }}</label>
                  <PSelect
                    v-model="newStatus"
                    :options="availableNextStatuses.map(s => ({ label: t(`orders.status.${s}`), value: s }))"
                    option-label="label"
                    option-value="value"
                    :placeholder="t('orders.newStatus')"
                    style="width:100%"
                  />
                </div>
                <div class="status-field">
                  <label class="info-label">{{ t('orders.statusNote') }}</label>
                  <PTextarea
                    v-model="statusNote"
                    :placeholder="t('orders.statusNotePlaceholder')"
                    rows="2"
                    style="width:100%"
                  />
                </div>
                <PButton
                  :label="updating ? t('common.loading') : t('orders.updateStatus')"
                  :loading="updating"
                  :disabled="!newStatus"
                  @click="submitStatusUpdate"
                />
              </div>
              <PMessage v-if="updateError" severity="error" :closable="false" class="status-msg">{{ updateError }}</PMessage>
              <PMessage v-if="updateSuccess" severity="success" :closable="false" class="status-msg">{{ updateSuccess }}</PMessage>
            </div>
          </PTabPanel>
        </PTabPanels>
      </PTabs>
    </template>
  </div>
</template>

<style scoped>
.card { text-align: left; }
.back-btn { margin-bottom: 16px; }
.loading-state { display: flex; justify-content: center; padding: 60px; color: #94a3b8; }

.detail-reference {
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
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 24px;
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
.info-notes { font-weight: 400; color: #475569; line-height: 1.6; }
.info-sub { font-size: 13px; color: #64748b; }

.status-section { padding-top: 4px; margin-bottom: 24px; }
.status-section-title {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  margin: 0 0 12px;
}
.status-form { display: flex; flex-direction: column; gap: 12px; }
.status-field { display: flex; flex-direction: column; gap: 5px; }
.status-msg { margin-top: 10px; }
.timeline-divider { border-top: 1px solid #f1f5f9; margin: 20px 0; }

.timeline-empty { display: flex; align-items: center; gap: 8px; color: #94a3b8; font-size: 13px; padding: 24px 0; }
.timeline { display: flex; flex-direction: column; padding-top: 8px; }
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
.timeline-note { font-size: 13px; color: #475569; margin-bottom: 2px; }
.timeline-author { font-size: 11px; color: #94a3b8; }

.tracking-section {
  margin-top: 20px;
  padding: 14px 16px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
}
.tracking-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}
.tracking-header .pi { color: #3b82f6; font-size: 16px; margin-top: 2px; }
.tracking-label { font-weight: 600; font-size: 13px; color: #1e40af; }
.tracking-hint { font-size: 12px; color: #3b82f6; margin-top: 2px; }
.tracking-url-row { display: flex; align-items: center; gap: 8px; }
.tracking-url {
  flex: 1;
  font-size: 12px;
  background: white;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  padding: 6px 10px;
  color: #1e40af;
  word-break: break-all;
}

.claimed-section {
  margin-top: 20px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
}
.claimed-section .pi { color: #16a34a; font-size: 16px; margin-top: 2px; }
.claimed-label { font-weight: 600; font-size: 13px; color: #15803d; }
.claimed-hint { font-size: 12px; color: #166534; margin-top: 2px; }
</style>

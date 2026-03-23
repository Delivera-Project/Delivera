<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'
import TimelineList from '@/components/TimelineList.vue'

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
      class="detail-back-btn"
      @click="router.push('/orders')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else-if="order">
      <h1 class="detail-title">{{ order.reference }}</h1>

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
            <TimelineList :events="order.events" :show-author="true" />

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
.tracking-section {
  margin-top: 20px;
  padding: 14px 16px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
}
.tracking-header { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 10px; }
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

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppConfig } from '@/composables/useAppConfig'
import { fetchPublicOrder, useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'
import TimelineList from '@/components/TimelineList.vue'

const { t } = useI18n()
const { formatDateTime } = useFormatDate()
const route = useRoute()
const router = useRouter()
const api = useApi()
const { load: loadConfig, statusSeverity } = useAppConfig()

const order = ref(null)
const loading = ref(false)
const error = ref('')

const messages = ref([])
const newMessage = ref('')
const sendingMessage = ref(false)

async function fetchOrder() {
  const reference = route.query.q
  if (!reference) { error.value = t('tracking.notFound'); return }
  loading.value = true
  try {
    order.value = await fetchPublicOrder(reference)
    loadMessages()
  } catch (e) {
    error.value = e.message === 'not_found' ? t('tracking.notFound') : t('error.connection')
  } finally {
    loading.value = false
  }
}

async function loadMessages() {
  if (!order.value?.id) return
  try {
    const res = await api.get(`/orders/${order.value.id}/messages`)
    if (res.ok) messages.value = await res.json()
  } catch { /* silent */ }
}

async function sendMessage() {
  const text = newMessage.value.trim()
  if (!text || !order.value?.id) return
  sendingMessage.value = true
  try {
    const res = await api.post(`/orders/${order.value.id}/messages`, { content: text })
    if (res.ok) {
      messages.value.push(await res.json())
      newMessage.value = ''
    }
  } catch { /* silent */ } finally {
    sendingMessage.value = false
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

      <h3 class="timeline-heading" style="margin-top:24px">{{ t('orders.chat') }}</h3>
      <div class="chat-panel" data-testid="chat-panel">
        <div class="chat-messages" data-testid="chat-messages">
          <div v-if="messages.length === 0" class="chat-empty">{{ t('orders.chatEmpty') }}</div>
          <div v-for="msg in messages" :key="msg.id" class="chat-message">
            <div class="chat-message-header">
              <span class="chat-sender">{{ msg.senderName }}</span>
              <span class="chat-time">{{ formatDateTime(msg.createdAt) }}</span>
            </div>
            <p class="chat-content">{{ msg.content }}</p>
          </div>
        </div>
        <div class="chat-input-row">
          <PTextarea
            v-model="newMessage"
            :placeholder="t('orders.chatPlaceholder')"
            rows="2"
            class="chat-textarea"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <PButton
            icon="pi pi-send"
            :loading="sendingMessage"
            :disabled="!newMessage.trim()"
            data-testid="chat-send"
            @click="sendMessage"
          />
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

.chat-panel { display: flex; flex-direction: column; gap: 12px; }
.chat-messages { display: flex; flex-direction: column; gap: 10px; min-height: 80px; max-height: 300px; overflow-y: auto; }
.chat-empty { text-align: center; color: #94a3b8; font-size: 14px; padding: 16px 0; }
.chat-message { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 14px; }
.chat-message-header { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 4px; }
.chat-sender { font-size: 13px; font-weight: 600; color: #334155; }
.chat-time { font-size: 11px; color: #94a3b8; }
.chat-content { font-size: 14px; color: #1e293b; margin: 0; white-space: pre-wrap; }
.chat-input-row { display: flex; gap: 8px; align-items: flex-end; }
.chat-textarea { flex: 1; }
</style>

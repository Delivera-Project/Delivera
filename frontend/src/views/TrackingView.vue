<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const { formatDateTime } = useFormatDate()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { load: loadConfig, statusSeverity } = useAppConfig()

const order = ref(null)
const loading = ref(false)
const error = ref('')
const searchRef = ref('')

// Claim registration
const claimFirstName = ref('')
const claimLastName = ref('')
const claimEmail = ref('')
const claimPassword = ref('')
const claimLoading = ref(false)
const claimError = ref('')

async function submitClaim() {
  claimError.value = ''
  claimLoading.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v1/orders/public/track/${route.params.token}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        firstName: claimFirstName.value.trim(),
        lastName: claimLastName.value.trim(),
        email: claimEmail.value.trim(),
        password: claimPassword.value,
      }),
    })
    const data = await res.json()
    if (res.ok) {
      auth.applyLoginData(data)
      router.push('/my-orders')
    } else {
      const code = data?.code
      if (code === 'ORDER_ALREADY_CLAIMED') claimError.value = t('tracking.claim.alreadyClaimed')
      else if (code === 'ORDER_CLAIM_EMAIL_MISMATCH') claimError.value = t('tracking.claim.emailMismatch')
      else if (code === 'EMAIL_ALREADY_EXISTS') claimError.value = t('tracking.claim.emailExists')
      else claimError.value = t('error.connection')
    }
  } catch {
    claimError.value = t('error.connection')
  } finally {
    claimLoading.value = false
  }
}

async function fetchByToken(token) {
  loading.value = true
  error.value = ''
  order.value = null
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v1/orders/public/track/${token}`)
    if (res.ok) order.value = await res.json()
    else error.value = t('tracking.notFound')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

async function fetchByReference() {
  if (!searchRef.value.trim()) return
  loading.value = true
  error.value = ''
  order.value = null
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v1/orders/public/search?reference=${encodeURIComponent(searchRef.value.trim())}`)
    if (res.ok) order.value = await res.json()
    else error.value = t('tracking.notFound')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadConfig()
  if (route.params.token) fetchByToken(route.params.token)
  else if (route.query.q) { searchRef.value = route.query.q; fetchByReference() }
})
</script>

<template>
  <div class="tracking-page">
    <div class="tracking-card">
      <div class="tracking-brand">
        <i class="pi pi-truck" />
        <span>Delivera</span>
      </div>

      <h1>{{ t('tracking.title') }}</h1>

      <template v-if="!route.params.token">
        <p class="tracking-subtitle">{{ t('tracking.searchTitle') }}</p>
        <div class="search-row">
          <PInputText
            v-model="searchRef"
            :placeholder="t('tracking.searchPlaceholder')"
            @keyup.enter="fetchByReference"
            fluid
          />
          <PButton :label="t('tracking.search')" :loading="loading" @click="fetchByReference" />
        </div>
      </template>

      <div v-if="loading" class="tracking-loading">
        <i class="pi pi-spin pi-spinner" style="font-size:24px" />
      </div>

      <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

      <template v-else-if="order">
        <div class="tracking-summary">
          <div class="tracking-ref">{{ order.reference }}</div>
          <PTag :value="t(`orders.status.${order.status}`)" :severity="statusSeverity[order.status]" />
        </div>

        <div class="tracking-info">
          <div class="info-row">
            <span class="info-label">{{ t('tracking.company') }}</span>
            <span>{{ order.companyName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">{{ t('tracking.origin') }}</span>
            <span>{{ order.originName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">{{ t('tracking.destination') }}</span>
            <span>{{ order.destinationName || order.recipientName || '—' }}</span>
          </div>
        </div>

        <div class="tracking-timeline">
          <h3>{{ t('orders.timeline') }}</h3>
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
        </div>
        <!-- Pedido reclamado: pide login -->
        <template v-if="!order.claimable && route.params.token">
          <div class="claimed-notice">
            <i class="pi pi-lock" />
            <div>
              <div class="claimed-notice-title">{{ t('tracking.alreadyClaimed') }}</div>
              <a class="claimed-notice-link" href="/">{{ t('tracking.signInToView') }}</a>
            </div>
          </div>
        </template>

        <template v-if="order.claimable && route.params.token">
          <div class="claim-panel">
            <div class="claim-header">
              <i class="pi pi-user-plus" />
              <div>
                <div class="claim-title">{{ t('tracking.claim.title') }}</div>
                <div class="claim-desc">{{ t('tracking.claim.description') }}</div>
              </div>
            </div>
            <p v-if="order.recipientEmailHint" class="claim-hint">
              {{ t('tracking.claim.emailHint', { hint: order.recipientEmailHint }) }}
            </p>
            <form class="claim-form" @submit.prevent="submitClaim">
              <div class="claim-row">
                <div class="claim-field">
                  <label>{{ t('tracking.claim.firstName') }}</label>
                  <PInputText v-model="claimFirstName" required fluid />
                </div>
                <div class="claim-field">
                  <label>{{ t('tracking.claim.lastName') }}</label>
                  <PInputText v-model="claimLastName" required fluid />
                </div>
              </div>
              <div class="claim-field">
                <label>{{ t('tracking.claim.email') }}</label>
                <PInputText v-model="claimEmail" type="email" required fluid />
              </div>
              <div class="claim-field">
                <label>{{ t('tracking.claim.password') }}</label>
                <PPassword v-model="claimPassword" :feedback="false" toggle-mask required fluid />
              </div>
              <PMessage v-if="claimError" severity="error" :closable="false" class="claim-msg">{{ claimError }}</PMessage>
              <PButton
                type="submit"
                :label="t('tracking.claim.submit')"
                :loading="claimLoading"
                fluid
              />
            </form>
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<style scoped>
.tracking-page {
  min-height: 100vh;
  background: #f8fafc;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 48px 16px;
}

.tracking-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 560px;
  box-shadow: 0 4px 24px rgba(0,0,0,.07);
}

.tracking-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 24px;
}
.tracking-brand .pi { font-size: 22px; color: #3b82f6; }

.tracking-card h1 { margin: 0 0 8px; font-size: 22px; }
.tracking-subtitle { color: #64748b; margin: 0 0 20px; }
.search-row { display: flex; gap: 10px; margin-bottom: 20px; }
.tracking-loading { display: flex; justify-content: center; padding: 40px; color: #94a3b8; }

.tracking-summary { display: flex; align-items: center; gap: 12px; margin: 20px 0 16px; }
.tracking-ref { font-size: 20px; font-weight: 700; color: #1e293b; }

.tracking-info { background: #f8fafc; border-radius: 10px; padding: 16px; margin-bottom: 20px; display: flex; flex-direction: column; gap: 8px; }
.info-row { display: flex; justify-content: space-between; font-size: 14px; }
.info-label { color: #94a3b8; }

.tracking-timeline h3 { margin: 0 0 12px; font-size: 13px; text-transform: uppercase; color: #94a3b8; letter-spacing: 0.05em; }

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

.claim-panel {
  margin-top: 24px;
  padding: 20px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
}

.claim-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}
.claim-header .pi { font-size: 20px; color: #16a34a; margin-top: 2px; }
.claim-title { font-weight: 700; font-size: 15px; color: #15803d; }
.claim-desc { font-size: 13px; color: #166534; margin-top: 2px; }

.claim-hint { font-size: 13px; color: #475569; margin: 0 0 14px; }

.claim-form { display: flex; flex-direction: column; gap: 10px; }

.claim-row { display: flex; gap: 10px; }
.claim-row .claim-field { flex: 1; }

.claim-field { display: flex; flex-direction: column; gap: 4px; }
.claim-field label { font-size: 12px; font-weight: 600; color: #475569; }

.claim-msg { margin: 0; }

.claimed-notice {
  margin-top: 24px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: #fafafa;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}
.claimed-notice .pi { font-size: 18px; color: #94a3b8; margin-top: 2px; }
.claimed-notice-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 4px; }
.claimed-notice-link { font-size: 13px; color: #3b82f6; text-decoration: none; }
.claimed-notice-link:hover { text-decoration: underline; }
</style>

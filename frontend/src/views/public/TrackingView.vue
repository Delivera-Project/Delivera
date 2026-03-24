<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAppConfig } from '@/composables/useAppConfig'
import { useAuthStore } from '@/stores/auth'
import TimelineList from '@/components/TimelineList.vue'

const { t } = useI18n()
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
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v2/orders/public/track/${route.params.token}/register`, {
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
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v2/orders/public/track/${token}`)
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
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v2/orders/public/search?reference=${encodeURIComponent(searchRef.value.trim())}`)
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
          <TimelineList :events="order.events" />
        </div>
        <!-- Pedido reclamado: pide login -->
        <template v-if="!order.claimable && route.params.token">
          <div class="claimed-notice">
            <i class="pi pi-lock" />
            <div>
              <div class="claimed-notice-title">{{ t('tracking.alreadyClaimed') }}</div>
              <RouterLink class="claimed-notice-link" to="/">{{ t('tracking.signInToView') }}</RouterLink>
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
                  <label for="claim-first-name">{{ t('tracking.claim.firstName') }}</label>
                  <PInputText id="claim-first-name" v-model="claimFirstName" required fluid />
                </div>
                <div class="claim-field">
                  <label for="claim-last-name">{{ t('tracking.claim.lastName') }}</label>
                  <PInputText id="claim-last-name" v-model="claimLastName" required fluid />
                </div>
              </div>
              <div class="claim-field">
                <label for="claim-email">{{ t('tracking.claim.email') }}</label>
                <PInputText id="claim-email" v-model="claimEmail" type="email" required fluid />
              </div>
              <div class="claim-field">
                <label for="claim-password">{{ t('tracking.claim.password') }}</label>
                <PPassword id="claim-password" v-model="claimPassword" :feedback="false" toggle-mask required fluid />
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
.claim-panel {
  margin-top: 24px;
  padding: 20px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
}
.claim-header { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 12px; }
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

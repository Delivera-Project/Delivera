<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()

const units = ref([])
const orders = ref([])
const loadingStats = ref(false)

const firstName = computed(() => auth.user?.firstName || auth.user?.email || '')

const isAdmin = computed(() => auth.role === 'COMPANY_ADMIN')
const isAnalyst = computed(() => auth.role === 'ANALYST')

const activeOrders = computed(() =>
  orders.value.filter(o => o.status === 'PENDING' || o.status === 'IN_TRANSIT').length
)

const showSetupBanner = computed(() =>
  isAdmin.value && !loadingStats.value && units.value.length === 0
)

const quickActions = computed(() => {
  if (isAdmin.value) return [
    { label: t('orders.new'), icon: 'pi-plus-circle', path: '/orders/new', severity: 'primary' },
    { label: t('units.new'), icon: 'pi-building', path: '/units/new', severity: 'secondary' },
    { label: t('orders.listTitle'), icon: 'pi-list', path: '/orders', severity: 'secondary' },
    { label: t('nav.activity'), icon: 'pi-chart-bar', path: '/activity', severity: 'secondary' },
  ]
  if (isAnalyst.value) return [
    { label: t('orders.new'), icon: 'pi-plus-circle', path: '/orders/new', severity: 'primary' },
    { label: t('orders.listTitle'), icon: 'pi-list', path: '/orders', severity: 'secondary' },
    { label: t('nav.activity'), icon: 'pi-chart-bar', path: '/activity', severity: 'secondary' },
    { label: t('nav.loyalUsers'), icon: 'pi-users', path: '/loyal-users', severity: 'secondary' },
  ]
  // OPERATOR
  return [
    { label: t('orders.listTitle'), icon: 'pi-list', path: '/orders', severity: 'primary' },
    { label: t('nav.units'), icon: 'pi-building', path: '/units', severity: 'secondary' },
  ]
})

onMounted(async () => {
  loadingStats.value = true
  try {
    const [unitsRes, ordersRes] = await Promise.all([
      api.get('/units'),
      api.get('/orders'),
    ])
    if (unitsRes.ok) units.value = await unitsRes.json()
    if (ordersRes.ok) orders.value = await ordersRes.json()
  } catch {
    // stats son opcionales, no bloqueamos la vista
  } finally {
    loadingStats.value = false
  }
})
</script>

<template>
  <div class="home-view">
    <div class="home-welcome">
      <h1>{{ t('home.welcome', { name: firstName }) }}</h1>
      <PTag v-if="auth.companyName" :value="auth.companyName" severity="secondary" />
    </div>

    <!-- Banner configuración pendiente (DSI-04.2) -->
    <PMessage v-if="showSetupBanner" severity="warn" :closable="false" class="setup-banner">
      {{ t('home.setupBanner') }}
      <div class="setup-actions">
        <PButton :label="t('home.createFirstUnit')" icon="pi pi-plus" size="small" @click="router.push('/units/new')" />
      </div>
    </PMessage>

    <!-- Stats (solo si hay datos) -->
    <div v-if="!loadingStats && (isAdmin || isAnalyst)" class="stats-row">
      <div class="stat-card">
        <i class="pi pi-building stat-icon" />
        <span class="stat-value">{{ units.length }}</span>
        <span class="stat-label">{{ t('home.stats.units') }}</span>
      </div>
      <div class="stat-card">
        <i class="pi pi-send stat-icon" />
        <span class="stat-value">{{ activeOrders }}</span>
        <span class="stat-label">{{ t('home.stats.orders') }}</span>
      </div>
    </div>

    <!-- Accesos rápidos (DSI-08.3) -->
    <div class="section">
      <h2>{{ t('home.quickActions') }}</h2>
      <div class="actions-grid">
        <PButton
          v-for="action in quickActions"
          :key="action.path"
          :label="action.label"
          :icon="'pi ' + action.icon"
          :severity="action.severity"
          class="action-btn"
          @click="router.push(action.path)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-view {
  max-width: 860px;
  margin: 0 auto;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.home-welcome {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.home-welcome h1 { margin: 0; font-size: 22px; }

.setup-banner { flex-direction: column; gap: 12px; }
.setup-actions { display: flex; gap: 8px; margin-top: 8px; }

.stats-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.stat-card {
  flex: 1;
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  text-align: center;
}
.stat-icon { font-size: 20px; color: var(--p-primary-color, #7c3aed); }
.stat-value { font-size: 28px; font-weight: 700; color: #1e293b; line-height: 1; }
.stat-label { font-size: 12px; color: #64748b; font-weight: 500; }

.section h2 { font-size: 14px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; margin: 0 0 14px; }

.actions-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.action-btn { min-width: 160px; }
</style>

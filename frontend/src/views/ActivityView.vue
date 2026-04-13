<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import Chart from 'primevue/chart'

const { t } = useI18n()
const api = useApi()

const period = ref('MONTH')
const metrics = ref(null)
const chartData = ref(null)
const loading = ref(false)
const error = ref('')

const PERIODS = ['TODAY', 'WEEK', 'MONTH']
const METRIC_KEYS = ['totalOrders', 'completedOrders', 'cancelledOrders', 'activeOrders', 'newLoyalUsers']
const METRIC_ICONS = {
  totalOrders: 'pi-send',
  completedOrders: 'pi-check-circle',
  cancelledOrders: 'pi-times-circle',
  activeOrders: 'pi-sync',
  newLoyalUsers: 'pi-user-plus',
}

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    y: { beginAtZero: true, ticks: { stepSize: 1, precision: 0 } },
    x: { grid: { display: false } },
  },
}

function formatDateLabel(dateStr) {
  const d = new Date(dateStr + 'T00:00:00')
  return d.toLocaleDateString(undefined, { day: '2-digit', month: 'short' })
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [metricsRes, chartRes] = await Promise.all([
      api.get(`/activity/metrics?period=${period.value}`),
      api.get(`/activity/orders-by-day?period=${period.value}`),
    ])
    if (metricsRes.ok) metrics.value = await metricsRes.json()
    else error.value = t('error.connection')

    if (chartRes.ok) {
      const entries = await chartRes.json()
      chartData.value = {
        labels: entries.map(e => formatDateLabel(e.date)),
        datasets: [{
          label: t('activity.panel.chart.orders'),
          data: entries.map(e => e.count),
          backgroundColor: 'rgba(124, 58, 237, 0.7)',
          borderRadius: 4,
        }],
      }
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

function selectPeriod(p) {
  period.value = p
  load()
}

onMounted(load)
</script>

<template>
  <div class="card card-wide">
    <div class="activity-header">
      <h1>{{ t('activity.panel.title') }}</h1>
      <div class="period-tabs">
        <button
          v-for="p in PERIODS" :key="p"
          :class="['period-btn', { 'period-btn--active': period === p }]"
          @click="selectPeriod(p)"
        >{{ t('activity.panel.period.' + p) }}</button>
      </div>
    </div>

    <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

    <div v-if="metrics && !loading" class="metrics-grid">
      <div v-for="key in METRIC_KEYS" :key="key" class="metric-card">
        <i :class="['pi', METRIC_ICONS[key], 'metric-icon']" />
        <span class="metric-value">{{ metrics[key] }}</span>
        <span class="metric-label">{{ t('activity.panel.metric.' + key) }}</span>
      </div>
    </div>

    <div v-else-if="loading" class="metrics-grid">
      <div v-for="i in 5" :key="i" class="metric-card metric-card--skeleton" />
    </div>

    <div v-if="chartData && !loading" class="chart-section">
      <h2>{{ t('activity.panel.chart.title') }}</h2>
      <div class="chart-wrapper">
        <Chart type="bar" :data="chartData" :options="chartOptions" />
      </div>
      <p v-if="chartData.labels.length === 0" class="chart-empty">{{ t('activity.panel.chart.empty') }}</p>
    </div>
  </div>
</template>

<style scoped>
.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}
.activity-header h1 { margin: 0; }

.period-tabs { display: flex; gap: 6px; }
.period-btn {
  padding: 6px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: white;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.12s;
}
.period-btn:hover { border-color: var(--p-primary-color, #7c3aed); color: var(--p-primary-color, #7c3aed); }
.period-btn--active {
  background: var(--p-primary-color, #7c3aed);
  border-color: var(--p-primary-color, #7c3aed);
  color: white;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}
.metric-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  text-align: center;
}
.metric-icon { font-size: 22px; color: var(--p-primary-color, #7c3aed); }
.metric-value { font-size: 32px; font-weight: 700; color: #1e293b; line-height: 1; }
.metric-label { font-size: 12px; color: #64748b; font-weight: 500; }
.metric-card--skeleton { background: #f1f5f9; min-height: 120px; animation: pulse 1.2s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }

.chart-section { margin-top: 32px; }
.chart-section h2 { margin: 0 0 16px; font-size: 18px; font-weight: 600; color: #1e293b; }
.chart-wrapper { position: relative; height: 300px; }
.chart-empty { text-align: center; color: #94a3b8; font-size: 14px; margin-top: 24px; }
</style>

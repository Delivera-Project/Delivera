<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const api = useApi()
const { formatDate } = useFormatDate()

const metrics = ref(null)
const organizations = ref([])
const loading = ref(false)
const error = ref('')

const METRIC_KEYS = ['totalOrganizations', 'totalCompanies', 'totalOrdersThisMonth', 'totalActiveUsers']
const METRIC_ICONS = {
  totalOrganizations: 'pi-building',
  totalCompanies: 'pi-briefcase',
  totalOrdersThisMonth: 'pi-send',
  totalActiveUsers: 'pi-users',
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [metricsRes, orgsRes] = await Promise.all([
      api.get('/admin/metrics'),
      api.get('/admin/organizations'),
    ])
    if (metricsRes.ok) metrics.value = await metricsRes.json()
    else error.value = t('error.connection')

    if (orgsRes.ok) organizations.value = await orgsRes.json()
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="card card-wide">
    <h1>{{ t('admin.title') }}</h1>

    <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

    <div v-if="metrics && !loading" class="metrics-grid">
      <div v-for="key in METRIC_KEYS" :key="key" class="metric-card">
        <i :class="['pi', METRIC_ICONS[key], 'metric-icon']" />
        <span class="metric-value">{{ metrics[key] }}</span>
        <span class="metric-label">{{ t('admin.metric.' + key) }}</span>
      </div>
    </div>

    <div v-else-if="loading" class="metrics-grid">
      <div v-for="i in 4" :key="i" class="metric-card metric-card--skeleton" />
    </div>

    <div v-if="!loading" class="orgs-section">
      <h2>{{ t('admin.organizations') }}</h2>
      <table v-if="organizations.length" class="orgs-table">
        <thead>
          <tr>
            <th>{{ t('admin.orgName') }}</th>
            <th>{{ t('admin.orgHandle') }}</th>
            <th>{{ t('admin.companies') }}</th>
            <th>{{ t('admin.workers') }}</th>
            <th>{{ t('admin.orders') }}</th>
            <th>{{ t('admin.createdAt') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="org in organizations" :key="org.id">
            <td class="org-name">{{ org.name }}</td>
            <td><code>{{ org.handle }}</code></td>
            <td class="text-center">{{ org.companyCount }}</td>
            <td class="text-center">{{ org.workerCount }}</td>
            <td class="text-center">{{ org.orderCount }}</td>
            <td>{{ formatDate(org.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="orgs-empty">{{ t('admin.empty') }}</p>
    </div>
  </div>
</template>

<style scoped>
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
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

.orgs-section h2 { margin: 0 0 16px; font-size: 18px; font-weight: 600; color: #1e293b; }
.orgs-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.orgs-table th { text-align: left; padding: 10px 12px; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 600; font-size: 12px; text-transform: uppercase; }
.orgs-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }
.org-name { font-weight: 600; }
.text-center { text-align: center; }
.orgs-empty { text-align: center; color: #94a3b8; font-size: 14px; }
</style>

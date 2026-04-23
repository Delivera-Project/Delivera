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
  const [metricsResult, orgsResult] = await Promise.allSettled([
    api.get('/admin/metrics'),
    api.get('/admin/organizations'),
  ])
  let partialError = false
  if (metricsResult.status === 'fulfilled' && metricsResult.value.ok) {
    metrics.value = await metricsResult.value.json()
  } else {
    partialError = true
  }
  if (orgsResult.status === 'fulfilled' && orgsResult.value.ok) {
    organizations.value = await orgsResult.value.json()
  } else {
    partialError = true
  }
  if (partialError) error.value = t('error.connection')
  loading.value = false
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

<style scoped src="./AdminDashboardView.css"></style>

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
  <div class="surface-card card-full">
    <div class="admin-header">
      <div class="admin-title-block">
        <h1>{{ t('admin.title') }}</h1>
        <p class="admin-subtitle">{{ t('admin.subtitle') }}</p>
      </div>
      <div v-if="metrics && !loading" class="stats-inline">
        <span v-for="key in METRIC_KEYS" :key="key" class="stat-chip">
          <i :class="['pi', METRIC_ICONS[key]]" />
          <strong>{{ metrics[key] }}</strong>
          {{ t('admin.metric.' + key) }}
        </span>
      </div>
      <div v-else-if="loading" class="stats-inline">
        <span v-for="i in 4" :key="i" class="stat-chip stat-chip--skeleton" />
      </div>
    </div>

    <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

    <div class="orgs-section">
      <h2>{{ t('admin.organizations') }}</h2>
      <DataTable
        :value="organizations"
        :loading="loading"
        striped-rows
        row-hover
        :rows="10"
        paginator
      >
        <template #empty>
          <EmptyState icon="pi-building" :message="t('admin.empty')" />
        </template>
        <Column field="name" :header="t('admin.orgName')">
          <template #body="{ data }">
            <span class="org-name">{{ data.name }}</span>
          </template>
        </Column>
        <Column field="handle" :header="t('admin.orgHandle')">
          <template #body="{ data }">
            <code class="org-handle">{{ data.handle }}</code>
          </template>
        </Column>
        <Column :header="t('admin.companies')" style="width:120px">
          <template #body="{ data }">
            <PTag :value="String(data.companyCount)" severity="secondary" />
          </template>
        </Column>
        <Column :header="t('admin.workers')" style="width:120px">
          <template #body="{ data }">
            <PTag :value="String(data.workerCount)" severity="info" />
          </template>
        </Column>
        <Column :header="t('admin.orders')" style="width:120px">
          <template #body="{ data }">
            <PTag :value="String(data.orderCount)" severity="warn" />
          </template>
        </Column>
        <Column :header="t('admin.createdAt')" style="width:130px;white-space:nowrap">
          <template #body="{ data }">{{ formatDate(data.createdAt) }}</template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<style scoped src="./AdminDashboardView.css"></style>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const router = useRouter()
const api = useApi()
const { load: loadConfig, statusSeverity } = useAppConfig()

const orders = ref([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loadConfig()
  loading.value = true
  try {
    const res = await api.get('/loyal-users/me/orders')
    if (res.ok) orders.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="card card-wide">
    <h1>{{ t('myOrders.title') }}</h1>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <DataTable :value="orders" :loading="loading" striped-rows row-hover @row-click="e => router.push(`/track?q=${e.data.reference}`)">
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-inbox empty-icon" />
          <p>{{ t('myOrders.empty') }}</p>
        </div>
      </template>
      <Column field="reference" :header="t('orders.reference')" style="font-weight:600;width:170px" />
      <Column :header="t('orders.route')">
        <template #body="{ data }">
          <span>{{ data.originName }}</span>
          <i class="pi pi-arrow-right" style="margin:0 6px;font-size:11px;color:#94a3b8" />
          <span>{{ data.destinationName || data.recipientName || data.recipientEmail }}</span>
        </template>
      </Column>
      <Column :header="t('orders.statusLabel')" style="width:130px">
        <template #body="{ data }">
          <PTag :value="t(`orders.status.${data.status}`)" :severity="statusSeverity[data.status]" />
        </template>
      </Column>
      <Column :header="t('orders.date')" style="width:120px">
        <template #body="{ data }">
          {{ formatDate(data.createdAt) }}
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
h1 { margin: 0 0 20px; }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 48px 24px; color: #94a3b8; }
.empty-icon { font-size: 40px; opacity: 0.4; }
.empty-state p { margin: 0; font-size: 14px; }
</style>

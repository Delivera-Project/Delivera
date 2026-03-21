<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const api = useApi()

const loyalUser = ref(null)
const orders = ref([])
const loading = ref(false)
const error = ref('')

const statusSeverity = {
  PENDING: 'warn',
  IN_TRANSIT: 'info',
  DELIVERED: 'success',
  CANCELLED: 'danger',
}

onMounted(async () => {
  loading.value = true
  try {
    const [luRes, ordersRes] = await Promise.all([
      api.get(`/loyal-users`),
      api.get(`/loyal-users/${route.params.id}/orders`),
    ])
    if (ordersRes.ok) orders.value = await ordersRes.json()
    if (luRes.ok) {
      const list = await luRes.json()
      loyalUser.value = list.find(l => l.id === route.params.id) || null
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="card card-wide">
    <PButton
      type="button"
      text
      severity="secondary"
      icon="pi pi-arrow-left"
      class="back-btn"
      @click="router.push('/loyal-users')"
    />

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" style="font-size:24px" />
    </div>

    <PMessage v-else-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <template v-else>
      <div class="lu-header">
        <div class="lu-avatar"><i class="pi pi-user" /></div>
        <div>
          <h1>{{ loyalUser?.email }}</h1>
          <PTag
            v-if="loyalUser"
            :value="loyalUser.registered ? t('loyalUsers.registered') : t('loyalUsers.notRegistered')"
            :severity="loyalUser?.registered ? 'success' : 'secondary'"
          />
        </div>
      </div>

      <h3 class="section-title">{{ t('loyalUsers.orders') }}</h3>

      <DataTable :value="orders" striped-rows row-hover @row-click="e => router.push(`/orders/${e.data.id}`)">
        <template #empty>
          <div class="empty-state">
            <i class="pi pi-send empty-icon" />
            <p>{{ t('loyalUsers.ordersEmpty') }}</p>
          </div>
        </template>
        <Column field="reference" :header="t('orders.reference')" style="font-weight:600;width:170px" />
        <Column :header="t('orders.statusLabel')" style="width:130px">
          <template #body="{ data }">
            <PTag :value="t(`orders.status.${data.status}`)" :severity="statusSeverity[data.status]" />
          </template>
        </Column>
        <Column :header="t('orders.date')" style="width:120px">
          <template #body="{ data }">
            {{ new Date(data.createdAt).toLocaleDateString() }}
          </template>
        </Column>
      </DataTable>
    </template>
  </div>
</template>

<style scoped>
.back-btn { margin-bottom: 16px; }
.loading-state { display: flex; justify-content: center; padding: 60px; color: #94a3b8; }
.lu-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
.lu-avatar { width: 56px; height: 56px; border-radius: 50%; background: #e2e8f0; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #64748b; }
.lu-header h1 { margin: 0 0 6px; }
.section-title { font-size: 13px; text-transform: uppercase; color: #94a3b8; letter-spacing: 0.05em; margin: 0 0 12px; }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 48px 24px; color: #94a3b8; }
.empty-icon { font-size: 40px; opacity: 0.4; }
.empty-state p { margin: 0; font-size: 14px; }
</style>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()

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
    const res = await api.get('/orders')
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
    <div class="orders-header">
      <h1>{{ t('orders.listTitle') }}</h1>
      <PButton
        v-if="auth.canCreateOrders"
        :label="t('orders.new')"
        icon="pi pi-plus"
        @click="router.push('/orders/new')"
      />
    </div>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <DataTable
      :value="orders"
      :loading="loading"
      :rows="20"
      striped-rows
    >
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-send empty-icon" />
          <p>{{ t('orders.empty') }}</p>
          <PButton
            v-if="auth.canCreateOrders"
            :label="t('orders.new')"
            icon="pi pi-plus"
            size="small"
            @click="router.push('/orders/new')"
          />
        </div>
      </template>
      <Column field="reference" :header="t('orders.reference')" style="width:160px;font-weight:600" />
      <Column :header="t('orders.route')">
        <template #body="{ data }">
          <span>{{ data.originName }}</span>
          <i class="pi pi-arrow-right" style="margin:0 6px;font-size:11px;color:#94a3b8" />
          <span>{{ data.destinationName }}</span>
        </template>
      </Column>
      <Column :header="t('orders.statusLabel')" style="width:130px">
        <template #body="{ data }">
          <PTag
            :value="t(`orders.status.${data.status}`)"
            :severity="statusSeverity[data.status] ?? 'secondary'"
          />
        </template>
      </Column>
      <Column :header="t('orders.date')" style="width:120px">
        <template #body="{ data }">
          {{ new Date(data.createdAt).toLocaleDateString() }}
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
.orders-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.orders-header h1 {
  margin: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 40px;
  opacity: 0.4;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}
</style>

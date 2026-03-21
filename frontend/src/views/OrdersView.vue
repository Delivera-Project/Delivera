<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const api = useApi()
const auth = useAuthStore()

const orders = ref([])
const loading = ref(false)
const error = ref('')
const successMsg = ref(route.query.created ? t('orders.created', { reference: route.query.created }) : '')
const filterStatus = ref('ALL')
const filterPriority = ref('ALL')
const filterText = ref('')

const statusSeverity = {
  PENDING: 'warn',
  IN_TRANSIT: 'info',
  DELIVERED: 'success',
  CANCELLED: 'danger',
}

const prioritySeverity = {
  HIGH: 'danger',
  NORMAL: 'secondary',
  LOW: 'info',
}

const statusOptions = computed(() => [
  { label: t('orders.filterAll'), value: 'ALL' },
  { label: t('orders.status.PENDING'), value: 'PENDING' },
  { label: t('orders.status.IN_TRANSIT'), value: 'IN_TRANSIT' },
  { label: t('orders.status.DELIVERED'), value: 'DELIVERED' },
  { label: t('orders.status.CANCELLED'), value: 'CANCELLED' },
])

const priorityOptions = computed(() => [
  { label: t('orders.filterAll'), value: 'ALL' },
  { label: t('orders.priority.HIGH'), value: 'HIGH' },
  { label: t('orders.priority.NORMAL'), value: 'NORMAL' },
  { label: t('orders.priority.LOW'), value: 'LOW' },
])

const filtered = computed(() => {
  return orders.value.filter(o => {
    if (filterStatus.value !== 'ALL' && o.status !== filterStatus.value) return false
    if (filterPriority.value !== 'ALL' && o.priority !== filterPriority.value) return false
    if (filterText.value) {
      const q = filterText.value.toLowerCase()
      const matches = o.reference.toLowerCase().includes(q)
        || (o.originName || '').toLowerCase().includes(q)
        || (o.destinationName || '').toLowerCase().includes(q)
        || (o.recipientEmail || '').toLowerCase().includes(q)
        || (o.recipientName || '').toLowerCase().includes(q)
      if (!matches) return false
    }
    return true
  })
})

async function load() {
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
}

async function deleteOrder(e, id) {
  e.stopPropagation()
  if (!confirm('¿Eliminar este pedido?')) return
  const res = await api.del(`/orders/${id}`)
  if (res.ok) orders.value = orders.value.filter(o => o.id !== id)
}

onMounted(load)
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

    <div class="orders-filters-wrapper">
      <span class="filters-label">Filtros:</span>
      <div class="orders-filters">
        <div class="filter-row">
          <label class="filter-group-label">{{ t('orders.reference') }}</label>
          <input
            v-model="filterText"
            :placeholder="t('orders.reference') + '...'"
            class="filter-search"
            type="text"
          />
        </div>
        <div class="filter-row">
          <label class="filter-group-label">{{ t('orders.statusLabel') }}</label>
          <PSelect
            v-model="filterStatus"
            :options="statusOptions"
            option-label="label"
            option-value="value"
            class="filter-select"
          />
          <label class="filter-group-label">{{ t('orders.priority.label') }}</label>
          <PSelect
            v-model="filterPriority"
            :options="priorityOptions"
            option-label="label"
            option-value="value"
            class="filter-select"
          />
        </div>
      </div>
    </div>

    <PMessage v-if="successMsg" severity="success" :closable="false">{{ successMsg }}</PMessage>
    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <DataTable
      :value="filtered"
      :loading="loading"
      :rows="20"
      striped-rows
      row-hover
      @row-click="e => router.push(`/orders/${e.data.id}`)"
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
      <Column field="reference" :header="t('orders.reference')" style="width:170px;font-weight:600" />
      <Column :header="t('orders.route')">
        <template #body="{ data }">
          <span>{{ data.originName }}</span>
          <i class="pi pi-arrow-right" style="margin:0 6px;font-size:11px;color:#94a3b8" />
          <span v-if="data.destinationName">{{ data.destinationName }}</span>
          <span v-else class="recipient-label">{{ data.recipientName || data.recipientEmail }}</span>
        </template>
      </Column>
      <Column :header="t('orders.priorityLabel')" style="width:100px">
        <template #body="{ data }">
          <PTag
            v-if="data.priority === 'HIGH'"
            :value="t(`orders.priority.${data.priority}`)"
            :severity="prioritySeverity[data.priority]"
          />
          <span v-else class="priority-normal">{{ t(`orders.priority.${data.priority}`) }}</span>
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
      <Column v-if="auth.isCompanyAdmin" style="width:48px;padding:0">
        <template #body="{ data }">
          <button class="delete-btn" @click="deleteOrder($event, data.id)" title="Eliminar">
            <i class="pi pi-times" />
          </button>
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
  margin-bottom: 16px;
}

.orders-header h1 { margin: 0; }

.orders-filters-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.filters-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  text-align: left;
  align-self: flex-start;
}

.orders-filters {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  min-width: 0;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group-label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.filter-search {
  flex: 1;
  height: 38px;
  padding: 0 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  color: #1e293b;
  background: #fff;
  outline: none;
  transition: border-color 0.15s;
}
.filter-search:focus { border-color: #8b5cf6; }
.filter-search::placeholder { color: #94a3b8; }
.filter-select { width: 160px; }

.recipient-label { color: #64748b; font-style: italic; }
.priority-normal { color: #94a3b8; font-size: 13px; }

.delete-btn {
  opacity: 0;
  background: none;
  border: none;
  cursor: pointer;
  color: #ef4444;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.15s, background 0.15s;
}
.delete-btn:hover { background: #fee2e2; }
:deep(tr:hover) .delete-btn { opacity: 1; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: #94a3b8;
}

.empty-icon { font-size: 40px; opacity: 0.4; }
.empty-state p { margin: 0; font-size: 14px; }
</style>

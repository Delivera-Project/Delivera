<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'
import EmptyState from '@/components/EmptyState.vue'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const router = useRouter()
const route = useRoute()
const api = useApi()
const auth = useAuthStore()
const { load: loadConfig, statusSeverity, prioritySeverity } = useAppConfig()

const orders = ref([])
const loading = ref(false)
const error = ref('')
const successMsg = ref(route.query.created ? t('orders.created', { reference: route.query.created }) : '')
const filterStatus = ref('ALL')
const filterPriority = ref('ALL')
const filterText = ref('')

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
  if (!confirm(t('orders.deleteConfirm'))) return
  const res = await api.del(`/orders/${id}`)
  if (res.ok) orders.value = orders.value.filter(o => o.id !== id)
}

onMounted(() => { loadConfig(); load() })
</script>

<template>
  <div class="card card-wide">
    <div class="list-header">
      <h1>{{ t('orders.listTitle') }}</h1>
      <PButton
        v-if="auth.canCreateOrders"
        :label="t('orders.new')"
        icon="pi pi-plus"
        @click="router.push('/orders/new')"
      />
    </div>

    <div class="filters-wrapper">
      <span class="filters-label">{{ t('common.filters') }}</span>
      <div class="filters-box">
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
        <EmptyState icon="pi-send" :message="t('orders.empty')">
          <PButton
            v-if="auth.canCreateOrders"
            :label="t('orders.new')"
            icon="pi pi-plus"
            size="small"
            @click="router.push('/orders/new')"
          />
        </EmptyState>
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
          {{ formatDate(data.createdAt) }}
        </template>
      </Column>
      <Column v-if="auth.isCompanyAdmin" style="width:48px;padding:0">
        <template #body="{ data }">
          <button class="delete-btn" @click="deleteOrder($event, data.id)" :title="t('common.delete')">
            <i class="pi pi-times" />
          </button>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
.filter-select { width: 160px; }
.recipient-label { color: #64748b; font-style: italic; }
.priority-normal { color: #94a3b8; font-size: 13px; }
:deep(tr:hover) .delete-btn { opacity: 1; }
</style>

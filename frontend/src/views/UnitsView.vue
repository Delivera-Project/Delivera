<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()

const units = ref([])
const loading = ref(false)
const error = ref('')
const deleteError = ref('')
const filterText = ref('')
const filterType = ref('ALL')

const typeOptions = computed(() => [
  { label: t('units.filterAll'), value: 'ALL' },
  { label: t('units.WAREHOUSE'), value: 'WAREHOUSE' },
  { label: t('units.STORE'), value: 'STORE' },
  { label: t('units.FACTORY'), value: 'FACTORY' },
  { label: t('units.LOGISTICS_CENTER'), value: 'LOGISTICS_CENTER' },
])

const filtered = computed(() => {
  return units.value.filter(u => {
    if (filterType.value !== 'ALL' && u.type !== filterType.value) return false
    if (filterText.value) {
      const q = filterText.value.toLowerCase()
      if (!u.name.toLowerCase().includes(q)) return false
    }
    return true
  })
})

async function load() {
  loading.value = true
  try {
    const res = await api.get('/units')
    if (res.ok) units.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

async function deleteUnit(e, id) {
  e.stopPropagation()
  deleteError.value = ''
  if (!confirm(t('units.deleteConfirm'))) return
  const res = await api.del(`/units/${id}`)
  if (res.ok) {
    units.value = units.value.filter(u => u.id !== id)
  } else {
    deleteError.value = res.status === 409
      ? t('units.deleteActiveOrders')
      : t('error.connection')
  }
}

onMounted(load)
</script>

<template>
  <div class="card card-wide">
    <div class="units-header">
      <h1>{{ t('units.title') }}</h1>
      <PButton
        v-if="auth.isCompanyAdmin"
        :label="t('units.new')"
        icon="pi pi-plus"
        @click="router.push('/units/new')"
      />
    </div>

    <div class="units-filters-wrapper">
      <span class="filters-label">{{ t('common.filters') }}</span>
      <div class="units-filters">
        <div class="filter-row">
          <label class="filter-group-label">{{ t('fields.unitName') }}</label>
          <input
            v-model="filterText"
            :placeholder="t('fields.unitName') + '...'"
            class="filter-search"
            type="text"
          />
        </div>
        <div class="filter-row">
          <label class="filter-group-label">{{ t('fields.type') }}</label>
          <PSelect
            v-model="filterType"
            :options="typeOptions"
            option-label="label"
            option-value="value"
            class="filter-select"
          />
        </div>
      </div>
    </div>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>
    <PMessage v-if="deleteError" severity="error" :closable="false">{{ deleteError }}</PMessage>

    <DataTable
      :value="filtered"
      :loading="loading"
      :rows="20"
      striped-rows
      row-hover
      @row-click="e => router.push(`/units/${e.data.id}`)"
    >
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-building empty-icon" />
          <p>{{ t('units.empty') }}</p>
          <PButton
            v-if="auth.isCompanyAdmin"
            :label="t('units.new')"
            icon="pi pi-plus"
            size="small"
            @click="router.push('/units/new')"
          />
        </div>
      </template>
      <Column :header="t('fields.type')" style="width:160px">
        <template #body="{ data }">
          <PTag :value="t(`units.${data.type}`)" />
        </template>
      </Column>
      <Column field="name" :header="t('fields.unitName')" />
      <Column field="address" :header="t('fields.address')" />
      <Column v-if="auth.isCompanyAdmin" style="width:48px;padding:0">
        <template #body="{ data }">
          <button class="delete-btn" @click="deleteUnit($event, data.id)" :title="t('common.delete')">
            <i class="pi pi-times" />
          </button>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
.units-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.units-header h1 { margin: 0; }

.units-filters-wrapper {
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

.units-filters {
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
.filter-select { width: 200px; }

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

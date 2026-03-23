<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import EmptyState from '@/components/EmptyState.vue'

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
    <div class="list-header">
      <h1>{{ t('units.title') }}</h1>
      <PButton
        v-if="auth.isCompanyAdmin"
        :label="t('units.new')"
        icon="pi pi-plus"
        @click="router.push('/units/new')"
      />
    </div>

    <div class="filters-wrapper">
      <span class="filters-label">{{ t('common.filters') }}</span>
      <div class="filters-box">
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
        <EmptyState icon="pi-warehouse" :message="t('units.empty')">
          <PButton
            v-if="auth.isCompanyAdmin"
            :label="t('units.new')"
            icon="pi pi-plus"
            size="small"
            @click="router.push('/units/new')"
          />
        </EmptyState>
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
.filter-select { width: 200px; }
:deep(tr:hover) .delete-btn { opacity: 1; }
</style>

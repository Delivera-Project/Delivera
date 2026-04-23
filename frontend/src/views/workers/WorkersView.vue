<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'
import { useAuthStore } from '@/stores/auth'
import { WORKER_ROLES } from '@/constants/roles'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const api = useApi()
const auth = useAuthStore()
const { formatDate } = useFormatDate()

const workers = ref([])
const loading = ref(false)
const error = ref('')
const filterRole = ref(null)
const filterText = ref('')
const currentPage = ref(1)
const PAGE_SIZE = 5
const inviteSuccess = ref(route.query.invited ? route.query.invited : null)

// Change role
const changingRoleId = ref(null)
const newRole = ref('')
const roleLoading = ref(false)
const roleError = ref('')

// Delete confirm
const removingId = ref(null)
const removeLoading = ref(false)
const removeError = ref('')

const ROLES = WORKER_ROLES

const roleOptions = computed(() =>
  ROLES.map(r => ({ label: t('workers.roles.' + r), value: r }))
)

const filtered = computed(() => {
  const q = filterText.value.toLowerCase()
  return workers.value.filter(w => {
    if (filterRole.value && w.role !== filterRole.value) return false
    if (q) {
      const name = `${w.firstName ?? ''} ${w.lastName ?? ''}`.toLowerCase()
      if (!name.includes(q) && !(w.email ?? '').toLowerCase().includes(q)) return false
    }
    return true
  })
})

const paginatedWorkers = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filtered.value.slice(start, start + PAGE_SIZE)
})

const totalPages = computed(() => Math.ceil(filtered.value.length / PAGE_SIZE))

watch(filtered, () => { currentPage.value = 1 })

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await api.get('/workers')
    if (res.ok) workers.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

function startChangeRole(worker) {
  removingId.value = null
  changingRoleId.value = worker.id
  newRole.value = worker.role
  roleError.value = ''
}

async function saveRole(workerId) {
  roleLoading.value = true
  roleError.value = ''
  try {
    const res = await api.patch(`/workers/${workerId}/role`, { role: newRole.value })
    if (res.ok) {
      const updated = await res.json()
      const idx = workers.value.findIndex(w => w.id === workerId)
      if (idx !== -1) workers.value[idx] = updated
      changingRoleId.value = null
    } else {
      const data = await res.json()
      roleError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    roleError.value = t('error.connection')
  } finally {
    roleLoading.value = false
  }
}

async function remove(workerId) {
  removeLoading.value = true
  removeError.value = ''
  try {
    const res = await api.del(`/workers/${workerId}`)
    if (res.ok) {
      workers.value = workers.value.filter(w => w.id !== workerId)
      removingId.value = null
    } else {
      const data = await res.json()
      removeError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    removeError.value = t('error.connection')
  } finally {
    removeLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="workers-page">
    <div class="workers-card">
      <div class="workers-header">
        <div>
          <h1 class="workers-title">{{ t('workers.title') }}</h1>
          <p class="workers-subtitle">{{ t('workers.subtitle') }}</p>
        </div>
        <PButton
          :label="t('workers.invite')"
          icon="pi pi-plus"
          @click="router.push('/workers/invite')"
        />
      </div>

      <PMessage v-if="inviteSuccess" severity="success" :closable="true" @close="inviteSuccess = null" class="form-message">
        {{ t('workers.invited') }}
      </PMessage>

      <div class="search-bar">
        <span class="search-icon pi pi-search" />
        <input v-model="filterText" class="search-input" :placeholder="t('workers.searchPlaceholder')" type="text" />
      </div>

      <div class="filter-bar">
        <button :class="['role-filter-btn', { active: filterRole === null }]" @click="filterRole = null">
          {{ t('workers.filterAll') }}
        </button>
        <button
          v-for="r in ROLES" :key="r"
          :class="['role-filter-btn', { active: filterRole === r }]"
          @click="filterRole = filterRole === r ? null : r"
        >{{ t('workers.roles.' + r) }}</button>
      </div>

      <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

      <div v-if="loading" class="workers-list">
        <div v-for="i in 3" :key="i" class="worker-card worker-card--skeleton" />
      </div>

      <div v-else-if="filtered.length" class="workers-list">
        <div
          v-for="data in paginatedWorkers" :key="data.id"
          :class="['worker-card', { 'worker-card--removing': removingId === data.id && data.email !== auth.user?.email }]"
        >
          <div class="worker-avatar">
            {{ (data.firstName?.[0] || data.email?.[0] || '?').toUpperCase() }}
          </div>
          <div class="worker-info">
            <span class="worker-name">{{ data.firstName }} {{ data.lastName }}</span>
            <small class="worker-email">{{ data.email }}</small>
            <small v-if="removingId !== data.id" class="worker-since">{{ t('workers.since') }}: {{ formatDate(data.createdAt) }}</small>
          </div>

          <!-- Estado: confirmar eliminación -->
          <template v-if="removingId === data.id && data.email !== auth.user?.email">
            <PTag :value="t('workers.roles.' + data.role)" :severity="data.role === 'COMPANY_ADMIN' ? 'warn' : 'info'" style="flex-shrink:0" />
            <div class="remove-question">
              <span>{{ t('workers.confirmRemove') }}</span>
              <small>{{ t('workers.confirmRemoveDesc') }}</small>
            </div>
            <div class="role-edit">
              <PButton :label="t('common.cancel')" icon="pi pi-times" size="small" severity="secondary" outlined @click="removingId = null" />
              <PButton :label="t('workers.removeWorker')" icon="pi pi-user-minus" size="small" severity="danger" :loading="removeLoading" @click="remove(data.id)" />
            </div>
          </template>

          <!-- Estado: normal -->
          <template v-else>
            <div class="worker-role">
              <template v-if="changingRoleId === data.id">
                <div class="role-edit">
                  <PSelect v-model="newRole" :options="roleOptions" option-label="label" option-value="value" size="small" />
                  <PButton :label="t('common.cancel')" icon="pi pi-times" size="small" severity="secondary" outlined @click="changingRoleId = null" />
                  <PButton :label="t('common.save')" icon="pi pi-check" size="small" :loading="roleLoading" @click="saveRole(data.id)" />
                </div>
                <small v-if="roleError" class="field-error">{{ roleError }}</small>
              </template>
              <template v-else>
                <PTag :value="t('workers.roles.' + data.role)" :severity="data.role === 'COMPANY_ADMIN' ? 'warn' : 'info'" />
              </template>
            </div>
            <div v-if="changingRoleId !== data.id" class="worker-actions">
              <button class="worker-action-btn" @click.stop="startChangeRole(data)" :title="t('workers.changeRole')">
                <i class="pi pi-pencil" />
              </button>
              <button v-if="data.email !== auth.user?.email" class="worker-action-btn worker-action-btn--danger" @click.stop="changingRoleId = null; removingId = data.id; removeError = ''" :title="t('workers.confirmRemove')">
                <i class="pi pi-times" />
              </button>
            </div>
          </template>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
          <i class="pi pi-chevron-left" />
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">
          <i class="pi pi-chevron-right" />
        </button>
      </div>

      <div v-else-if="!loading && !filtered.length" class="workers-empty">
        <i class="pi pi-users" style="font-size:32px;color:#cbd5e1" />
        <p>{{ t('workers.empty') }}</p>
      </div>

      <PMessage v-if="removeError" severity="error" :closable="true" @close="removeError = ''" class="form-message">{{ removeError }}</PMessage>
    </div>
  </div>
</template>

<style scoped src="./WorkersView.css"></style>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const api = useApi()
const { formatDate } = useFormatDate()

const workers = ref([])
const loading = ref(false)
const error = ref('')
const filterRole = ref(null)

// Invite form
const showInvite = ref(false)
const inviteEmail = ref('')
const inviteRole = ref('OPERATOR')
const inviting = ref(false)
const inviteError = ref('')
const inviteSuccess = ref(null)

// Confirm remove
const removingId = ref(null)
const removeLoading = ref(false)
const removeError = ref('')

// Change role
const changingRoleId = ref(null)
const newRole = ref('')
const roleLoading = ref(false)
const roleError = ref('')

const ROLES = ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR']

const roleOptions = computed(() =>
  ROLES.map(r => ({ label: t('workers.roles.' + r), value: r }))
)

const filtered = computed(() =>
  filterRole.value ? workers.value.filter(w => w.role === filterRole.value) : workers.value
)

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

async function invite() {
  if (!inviteEmail.value.trim()) return
  inviting.value = true
  inviteError.value = ''
  inviteSuccess.value = null
  try {
    const res = await api.post('/workers/invite', { email: inviteEmail.value.trim(), role: inviteRole.value })
    if (res.ok) {
      const created = await res.json()
      workers.value.push(created)
      inviteSuccess.value = created
      inviteEmail.value = ''
      inviteRole.value = 'OPERATOR'
      showInvite.value = false
    } else {
      const data = await res.json()
      inviteError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    inviteError.value = t('error.connection')
  } finally {
    inviting.value = false
  }
}

function startChangeRole(worker) {
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
  <div class="card card-wide">
    <div class="list-header">
      <h1>{{ t('workers.title') }}</h1>
      <PButton :label="t('workers.invite')" icon="pi pi-plus" @click="showInvite = !showInvite; inviteError = ''; inviteSuccess = null" />
    </div>

    <!-- Resultado de invitación -->
    <PMessage v-if="inviteSuccess" severity="success" :closable="true" @close="inviteSuccess = null" class="form-message">
      {{ t('workers.invited') }}
      <template v-if="inviteSuccess.tempPassword">
        <br />
        <strong>{{ t('workers.tempPasswordHint') }}</strong>
        <code class="temp-pass">{{ inviteSuccess.tempPassword }}</code>
      </template>
    </PMessage>

    <!-- Formulario invitación -->
    <div v-if="showInvite" class="invite-form">
      <h3>{{ t('workers.inviteTitle') }}</h3>
      <div class="form-row">
        <div class="form-field">
          <label>{{ t('workers.email') }}</label>
          <InputText v-model="inviteEmail" type="email" :placeholder="t('fields.emailPlaceholder')" fluid />
        </div>
        <div class="form-field">
          <label>{{ t('workers.role') }}</label>
          <PSelect v-model="inviteRole" :options="roleOptions" option-label="label" option-value="value" fluid />
        </div>
      </div>
      <PMessage v-if="inviteError" severity="error" :closable="false" class="form-message">{{ inviteError }}</PMessage>
      <div class="form-actions">
        <PButton :label="t('common.cancel')" severity="secondary" text @click="showInvite = false" />
        <PButton :label="inviting ? t('common.loading') : t('workers.invite')" :loading="inviting" @click="invite" />
      </div>
    </div>

    <!-- Filtro por rol -->
    <div class="filter-bar">
      <PButton
        :label="t('workers.filterAll')"
        :severity="filterRole === null ? 'primary' : 'secondary'"
        text
        @click="filterRole = null"
      />
      <PButton
        v-for="r in ROLES" :key="r"
        :label="t('workers.roles.' + r)"
        :severity="filterRole === r ? 'primary' : 'secondary'"
        text
        @click="filterRole = filterRole === r ? null : r"
      />
    </div>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <DataTable :value="filtered" :loading="loading" striped-rows>
      <template #empty>
        <EmptyState icon="pi-users" :message="t('workers.empty')" />
      </template>

      <Column :header="t('workers.name')">
        <template #body="{ data }">
          <span class="worker-name">{{ data.firstName }} {{ data.lastName }}</span>
          <small class="worker-email">{{ data.email }}</small>
        </template>
      </Column>

      <Column :header="t('workers.role')" style="width:200px">
        <template #body="{ data }">
          <template v-if="changingRoleId === data.id">
            <div class="role-edit">
              <PSelect v-model="newRole" :options="roleOptions" option-label="label" option-value="value" size="small" />
              <PButton icon="pi pi-check" size="small" :loading="roleLoading" @click="saveRole(data.id)" />
              <PButton icon="pi pi-times" size="small" severity="secondary" text @click="changingRoleId = null" />
            </div>
            <small v-if="roleError" class="field-error">{{ roleError }}</small>
          </template>
          <template v-else>
            <PTag :value="t('workers.roles.' + data.role)" :severity="data.role === 'COMPANY_ADMIN' ? 'warn' : 'info'" />
          </template>
        </template>
      </Column>

      <Column :header="t('workers.since')" style="width:130px">
        <template #body="{ data }">{{ formatDate(data.createdAt) }}</template>
      </Column>

      <Column style="width:100px">
        <template #body="{ data }">
          <div class="row-actions">
            <PButton icon="pi pi-pencil" text severity="secondary" size="small" @click="startChangeRole(data)" />
            <PButton icon="pi pi-trash" text severity="danger" size="small" @click="removingId = data.id; removeError = ''" />
          </div>
        </template>
      </Column>
    </DataTable>

    <!-- Confirm remove -->
    <PDialog :visible="removingId !== null" @update:visible="val => { if (!val) removingId = null }" modal :header="t('workers.confirmRemove')" style="width:360px">
      <PMessage v-if="removeError" severity="error" :closable="false" class="form-message">{{ removeError }}</PMessage>
      <div class="dialog-actions">
        <PButton :label="t('common.cancel')" severity="secondary" text @click="removingId = null" />
        <PButton :label="removeLoading ? t('common.loading') : t('common.delete')" severity="danger" :loading="removeLoading" @click="remove(removingId)" />
      </div>
    </PDialog>
  </div>
</template>

<style scoped>
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.list-header h1 { margin: 0; }

.invite-form {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.invite-form h3 { margin: 0; font-size: 14px; font-weight: 600; color: #374151; }
.form-row { display: flex; gap: 16px; }
.form-row .form-field { flex: 1; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-size: 13px; font-weight: 500; color: #374151; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; }

.filter-bar { display: flex; gap: 4px; margin-bottom: 12px; flex-wrap: wrap; }

.worker-name { display: block; font-size: 14px; font-weight: 500; color: #1e293b; }
.worker-email { display: block; font-size: 12px; color: #94a3b8; }

.role-edit { display: flex; align-items: center; gap: 6px; }

.row-actions { display: flex; gap: 4px; justify-content: flex-end; }

.temp-pass {
  display: inline-block;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  margin-left: 8px;
}

.dialog-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 8px; }
.field-error { color: #ef4444; font-size: 11px; }
</style>

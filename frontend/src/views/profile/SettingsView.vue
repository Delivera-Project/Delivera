<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import { useValidation } from '@/composables/useValidation'
import { useActivityTypes } from '@/composables/useActivityTypes'
import DeleteConfirmPanel from '@/components/DeleteConfirmPanel.vue'

const { t } = useI18n()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()
const { validate, required, errors: vErrors, invalids } = useValidation()
const { activityTypes, load: loadActivityTypes } = useActivityTypes()

const settings = ref(null)
const allCompanies = ref([])
const subscription = ref(null)
const loadError = ref('')
const activeTab = ref(0)

// Edición org
const editingOrg = ref(false)
const orgName = ref('')
const orgHandle = ref('')
const orgSaving = ref(false)
const orgError = ref('')
const orgSuccess = ref(false)

// Edición empresa
const editingCompanyId = ref(null)
const companyName = ref('')
const activityType = ref(null)
const companySaving = ref(false)
const companyError = ref('')
const companySuccess = ref(false)

// Eliminar empresa
const deletingCompanyId = ref(null)
const deleteHasActiveOrders = ref(false)
const deleteError = ref('')
const deleteLoading = ref(false)

// Añadir empresa
const addingCompany = ref(false)
const newCompanyName = ref('')
const newActivityType = ref(null)
const addSaving = ref(false)
const addError = ref('')

const activityOptions = computed(() =>
  activityTypes.value.map(a => ({ label: a.label, value: a.value }))
)

function activityLabel(code) {
  return activityTypes.value.find(a => a.value === code)?.label ?? code
}

async function load() {
  loadError.value = ''
  try {
    const [settRes, compRes, subRes] = await Promise.all([
      api.get('/settings'),
      api.get('/settings/companies'),
      api.get('/settings/subscription'),
    ])
    if (settRes.ok) settings.value = await settRes.json()
    else loadError.value = t('error.connection')
    if (compRes.ok) allCompanies.value = await compRes.json()
    if (subRes.ok) subscription.value = await subRes.json()
  } catch {
    loadError.value = t('error.connection')
  }
}

function usagePercent(usage) {
  if (!usage || usage.unlimited) return 0
  return Math.round((usage.current / usage.max) * 100)
}

function usageSeverity(pct) {
  if (pct > 85) return 'danger'
  if (pct > 60) return 'warn'
  return 'success'
}

const PLANS = [
  { code: 'FREE',  name: 'Free',  units: 3,  workers: 5,  ordersThisMonth: 50,  loyalUsers: 20,  companies: 1 },
  { code: 'BASIC', name: 'Basic', units: 10, workers: 15, ordersThisMonth: 200, loyalUsers: 100, companies: 5 },
  { code: 'PRO',   name: 'Pro',   units: -1, workers: -1, ordersThisMonth: -1,  loyalUsers: -1,  companies: -1 },
]

const planChanging = ref(false)
const planChangeError = ref('')

async function selectPlan(planCode) {
  planChangeError.value = ''
  planChanging.value = true
  try {
    const res = await api.patch('/settings/subscription/plan', { planCode })
    if (res.ok) subscription.value = await res.json()
    else {
      const data = await res.json()
      planChangeError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    planChangeError.value = t('error.connection')
  } finally {
    planChanging.value = false
  }
}

const showUpgradeBanner = computed(() =>
  subscription.value && ['units','workers','ordersThisMonth','loyalUsers','companies'].some(k => {
    const r = subscription.value[k]
    return !r.unlimited && usagePercent(r) > 85
  })
)

onMounted(() => { load(); loadActivityTypes() })

function startEditOrg() {
  orgName.value = settings.value.orgName
  orgHandle.value = settings.value.orgHandle
  orgError.value = ''
  orgSuccess.value = false
  editingOrg.value = true
}

async function saveOrg() {
  if (!validate({ orgName: [required(orgName.value, 'orgName')] })) return
  if (!orgHandle.value || !/^[a-z0-9]([a-z0-9-]*[a-z0-9])?$/.test(orgHandle.value)) {
    orgError.value = t('validation.orgCodeInvalid')
    return
  }
  orgSaving.value = true
  orgError.value = ''
  try {
    const res = await api.put('/settings/organization', { name: orgName.value, handle: orgHandle.value })
    if (res.ok) {
      settings.value = await res.json()
      auth.setOrganization(settings.value.orgHandle)
      auth.setOrgName(settings.value.orgName)
      editingOrg.value = false
      orgSuccess.value = true
      setTimeout(() => { orgSuccess.value = false }, 3000)
    } else {
      const data = await res.json()
      orgError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    orgError.value = t('error.connection')
  } finally {
    orgSaving.value = false
  }
}

function startEditCompany(company) {
  editingCompanyId.value = company.id
  companyName.value = company.name
  activityType.value = company.activityType
  companyError.value = ''
  companySuccess.value = false
}

function cancelEditCompany() {
  editingCompanyId.value = null
  companyError.value = ''
}

async function saveCompany() {
  if (!validate({ companyName: [required(companyName.value, 'companyName')] })) return
  if (!activityType.value) { companyError.value = t('validation.activityTypeRequired'); return }
  companySaving.value = true
  companyError.value = ''
  try {
    const res = await api.put('/settings/company', { name: companyName.value, activityType: activityType.value })
    if (res.ok) {
      const updated = await res.json()
      settings.value = updated
      const idx = allCompanies.value.findIndex(c => c.id === editingCompanyId.value)
      if (idx !== -1) allCompanies.value[idx] = { ...allCompanies.value[idx], name: companyName.value, activityType: activityType.value }
      if (updated.companyId === editingCompanyId.value) auth.setCompanyName(updated.companyName)
      editingCompanyId.value = null
      companySuccess.value = true
      setTimeout(() => { companySuccess.value = false }, 3000)
    } else {
      const data = await res.json()
      companyError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    companyError.value = t('error.connection')
  } finally {
    companySaving.value = false
  }
}

function startDeleteCompany(id) {
  editingCompanyId.value = null
  deletingCompanyId.value = id
  deleteHasActiveOrders.value = false
  deleteError.value = ''
}

function cancelDelete() {
  deletingCompanyId.value = null
  deleteHasActiveOrders.value = false
  deleteError.value = ''
}

async function confirmDeleteCompany(id) {
  deleteLoading.value = true
  deleteError.value = ''
  const isCurrent = id === settings.value?.companyId
  const force = deleteHasActiveOrders.value
  try {
    // Si es la empresa actual, hacer switch antes de eliminar
    if (isCurrent) {
      const next = allCompanies.value.find(c => c.id !== id)
      if (!next) return
      const switchRes = await api.post('/auth/switch-company', { companyId: next.id })
      if (!switchRes.ok) { deleteError.value = t('error.connection'); return }
      auth.applyLoginData(await switchRes.json())
    }

    const url = force ? `/settings/companies/${id}?force=true` : `/settings/companies/${id}`
    const res = await api.del(url)
    if (res.ok) {
      if (isCurrent) {
        router.push('/settings')
      } else {
        allCompanies.value = allCompanies.value.filter(c => c.id !== id)
        auth.loadCompanies()
        deletingCompanyId.value = null
        deleteHasActiveOrders.value = false
      }
    } else {
      const data = await res.json()
      if (data?.code === 'COMPANY_HAS_ACTIVE_ORDERS') {
        deleteHasActiveOrders.value = true
        deleteError.value = ''
      } else {
        deleteError.value = api.translateError(data, 'error.saveFailed')
      }
    }
  } catch {
    deleteError.value = t('error.connection')
  } finally {
    deleteLoading.value = false
  }
}

async function addCompany() {
  if (!newCompanyName.value.trim()) { addError.value = t('error.saveFailed'); return }
  if (!newActivityType.value) { addError.value = t('validation.activityTypeRequired'); return }
  addSaving.value = true
  addError.value = ''
  try {
    const res = await api.post('/settings/companies', { name: newCompanyName.value.trim(), activityType: newActivityType.value })
    if (res.ok) {
      const created = await res.json()
      allCompanies.value.push(created)
      auth.loadCompanies()
      newCompanyName.value = ''
      newActivityType.value = null
      addingCompany.value = false
    } else {
      const data = await res.json()
      addError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    addError.value = t('error.connection')
  } finally {
    addSaving.value = false
  }
}

async function copyHandle() {
  if (!settings.value?.orgHandle) return
  await navigator.clipboard.writeText(settings.value.orgHandle)
}
</script>

<template>
  <div class="card card-wide">
    <h1>{{ t('settings.title') }}</h1>

    <PMessage v-if="loadError" severity="error" :closable="false" class="form-message">{{ loadError }}</PMessage>
    <PMessage v-if="showUpgradeBanner" severity="warn" :closable="true" class="form-message">{{ t('settings.upgradeBanner') }}</PMessage>

    <PTabs v-if="settings" v-model:value="activeTab">
      <PTabList>
        <PTab :value="0">{{ t('settings.orgSection') }}</PTab>
        <PTab :value="1">{{ t('settings.companySection') }}</PTab>
        <PTab :value="2">{{ t('settings.subscriptionSection') }}</PTab>
      </PTabList>

      <PTabPanels>
        <!-- Organización -->
        <PTabPanel :value="0">
          <div class="settings-section">
            <PMessage v-if="orgSuccess" severity="success" :closable="false" class="form-message">{{ t('settings.saved') }}</PMessage>

            <div v-if="!editingOrg" class="settings-info">
              <div class="info-row">
                <span class="info-label">{{ t('fields.orgName') }}</span>
                <span class="info-value">{{ settings.orgName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">{{ t('settings.orgCode') }}</span>
                <span class="info-value code-value">
                  {{ settings.orgHandle }}
                  <PButton icon="pi pi-copy" text severity="secondary" size="small" @click="copyHandle" v-tooltip="t('settings.orgCodeCopied')" />
                </span>
              </div>
              <small class="field-hint">{{ t('settings.orgCodeHint') }}</small>
              <div class="settings-actions">
                <PButton :label="t('settings.editOrg')" icon="pi pi-pencil" severity="secondary" @click="startEditOrg" />
              </div>
            </div>

            <div v-else class="settings-form">
              <div class="form-field">
                <label>{{ t('fields.orgName') }}</label>
                <InputText v-model="orgName" :placeholder="t('fields.orgNamePlaceholder')" :invalid="!!invalids.orgName" fluid />
                <small v-if="vErrors.orgName" class="field-error">{{ vErrors.orgName }}</small>
              </div>
              <div class="form-field">
                <label>{{ t('settings.orgCode') }}</label>
                <InputText v-model="orgHandle" :placeholder="t('fields.orgCodePlaceholder')" maxlength="100" fluid />
                <small class="field-hint">{{ t('fields.orgCodeHint') }}</small>
              </div>
              <PMessage v-if="orgError" severity="error" :closable="false" class="form-message">{{ orgError }}</PMessage>
              <div class="form-actions">
                <PButton :label="t('common.cancel')" severity="secondary" text @click="editingOrg = false" />
                <PButton :label="orgSaving ? t('common.loading') : t('common.save')" :loading="orgSaving" @click="saveOrg" />
              </div>
            </div>
          </div>
        </PTabPanel>

        <!-- Empresa -->
        <PTabPanel :value="1">
          <div class="settings-section">
            <PMessage v-if="companySuccess" severity="success" :closable="false" class="form-message">{{ t('settings.saved') }}</PMessage>

            <!-- Lista de empresas -->
            <div class="company-list">
              <div v-for="c in allCompanies" :key="c.id" class="company-item-wrapper">
                <template v-if="editingCompanyId === c.id">
                  <div class="settings-form">
                    <div class="form-field">
                      <label>{{ t('fields.companyName') }}</label>
                      <InputText v-model="companyName" :placeholder="t('fields.companyNamePlaceholder')" :invalid="!!invalids.companyName" fluid />
                      <small v-if="vErrors.companyName" class="field-error">{{ vErrors.companyName }}</small>
                    </div>
                    <div class="form-field">
                      <label>{{ t('fields.type') }}</label>
                      <PSelect v-model="activityType" :options="activityOptions" option-label="label" option-value="value" fluid />
                    </div>
                    <PMessage v-if="companyError" severity="error" :closable="false" class="form-message">{{ companyError }}</PMessage>
                    <div class="form-actions">
                      <PButton :label="t('common.cancel')" severity="secondary" text @click="cancelEditCompany" />
                      <PButton :label="companySaving ? t('common.loading') : t('common.save')" :loading="companySaving" @click="saveCompany" />
                    </div>
                  </div>
                </template>
                <template v-else>
                  <div class="company-item-row" @click="startEditCompany(c)">
                    <div class="company-item-info">
                      <span class="company-item-avatar">{{ c.name[0].toUpperCase() }}</span>
                      <div>
                        <span class="company-item-name">{{ c.name }}</span>
                        <span v-if="c.id === settings.companyId" class="company-item-badge">{{ t('settings.current') }}</span>
                        <span class="company-item-type">{{ activityLabel(c.activityType) }}</span>
                      </div>
                    </div>
                    <div v-if="allCompanies.length > 1" class="company-item-actions" @click.stop>
                      <PButton icon="pi pi-times" text severity="danger" size="small" @click="startDeleteCompany(c.id)" />
                    </div>
                  </div>

                  <Transition name="delete-panel">
                    <DeleteConfirmPanel
                      v-if="deletingCompanyId === c.id"
                      :has-active-orders="deleteHasActiveOrders"
                      :loading="deleteLoading"
                      :error="deleteError"
                      @confirm="confirmDeleteCompany(c.id)"
                      @cancel="cancelDelete"
                    />
                  </Transition>
                </template>
              </div>
            </div>

            <!-- Añadir empresa -->
            <div class="settings-actions settings-actions--top">
              <PButton :label="t('settings.addCompany')" icon="pi pi-plus" @click="addingCompany = !addingCompany; addError = ''" />
            </div>

            <div v-if="addingCompany" class="add-company-form">
              <h3>{{ t('settings.newCompany') }}</h3>
              <div class="form-field">
                <label>{{ t('fields.companyName') }}</label>
                <InputText v-model="newCompanyName" :placeholder="t('fields.companyNamePlaceholder')" fluid />
              </div>
              <div class="form-field">
                <label>{{ t('fields.type') }}</label>
                <PSelect v-model="newActivityType" :options="activityOptions" option-label="label" option-value="value" fluid />
              </div>
              <PMessage v-if="addError" severity="error" :closable="false" class="form-message">{{ addError }}</PMessage>
              <div class="form-actions">
                <PButton :label="t('common.cancel')" severity="secondary" text @click="addingCompany = false; addError = ''" />
                <PButton :label="addSaving ? t('common.loading') : t('settings.addCompany')" :loading="addSaving" @click="addCompany" />
              </div>
            </div>
          </div>
        </PTabPanel>
        <!-- Suscripción -->
        <PTabPanel :value="2">
          <div class="settings-section" v-if="subscription">
            <div class="sub-plan-header">
              <span class="info-label">{{ t('settings.currentPlan') }}</span>
              <span class="sub-plan-badge">{{ subscription.planName }}</span>
            </div>
            <div class="sub-resources">
              <div v-for="key in ['units','workers','ordersThisMonth','loyalUsers','companies']" :key="key" class="sub-resource">
                <div class="sub-resource-header">
                  <span class="sub-resource-label">{{ t('settings.resource.' + key) }}</span>
                  <span class="sub-resource-count">
                    {{ subscription[key].current }}
                    <template v-if="!subscription[key].unlimited"> / {{ subscription[key].max }}</template>
                    <template v-else> / ∞</template>
                  </span>
                </div>
                <PProgressBar
                  v-if="!subscription[key].unlimited"
                  :value="usagePercent(subscription[key])"
                  :pt="{ value: { class: 'sub-bar-' + usageSeverity(usagePercent(subscription[key])) } }"
                  class="sub-progress"
                />
              </div>
            </div>
            <div class="plans-comparison">
              <p class="info-label" style="margin-bottom:12px">{{ t('settings.availablePlans') }}</p>
              <PMessage v-if="planChangeError" severity="error" :closable="false" class="form-message">{{ planChangeError }}</PMessage>
              <div class="plans-grid">
                <div v-for="plan in PLANS" :key="plan.code"
                  :class="['plan-card', { 'plan-card--current': subscription?.planCode === plan.code }]">
                  <div class="plan-card-name">{{ plan.name }}
                    <span v-if="subscription?.planCode === plan.code" class="sub-plan-badge">{{ t('settings.currentPlan') }}</span>
                  </div>
                  <ul class="plan-features">
                    <li v-for="key in ['units','workers','ordersThisMonth','loyalUsers','companies']" :key="key">
                      <span class="plan-feature-label">{{ t('settings.resource.' + key) }}</span>
                      <span class="plan-feature-val">{{ plan[key] === -1 ? '∞' : plan[key] }}</span>
                    </li>
                  </ul>
                  <PButton v-if="subscription?.planCode !== plan.code"
                    :label="t('settings.selectPlan')" size="small" class="plan-select-btn"
                    :loading="planChanging" @click="selectPlan(plan.code)" />
                </div>
              </div>
            </div>
          </div>
        </PTabPanel>
      </PTabPanels>
    </PTabs>
  </div>
</template>

<style scoped>
.card { text-align: left; }

.settings-section {
  padding: 20px 0;
}

.settings-info, .settings-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.info-value {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.code-value {
  font-family: monospace;
  font-size: 14px;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  width: fit-content;
  gap: 6px;
}

.field-hint {
  color: #94a3b8;
  font-size: 11px;
}

.settings-actions {
  display: flex;
  gap: 10px;
  padding-top: 8px;
}

.settings-actions--top {
  padding-top: 20px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.add-company-form {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.add-company-form h3 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.company-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.company-item-wrapper {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.company-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.12s;
}

.company-item-row:hover {
  background: #f8fafc;
}

.company-item-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.company-item-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 8px;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 12%, white);
  color: var(--p-primary-color, #7c3aed);
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.company-item-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.company-item-type {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.company-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.12s;
}

.company-item-row:hover .company-item-actions {
  opacity: 1;
}

.company-item-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 10%, white);
  color: var(--p-primary-color, #7c3aed);
  border: 1px solid color-mix(in srgb, var(--p-primary-color, #7c3aed) 20%, white);
  margin-left: 8px;
  vertical-align: middle;
}

.delete-panel-enter-active, .delete-panel-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}
.delete-panel-enter-from, .delete-panel-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.sub-plan-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.sub-plan-badge {
  font-size: 13px;
  font-weight: 700;
  padding: 3px 12px;
  border-radius: 20px;
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 12%, white);
  color: var(--p-primary-color, #7c3aed);
  border: 1px solid color-mix(in srgb, var(--p-primary-color, #7c3aed) 25%, white);
}
.sub-resources {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.sub-resource-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}
.sub-resource-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}
.sub-resource-count {
  font-size: 13px;
  color: #64748b;
}
.sub-progress {
  height: 8px;
  border-radius: 4px;
}
:deep(.sub-bar-success) { background: #22c55e; }
:deep(.sub-bar-warn)    { background: #f59e0b; }
:deep(.sub-bar-danger)  { background: #ef4444; }

.plans-comparison { margin-top: 32px; padding-top: 24px; border-top: 1px solid #e2e8f0; }
.plans-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.plan-card {
  border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px;
}
.plan-card--current {
  border-color: var(--p-primary-color, #7c3aed);
  background: color-mix(in srgb, var(--p-primary-color, #7c3aed) 4%, white);
}
.plan-select-btn { margin-top: 12px; width: 100%; }
.plan-card-name {
  font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 12px;
  display: flex; align-items: center; gap: 8px;
}
.plan-features { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px; }
.plan-features li { display: flex; justify-content: space-between; font-size: 13px; }
.plan-feature-label { color: #64748b; }
.plan-feature-val { font-weight: 600; color: #1e293b; }
</style>

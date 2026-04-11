<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'

const { t } = useI18n()
const router = useRouter()
const api = useApi()

const step = ref(1) // 1 = crear unidad, 2 = invitar trabajador

// Step 1: unidad
const unitName = ref('')
const unitType = ref('WAREHOUSE')
const unitSaving = ref(false)
const unitError = ref('')

const UNIT_TYPES = ['WAREHOUSE', 'STORE', 'FACTORY', 'LOGISTICS_CENTER']
const unitTypeOptions = UNIT_TYPES.map(t => ({ label: t, value: t }))

// Step 2: trabajador
const workerEmail = ref('')
const workerRole = ref('OPERATOR')
const workerSaving = ref(false)
const workerError = ref('')

const ROLES = ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR']
const roleOptions = ROLES.map(r => ({ label: t('workers.roles.' + r), value: r }))

async function createUnit() {
  if (!unitName.value.trim()) { unitError.value = t('validation.required', { field: t('fields.unitName') }); return }
  unitSaving.value = true
  unitError.value = ''
  try {
    const res = await api.post('/units', { name: unitName.value.trim(), type: unitType.value })
    if (res.ok) step.value = 2
    else { const d = await res.json(); unitError.value = api.translateError(d, 'error.saveFailed') }
  } catch { unitError.value = t('error.connection') }
  finally { unitSaving.value = false }
}

async function inviteWorker() {
  if (!workerEmail.value.trim()) { workerError.value = t('validation.required', { field: t('workers.email') }); return }
  workerSaving.value = true
  workerError.value = ''
  try {
    const res = await api.post('/workers/invite', { email: workerEmail.value.trim(), role: workerRole.value })
    if (res.ok) router.push('/home')
    else { const d = await res.json(); workerError.value = api.translateError(d, 'error.saveFailed') }
  } catch { workerError.value = t('error.connection') }
  finally { workerSaving.value = false }
}
</script>

<template>
  <div class="onboarding-wrapper">
    <div class="onboarding-card">
      <div class="onboarding-logo">
        <span class="logo-text">Delivera</span>
      </div>

      <div class="step-indicator">
        <span :class="['step-dot', { active: step >= 1, done: step > 1 }]">1</span>
        <span class="step-line" />
        <span :class="['step-dot', { active: step >= 2 }]">2</span>
      </div>

      <!-- Step 1: Crear unidad -->
      <template v-if="step === 1">
        <h2>{{ t('onboarding.createUnit') }}</h2>
        <p class="step-hint">{{ t('onboarding.createUnitHint') }}</p>

        <div class="form-field">
          <label>{{ t('fields.unitName') }}</label>
          <InputText v-model="unitName" :placeholder="t('fields.unitNamePlaceholder')" fluid />
        </div>
        <div class="form-field">
          <label>{{ t('fields.type') }}</label>
          <PSelect v-model="unitType" :options="unitTypeOptions" option-label="label" option-value="value" fluid />
        </div>

        <PMessage v-if="unitError" severity="error" :closable="false" class="form-message">{{ unitError }}</PMessage>

        <div class="form-actions">
          <PButton :label="t('common.next')" :loading="unitSaving" icon-pos="right" icon="pi pi-arrow-right" @click="createUnit" />
          <PButton :label="t('onboarding.skip')" severity="secondary" text @click="step = 2" />
        </div>
      </template>

      <!-- Step 2: Invitar trabajador -->
      <template v-else>
        <h2>{{ t('onboarding.inviteWorker') }}</h2>
        <p class="step-hint">{{ t('onboarding.inviteWorkerHint') }}</p>

        <div class="form-field">
          <label>{{ t('workers.email') }}</label>
          <InputText v-model="workerEmail" type="email" :placeholder="t('fields.emailPlaceholder')" fluid />
        </div>
        <div class="form-field">
          <label>{{ t('workers.role') }}</label>
          <PSelect v-model="workerRole" :options="roleOptions" option-label="label" option-value="value" fluid />
        </div>

        <PMessage v-if="workerError" severity="error" :closable="false" class="form-message">{{ workerError }}</PMessage>

        <div class="form-actions">
          <PButton :label="t('onboarding.finish')" :loading="workerSaving" @click="inviteWorker" />
          <PButton :label="t('onboarding.skip')" severity="secondary" text @click="router.push('/home')" />
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.onboarding-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  padding: 24px;
}
.onboarding-card {
  background: white;
  border-radius: 16px;
  padding: 40px 36px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.onboarding-logo { text-align: center; }
.logo-text { font-size: 22px; font-weight: 800; color: var(--p-primary-color, #7c3aed); letter-spacing: -0.5px; }

.step-indicator { display: flex; align-items: center; justify-content: center; gap: 8px; }
.step-dot {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
  background: #f1f5f9; color: #94a3b8;
  border: 2px solid #e2e8f0;
}
.step-dot.active { background: var(--p-primary-color, #7c3aed); color: white; border-color: var(--p-primary-color, #7c3aed); }
.step-dot.done { background: #22c55e; color: white; border-color: #22c55e; }
.step-line { flex: 1; max-width: 60px; height: 2px; background: #e2e8f0; }

h2 { margin: 0; font-size: 18px; font-weight: 700; color: #1e293b; }
.step-hint { margin: 0; font-size: 13px; color: #64748b; }
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-size: 13px; font-weight: 500; color: #374151; }
.form-actions { display: flex; flex-direction: column; gap: 8px; }
</style>

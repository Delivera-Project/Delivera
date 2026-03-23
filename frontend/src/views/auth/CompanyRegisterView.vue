<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCompanyRegistration } from '@/composables/useCompanyRegistration'
import { useActivityTypes } from '@/composables/useActivityTypes'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const overlayActive = ref(true)
const { activityTypes, load: loadActivityTypes } = useActivityTypes()

onMounted(loadActivityTypes)

function handleSignIn() {
  overlayActive.value = false
  setTimeout(() => router.push('/'), 300)
}

const {
  step,
  orgName, orgHandle, handleChecking, handleAvailable, isHandleFormat,
  activityType, companyName,
  email, username, fullName, phone, usernameChecking, usernameAvailable, isUsernameFormat,
  password,
  error, errors, invalids, loading,
  goToStep2, goToStep3, submitRegistration,
} = useCompanyRegistration()

const handleState = computed(() => {
  if (!orgHandle.value || !isHandleFormat(orgHandle.value)) return 'invalid'
  if (handleChecking.value) return 'checking'
  if (handleAvailable.value === true) return 'available'
  if (handleAvailable.value === false) return 'taken'
  return 'idle'
})

const usernameState = computed(() => {
  if (!username.value || !isUsernameFormat(username.value)) return 'invalid'
  if (usernameChecking.value) return 'checking'
  if (usernameAvailable.value === true) return 'available'
  if (usernameAvailable.value === false) return 'taken'
  return 'idle'
})
</script>

<template>
  <div class="bg-overlay bg-purple" :class="{ active: overlayActive }"></div>
  <BaseLayout>
    <div class="card card-wide theme-company">
      <!-- Indicador de pasos -->
      <div class="steps">
        <div v-for="(label, i) in [t('register.stepOrg'), t('register.stepCompany'), t('register.stepAccount')]"
             :key="i" class="step-item" :class="{ done: step > i + 1, active: step === i + 1 }">
          <div class="step-dot" :class="{ active: step === i + 1, done: step > i + 1 }">
            <svg v-if="step > i + 1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
            </svg>
            <span v-else>{{ i + 1 }}</span>
          </div>
          <span class="step-label" :class="{ active: step === i + 1, done: step > i + 1 }">{{ label }}</span>
        </div>
      </div>

      <!-- Paso 1 — Organización -->
      <div v-if="step === 1">
        <PButton type="button" text severity="secondary" icon="pi pi-arrow-left" class="back-btn" rounded @click="router.push('/register')" />
        <h1>{{ t('register.orgTitle') }}</h1>
        <p class="subtitle">{{ t('register.orgSubtitle') }}</p>

        <div class="form-field">
          <label for="org-name">{{ t('fields.orgName') }}</label>
          <InputText
            id="org-name"
            v-model="orgName"
            :placeholder="t('fields.orgNamePlaceholder')"
            :invalid="!!invalids.orgName"
            maxlength="255"
            fluid
          />
          <small v-if="errors.orgName" class="field-error">{{ errors.orgName }}</small>
        </div>

        <div class="form-field">
          <label for="org-handle">{{ t('fields.orgCode') }}</label>
          <div class="check-field">
            <InputText
              id="org-handle"
              v-model="orgHandle"
              :placeholder="t('fields.orgCodePlaceholder')"
              :invalid="handleState === 'taken' || handleState === 'invalid' && !!orgHandle"
              maxlength="100"
              fluid
            />
            <span v-if="handleState === 'checking'" class="check-badge check-checking">
              <i class="pi pi-spin pi-spinner" />
            </span>
            <span v-else-if="handleState === 'available'" class="check-badge check-ok">
              <i class="pi pi-check" />
            </span>
            <span v-else-if="handleState === 'taken'" class="check-badge check-taken">
              <i class="pi pi-times" />
            </span>
          </div>
          <small class="field-hint">{{ t('fields.orgCodeHint') }}</small>
          <small v-if="handleState === 'taken'" class="field-error">{{ t('validation.orgCodeTaken') }}</small>
          <small v-else-if="orgHandle && handleState === 'invalid'" class="field-error">{{ t('validation.orgCodeInvalid') }}</small>
        </div>

        <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>
        <PButton :label="t('common.next')" icon="pi pi-arrow-right" icon-pos="right" fluid @click="goToStep2" />
        <p class="form-link">{{ t('auth.hasAccount') }} <a href="/" @click.prevent="handleSignIn">{{ t('auth.signIn') }}</a></p>
      </div>

      <!-- Paso 2 — Empresa -->
      <div v-else-if="step === 2">
        <PButton type="button" text severity="secondary" icon="pi pi-arrow-left" class="back-btn" rounded @click="step = 1; error = ''" />
        <h1>{{ t('register.companyTitle') }}</h1>
        <p class="subtitle">{{ t('register.companySubtitle') }}</p>

        <div class="form-field">
          <label for="company-name">{{ t('fields.companyName') }}</label>
          <InputText
            id="company-name"
            v-model="companyName"
            :placeholder="t('fields.companyNamePlaceholder')"
            :invalid="!!invalids.companyName"
            maxlength="255"
            fluid
          />
          <small v-if="errors.companyName" class="field-error">{{ errors.companyName }}</small>
        </div>

        <div class="form-field">
          <label>{{ t('fields.type') }}</label>
          <div class="activity-grid">
            <button
              v-for="at in activityTypes.value"
              :key="at.value"
              type="button"
              class="activity-option"
              :class="{ selected: activityType === at.value }"
              @click="activityType = at.value; error = ''"
            >
              <span>{{ at.label }}</span>
            </button>
          </div>
        </div>

        <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>
        <PButton :label="t('common.next')" icon="pi pi-arrow-right" icon-pos="right" fluid @click="goToStep3" />
      </div>

      <!-- Paso 3 — Cuenta -->
      <form v-else @submit.prevent="submitRegistration">
        <PButton type="button" text severity="secondary" icon="pi pi-arrow-left" class="back-btn" rounded @click="step = 2; error = ''" />
        <h1>{{ t('register.registerUserTitle') }}</h1>
        <p class="subtitle">{{ t('register.accountSubtitle') }}</p>

        <div class="form-field">
          <label for="company-email">{{ t('fields.email') }}</label>
          <InputText id="company-email" v-model="email" type="email" :placeholder="t('fields.emailPlaceholder')" :invalid="!!invalids.email" fluid />
          <small v-if="errors.email" class="field-error">{{ errors.email }}</small>
        </div>

        <div class="form-field">
          <label for="company-username">{{ t('fields.username') }}</label>
          <div class="check-field">
            <InputText id="company-username" v-model="username" type="text" :placeholder="t('fields.usernamePlaceholder')" :invalid="!!invalids.username || usernameState === 'taken'" autocomplete="username" fluid />
            <span v-if="usernameState === 'checking'" class="check-badge check-checking"><i class="pi pi-spin pi-spinner" /></span>
            <span v-else-if="usernameState === 'available'" class="check-badge check-ok"><i class="pi pi-check" /></span>
            <span v-else-if="usernameState === 'taken'" class="check-badge check-taken"><i class="pi pi-times" /></span>
          </div>
          <small v-if="errors.username" class="field-error">{{ errors.username }}</small>
          <small v-else-if="usernameState === 'taken'" class="field-error">{{ t('error.USERNAME_ALREADY_EXISTS') }}</small>
          <small v-else class="field-hint">{{ t('fields.usernameHint') }}</small>
        </div>

        <div class="form-field">
          <label for="company-fullname">{{ t('fields.fullName') }}</label>
          <InputText id="company-fullname" v-model="fullName" type="text" :placeholder="t('fields.fullNamePlaceholder')" :invalid="!!invalids.fullName" fluid />
          <small v-if="errors.fullName" class="field-error">{{ errors.fullName }}</small>
        </div>

        <div class="form-field">
          <label for="company-phone">{{ t('fields.phone') }}</label>
          <InputText id="company-phone" v-model="phone" type="tel" :placeholder="t('fields.phonePlaceholder')" maxlength="20" fluid />
        </div>

        <div class="form-field">
          <label for="company-password">{{ t('fields.password') }}</label>
          <PPassword id="company-password" v-model="password" :feedback="false" toggle-mask :placeholder="t('fields.password')" :invalid="!!invalids.password" fluid />
          <small v-if="errors.password" class="field-error">{{ errors.password }}</small>
        </div>

        <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>
        <PButton type="submit" :label="loading ? t('common.loading') : t('register.createCompany')" :loading="loading" fluid class="submit-btn" />
        <p class="form-link">{{ t('auth.hasAccount') }} <a href="/" @click.prevent="handleSignIn">{{ t('auth.signIn') }}</a></p>
      </form>
    </div>
  </BaseLayout>
</template>

<style scoped>
.field-hint {
  color: #94a3b8;
  font-size: 11px;
  margin-top: 4px;
  display: block;
}
</style>

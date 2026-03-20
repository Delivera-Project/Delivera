<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const api = useApi()
const { validate, required, email: emailRule, minLength, match, passwordStrength, errors, invalids } = useValidation()

const mode = ref(null)
const hovered = ref(null)
const selectedTheme = ref(null)
const activeTheme = computed(() => hovered.value ?? selectedTheme.value)
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')

function handleSignIn() {
  selectedTheme.value = null
  hovered.value = null
  setTimeout(() => router.push('/'), 300)
}

async function handleRegister() {
  error.value = ''
  const valid = validate({
    email: [required(email.value, 'email'), emailRule(email.value)],
    password: [required(password.value, 'password'), minLength(password.value, 8, 'password'), passwordStrength(password.value)],
    confirmPassword: [match(password.value, confirmPassword.value)],
  })
  if (!valid) return
  try {
    const response = await api.post('/auth/register', { email: email.value, password: password.value })
    const data = await response.json()
    if (response.ok) {
      auth.setToken(data.token)
      router.push('/profile')
    } else {
      error.value = api.translateError(data, 'error.registerFailed')
    }
  } catch {
    error.value = t('error.connection')
  }
}
</script>

<template>
  <div class="bg-overlay bg-celeste" :class="{ active: activeTheme === 'personal' }"></div>
  <div class="bg-overlay bg-purple" :class="{ active: activeTheme === 'company' }"></div>
  <BaseLayout>
    <!-- Selector tipo de cuenta -->
    <div v-if="mode === null" class="card card-wide">
      <h1>{{ t('app.name') }}</h1>
      <p class="subtitle">{{ t('auth.chooseAccountType') }}</p>

      <div class="register-type-grid">
        <button class="register-type-option" type="button" :style="hovered === 'personal' ? { borderColor: '#0ea5e9', boxShadow: '0 0 0 3px rgba(14, 165, 233, 0.1)' } : {}" @click="mode = 'individual'; selectedTheme = 'personal'" @mouseenter="hovered = 'personal'" @mouseleave="hovered = null">
          <svg class="option-icon" :style="{ color: activeTheme === 'personal' ? '#0ea5e9' : undefined }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="8" r="4" />
            <path stroke-linecap="round" d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
          </svg>
          <div class="option-title">{{ t('auth.individualTitle') }}</div>
          <div class="option-desc">{{ t('auth.individualDesc') }}</div>
        </button>

        <router-link to="/register/company" class="register-type-option" :style="hovered === 'company' ? { borderColor: '#8b5cf6', boxShadow: '0 0 0 3px rgba(139, 92, 246, 0.1)' } : {}" @click="selectedTheme = 'company'" @mouseenter="hovered = 'company'" @mouseleave="hovered = null">
          <svg class="option-icon" :style="{ color: activeTheme === 'company' ? '#8b5cf6' : undefined }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="9" width="18" height="13" rx="1" />
            <path stroke-linecap="round" d="M8 22V9M16 22V9M3 13h18M7 9V6a5 5 0 0110 0v3" />
          </svg>
          <div class="option-title">{{ t('auth.companyTitle') }}</div>
          <div class="option-desc">{{ t('auth.companyDesc') }}</div>
        </router-link>
      </div>

      <p class="form-link">{{ t('auth.hasAccount') }} <a href="/" :style="{ color: activeTheme === 'personal' ? '#0ea5e9' : activeTheme === 'company' ? '#8b5cf6' : undefined }" @click.prevent="handleSignIn">{{ t('auth.signIn') }}</a></p>
    </div>

    <!-- Formulario individual -->
    <form v-else class="card theme-personal" @submit.prevent="handleRegister">
      <PButton
        type="button"
        text
        severity="secondary"
        icon="pi pi-arrow-left"
        class="back-btn"
        rounded
        @click="mode = null; selectedTheme = null"
      />
      <h1>{{ t('app.name') }}</h1>
      <p class="subtitle">{{ t('auth.createAccount') }}</p>

      <div class="form-field">
        <label for="register-email">{{ t('fields.email') }}</label>
        <InputText id="register-email" v-model="email" type="email" :placeholder="t('fields.emailPlaceholder')" :invalid="!!invalids.email" fluid />
        <small v-if="errors.email" class="field-error">{{ errors.email }}</small>
      </div>
      <div class="form-field">
        <label for="register-password">{{ t('fields.password') }}</label>
        <PPassword id="register-password" v-model="password" :feedback="false" toggle-mask :placeholder="t('fields.passwordHint')" :invalid="!!invalids.password" fluid />
        <small v-if="errors.password" class="field-error">{{ errors.password }}</small>
      </div>
      <div class="form-field">
        <label for="register-confirm-password">{{ t('fields.confirmPassword') }}</label>
        <PPassword id="register-confirm-password" v-model="confirmPassword" :feedback="false" toggle-mask :placeholder="t('fields.confirmPassword')" :invalid="!!invalids.confirmPassword" fluid />
        <small v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</small>
      </div>

      <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

      <PButton type="submit" :label="t('auth.register')" fluid class="submit-btn" />
      <p class="form-link">{{ t('auth.hasAccount') }} <a href="/" @click.prevent="handleSignIn">{{ t('auth.signIn') }}</a></p>
    </form>
  </BaseLayout>
</template>

<script setup>
import { ref } from 'vue'
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
const { validate, required, email: emailRule, minLength, match, passwordStrength, firstError } = useValidation()

const mode = ref(null) // null | 'individual'
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')

async function handleRegister() {
  error.value = ''
  const valid = validate({
    email: [required(email.value, 'email'), emailRule(email.value)],
    password: [required(password.value, 'password'), minLength(password.value, 8, 'password'), passwordStrength(password.value)],
    confirmPassword: [match(password.value, confirmPassword.value)],
  })
  if (!valid) {
    error.value = firstError()
    return
  }
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
  <BaseLayout>
    <!-- Selector tipo de cuenta -->
    <div v-if="mode === null" class="card card-wide">
      <h1>{{ t('app.name') }}</h1>
      <p class="subtitle">{{ t('auth.chooseAccountType') }}</p>

      <div class="register-type-grid">
        <button class="register-type-option" type="button" @click="mode = 'individual'">
          <svg class="option-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="8" r="4" />
            <path stroke-linecap="round" d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
          </svg>
          <div class="option-title">{{ t('auth.individualTitle') }}</div>
          <div class="option-desc">{{ t('auth.individualDesc') }}</div>
        </button>

        <router-link to="/register/company" class="register-type-option">
          <svg class="option-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="9" width="18" height="13" rx="1" />
            <path stroke-linecap="round" d="M8 22V9M16 22V9M3 13h18M7 9V6a5 5 0 0110 0v3" />
          </svg>
          <div class="option-title">{{ t('auth.companyTitle') }}</div>
          <div class="option-desc">{{ t('auth.companyDesc') }}</div>
        </router-link>
      </div>

      <p class="form-link">{{ t('auth.hasAccount') }} <router-link to="/">{{ t('auth.signIn') }}</router-link></p>
    </div>

    <!-- Formulario individual -->
    <form v-else class="card" @submit.prevent="handleRegister">
      <button type="button" class="back-btn" @click="mode = null">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
        {{ t('common.back') }}
      </button>
      <h1>{{ t('app.name') }}</h1>
      <p class="subtitle">{{ t('auth.createAccount') }}</p>
      <input v-model="email" class="form-input" type="email" :placeholder="t('fields.email')" required />
      <input v-model="password" class="form-input" type="password" :placeholder="t('fields.password')" minlength="8" required />
      <input v-model="confirmPassword" class="form-input" type="password" :placeholder="t('fields.confirmPassword')" minlength="8" required />
      <p v-if="error" class="msg-error">{{ error }}</p>
      <button class="btn" type="submit">{{ t('auth.register') }}</button>
      <p class="form-link">{{ t('auth.hasAccount') }} <router-link to="/">{{ t('auth.signIn') }}</router-link></p>
    </form>
  </BaseLayout>
</template>

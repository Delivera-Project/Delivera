<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'
import { useOrganizationDetection } from '@/composables/useOrganizationDetection'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const api = useApi()
const { validate, required, email: emailRule, firstError } = useValidation()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const { organizations, organizationSlug, detectOrganizations } = useOrganizationDetection(email)

async function handleLogin() {
  error.value = ''

  const valid = validate({
    email: [required(email.value, 'email'), emailRule(email.value)],
    password: [required(password.value, 'password')],
  })
  if (!valid) {
    error.value = firstError()
    return
  }

  loading.value = true
  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      password: password.value,
    })
    if (response.ok) {
      const data = await response.json()
      auth.setToken(data.token)
      router.push('/profile')
    } else {
      const data = await response.json()
      error.value = api.translateError(data, 'error.invalidCredentials')
    }
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <BaseLayout>
    <form class="card" @submit.prevent="handleLogin">
      <h1>{{ t('app.name') }}</h1>
      <p class="subtitle">{{ t('auth.signIn') }}</p>
      <div class="form-field">
        <label for="login-email">{{ t('fields.email') }}</label>
        <input
          id="login-email"
          v-model="email"
          class="form-input"
          type="email"
          :placeholder="t('fields.emailPlaceholder')"
          autocomplete="email"
          required
          @blur="detectOrganizations"
        />
      </div>
      <div v-if="organizations.length" class="form-field">
        <label for="login-org">{{ t('fields.organization') }}</label>
        <input
          v-if="organizations.length === 1"
          id="login-org"
          :value="organizations[0].name"
          class="form-input org-detected"
          type="text"
          readonly
        />
        <select
          v-else
          id="login-org"
          v-model="organizationSlug"
          class="form-input"
          @change="auth.setOrganization(organizationSlug)"
        >
          <option v-for="org in organizations" :key="org.slug" :value="org.slug">{{ org.name }}</option>
        </select>
      </div>

      <div class="form-field">
        <label for="login-password">{{ t('fields.password') }}</label>
        <input
          id="login-password"
          v-model="password"
          class="form-input"
          type="password"
          :placeholder="t('fields.password')"
          autocomplete="current-password"
          required
        />
      </div>
      <p v-if="error" class="msg-error">{{ error }}</p>
      <button class="btn" type="submit" :disabled="loading">
        {{ loading ? t('common.loading') : t('auth.login') }}
      </button>
      <p class="form-link">{{ t('auth.noAccount') }} <router-link to="/register">{{ t('auth.signUp') }}</router-link></p>
    </form>
  </BaseLayout>
</template>

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
const { validate, required, email: emailRule, errors, invalids } = useValidation()

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
  if (!valid) return

  loading.value = true
  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      password: password.value,
      organizationSlug: organizationSlug.value || undefined,
    })
    if (response.ok) {
      const data = await response.json()
      auth.setToken(data.token)
      auth.setRole(data.role ?? null)
      auth.setCompanyId(data.companyId ?? null)
      const companyRoles = ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR']
      router.push(companyRoles.includes(data.role) ? '/units' : '/profile')
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
        <InputText
          id="login-email"
          v-model="email"
          type="email"
          :placeholder="t('fields.emailPlaceholder')"
          :invalid="!!invalids.email"
          autocomplete="email"
          fluid
          @blur="detectOrganizations"
        />
        <small v-if="errors.email" class="field-error">{{ errors.email }}</small>
      </div>

      <div v-if="organizations.length" class="form-field">
        <label for="login-org">{{ t('fields.organization') }}</label>
        <InputText
          v-if="organizations.length === 1"
          id="login-org"
          :value="organizations[0].name"
          readonly
          fluid
          class="org-detected"
        />
        <PSelect
          v-else
          id="login-org"
          v-model="organizationSlug"
          :options="organizations"
          option-label="name"
          option-value="slug"
          fluid
          @change="auth.setOrganization(organizationSlug)"
        />
      </div>

      <div class="form-field">
        <label for="login-password">{{ t('fields.password') }}</label>
        <PPassword
          id="login-password"
          v-model="password"
          :feedback="false"
          toggle-mask
          :placeholder="t('fields.password')"
          :invalid="!!invalids.password"
          autocomplete="current-password"
          fluid
        />
        <small v-if="errors.password" class="field-error">{{ errors.password }}</small>
      </div>

      <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>

      <PButton
        type="submit"
        :label="loading ? t('common.loading') : t('auth.login')"
        :loading="loading"
        fluid
        class="submit-btn"
      />

      <p class="form-link">
        {{ t('auth.noAccount') }} <router-link to="/register">{{ t('auth.signUp') }}</router-link>
      </p>
    </form>
  </BaseLayout>
</template>

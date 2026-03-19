import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'

export function useCompanyRegistration() {
  const { t } = useI18n()
  const router = useRouter()
  const auth = useAuthStore()
  const api = useApi()
  const { validate, required, email: emailRule, minLength, match, passwordStrength, firstError } = useValidation()

  const step = ref(1)
  const activityType = ref(null)
  const companyName = ref('')
  const email = ref('')
  const password = ref('')
  const confirmPassword = ref('')
  const error = ref('')
  const loading = ref(false)

  function goToStep2() {
    if (!activityType.value) {
      error.value = t('validation.activityTypeRequired')
      return
    }
    error.value = ''
    step.value = 2
  }

  async function submitRegistration() {
    error.value = ''
    const valid = validate({
      companyName: [required(companyName.value, t('fields.companyName'))],
      email: [required(email.value, 'email'), emailRule(email.value)],
      password: [required(password.value, 'password'), minLength(password.value, 8, 'password'), passwordStrength(password.value)],
      confirmPassword: [match(password.value, confirmPassword.value)],
    })
    if (!valid) {
      error.value = firstError()
      return
    }
    loading.value = true
    try {
      const response = await api.post('/auth/register/company', {
        email: email.value,
        password: password.value,
        companyName: companyName.value,
        activityType: activityType.value,
      })
      const isJson = response.headers.get('content-type')?.includes('application/json')
      let data = null
      if (isJson) {
        try { data = await response.json() } catch { /* parse failed, data stays null */ }
      }
      if (response.ok && data?.token) {
        auth.setToken(data.token)
        router.push('/profile')
      } else {
        error.value = data ? api.translateError(data, 'error.registerFailed') : t('error.registerFailed')
      }
    } catch {
      error.value = t('error.connection')
    } finally {
      loading.value = false
    }
  }

  return { step, activityType, companyName, email, password, confirmPassword, error, loading, goToStep2, submitRegistration }
}

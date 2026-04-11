import { ref, watch } from 'vue'
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
  const { validate, required, email: emailRule, minLength, passwordStrength, usernameFormat, errors, invalids } = useValidation()

  const step = ref(1)

  // Paso 1 — organización
  const orgName = ref('')
  const orgHandle = ref('')
  const handleChecking = ref(false)
  const handleAvailable = ref(null) // null = no verificado, true/false

  // Paso 2 — empresa
  const activityType = ref(null)
  const companyName = ref('')

  // Paso 3 — cuenta
  const email = ref('')
  const username = ref('')
  const fullName = ref('')
  const phone = ref('')
  const password = ref('')

  const usernameChecking = ref(false)
  const usernameAvailable = ref(null)

  const error = ref('')
  const loading = ref(false)

  function isUsernameFormat(val) {
    return /^[a-z0-9_-]{3,50}$/.test(val)
  }

  let usernameTimer = null
  watch(username, (val) => {
    usernameAvailable.value = null
    clearTimeout(usernameTimer)
    if (!isUsernameFormat(val)) return
    usernameTimer = setTimeout(() => checkUsername(val), 500)
  })

  async function checkUsername(val) {
    usernameChecking.value = true
    try {
      const res = await api.get(`/auth/check-username?username=${encodeURIComponent(val)}`)
      if (res.ok) {
        const data = await res.json()
        usernameAvailable.value = data.available
      }
    } catch { /* silencioso */ } finally {
      usernameChecking.value = false
    }
  }

  // Auto-sugerir handle desde orgName
  watch(orgName, (val) => {
    if (step.value === 1) {
      orgHandle.value = val
        .toLowerCase()
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '')
      handleAvailable.value = null
    }
  })

  let handleCheckTimer = null
  watch(orgHandle, (val) => {
    handleAvailable.value = null
    clearTimeout(handleCheckTimer)
    if (!val || !isHandleFormat(val)) return
    handleCheckTimer = setTimeout(() => checkHandle(val), 500)
  })

  function isHandleFormat(val) {
    return /^[a-z0-9]([a-z0-9-]*[a-z0-9])?$/.test(val)
  }

  async function checkHandle(handle) {
    handleChecking.value = true
    try {
      const res = await api.get(`/organizations/check-handle?handle=${encodeURIComponent(handle)}`)
      if (res.ok) {
        const data = await res.json()
        handleAvailable.value = data.available
      }
    } catch {
      // silencioso — la validación definitiva la hace el backend
    } finally {
      handleChecking.value = false
    }
  }

  function goToStep2() {
    error.value = ''
    if (!validate({ orgName: [required(orgName.value, 'orgName')] })) return
    if (!orgHandle.value || !isHandleFormat(orgHandle.value)) {
      error.value = t('validation.orgCodeInvalid')
      return
    }
    if (handleAvailable.value === false) {
      error.value = t('validation.orgCodeTaken')
      return
    }
    step.value = 2
  }

  function goToStep3() {
    error.value = ''
    if (!activityType.value) {
      error.value = t('validation.activityTypeRequired')
      return
    }
    if (!validate({ companyName: [required(companyName.value, 'companyName')] })) return
    step.value = 3
  }

  async function submitRegistration() {
    error.value = ''
    const parts = fullName.value.trim().split(/\s+/)
    const firstNameVal = parts[0] || ''
    const lastNameVal = parts.length > 1 ? parts.slice(1).join(' ') : null
    const valid = validate({
      email: [required(email.value, 'email'), emailRule(email.value)],
      username: [required(username.value, 'username'), minLength(username.value, 3, 'username'), usernameFormat(username.value)],
      fullName: [required(firstNameVal, 'fullName')],
      password: [required(password.value, 'password'), minLength(password.value, 8, 'password'), passwordStrength(password.value)],
    })
    if (!valid) return

    loading.value = true
    try {
      const res = await api.post('/auth/register/company', {
        email: email.value,
        username: username.value,
        firstName: firstNameVal,
        lastName: lastNameVal,
        phone: phone.value || null,
        password: password.value,
        orgName: orgName.value,
        orgHandle: orgHandle.value,
        companyName: companyName.value,
        activityType: activityType.value,
      })
      const isJson = res.headers.get('content-type')?.includes('application/json')
      const data = isJson ? await res.json().catch(() => null) : null
      if (res.ok && data?.token) {
        auth.applyLoginData(data)
        router.push('/onboarding')
      } else {
        error.value = data ? api.translateError(data, 'error.registerFailed') : t('error.registerFailed')
      }
    } catch {
      error.value = t('error.connection')
    } finally {
      loading.value = false
    }
  }

  return {
    step,
    orgName, orgHandle, handleChecking, handleAvailable, isHandleFormat,
    activityType, companyName,
    email, username, fullName, phone, usernameChecking, usernameAvailable, isUsernameFormat,
    password,
    error, errors, invalids, loading,
    goToStep2, goToStep3, submitRegistration,
  }
}

import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'

export function useOrderForm() {
  const { t } = useI18n()
  const router = useRouter()
  const api = useApi()
  const { validate, required, email: emailRule, errors, invalids } = useValidation()

  const units = ref([])
  const loadError = ref('')
  const originId = ref('')
  const destinationId = ref('')
  const recipientEmail = ref('')
  const recipientName = ref('')
  const priority = ref('NORMAL')
  const notes = ref('')
  const isExternal = ref(false)
  const loading = ref(false)
  const error = ref('')

  const destinationOptions = computed(() =>
    units.value.filter(u => u.id !== originId.value)
  )

  onMounted(async () => {
    try {
      const res = await api.get('/units')
      if (res.ok) {
        units.value = await res.json()
      } else {
        const data = await res.json().catch(() => null)
        loadError.value = api.translateError(data, 'error.connection')
      }
    } catch {
      loadError.value = t('error.connection')
    }
  })

  async function handleSubmit() {
    if (loading.value) return
    error.value = ''

    const rules = { originId: [required(originId.value, 'unitName')] }
    if (!isExternal.value) {
      rules.destinationId = [required(destinationId.value, 'unitName')]
    } else {
      rules.recipientEmail = [required(recipientEmail.value, 'email'), emailRule(recipientEmail.value)]
    }
    if (!validate(rules)) return

    if (!isExternal.value && originId.value === destinationId.value) {
      error.value = t('orders.sameUnit')
      return
    }

    loading.value = true
    try {
      const body = {
        originId: originId.value,
        priority: priority.value,
        notes: notes.value.trim() || null,
      }
      if (isExternal.value) {
        body.recipientEmail = recipientEmail.value.trim() || null
        body.recipientName = recipientName.value.trim() || null
      } else {
        body.destinationId = destinationId.value
      }

      const res = await api.post('/orders', body)
      if (res.ok) {
        const data = await res.json()
        router.push({ path: '/orders', query: { created: data.reference } })
      } else {
        const data = await res.json()
        error.value = api.translateError(data, 'error.saveFailed')
      }
    } catch {
      error.value = t('error.connection')
    } finally {
      loading.value = false
    }
  }

  return { units, loadError, originId, destinationId, recipientEmail, recipientName, priority, notes, isExternal, loading, error, errors, invalids, destinationOptions, handleSubmit }
}

import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

export function useOrderForm() {
  const { t } = useI18n()
  const api = useApi()

  const units = ref([])
  const loadError = ref('')
  const originId = ref('')
  const destinationId = ref('')
  const notes = ref('')
  const loading = ref(false)
  const error = ref('')
  const success = ref('')

  const destinationOptions = computed(() =>
    units.value.filter(u => u.id !== originId.value)
  )

  onMounted(async () => {
    try {
      const res = await api.get('/units')
      if (res.ok) units.value = await res.json()
      else loadError.value = t('error.connection')
    } catch {
      loadError.value = t('error.connection')
    }
  })

  async function handleSubmit() {
    if (loading.value) return
    error.value = ''
    success.value = ''

    if (originId.value === destinationId.value) {
      error.value = t('orders.sameUnit')
      return
    }

    loading.value = true
    try {
      const res = await api.post('/orders', {
        originId: originId.value,
        destinationId: destinationId.value,
        notes: notes.value.trim() || null,
      })
      if (res.ok) {
        const data = await res.json()
        success.value = t('orders.created', { reference: data.reference })
        originId.value = ''
        destinationId.value = ''
        notes.value = ''
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

  return { units, loadError, originId, destinationId, notes, loading, error, success, destinationOptions, handleSubmit }
}

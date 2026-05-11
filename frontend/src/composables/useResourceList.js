import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

export function useResourceList(endpoint) {
  const { t } = useI18n()
  const api = useApi()
  const items = ref([])
  const loading = ref(false)
  const error = ref('')

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const res = await api.get(endpoint)
      if (res.ok) items.value = await res.json()
      else error.value = t('error.connection')
    } catch {
      error.value = t('error.connection')
    } finally {
      loading.value = false
    }
  }

  onMounted(load)
  return { items, loading, error, load }
}

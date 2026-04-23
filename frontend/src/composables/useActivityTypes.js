import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

const cache = ref(null)

export function useActivityTypes() {
  const { locale } = useI18n()
  const api = useApi()
  const loading = ref(false)

  async function load() {
    if (cache.value) return
    loading.value = true
    try {
      const res = await api.get('/activity-types')
      if (res.ok) cache.value = await res.json()
    } catch {
      // silencioso
    } finally {
      loading.value = false
    }
  }

  // computed: solo se recalcula cuando cambian cache o locale.
  const activityTypes = computed(() => (cache.value ?? []).map(t => ({
    value: t.code,
    label: locale.value === 'es' ? t.labelEs : t.labelEn,
    labelEs: t.labelEs,
    labelEn: t.labelEn,
  })))

  return { activityTypes, loading, load }
}

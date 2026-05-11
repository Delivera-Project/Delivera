// Pequeño composable para leer la configuración heredada de la empresa
// (DSI-23.1/2/3). Aislado del componente para poder testear el flujo y los
// fallbacks sin necesidad de montar la vista entera.

import { ref } from 'vue'
import { useApi } from '@/composables/useApi'

export function useCompanySettings() {
  const api = useApi()
  const settings = ref(null)

  async function load() {
    try {
      const res = await api.get('/settings')
      if (res.ok) settings.value = await res.json()
    } catch (e) {
      console.error('useCompanySettings: failed to load settings', e)
      settings.value = null
    }
    return settings.value
  }

  return { settings, load }
}

// Devuelve true si una unidad puede sobreescribir la prioridad por defecto
// según la configuración de empresa cargada. Falla seguro a true si no hay datos.
export function canUnitOverridePriority(companySettings) {
  if (!companySettings) return true
  return !companySettings.defaultPriorityLocked
}

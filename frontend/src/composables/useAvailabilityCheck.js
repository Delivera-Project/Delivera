import { ref, watch, onUnmounted, getCurrentInstance } from 'vue'
import { useApi } from './useApi'

// Observa un ref de texto y, tras un debounce, llama al endpoint dado para
// comprobar disponibilidad (ej. handle, username). Devuelve:
//   - checking: ref<boolean> mientras la petición está en vuelo
//   - available: ref<boolean|null> — null = no evaluado aún
export function useAvailabilityCheck(valueRef, { endpoint, paramName, validate, delay = 500 }) {
  const api = useApi()
  const checking = ref(false)
  const available = ref(null)
  let timer = null
  let alive = true

  const stop = watch(valueRef, (val) => {
    available.value = null
    clearTimeout(timer)
    if (!validate(val)) return
    timer = setTimeout(async () => {
      if (!alive) return
      checking.value = true
      try {
        const res = await api.get(`${endpoint}?${paramName}=${encodeURIComponent(val)}`)
        if (!alive) return
        if (res.ok) {
          const data = await res.json()
          if (alive) available.value = data.available
        }
      } catch { /* silencioso */ } finally {
        if (alive) checking.value = false
      }
    }, delay)
  })

  if (getCurrentInstance()) {
    onUnmounted(() => {
      alive = false
      clearTimeout(timer)
      stop()
    })
  }

  return { checking, available }
}

import { ref, computed } from 'vue'

// Singleton — caché a nivel de módulo
const loaded = ref(false)
const loading = ref(false)
const _statuses = ref([])
const _priorities = ref([])
const _roles = ref({})
const _fileMaxUploadBytes = ref(2 * 1024 * 1024)

async function loadConfig() {
  if (loaded.value || loading.value) return
  loading.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v2/app-config`)
    if (res.ok) {
      const data = await res.json()
      _statuses.value = data.orderStatuses ?? []
      _priorities.value = data.orderPriorities ?? []
      const caps = {}
      for (const rc of (data.roleCapabilities ?? [])) {
        caps[rc.role] = rc
      }
      _roles.value = caps
      if (typeof data.fileMaxUploadBytes === 'number') _fileMaxUploadBytes.value = data.fileMaxUploadBytes
      loaded.value = true
    }
  } catch {
    // silencioso — fallback a mapas vacíos
  } finally {
    loading.value = false
  }
}

export function useAppConfig() {
  const statusSeverity = computed(() => {
    const map = {}
    for (const s of _statuses.value) map[s.status] = s.uiSeverity
    return map
  })

  const prioritySeverity = computed(() => {
    const map = {}
    for (const p of _priorities.value) map[p.priority] = p.uiSeverity
    return map
  })

  function getNextStatuses(status) {
    return _statuses.value.find(s => s.status === status)?.allowedTransitions ?? []
  }

  function getRoleCapabilities(role) {
    return _roles.value[role] ?? {}
  }

  return {
    load: loadConfig,
    statusSeverity,
    prioritySeverity,
    getNextStatuses,
    getRoleCapabilities,
    fileMaxUploadBytes: _fileMaxUploadBytes,
  }
}

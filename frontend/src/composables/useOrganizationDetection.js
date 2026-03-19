import { ref } from 'vue'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

export function useOrganizationDetection(email) {
  const api = useApi()
  const auth = useAuthStore()

  const organizations = ref([])
  const organizationSlug = ref(null)
  const detectingOrg = ref(false)

  function resetOrgs() {
    organizations.value = []
    organizationSlug.value = null
    auth.setOrganization(null)
  }

  let requestId = 0

  async function detectOrganizations() {
    const val = email.value.trim()
    if (!val || val.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) {
      resetOrgs()
      return
    }
    const currentId = ++requestId
    detectingOrg.value = true
    try {
      const response = await api.get(`/auth/organizations?email=${encodeURIComponent(val)}`)
      if (currentId !== requestId) return
      if (response.ok) {
        organizations.value = await response.json()
        organizationSlug.value = organizations.value.length === 1 ? organizations.value[0].slug : null
        auth.setOrganization(organizationSlug.value)
      } else {
        resetOrgs()
      }
    } catch {
      if (currentId === requestId) resetOrgs()
    } finally {
      if (currentId === requestId) detectingOrg.value = false
    }
  }

  return { organizations, organizationSlug, detectingOrg, detectOrganizations }
}

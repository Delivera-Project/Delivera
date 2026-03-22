import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const organizationHandle = ref(localStorage.getItem('organizationHandle') || null)
  const orgName = ref(localStorage.getItem('orgName') || null)
  const companyName = ref(localStorage.getItem('companyName') || null)
  const role = ref(localStorage.getItem('role') || null)
  const companyId = ref(localStorage.getItem('companyId') || null)

  // Empresas del usuario en la misma org (en memoria, se recarga cuando es necesario)
  const companies = ref([])

  const isAuthenticated = computed(() => !!token.value)
  const isCompanyAdmin = computed(() => role.value === 'COMPANY_ADMIN')
  const isWorker = computed(() => ['COMPANY_ADMIN', 'ANALYST', 'OPERATOR'].includes(role.value))
  const canCreateOrders = computed(() => ['COMPANY_ADMIN', 'ANALYST'].includes(role.value))

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setOrganization(handle) {
    const v = handle || null
    organizationHandle.value = v
    if (v) localStorage.setItem('organizationHandle', v)
    else localStorage.removeItem('organizationHandle')
  }

  function setOrgName(name) {
    const v = name || null
    orgName.value = v
    if (v) localStorage.setItem('orgName', v)
    else localStorage.removeItem('orgName')
  }

  function setCompanyName(name) {
    const v = name || null
    companyName.value = v
    if (v) localStorage.setItem('companyName', v)
    else localStorage.removeItem('companyName')
  }

  function setRole(newRole) {
    const v = newRole || null
    role.value = v
    if (v) localStorage.setItem('role', v)
    else localStorage.removeItem('role')
  }

  function setCompanyId(id) {
    const v = id || null
    companyId.value = v
    if (v) localStorage.setItem('companyId', v)
    else localStorage.removeItem('companyId')
  }

  function applyLoginData(data) {
    setToken(data.token)
    setRole(data.role ?? null)
    setCompanyId(data.companyId ?? null)
    setCompanyName(data.companyName ?? null)
    setOrganization(data.orgHandle ?? null)
    setOrgName(data.orgName ?? null)
  }

  function logout() {
    token.value = ''
    user.value = null
    companies.value = []
    organizationHandle.value = null
    orgName.value = null
    companyName.value = null
    role.value = null
    companyId.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('organizationHandle')
    localStorage.removeItem('orgName')
    localStorage.removeItem('companyName')
    localStorage.removeItem('role')
    localStorage.removeItem('companyId')
  }

  function setUser(profile) {
    user.value = profile
  }

  async function loadCompanies() {
    if (!isCompanyAdmin.value || !token.value) return
    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/api/v1/settings/companies`, {
        headers: { Authorization: `Bearer ${token.value}` },
      })
      if (res.ok) companies.value = await res.json()
    // eslint-disable-next-line no-empty
    } catch {}
  }

  return {
    token, user, organizationHandle, orgName, companyName, role, companyId,
    companies,
    isAuthenticated, isCompanyAdmin, isWorker, canCreateOrders,
    setToken, setUser, setOrganization, setOrgName, setCompanyName,
    setRole, setCompanyId, applyLoginData,
    logout, loadCompanies,
  }
})

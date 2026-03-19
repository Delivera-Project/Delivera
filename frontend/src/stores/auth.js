import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// TODO: Se mejorará esto a un sistema basado en cookies
export const useAuthStore = defineStore('auth', () => {
  // Se almacena el token en la sesión al hacer login/register
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const organizationSlug = ref(localStorage.getItem('organizationSlug') || null)
  const role = ref(localStorage.getItem('role') || null)
  const companyId = ref(localStorage.getItem('companyId') || null)

  // Devuelve el estado del token
  const isAuthenticated = computed(() => !!token.value)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setOrganization(slug) {
    const normalized = (slug && typeof slug === 'string') ? slug : null
    organizationSlug.value = normalized
    if (normalized) localStorage.setItem('organizationSlug', normalized)
    else localStorage.removeItem('organizationSlug')
  }

  function setRole(newRole) {
    const normalized = (newRole && typeof newRole === 'string') ? newRole : null
    role.value = normalized
    if (normalized) localStorage.setItem('role', normalized)
    else localStorage.removeItem('role')
  }

  function setCompanyId(id) {
    const normalized = (id && typeof id === 'string') ? id : null
    companyId.value = normalized
    if (normalized) localStorage.setItem('companyId', normalized)
    else localStorage.removeItem('companyId')
  }

  function logout() {
    token.value = ''
    user.value = null
    organizationSlug.value = null
    role.value = null
    companyId.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('organizationSlug')
    localStorage.removeItem('role')
    localStorage.removeItem('companyId')
  }

  function setUser(profile) {
    user.value = profile
  }

  return { token, user, organizationSlug, role, companyId, isAuthenticated, setToken, setUser, setOrganization, setRole, setCompanyId, logout }
})

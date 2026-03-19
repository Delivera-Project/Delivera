import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// TODO: Se mejorará esto a un sistema basado en cookies
export const useAuthStore = defineStore('auth', () => {
  // Se almacena el token en la sesión al hacer login/register
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const organizationSlug = ref(localStorage.getItem('organizationSlug') || null)

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

  function logout() {
    token.value = ''
    user.value = null
    organizationSlug.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('organizationSlug')
  }

  function setUser(profile) {
    user.value = profile
  }

  return { token, user, organizationSlug, isAuthenticated, setToken, setUser, setOrganization, logout }
})

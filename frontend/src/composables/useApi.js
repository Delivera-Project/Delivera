import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

export function useApi() {
  const auth = useAuthStore()
  const router = useRouter()
  const { t, te } = useI18n()

  // Función que devuelve el código de error traducido con i18n
  function translateError(data, fallbackKey) {
    if (data?.code && te(`error.${data.code}`)) {
      return t(`error.${data.code}`)
    }
    if (data?.message && te(`error.${data.message}`)) {
      return t(`error.${data.message}`)
    }
    return data?.message || t(fallbackKey)
  }


  // Realiza la request a la API junto al header y el token JWT
  async function request(endpoint, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...options.headers }

    if (auth.token) {
      headers.Authorization = `Bearer ${auth.token}`
    }

    const response = await fetch(`${import.meta.env.VITE_API_URL}/api/v1${endpoint}`, {
      ...options,
      headers,
    })

    if (response.status === 401 && !endpoint.startsWith('/auth/')) {
      auth.logout()
      router.push('/')
      throw new Error('No autorizado')
    }

    return response
  }

  // Peticiones GET
  async function get(endpoint) {
    return request(endpoint)
  }

  // Peticiones POST
  async function post(endpoint, body) {
    return request(endpoint, { method: 'POST', body: JSON.stringify(body) })
  }

  // Peticiones PUT
  async function put(endpoint, body) {
    return request(endpoint, { method: 'PUT', body: JSON.stringify(body) })
  }

  // TODO: Se pueden ir añadiendo más peticiones

  return { get, post, put, translateError }
}

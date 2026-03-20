import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

export function useUnitForm() {
  const { t } = useI18n()
  const api = useApi()

  const name = ref('')
  const unitType = ref(null)
  const address = ref('')
  const latitude = ref('')
  const longitude = ref('')
  const error = ref('')
  const success = ref('')
  const loading = ref(false)

  function parseCoord(val) {
    const trimmed = String(val).trim()
    if (!trimmed) return null
    const n = parseFloat(trimmed)
    return Number.isFinite(n) ? n : NaN
  }

  function validateUnit() {
    if (!name.value.trim()) {
      error.value = t('validation.required', { field: t('fields.unitName') })
      return false
    }
    if (!unitType.value) {
      error.value = t('validation.required', { field: t('fields.type') })
      return false
    }
    const lat = parseCoord(latitude.value)
    const lon = parseCoord(longitude.value)
    const hasLat = latitude.value.trim() !== ''
    const hasLon = longitude.value.trim() !== ''
    if (hasLat !== hasLon) {
      error.value = t('validation.coordinatesIncomplete')
      return false
    }
    if (hasLat && hasLon) {
      if (!Number.isFinite(lat) || !Number.isFinite(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
        error.value = t('validation.coordinatesInvalid')
        return false
      }
    }
    return true
  }

  async function submitUnit({ isEdit, unitId }) {
    if (loading.value) return
    error.value = ''
    success.value = ''
    if (!validateUnit()) return
    if (isEdit && !unitId) {
      error.value = t('error.saveFailed')
      return
    }

    loading.value = true
    try {
      const lat = parseCoord(latitude.value)
      const lon = parseCoord(longitude.value)
      const body = {
        name: name.value.trim(),
        type: unitType.value,
        address: address.value?.trim() || null,
        latitude: Number.isFinite(lat) ? lat : null,
        longitude: Number.isFinite(lon) ? lon : null,
      }
      const res = isEdit
        ? await api.put(`/units/${unitId}`, body)
        : await api.post('/units', body)

      if (res.ok) {
        success.value = t(isEdit ? 'units.updated' : 'units.created')
        if (!isEdit) {
          name.value = ''
          unitType.value = null
          address.value = ''
          latitude.value = ''
          longitude.value = ''
        }
      } else {
        const data = await res.json()
        error.value = api.translateError(data, 'error.saveFailed')
      }
    } catch {
      error.value = t('error.connection')
    } finally {
      loading.value = false
    }
  }

  return { name, unitType, address, latitude, longitude, error, success, loading, validateUnit, submitUnit }
}

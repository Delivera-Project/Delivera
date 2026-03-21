import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import { useValidation } from '@/composables/useValidation'

export function useUnitForm() {
  const { t } = useI18n()
  const router = useRouter()
  const api = useApi()
  const { validate, required, errors, invalids } = useValidation()

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

  async function submitUnit({ isEdit, unitId }) {
    if (loading.value) return
    error.value = ''
    success.value = ''

    const fieldValid = validate({
      name: [required(name.value, 'unitName')],
      unitType: [() => !unitType.value ? { message: '', type: 'required' } : null],
    })

    const lat = parseCoord(latitude.value)
    const lon = parseCoord(longitude.value)
    const hasLat = latitude.value.toString().trim() !== ''
    const hasLon = longitude.value.toString().trim() !== ''
    let coordValid = true
    if (hasLat !== hasLon) {
      error.value = t('validation.coordinatesIncomplete')
      coordValid = false
    } else if (hasLat && hasLon && (!Number.isFinite(lat) || !Number.isFinite(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180)) {
      error.value = t('validation.coordinatesInvalid')
      coordValid = false
    }

    if (!fieldValid || !coordValid) return

    if (isEdit && !unitId) {
      error.value = t('error.saveFailed')
      return
    }

    loading.value = true
    try {
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
        if (isEdit) {
          success.value = t('units.updated')
        } else {
          router.push('/units')
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

  return { name, unitType, address, latitude, longitude, error, success, loading, errors, invalids, submitUnit }
}

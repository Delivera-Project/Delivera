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

  function validateUnit() {
    if (!unitType.value) {
      error.value = t('validation.required', { field: t('fields.type') })
      return false
    }
    return true
  }

  async function submitUnit({ isEdit, unitId }) {
    error.value = ''
    success.value = ''
    if (!validateUnit()) return

    loading.value = true
    try {
      const body = {
        name: name.value,
        type: unitType.value,
        address: address.value || null,
        latitude: latitude.value !== '' ? Number(latitude.value) : null,
        longitude: longitude.value !== '' ? Number(longitude.value) : null,
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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const api = useApi()

const unitId = route.params.id || null
const isEdit = computed(() => !!unitId)

const name = ref('')
const unitType = ref(null)
const address = ref('')
const latitude = ref('')
const longitude = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

const UNIT_TYPES = ['WAREHOUSE', 'STORE', 'FACTORY', 'LOGISTICS_CENTER']

onMounted(async () => {
  if (!isEdit.value) return
  const res = await api.get(`/units`)
  if (res.ok) {
    const units = await res.json()
    const unit = units.find(u => u.id === unitId)
    if (unit) {
      name.value = unit.name
      unitType.value = unit.type
      address.value = unit.address || ''
      latitude.value = unit.latitude ?? ''
      longitude.value = unit.longitude ?? ''
    }
  }
})

async function handleSubmit() {
  error.value = ''
  success.value = ''
  if (!unitType.value) {
    error.value = t('validation.required', { field: t('fields.type') })
    return
  }

  loading.value = true
  try {
    const body = {
      name: name.value,
      type: unitType.value,
      address: address.value || null,
      latitude: latitude.value !== '' ? Number(latitude.value) : null,
      longitude: longitude.value !== '' ? Number(longitude.value) : null,
    }
    const res = isEdit.value
      ? await api.put(`/units/${unitId}`, body)
      : await api.post('/units', body)

    if (res.ok) {
      success.value = t(isEdit.value ? 'units.updated' : 'units.created')
      if (!isEdit.value) {
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
</script>

<template>
  <BaseLayout>
    <form class="card card-wide" @submit.prevent="handleSubmit">
      <button type="button" class="back-btn" @click="router.push('/units')">
        ← {{ t('common.back') }}
      </button>

      <h1>{{ t(isEdit ? 'units.edit' : 'units.new') }}</h1>

      <div class="unit-type-grid">
        <button
          v-for="type in UNIT_TYPES"
          :key="type"
          type="button"
          class="unit-type-option"
          :class="{ selected: unitType === type }"
          @click="unitType = type"
        >
          <span class="option-title">{{ t(`units.${type}`) }}</span>
        </button>
      </div>

      <div class="form-field">
        <label for="unit-name">{{ t('fields.unitName') }}</label>
        <input
          id="unit-name"
          v-model="name"
          class="form-input"
          type="text"
          :placeholder="t('fields.unitNamePlaceholder')"
          required
        />
      </div>

      <div class="form-field">
        <label for="unit-address">{{ t('fields.address') }}</label>
        <input
          id="unit-address"
          v-model="address"
          class="form-input"
          type="text"
          :placeholder="t('fields.addressPlaceholder')"
        />
      </div>

      <div class="actions">
        <div class="form-field" style="flex:1">
          <label for="unit-lat">{{ t('fields.latitude') }}</label>
          <input
            id="unit-lat"
            v-model="latitude"
            class="form-input"
            type="number"
            step="any"
            :placeholder="t('fields.latitudePlaceholder')"
          />
        </div>
        <div class="form-field" style="flex:1">
          <label for="unit-lon">{{ t('fields.longitude') }}</label>
          <input
            id="unit-lon"
            v-model="longitude"
            class="form-input"
            type="number"
            step="any"
            :placeholder="t('fields.longitudePlaceholder')"
          />
        </div>
      </div>

      <p v-if="error" class="msg-error">{{ error }}</p>
      <p v-if="success" class="msg-success">{{ success }}</p>

      <button class="btn" type="submit" :disabled="loading">
        {{ loading ? t('common.loading') : t('common.save') }}
      </button>
    </form>
  </BaseLayout>
</template>

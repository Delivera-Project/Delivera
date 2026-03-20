<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useUnitForm } from '@/composables/useUnitForm'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const api = useApi()

const unitId = computed(() => route.params.id || null)
const isEdit = computed(() => !!unitId.value)

const { name, unitType, address, latitude, longitude, error, success, loading, submitUnit } = useUnitForm()
const loadError = ref('')

const UNIT_TYPES = ['WAREHOUSE', 'STORE', 'FACTORY', 'LOGISTICS_CENTER']

async function loadUnit(id) {
  if (!id) return
  loadError.value = ''
  try {
    const res = await api.get('/units')
    if (!res.ok) {
      loadError.value = t('error.connection')
      return
    }
    const units = await res.json()
    const unit = units.find(u => u.id === id)
    if (unit) {
      name.value = unit.name
      unitType.value = unit.type
      address.value = unit.address || ''
      latitude.value = unit.latitude ?? ''
      longitude.value = unit.longitude ?? ''
    }
  } catch {
    loadError.value = t('error.connection')
  }
}

watch(unitId, (newId) => loadUnit(newId))
onMounted(() => loadUnit(unitId.value))

function handleSubmit() {
  submitUnit({ isEdit: isEdit.value, unitId: unitId.value })
}
</script>

<template>
  <BaseLayout>
    <form class="card card-wide" @submit.prevent="handleSubmit">
      <button type="button" class="back-btn" @click="router.push('/units')">
        ← {{ t('common.back') }}
      </button>

      <h1>{{ t(isEdit ? 'units.edit' : 'units.new') }}</h1>

      <p v-if="loadError" class="msg-error">{{ loadError }}</p>

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

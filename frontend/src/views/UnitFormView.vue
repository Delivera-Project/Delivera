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
const unitTypeOptions = computed(() =>
  UNIT_TYPES.map(type => ({ label: t(`units.${type}`), value: type }))
)

async function loadUnit(id) {
  if (!id) return
  loadError.value = ''
  try {
    const res = await api.get('/units')
    if (!res.ok) { loadError.value = t('error.connection'); return }
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
      <PButton
        type="button"
        :label="t('common.back')"
        text
        severity="secondary"
        icon="pi pi-arrow-left"
        class="back-btn"
        @click="router.push('/units')"
      />

      <h1>{{ t(isEdit ? 'units.edit' : 'units.new') }}</h1>

      <PMessage v-if="loadError" severity="error" :closable="false" class="form-message">{{ loadError }}</PMessage>

      <div class="form-field">
        <label>{{ t('fields.type') }}</label>
        <SelectButton
          v-model="unitType"
          :options="unitTypeOptions"
          option-label="label"
          option-value="value"
          class="unit-type-selector"
        />
      </div>

      <div class="form-field">
        <label for="unit-name">{{ t('fields.unitName') }}</label>
        <InputText
          id="unit-name"
          v-model="name"
          :placeholder="t('fields.unitNamePlaceholder')"
          fluid
        />
      </div>

      <div class="form-field">
        <label for="unit-address">{{ t('fields.address') }}</label>
        <InputText
          id="unit-address"
          v-model="address"
          :placeholder="t('fields.addressPlaceholder')"
          fluid
        />
      </div>

      <div class="actions">
        <div class="form-field" style="flex:1">
          <label for="unit-lat">{{ t('fields.latitude') }}</label>
          <InputText
            id="unit-lat"
            v-model="latitude"
            type="number"
            step="any"
            :placeholder="t('fields.latitudePlaceholder')"
            fluid
          />
        </div>
        <div class="form-field" style="flex:1">
          <label for="unit-lon">{{ t('fields.longitude') }}</label>
          <InputText
            id="unit-lon"
            v-model="longitude"
            type="number"
            step="any"
            :placeholder="t('fields.longitudePlaceholder')"
            fluid
          />
        </div>
      </div>

      <PMessage v-if="error" severity="error" :closable="false" class="form-message">{{ error }}</PMessage>
      <PMessage v-if="success" severity="success" :closable="false" class="form-message">{{ success }}</PMessage>

      <PButton
        type="submit"
        :label="loading ? t('common.loading') : t('common.save')"
        :loading="loading"
        fluid
        class="submit-btn"
      />
    </form>
  </BaseLayout>
</template>

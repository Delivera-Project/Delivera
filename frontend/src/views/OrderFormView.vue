<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const api = useApi()

const units = ref([])
const originId = ref('')
const destinationId = ref('')
const notes = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

const destinationOptions = computed(() =>
  units.value.filter(u => u.id !== originId.value)
)

onMounted(async () => {
  try {
    const res = await api.get('/units')
    if (res.ok) units.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  }
})

async function handleSubmit() {
  if (loading.value) return
  error.value = ''
  success.value = ''

  if (originId.value === destinationId.value) {
    error.value = t('orders.sameUnit')
    return
  }

  loading.value = true
  try {
    const res = await api.post('/orders', {
      originId: originId.value,
      destinationId: destinationId.value,
      notes: notes.value.trim() || null,
    })
    if (res.ok) {
      const data = await res.json()
      success.value = t('orders.created', { reference: data.reference })
      originId.value = ''
      destinationId.value = ''
      notes.value = ''
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

      <h1>{{ t('orders.title') }}</h1>
      <p class="subtitle">{{ t('orders.subtitle') }}</p>

      <p v-if="units.length < 2 && !error" class="msg-error">{{ t('orders.noUnits') }}</p>

      <template v-else>
        <div class="form-field">
          <label for="order-origin">{{ t('orders.origin') }}</label>
          <select
            id="order-origin"
            v-model="originId"
            class="form-input"
            required
          >
            <option value="" disabled>{{ t('orders.originPlaceholder') }}</option>
            <option v-for="unit in units" :key="unit.id" :value="unit.id">
              {{ unit.name }}
            </option>
          </select>
        </div>

        <div class="form-field">
          <label for="order-destination">{{ t('orders.destination') }}</label>
          <select
            id="order-destination"
            v-model="destinationId"
            class="form-input"
            required
          >
            <option value="" disabled>{{ t('orders.destinationPlaceholder') }}</option>
            <option v-for="unit in destinationOptions" :key="unit.id" :value="unit.id">
              {{ unit.name }}
            </option>
          </select>
        </div>

        <div class="form-field">
          <label for="order-notes">{{ t('orders.notes') }}</label>
          <textarea
            id="order-notes"
            v-model="notes"
            class="form-input"
            :placeholder="t('orders.notesPlaceholder')"
            rows="3"
          />
        </div>

        <p v-if="error" class="msg-error">{{ error }}</p>
        <p v-if="success" class="msg-success">{{ success }}</p>

        <button class="btn" type="submit" :disabled="loading">
          {{ loading ? t('common.loading') : t('common.save') }}
        </button>
      </template>
    </form>
  </BaseLayout>
</template>

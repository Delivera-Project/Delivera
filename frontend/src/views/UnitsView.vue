<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const api = useApi()
const auth = useAuthStore()

const units = ref([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get('/units')
    if (res.ok) units.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <BaseLayout>
    <div class="card card-wide">
      <div class="units-header">
        <h1>{{ t('units.title') }}</h1>
        <div class="actions">
          <button
            v-if="auth.canCreateOrders"
            class="btn btn-outline btn-header"
            @click="router.push('/orders/new')"
          >
            {{ t('orders.new') }}
          </button>
          <button
            v-if="auth.isCompanyAdmin"
            class="btn btn-header"
            @click="router.push('/units/new')"
          >
            {{ t('units.new') }}
          </button>
        </div>
      </div>

      <p v-if="loading" class="subtitle">{{ t('common.loading') }}</p>

      <p v-else-if="error" class="msg-error">{{ error }}</p>

      <p v-else-if="!units.length" class="subtitle">{{ t('units.empty') }}</p>

      <ul v-else class="unit-list">
        <li v-for="unit in units" :key="unit.id" class="unit-item">
          <span class="unit-type-badge">{{ t(`units.${unit.type}`) }}</span>
          <span class="unit-name">{{ unit.name }}</span>
          <span v-if="unit.address" class="unit-address">{{ unit.address }}</span>
          <button
            v-if="auth.isCompanyAdmin"
            class="btn btn-outline btn-sm"
            @click="router.push(`/units/${unit.id}/edit`)"
          >
            {{ t('profile.edit') }}
          </button>
        </li>
      </ul>
    </div>
  </BaseLayout>
</template>

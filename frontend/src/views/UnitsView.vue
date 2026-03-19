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

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get('/units')
    if (res.ok) units.value = await res.json()
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
        <button
          v-if="auth.role === 'COMPANY_ADMIN'"
          class="btn"
          style="width:auto;padding:8px 16px"
          @click="router.push('/units/new')"
        >
          {{ t('units.new') }}
        </button>
      </div>

      <p v-if="loading" class="subtitle">{{ t('common.loading') }}</p>

      <p v-else-if="!units.length" class="subtitle">{{ t('units.empty') }}</p>

      <ul v-else class="unit-list">
        <li v-for="unit in units" :key="unit.id" class="unit-item">
          <span class="unit-type-badge">{{ t(`units.${unit.type}`) }}</span>
          <span class="unit-name">{{ unit.name }}</span>
          <span v-if="unit.address" class="unit-address">{{ unit.address }}</span>
          <button
            v-if="auth.role === 'COMPANY_ADMIN'"
            class="btn btn-outline"
            style="width:auto;padding:6px 12px;font-size:13px"
            @click="router.push(`/units/${unit.id}/edit`)"
          >
            {{ t('profile.edit') }}
          </button>
        </li>
      </ul>
    </div>
  </BaseLayout>
</template>

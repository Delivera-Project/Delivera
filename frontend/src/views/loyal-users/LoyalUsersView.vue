<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const router = useRouter()
const api = useApi()

const loyalUsers = ref([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  try {
    const res = await api.get('/loyal-users')
    if (res.ok) loyalUsers.value = await res.json()
    else error.value = t('error.connection')
  } catch {
    error.value = t('error.connection')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="card card-wide">
    <div class="list-header">
      <h1>{{ t('loyalUsers.title') }}</h1>
    </div>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <DataTable :value="loyalUsers" :loading="loading" striped-rows row-hover @row-click="e => router.push(`/loyal-users/${e.data.id}`)">
      <template #empty>
        <EmptyState icon="pi-users" :message="t('loyalUsers.empty')" />
      </template>
      <Column field="email" :header="t('loyalUsers.email')" />
      <Column :header="t('loyalUsers.orders')" style="width:130px">
        <template #body="{ data }">
          <PTag :value="String(data.orderCount)" severity="info" />
        </template>
      </Column>
      <Column :header="t('loyalUsers.registered')" style="width:140px">
        <template #body="{ data }">
          <PTag
            :value="data.registered ? t('loyalUsers.registered') : t('loyalUsers.notRegistered')"
            :severity="data.registered ? 'success' : 'secondary'"
          />
        </template>
      </Column>
      <Column :header="t('loyalUsers.since')" style="width:130px">
        <template #body="{ data }">
          {{ formatDate(data.createdAt) }}
        </template>
      </Column>
    </DataTable>
  </div>
</template>


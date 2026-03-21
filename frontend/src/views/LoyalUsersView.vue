<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'

const { t } = useI18n()
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
    <div class="header-row">
      <h1>{{ t('loyalUsers.title') }}</h1>
    </div>

    <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

    <DataTable :value="loyalUsers" :loading="loading" striped-rows row-hover @row-click="e => router.push(`/loyal-users/${e.data.id}`)">
      <template #empty>
        <div class="empty-state">
          <i class="pi pi-users empty-icon" />
          <p>{{ t('loyalUsers.empty') }}</p>
        </div>
      </template>
      <Column field="email" :header="t('loyalUsers.email')" />
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
          {{ new Date(data.createdAt).toLocaleDateString() }}
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<style scoped>
.header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.header-row h1 { margin: 0; }
.add-form { background: #f8fafc; border-radius: 10px; padding: 16px; margin-bottom: 16px; display: flex; flex-direction: column; gap: 10px; }
.add-actions { display: flex; gap: 8px; }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 48px 24px; color: #94a3b8; }
.empty-icon { font-size: 40px; opacity: 0.4; }
.empty-state p { margin: 0; font-size: 14px; }
</style>

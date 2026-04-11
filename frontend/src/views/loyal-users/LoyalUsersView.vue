<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useFormatDate } from '@/composables/useFormatDate'
import { useResourceList } from '@/composables/useResourceList'
import { useApi } from '@/composables/useApi'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const router = useRouter()
const api = useApi()
const { items: loyalUsers, loading, error } = useResourceList('/loyal-users')

const showAdd = ref(false)
const addEmail = ref('')
const adding = ref(false)
const addError = ref('')
const addSuccess = ref(false)

async function addLoyalUser() {
  if (!addEmail.value.trim()) return
  adding.value = true
  addError.value = ''
  addSuccess.value = false
  try {
    const res = await api.post('/loyal-users', { email: addEmail.value.trim() })
    if (res.ok) {
      const created = await res.json()
      loyalUsers.value.unshift(created)
      addEmail.value = ''
      showAdd.value = false
      addSuccess.value = true
      setTimeout(() => { addSuccess.value = false }, 3000)
    } else {
      const data = await res.json()
      addError.value = api.translateError(data, 'error.saveFailed')
    }
  } catch {
    addError.value = t('error.connection')
  } finally {
    adding.value = false
  }
}
</script>

<template>
  <div class="card card-wide">
    <div class="list-header">
      <h1>{{ t('loyalUsers.title') }}</h1>
      <PButton :label="t('loyalUsers.new')" icon="pi pi-plus" @click="showAdd = !showAdd; addError = ''" />
    </div>

    <PMessage v-if="addSuccess" severity="success" :closable="false" class="form-message">{{ t('loyalUsers.added') }}</PMessage>

    <div v-if="showAdd" class="add-form">
      <div class="form-row">
        <InputText v-model="addEmail" type="email" :placeholder="t('fields.emailPersonalPlaceholder')" fluid />
        <PButton :label="adding ? t('common.loading') : t('loyalUsers.new')" :loading="adding" @click="addLoyalUser" />
        <PButton :label="t('common.cancel')" severity="secondary" text @click="showAdd = false" />
      </div>
      <PMessage v-if="addError" severity="error" :closable="false" class="form-message">{{ addError }}</PMessage>
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

<style scoped>
.list-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.list-header h1 { margin: 0; }
.add-form { border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; margin-bottom: 16px; display: flex; flex-direction: column; gap: 10px; }
.form-row { display: flex; gap: 10px; align-items: center; }
.form-row .p-inputtext { flex: 1; }
</style>


<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useApi } from '@/composables/useApi'
import { useAuthStore } from '@/stores/auth'

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
  <div class="card card-wide">
      <div class="units-header">
        <h1>{{ t('units.title') }}</h1>
        <PButton
          v-if="auth.isCompanyAdmin"
          :label="t('units.new')"
          icon="pi pi-plus"
          @click="router.push('/units/new')"
        />
      </div>

      <PMessage v-if="error" severity="error" :closable="false">{{ error }}</PMessage>

      <DataTable
        :value="units"
        :loading="loading"
        :rows="20"
        striped-rows
        class="unit-table"
      >
        <template #empty>
          <div class="empty-state">
            <i class="pi pi-building empty-icon" />
            <p>{{ t('units.empty') }}</p>
            <PButton
              v-if="auth.isCompanyAdmin"
              :label="t('units.new')"
              icon="pi pi-plus"
              size="small"
              @click="router.push('/units/new')"
            />
          </div>
        </template>
        <Column :header="t('fields.type')" style="width:140px">
          <template #body="{ data }">
            <PTag :value="t(`units.${data.type}`)" />
          </template>
        </Column>
        <Column field="name" :header="t('fields.unitName')" />
        <Column field="address" :header="t('fields.address')" />
        <Column v-if="auth.isCompanyAdmin" style="width:60px">
          <template #body="{ data }">
            <PButton
              icon="pi pi-pencil"
              text
              rounded
              severity="secondary"
              @click="router.push(`/units/${data.id}/edit`)"
            />
          </template>
        </Column>
      </DataTable>
  </div>
</template>

<style scoped>
.units-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.units-header h1 {
  margin: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 40px;
  opacity: 0.4;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}
</style>

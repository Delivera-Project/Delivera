<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useFormatDate } from '@/composables/useFormatDate'
import { useResourceList } from '@/composables/useResourceList'

const { t } = useI18n()
const { formatDate } = useFormatDate()
const router = useRouter()
const { items: loyalUsers, loading, error } = useResourceList('/loyal-users')
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


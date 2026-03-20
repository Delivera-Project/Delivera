<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useOrderForm } from '@/composables/useOrderForm'

const { t } = useI18n()
const router = useRouter()
const { units, loadError, originId, destinationId, notes, loading, error, success, destinationOptions, handleSubmit } = useOrderForm()
</script>

<template>
  <form class="card card-wide" @submit.prevent="handleSubmit">
      <PButton
        type="button"
        text
        severity="secondary"
        icon="pi pi-arrow-left"
        class="back-btn"
        @click="router.push('/orders')"
      />

      <h1>{{ t('orders.title') }}</h1>
      <p class="subtitle">{{ t('orders.subtitle') }}</p>

      <PMessage v-if="loadError" severity="error" :closable="false" class="form-message">{{ loadError }}</PMessage>
      <PMessage v-else-if="units.length < 2 && !loadError" severity="warn" :closable="false" class="form-message">{{ t('orders.noUnits') }}</PMessage>

      <template v-else-if="units.length >= 2 && !loadError">
        <div class="form-field">
          <label for="order-origin">{{ t('orders.origin') }}</label>
          <PSelect
            id="order-origin"
            v-model="originId"
            :options="units"
            option-label="name"
            option-value="id"
            :placeholder="t('orders.originPlaceholder')"
            fluid
          />
        </div>

        <div class="form-field">
          <label for="order-destination">{{ t('orders.destination') }}</label>
          <PSelect
            id="order-destination"
            v-model="destinationId"
            :options="destinationOptions"
            option-label="name"
            option-value="id"
            :placeholder="t('orders.destinationPlaceholder')"
            fluid
          />
        </div>

        <div class="form-field">
          <label for="order-notes">{{ t('orders.notes') }}</label>
          <PTextarea
            id="order-notes"
            v-model="notes"
            :placeholder="t('orders.notesPlaceholder')"
            rows="3"
            fluid
          />
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
      </template>
  </form>
</template>

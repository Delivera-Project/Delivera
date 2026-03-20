<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useOrderForm } from '@/composables/useOrderForm'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
const router = useRouter()
const { units, loadError, originId, destinationId, notes, loading, error, success, destinationOptions, handleSubmit } = useOrderForm()
</script>

<template>
  <BaseLayout>
    <form class="card card-wide" @submit.prevent="handleSubmit">
      <button type="button" class="back-btn" @click="router.push('/units')">
        ← {{ t('common.back') }}
      </button>

      <h1>{{ t('orders.title') }}</h1>
      <p class="subtitle">{{ t('orders.subtitle') }}</p>

      <p v-if="loadError" class="msg-error">{{ loadError }}</p>
      <p v-else-if="units.length < 2" class="msg-error">{{ t('orders.noUnits') }}</p>

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

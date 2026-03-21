<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useOrderForm } from '@/composables/useOrderForm'

const { t } = useI18n()
const router = useRouter()
const { units, loadError, originId, destinationId, recipientEmail, recipientName, priority, notes, isExternal, loading, error, errors, invalids, destinationOptions, handleSubmit } = useOrderForm()

const priorityOptions = computed(() => [
  { label: t('orders.priority.HIGH'), value: 'HIGH' },
  { label: t('orders.priority.NORMAL'), value: 'NORMAL' },
  { label: t('orders.priority.LOW'), value: 'LOW' },
])
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

    <PMessage v-if="loadError" severity="error" :closable="false" class="form-message">{{ loadError }}</PMessage>
    <PMessage v-else-if="units.length < 1 && !loadError" severity="warn" :closable="false" class="form-message">{{ t('orders.noUnits') }}</PMessage>

    <template v-else-if="units.length >= 1 && !loadError">
      <!-- Toggle interno/externo -->
      <div class="form-field">
        <div class="toggle-row">
          <PToggleButton
            v-model="isExternal"
            :on-label="t('orders.externalOrder')"
            :off-label="t('orders.subtitle')"
            on-icon="pi pi-user"
            off-icon="pi pi-building"
            fluid
          />
        </div>
      </div>

      <div class="form-field">
        <label for="order-origin">{{ t('orders.origin') }}</label>
        <PSelect
          id="order-origin"
          v-model="originId"
          :options="units"
          option-label="name"
          option-value="id"
          :placeholder="t('orders.originPlaceholder')"
          :invalid="!!invalids.originId"
          fluid
        />
      </div>

      <!-- Destino: interno o externo -->
      <template v-if="!isExternal">
        <div class="form-field">
          <label for="order-destination">{{ t('orders.destination') }}</label>
          <PSelect
            id="order-destination"
            v-model="destinationId"
            :options="destinationOptions"
            option-label="name"
            option-value="id"
            :placeholder="t('orders.destinationPlaceholder')"
            :invalid="!!invalids.destinationId"
            fluid
          />
        </div>
      </template>
      <template v-else>
        <div class="form-field">
          <label for="order-recipient-email">{{ t('orders.recipientEmail') }}</label>
          <PInputText
            id="order-recipient-email"
            v-model="recipientEmail"
            :placeholder="t('orders.recipientEmailPlaceholder')"
            :invalid="!!invalids.recipientEmail"
            type="email"
            fluid
          />
          <small v-if="errors.recipientEmail" class="field-error">{{ errors.recipientEmail }}</small>
        </div>
        <div class="form-field">
          <label for="order-recipient-name">{{ t('orders.recipientName') }}</label>
          <PInputText
            id="order-recipient-name"
            v-model="recipientName"
            :placeholder="t('orders.recipientNamePlaceholder')"
            fluid
          />
        </div>
      </template>

      <div class="form-field">
        <label for="order-priority">{{ t('orders.priority.label') }}</label>
        <PSelect
          id="order-priority"
          v-model="priority"
          :options="priorityOptions"
          option-label="label"
          option-value="value"
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

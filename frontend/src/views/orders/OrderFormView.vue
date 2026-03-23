<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useOrderForm } from '@/composables/useOrderForm'

const { t } = useI18n()
const router = useRouter()
const {
  units, loyalUserMatch, loadError,
  orderType, originId, destinationId, b2bCompanyId, b2bDestinationId,
  recipientEmail, recipientName,
  priority, notes, loading, error, errors, invalids,
  destinationOptions, b2bCompanies, b2bUnitOptions, handleSubmit,
} = useOrderForm()

const typeOptions = computed(() => [
  { label: t('orders.type.INTERNAL'), value: 'INTERNAL' },
  { label: t('orders.type.B2C'),      value: 'B2C' },
  { label: t('orders.type.B2B'),      value: 'B2B' },
])

const priorityOptions = computed(() => [
  { label: t('orders.priority.HIGH'),   value: 'HIGH' },
  { label: t('orders.priority.NORMAL'), value: 'NORMAL' },
  { label: t('orders.priority.LOW'),    value: 'LOW' },
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

    <template v-else>
      <!-- Tipo de pedido -->
      <div class="form-field">
        <label for="order-type">{{ t('orders.orderType') }}</label>
        <SelectButton id="order-type" v-model="orderType" :options="typeOptions" option-label="label" option-value="value" />
      </div>

      <!-- Origen (siempre) -->
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

      <!-- INTERNAL: unidad de destino -->
      <template v-if="orderType === 'INTERNAL'">
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

      <!-- B2C: email + nombre -->
      <template v-else-if="orderType === 'B2C'">
        <div class="form-field">
          <label for="order-email">{{ t('orders.recipientEmail') }}</label>
          <PInputText
            id="order-email"
            v-model="recipientEmail"
            :placeholder="t('orders.recipientEmailPlaceholder')"
            :invalid="!!invalids.recipientEmail"
            type="email"
            fluid
          />
          <small v-if="errors.recipientEmail" class="field-error">{{ errors.recipientEmail }}</small>
          <small v-else-if="loyalUserMatch" class="field-hint">
            <i class="pi pi-check-circle" /> {{ t('orders.loyalUserExists') }}
          </small>
        </div>
        <div class="form-field">
          <label for="order-name">{{ t('orders.recipientName') }}</label>
          <PInputText
            id="order-name"
            v-model="recipientName"
            :placeholder="t('orders.recipientNamePlaceholder')"
            fluid
          />
        </div>
      </template>

      <!-- B2B: empresa destino + unidad destino -->
      <template v-else>
        <div class="form-field">
          <label for="order-b2b-company">{{ t('orders.destinationCompany') }}</label>
          <PSelect
            id="order-b2b-company"
            v-model="b2bCompanyId"
            :options="b2bCompanies"
            option-label="name"
            option-value="id"
            :placeholder="t('orders.destinationCompanyPlaceholder')"
            :invalid="!!invalids.b2bCompanyId"
            fluid
          />
        </div>
        <div class="form-field">
          <label for="order-b2b-unit">{{ t('orders.destinationUnit') }}</label>
          <PSelect
            id="order-b2b-unit"
            v-model="b2bDestinationId"
            :options="b2bUnitOptions"
            option-label="name"
            option-value="id"
            :placeholder="t('orders.destinationUnitPlaceholder')"
            :invalid="!!invalids.b2bDestinationId"
            :disabled="!b2bCompanyId"
            fluid
          />
        </div>
      </template>

      <!-- Prioridad -->
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

      <!-- Notas -->
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

<style scoped>
.card { text-align: left; }
.back-btn { margin-bottom: 16px; }
.form-field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-field label { font-size: 13px; font-weight: 600; color: #374151; }
.field-error { color: #ef4444; font-size: 12px; }
.field-hint { color: #16a34a; font-size: 12px; display: flex; align-items: center; gap: 4px; }
.form-message { margin-bottom: 12px; }
.submit-btn { margin-top: 8px; }
</style>

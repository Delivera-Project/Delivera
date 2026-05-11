<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps({
  hasActiveOrders: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})

defineEmits(['confirm', 'cancel'])
</script>

<template>
  <div class="delete-confirm-panel">
    <div class="delete-confirm-content">
      <i v-if="hasActiveOrders" class="pi pi-exclamation-triangle delete-confirm-icon" />
      <div class="delete-confirm-texts">
        <span class="delete-confirm-title">
          {{ hasActiveOrders ? t('settings.deleteActiveOrdersTitle') : t('settings.deleteConfirm') }}
        </span>
        <span v-if="hasActiveOrders" class="delete-confirm-subtitle">
          {{ t('settings.deleteActiveOrdersHint') }}
        </span>
        <span v-if="error" class="delete-confirm-error">{{ error }}</span>
      </div>
    </div>
    <div class="delete-confirm-actions">
      <PButton :label="t('common.cancel')" icon="pi pi-times" severity="secondary" outlined size="small" @click="$emit('cancel')" />
      <PButton
        :label="loading ? t('common.loading') : t('common.delete')"
        icon="pi pi-building"
        severity="danger"
        size="small"
        :loading="loading"
        @click="$emit('confirm')"
      />
    </div>
  </div>
</template>

<style scoped src="./DeleteConfirmPanel.css"></style>

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
      <i :class="['pi', hasActiveOrders ? 'pi-exclamation-triangle' : 'pi-trash', 'delete-confirm-icon']" />
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
      <PButton :label="t('common.cancel')" severity="secondary" text size="small" @click="$emit('cancel')" />
      <PButton
        :label="loading ? t('common.loading') : t('common.delete')"
        severity="danger"
        size="small"
        :loading="loading"
        @click="$emit('confirm')"
      />
    </div>
  </div>
</template>

<style scoped>
.delete-confirm-panel {
  border-top: 1px solid #fee2e2;
  background: #fff8f8;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.delete-confirm-content {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.delete-confirm-icon {
  font-size: 15px;
  color: #ef4444;
  flex-shrink: 0;
  margin-top: 2px;
}

.delete-confirm-texts {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.delete-confirm-title {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
}

.delete-confirm-subtitle {
  font-size: 12px;
  color: #6b7280;
}

.delete-confirm-error {
  font-size: 12px;
  color: #ef4444;
}

.delete-confirm-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
</style>

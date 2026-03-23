<script setup>
import { useI18n } from 'vue-i18n'
import { useAppConfig } from '@/composables/useAppConfig'
import { useFormatDate } from '@/composables/useFormatDate'

const { t } = useI18n()
const { formatDateTime } = useFormatDate()
const { statusSeverity } = useAppConfig()

defineProps({
  events: { type: Array, required: true },
  showAuthor: { type: Boolean, default: false },
})
</script>

<template>
  <div v-if="!events.length" class="timeline-empty">
    <i class="pi pi-clock" />
    <span>{{ t('tracking.noEvents') }}</span>
  </div>
  <div v-else class="timeline">
    <div v-for="ev in events" :key="ev.id" class="timeline-item">
      <div class="timeline-dot" :class="`dot-${ev.status.toLowerCase()}`" />
      <div class="timeline-content">
        <div class="timeline-status">
          <PTag :value="t(`orders.status.${ev.status}`)" :severity="statusSeverity[ev.status]" size="small" />
          <span class="timeline-date">{{ formatDateTime(ev.createdAt) }}</span>
        </div>
        <div v-if="ev.note" class="timeline-note">{{ ev.note }}</div>
        <div v-if="showAuthor && ev.authorEmail" class="timeline-author">{{ ev.authorEmail }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import BaseLayout from '@/components/BaseLayout.vue'

const { t } = useI18n()
</script>

<template>
  <BaseLayout>
    <div class="card">
      <svg class="not-found-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <circle cx="10.5" cy="10.5" r="7.5" />
        <path d="m21 21-5.2-5.2" />
        <path d="m8.5 8.5 4 4m0-4-4 4" />
      </svg>
      <div class="not-found-code">404</div>
      <p class="subtitle">{{ t('notFound.message') }}</p>
      <router-link to="/" class="btn">{{ t('notFound.goHome') }}</router-link>
    </div>
  </BaseLayout>
</template>

<script setup>
defineProps({
  // 'checking' | 'available' | 'taken' | null
  state: { type: String, default: null }
})
</script>

<template>
  <span v-if="state === 'checking'" class="check-badge check-checking"><i class="pi pi-spin pi-spinner" /></span>
  <span v-else-if="state === 'available'" class="check-badge check-ok"><i class="pi pi-check" /></span>
  <span v-else-if="state === 'taken'" class="check-badge check-taken"><i class="pi pi-times" /></span>
</template>

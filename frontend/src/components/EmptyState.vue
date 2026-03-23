<script setup>
defineProps({
  icon: { type: String, default: 'pi-inbox' },
  message: { type: String, required: true },
})
</script>

<template>
  <div class="empty-state">
    <i :class="`pi ${icon} empty-icon`" />
    <p>{{ message }}</p>
    <slot />
  </div>
</template>

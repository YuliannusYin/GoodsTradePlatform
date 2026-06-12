<template>
  <div>
    <h2 :class="['text-xl font-semibold', additionalClass]">{{ text }}</h2>
    <div v-if="hasUnderline" class="border border-t-1 w-full mt-1"></div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text: string
  hasUnderline?: boolean
  additionalClass?: string
}>(), {
  hasUnderline: true
})
</script>

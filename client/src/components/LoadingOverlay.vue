<template>
  <div v-if="isLoading" class="bg-white opacity-40 top-0 left-0 w-full h-full absolute flex justify-center items-center">
    <div class="spinner-border text-white" role="status" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  isLoading?: boolean
}>()
</script>

<style scoped>
.spinner-border {
  display: inline-block;
  width: 2rem;
  height: 2rem;
  border-width: .25em;
  border-color: gray;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spinner-border .75s linear infinite;
}

@keyframes spinner-border {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>

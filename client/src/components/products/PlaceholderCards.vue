<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 min-h-[15rem]">
    <div v-for="card in placeholderCards" :key="card.id"
      class="bg-white p-4 shadow-md rounded-lg flex justify-center items-center">
      <div class="spinner-border" role="status" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface PlaceHolderCard {
  id: string
}

const props = defineProps<{
  placeholderAmount: number
}>()

const placeholderCards = ref<PlaceHolderCard[]>([])

onMounted(() => {
  for (let amount = 0; amount < props.placeholderAmount; amount++) {
    placeholderCards.value.push({ id: `${amount}` })
  }
})
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

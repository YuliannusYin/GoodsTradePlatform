<template>
  <article class="flex flex-col justify-center items-center mt-2">
    <h2 class="text-2xl font-bold text-gray-800 mb-2">热门周边</h2>
    <div class="bg-primary-200 h-[0.15rem] w-[8rem] mb-4 rounded-full"></div>
    <ProductCards :placeholderAmount="4" :products="featuredProducts" class="w-full md:max-w-max mt-2" />
  </article>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import ProductCards from './ProductCards.vue'
import type { Product } from '@/types/product'

const productStore = useProductStore()
const featuredProducts = ref<Product[]>([])

onMounted(async () => {
  featuredProducts.value = await productStore.getFeaturedProducts()
})
</script>

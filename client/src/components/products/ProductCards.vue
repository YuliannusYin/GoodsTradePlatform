<template>
  <div v-if="products.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-2.5">
    <div v-for="product in products" :key="product.id">
      <ProductCard :product="product" />
    </div>
  </div>
  <PlaceholderCards v-else :placeholderAmount="placeholderAmount" />
</template>

<script setup lang="ts">
import type { Product } from '@/types/product'
import PlaceholderCards from './PlaceholderCards.vue'
import ProductCard from './ProductCard.vue'

defineProps<{
  placeholderAmount: number
  products: Product[]
}>()
</script>

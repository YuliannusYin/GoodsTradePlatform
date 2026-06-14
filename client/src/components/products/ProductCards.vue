<template>
  <div v-if="products.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
    <div v-for="product in products" :key="product.id" class="flex">
      <ProductCard :product="product" class="w-full" />
    </div>
  </div>
  <PlaceholderCards v-else :placeholderAmount="placeholderAmount" />
</template>

<script setup lang="ts">
/**
 * @file ProductCards.vue
 * @description 商品卡片列表组件，有商品时展示卡片网格，无商品时显示加载占位卡片
 */
import type { Product } from '@/types/product'
import PlaceholderCards from './PlaceholderCards.vue'
import ProductCard from './ProductCard.vue'

defineProps<{
  placeholderAmount: number
  products: Product[]
}>()
</script>

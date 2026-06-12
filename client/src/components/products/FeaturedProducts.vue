<template>
  <article class="flex flex-col justify-center items-center mt-2">
    <h2 class="text-2xl font-bold text-gray-800 mb-2">热门周边</h2>
    <div class="bg-primary-200 h-[0.15rem] w-[8rem] mb-4 rounded-full"></div>
    <ProductCards :placeholderAmount="4" :products="featuredProducts" class="w-full md:max-w-max mt-2" />
  </article>
</template>

<script setup lang="ts">
/**
 * @file FeaturedProducts.vue
 * @description 热门周边商品展示组件，加载并展示推荐商品卡片列表
 */
import { onMounted, ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import ProductCards from './ProductCards.vue'
import type { Product } from '@/types/product'

const productStore = useProductStore()
// 热门推荐商品列表
const featuredProducts = ref<Product[]>([])

// 组件挂载时获取热门推荐商品数据
onMounted(async () => {
  featuredProducts.value = await productStore.getFeaturedProducts()
})
</script>

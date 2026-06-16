<template>
  <div v-if="products.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-2.5">
    <div v-for="product in products" :key="product.id">
      <ProductCard :product="product" />
    </div>
  </div>
  <PlaceholderCards v-else :placeholderAmount="placeholderAmount" />
</template>

<script setup lang="ts">
/**
 * @file ProductCards.vue
 * @description 商品卡片列表组件，有商品时展示卡片网格，无商品时显示加载占位卡片
 * 挂载时批量加载收藏缓存，子组件 ProductCard 从缓存读取收藏状态，避免 N+1 请求
 */
import { onMounted } from 'vue'
import type { Product } from '@/types/product'
import PlaceholderCards from './PlaceholderCards.vue'
import ProductCard from './ProductCard.vue'
import { useFavoriteStore } from '@/stores/network/favoriteStore'
import { useAccountStore } from '@/stores/network/accountStore'

defineProps<{
  placeholderAmount: number
  products: Product[]
}>()

const favoriteStore = useFavoriteStore()
const accountStore = useAccountStore()

// 组件挂载时批量加载收藏缓存，所有子 ProductCard 共享此缓存
onMounted(async () => {
  if (accountStore.isAuthenticated && !favoriteStore.favoritesLoaded) {
    try {
      await favoriteStore.loadFavoriteIds()
    } catch {
      // 加载失败不影响商品列表展示
    }
  }
})
</script>

/**
 * @file FeaturedProducts.vue
 * @description 随机推荐商品展示组件，2行×4列网格布局，支持点击刷新按钮或刷新浏览器时重新获取随机商品
 * @input 无
 * @output 无
 */
<template>
  <article class="flex flex-col justify-center items-center mt-2">
    <!-- 标题区域：标题 + 刷新按钮 -->
    <div class="flex items-center gap-3 mb-2">
      <h2 class="text-2xl font-bold text-gray-800">随机推荐</h2>
      <button
        class="w-8 h-8 rounded-full flex items-center justify-center text-gray-500 hover:text-blue-500 hover:bg-blue-50 transition-colors duration-200"
        :class="{ 'animate-spin': isLoading }"
        :disabled="isLoading"
        title="换一批"
        @click="refreshProducts"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      </button>
    </div>
    <div class="bg-primary-200 h-[0.15rem] w-[8rem] mb-4 rounded-full"></div>

    <!-- 加载中状态 -->
    <div v-if="isLoading && products.length === 0" class="py-10">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
      <p class="text-gray-400 text-sm mt-3 text-center">加载中...</p>
    </div>

    <!-- 商品网格 -->
    <ProductCards
      v-else-if="products.length > 0"
      :placeholderAmount="8"
      :products="products"
      class="w-full md:max-w-max mt-2"
    />

    <!-- 无商品提示 -->
    <div v-else class="py-10 text-center">
      <p class="text-gray-400">暂无推荐商品</p>
    </div>
  </article>
</template>

<script setup lang="ts">
/**
 * 随机推荐商品展示组件
 * 职责：加载并展示2行×4列随机推荐商品，支持刷新按钮重新获取
 */
import { onMounted, ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import ProductCards from './ProductCards.vue'
import type { Product } from '@/types/product'

const productStore = useProductStore()
// 随机推荐商品列表
const products = ref<Product[]>([])
// 是否正在加载
const isLoading = ref(false)

/**
 * 加载随机推荐商品
 * 每次调用都会从后端获取新的随机商品列表
 */
async function refreshProducts() {
  // 防止重复请求
  if (isLoading.value) return
  isLoading.value = true
  try {
    products.value = await productStore.getRandomProducts(8)
  } catch {
    // 加载失败时保留已有数据，不覆盖为空
  } finally {
    isLoading.value = false
  }
}

// 组件挂载时加载随机推荐商品
onMounted(() => {
  refreshProducts()
})
</script>

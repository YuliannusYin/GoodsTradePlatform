<template>
  <div>
    <h3 class="text-xl font-bold text-gray-800 mb-4">我发布的商品</h3>
    <div v-if="myProducts.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div v-for="product in myProducts" :key="product.id"
        class="bg-white rounded-xl shadow-md p-4 flex gap-4">
        <img v-if="product.imageUrls && product.imageUrls.length > 0"
          :src="product.imageUrls[0]" :alt="product.name"
          class="w-20 h-20 object-contain rounded-lg"
          @error="(e: Event) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2280%22 height=%2280%22 viewBox=%220 0 80 80%22><rect fill=%22%23f3f4f6%22 width=%2280%22 height=%2280%22/><text x=%2240%22 y=%2245%22 text-anchor=%22middle%22 fill=%22%239ca3af%22 font-size=%2210%22>无图</text></svg>') }">
        <div v-else class="w-20 h-20 bg-gray-100 rounded-lg flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
          </svg>
        </div>
        <div class="flex-1">
          <h4 class="font-semibold text-gray-800">{{ product.name }}</h4>
          <span class="text-accent-600 font-bold">¥{{ product.price.toFixed(2) }}</span>
          <p class="text-xs text-gray-500 mt-1">库存：{{ product.quantity }}</p>
        </div>
      </div>
    </div>
    <div v-else class="text-center py-8 text-gray-400">
      你还没有发布过商品
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file MyProductsList.vue
 * @description 已发布商品列表组件，展示当前用户发布的商品概览
 * 职责单一：仅负责已发布商品列表的展示
 */
import { onMounted, ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import type { Product } from '@/types/product'

const productStore = useProductStore()
const myProducts = ref<Product[]>([])     // 用户已发布的商品列表

/**
 * 加载当前用户已发布的商品列表
 */
async function loadMyProducts() {
  try {
    myProducts.value = await productStore.getMyProducts()
  } catch (error) {
    // 错误已由拦截器处理
  }
}

// 暴露刷新方法，供父组件在发布成功后调用
defineExpose({ loadMyProducts })

// 组件挂载时加载商品列表
onMounted(loadMyProducts)
</script>

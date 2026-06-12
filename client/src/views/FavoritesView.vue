<template>
  <section class="max-w-5xl mx-auto px-4 py-8">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">我的收藏</h2>
    <div v-if="favorites.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="fav in favorites" :key="fav.id"
        class="bg-white rounded-xl shadow-md p-4 card-hover">
        <div class="flex gap-4">
          <img :src="fav.imageUrl" :alt="fav.productName"
            class="w-24 h-24 object-contain rounded-lg cursor-pointer"
            @click="goToProduct(fav.productId)">
          <div class="flex-1 flex flex-col justify-between">
            <h3 class="font-semibold text-gray-800 cursor-pointer hover:text-primary-600"
              @click="goToProduct(fav.productId)">{{ fav.productName }}</h3>
            <div class="flex items-center justify-between">
              <span class="text-lg font-bold text-accent-600">¥{{ fav.price.toFixed(2) }}</span>
              <button @click="removeFavorite(fav.productId)"
                class="text-xs text-red-400 hover:text-red-600 transition-colors">
                取消收藏
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="text-center py-16">
      <p class="text-gray-400 text-lg mb-4">还没有收藏任何商品</p>
      <router-link to="/shop" class="btn-primary inline-block">去逛逛</router-link>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file FavoritesView.vue
 * @description 收藏视图，展示用户收藏的商品列表，支持取消收藏和跳转商品详情
 */
import { onMounted, ref } from 'vue'
import type { Favorite } from '@/types/favorite'
import { useFavoriteStore } from '@/stores/network/favoriteStore'
import { useRouter } from 'vue-router'

const favoriteStore = useFavoriteStore() // 收藏状态管理实例
const router = useRouter()
const favorites = ref<Favorite[]>([])    // 收藏列表数据

/**
 * 加载用户收藏列表
 */
async function loadFavorites() {
  try {
    favorites.value = await favoriteStore.getUserFavorites()
  } catch (error) {
    console.error('Failed to load favorites:', error)
  }
}

/**
 * 取消收藏指定商品
 * @param {string} productId - 商品ID
 */
async function removeFavorite(productId: string) {
  try {
    await favoriteStore.removeFavorite(productId)
    // 从本地列表中移除已取消收藏的商品
    favorites.value = favorites.value.filter(f => f.productId !== productId)
  } catch (error) {
    console.error('Failed to remove favorite:', error)
  }
}

/**
 * 跳转到商品详情页
 * @param {string} productId - 商品ID
 */
function goToProduct(productId: string) {
  router.push({ name: 'productView', params: { productId } })
}

// 组件挂载时加载收藏列表
onMounted(loadFavorites)
</script>

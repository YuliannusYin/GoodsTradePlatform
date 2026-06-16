<template>
  <div class="flex justify-center">
    <div class="bg-white p-6 w-full flex flex-col justify-center items-center md:flex-row md:items-start gap-6 rounded-2xl shadow-lg">
      <div class="w-full md:w-1/2">
        <img v-if="product.imageUrls && product.imageUrls.length > 0"
          :src="product.imageUrls[0]" :alt="product.name"
          class="w-full h-[20rem] object-contain rounded-xl"
          @error="handleImageError">
        <div v-else class="w-full h-[20rem] bg-gray-100 rounded-xl flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
          </svg>
        </div>
      </div>
      <div class="flex flex-col justify-start items-start space-y-3 w-full md:w-1/2">
        <div class="flex items-center gap-2">
          <span v-if="product.category" class="text-xs bg-primary-100 text-primary-700 px-2 py-1 rounded-full font-medium">
            {{ getCategoryLabel(product.category) }}
          </span>
          <span v-if="product.condition" class="text-xs bg-mint-100 text-mint-700 px-2 py-1 rounded-full font-medium">
            {{ getConditionLabel(product.condition) }}
          </span>
          <span v-if="product.source === 'USER'" class="text-xs bg-accent-100 text-accent-700 px-2 py-1 rounded-full font-medium">
            个人闲置
          </span>
        </div>
        <h3 class="text-2xl font-bold text-gray-800">{{ product.name }}</h3>
        <p class="text-gray-600 leading-relaxed">{{ product.description }}</p>
        <div v-if="product.seller" class="text-sm text-gray-500">
          卖家：{{ product.seller.username }}
        </div>
        <span class="text-2xl font-bold text-accent-600">¥{{ product.price.toFixed(2) }}</span>
        <div class="flex items-center gap-3">
          <button @click="addToCart(product.id)"
            class="bg-primary-500 hover:bg-primary-600 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors">
            加入购物车
          </button>
          <button @click="toggleFavorite" class="p-2 rounded-full transition-colors"
            :class="isFavorited ? 'text-accent-500' : 'text-gray-400 hover:text-accent-400'">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" :fill="isFavorited ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ProductDetailCard.vue
 * @description 商品详情卡片组件，用于商品详情页中展示完整商品信息
 * 职责单一：仅负责详情视图的展示，包含收藏和加入购物车功能
 */
import { computed, onMounted } from 'vue'
import type { Product } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS } from '@/types/product'
import { useFavoriteStore } from '@/stores/network/favoriteStore'
import { useAccountStore } from '@/stores/network/accountStore'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'
import { useRouter } from 'vue-router'

const props = defineProps<{
  product: Product
}>()

const router = useRouter()
const favoriteStore = useFavoriteStore()
const accountStore = useAccountStore()
const shoppingCartStore = useShoppingCartStore()

// 从收藏缓存中计算当前商品是否已收藏，缓存未加载时默认为 false
const isFavorited = computed(() => {
  return favoriteStore.favoritesLoaded && favoriteStore.favoriteProductIds.includes(props.product.id)
})

// 组件挂载时若缓存未加载且用户已登录，则加载收藏缓存
onMounted(async () => {
  if (accountStore.isAuthenticated && !favoriteStore.favoritesLoaded) {
    try {
      await favoriteStore.loadFavoriteIds()
    } catch {
      // 加载失败不影响页面展示
    }
  }
})

// 将商品添加到购物车
function addToCart(productId: string) {
  shoppingCartStore.addItem(productId)
}

/**
 * 切换商品收藏状态
 * 未登录用户跳转到登录页面，已登录用户切换收藏/取消收藏
 */
async function toggleFavorite() {
  if (!accountStore.isAuthenticated) {
    // 未登录时跳转到登录页
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      // 已收藏则取消收藏
      await favoriteStore.removeFavorite(props.product.id)
    } else {
      // 未收藏则添加收藏
      await favoriteStore.addFavorite(props.product.id)
    }
  } catch (error) {
    // 错误已由拦截器处理
  }
}

/**
 * 图片加载失败时，替换为占位图SVG
 * @param {Event} event - 图片加载错误事件
 */
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  // 使用内联SVG作为占位图，避免再次触发网络请求
  img.src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200"><rect fill="#f3f4f6" width="200" height="200"/><g transform="translate(50,40)"><path fill="#d1d5db" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" transform="scale(4)"/></g><text x="100" y="160" text-anchor="middle" fill="#9ca3af" font-size="14">图片加载失败</text></svg>')
}

/**
 * 获取商品分类的中文标签
 * @param {string} category - 分类标识
 * @returns {string} 分类中文名称
 */
function getCategoryLabel(category: string): string {
  return PRODUCT_CATEGORIES[category] || category
}

/**
 * 获取商品成色的中文标签
 * @param {string} condition - 成色标识
 * @returns {string} 成色中文名称
 */
function getConditionLabel(condition: string): string {
  return PRODUCT_CONDITIONS[condition] || condition
}
</script>

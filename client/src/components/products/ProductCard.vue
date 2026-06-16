<template>
  <div class="bg-white p-4 flex flex-col justify-between min-w-max rounded-xl shadow-md card-hover">
    <div class="relative">
      <img v-if="product.imageUrls && product.imageUrls.length > 0"
        :src="product.imageUrls[0]" :alt="product.name"
        class="mb-3 h-[12rem] w-full object-contain cursor-pointer rounded-lg"
        @click="showProductView(product.id)"
        @error="handleImageError">
      <div v-else class="mb-3 h-[12rem] w-full bg-gray-100 rounded-lg flex items-center justify-center cursor-pointer"
        @click="showProductView(product.id)">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
        </svg>
      </div>
      <button @click.stop="toggleFavorite" class="absolute top-2 right-2 p-1.5 rounded-full bg-white/80 backdrop-blur-sm transition-colors"
        :class="isFavorited ? 'text-accent-500' : 'text-gray-400 hover:text-accent-400'">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" :fill="isFavorited ? 'currentColor' : 'none'" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
      </button>
    </div>
    <div class="flex items-center gap-1.5 mb-2">
      <span v-if="product.category" class="text-[10px] bg-primary-100 text-primary-700 px-1.5 py-0.5 rounded-full">
        {{ getCategoryLabel(product.category) }}
      </span>
      <span v-if="product.condition && product.condition !== 'NEW'" class="text-[10px] bg-mint-100 text-mint-700 px-1.5 py-0.5 rounded-full">
        {{ getConditionLabel(product.condition) }}
      </span>
    </div>
    <h3 class="text-sm font-semibold mb-2 text-gray-800 line-clamp-2 cursor-pointer hover:text-primary-600"
      @click="showProductView(product.id)">{{ product.name }}</h3>
    <div class="flex flex-col justify-between items-center gap-2">
      <span class="text-lg font-bold text-accent-600">¥{{ product.price.toFixed(2) }}</span>
      <button @click="addToCart(product.id)"
        class="bg-primary-500 hover:bg-primary-600 text-white px-3 py-1.5 rounded-lg text-xs font-medium transition-colors">
        加入购物车
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ProductCard.vue
 * @description 商品卡片组件，用于商品列表中的网格卡片展示
 * 职责单一：仅负责列表卡片视图，收藏状态从 favoriteStore 缓存读取（不单独发起请求）
 */
import { computed } from 'vue'
import type { Product } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS } from '@/types/product'
import { useRouter } from 'vue-router'
import { useFavoriteStore } from '@/stores/network/favoriteStore'
import { useAccountStore } from '@/stores/network/accountStore'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'

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

/**
 * 跳转到商品详情页面
 * @param {string} productId - 商品ID
 */
function showProductView(productId: string) {
  router.push({ name: 'productView', params: { productId } })
}

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

<template>
  <!-- 购物车页面，左侧商品列表 + 右侧汇总面板 + 底部推荐商品 -->
  <section class="max-w-7xl mx-auto px-4 py-6">
    <!-- 页面标题 -->
    <h2 class="text-2xl font-bold text-gray-800 mb-6">
      我的购物车 ({{ shoppingCartStore.totalQuantity }}件商品)
    </h2>

    <!-- 有商品时：左右布局 -->
    <div v-if="shoppingCartStore.totalKinds > 0" class="flex flex-col lg:flex-row gap-6">
      <!-- 左侧：商品列表 -->
      <div class="flex-1 min-w-0">
        <CartItemList />
      </div>

      <!-- 右侧：汇总面板 -->
      <div class="w-full lg:w-80 shrink-0">
        <CartSummary :totalPrice="totalPrice" :totalQuantity="shoppingCartStore.totalQuantity" />
      </div>
    </div>

    <!-- 空购物车状态 -->
    <div v-else class="text-center py-20">
      <!-- 空购物车图标 -->
      <svg xmlns="http://www.w3.org/2000/svg" class="h-20 w-20 text-gray-200 mx-auto mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
        <path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
      </svg>
      <p class="text-gray-400 text-lg mb-2">购物车还是空的</p>
      <p class="text-gray-300 text-sm mb-6">快去挑选心仪的商品吧</p>
      <router-link to="/shop"
        class="inline-block bg-primary-500 hover:bg-primary-600 text-white px-6 py-2.5 rounded-lg font-medium transition-colors">
        去逛逛
      </router-link>
    </div>

    <!-- 底部推荐商品区域 -->
    <div v-if="featuredProducts.length > 0" class="mt-12">
      <h3 class="text-xl font-bold text-gray-800 mb-4">猜你喜欢</h3>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <div v-for="product in featuredProducts" :key="product.id"
          class="bg-white p-4 rounded-xl shadow-md card-hover flex flex-col justify-between">
          <!-- 推荐商品图片 -->
          <div class="mb-3">
            <img v-if="product.imageUrls && product.imageUrls.length > 0"
              :src="product.imageUrls[0]" :alt="product.name"
              class="h-[10rem] w-full object-contain rounded-lg cursor-pointer"
              @click="goToProduct(product.id)"
              @error="handleImageError">
            <!-- 无图片时显示占位图 -->
            <div v-else class="h-[10rem] w-full bg-gray-100 rounded-lg flex items-center justify-center cursor-pointer"
              @click="goToProduct(product.id)">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
              </svg>
            </div>
          </div>
          <!-- 推荐商品信息 -->
          <div>
            <h4 class="text-sm font-semibold text-gray-800 line-clamp-2 mb-2 cursor-pointer hover:text-primary-600 transition-colors"
              @click="goToProduct(product.id)">{{ product.name }}</h4>
            <div class="flex items-center justify-between">
              <span class="text-base font-bold text-accent-600">¥{{ product.price.toFixed(2) }}</span>
              <button @click="addToCart(product.id)"
                class="bg-primary-500 hover:bg-primary-600 text-white px-2.5 py-1 rounded-lg text-xs font-medium transition-colors">
                加入购物车
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file CartView.vue
 * @description 购物车页面视图，包含商品列表、订单汇总面板和推荐商品区域
 * @input 无（通过 store 获取购物车数据）
 * @output 无
 */
import { ref, computed, onMounted, watch } from 'vue'
import type { Product } from '@/types/product'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'
import { useProductStore } from '@/stores/network/productStore'
import { useRouter } from 'vue-router'
import CartItemList from '@/components/cart/CartItemList.vue'
import CartSummary from '@/components/cart/CartSummary.vue'

const shoppingCartStore = useShoppingCartStore()
const productStore = useProductStore()
const router = useRouter()

// 推荐商品列表
const featuredProducts = ref<Product[]>([])
// 商品详情缓存，key为商品ID，value为商品详情（用于计算总价）
const productMap = ref<Map<string, Product>>(new Map())

/**
 * 计算购物车商品总价（基于已加载的商品详情）
 * 遍历购物车所有项，从 productMap 中获取价格并乘以数量求和
 */
const totalPrice = computed(() => {
  let total = 0
  for (const item of shoppingCartStore.getAllItems()) {
    const product = productMap.value.get(item.productId)
    if (product) {
      total += product.price * item.quantity
    }
  }
  return total
})

/**
 * 加载购物车中所有商品的详情并缓存到 productMap
 * 用于计算购物车总价，与 CartItemList 中的加载逻辑独立
 */
async function loadProductDetails() {
  const items = shoppingCartStore.getAllItems()
  if (items.length === 0) {
    productMap.value = new Map()
    return
  }
  // 并行请求所有商品详情
  const results = await Promise.allSettled(
    items.map(async (item) => {
      const product = await productStore.getProduct(item.productId)
      if (!product) throw new Error(`商品 ${item.productId} 不存在`)
      return { productId: item.productId, product }
    })
  )
  // 将成功加载的商品详情缓存到 Map 中
  const newMap = new Map<string, Product>()
  results.forEach((result) => {
    if (result.status === 'fulfilled') {
      newMap.set(result.value.productId, result.value.product)
    }
  })
  productMap.value = newMap
}

/**
 * 加载推荐商品列表
 * 最多展示5个推荐商品
 */
async function loadFeaturedProducts() {
  try {
    const products = await productStore.getFeaturedProducts()
    // 最多显示5个推荐商品
    featuredProducts.value = products.slice(0, 5)
  } catch (error) {
    // 错误已由拦截器处理
  }
}

/**
 * 将商品添加到购物车
 * @param {string} productId - 商品ID
 */
function addToCart(productId: string) {
  shoppingCartStore.addItem(productId)
  // 添加后重新加载商品详情以更新总价
  loadProductDetails()
}

/**
 * 跳转到商品详情页
 * @param {string} productId - 商品ID
 */
function goToProduct(productId: string) {
  router.push({ name: 'productView', params: { productId } })
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

// 组件挂载时加载商品详情和推荐商品
onMounted(() => {
  loadProductDetails()
  loadFeaturedProducts()
})

// 监听购物车商品总数量变化，当增删商品或修改数量时重新加载商品详情以更新总价
watch(
  () => shoppingCartStore.totalQuantity,
  () => {
    loadProductDetails()
  }
)
</script>

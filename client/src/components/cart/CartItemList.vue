<template>
  <!-- 购物车商品列表，遍历购物车项并加载商品详情 -->
  <div class="space-y-4">
    <!-- 加载中状态 -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-12">
      <div class="animate-spin rounded-full h-10 w-10 border-4 border-primary-200 border-t-primary-500"></div>
      <p class="mt-4 text-gray-400 text-sm">正在加载购物车商品...</p>
    </div>

    <!-- 商品列表 -->
    <div v-else-if="cartEntries.length > 0" class="space-y-3">
      <CartItemRow
        v-for="entry in cartEntries"
        :key="entry.productId"
        :product="entry.product"
        :quantity="entry.quantity"
        @update:quantity="(q: number) => handleUpdateQuantity(entry.productId, q)"
        @remove="handleRemoveItem(entry.productId)"
      />
    </div>

    <!-- 加载失败提示 -->
    <div v-else-if="hasError" class="text-center py-8">
      <p class="text-red-400 mb-2">部分商品信息加载失败</p>
      <button @click="loadProducts"
        class="text-primary-500 hover:text-primary-600 text-sm underline transition-colors">
        点击重试
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file CartItemList.vue
 * @description 购物车商品列表组件，负责从后端加载商品详情并渲染购物车商品行列表
 * @input 无（通过 shoppingCartStore 获取购物车数据）
 * @output 无（通过 store 方法直接操作购物车）
 */
import { ref, onMounted, watch } from 'vue'
import type { Product } from '@/types/product'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'
import { useProductStore } from '@/stores/network/productStore'
import CartItemRow from './CartItemRow.vue'

/**
 * 购物车条目，包含商品详情和购买数量
 */
interface CartEntry {
  /** 商品ID */
  productId: string
  /** 商品详情 */
  product: Product
  /** 购买数量 */
  quantity: number
}

const shoppingCartStore = useShoppingCartStore()
const productStore = useProductStore()

// 购物车条目列表，包含商品详情和数量
const cartEntries = ref<CartEntry[]>([])
// 是否正在加载商品详情
const isLoading = ref(false)
// 是否加载出错
const hasError = ref(false)

/**
 * 从后端批量加载购物车中所有商品的详情
 * 遍历购物车Map，并行调用 productStore.getProduct 获取商品信息
 */
async function loadProducts() {
  const items = shoppingCartStore.getAllItems()
  if (items.length === 0) {
    // 购物车为空时无需加载
    cartEntries.value = []
    return
  }

  isLoading.value = true
  hasError.value = false

  // 并行请求所有商品详情
  const results = await Promise.allSettled(
    items.map(async (item) => {
      const product = await productStore.getProduct(item.productId)
      if (!product) throw new Error(`商品 ${item.productId} 不存在`)
      return { productId: item.productId, product, quantity: item.quantity }
    })
  )

  // 筛选成功加载的条目，记录是否有失败项
  const successEntries: CartEntry[] = []
  let hasAnyFailed = false

  results.forEach((result) => {
    if (result.status === 'fulfilled') {
      successEntries.push(result.value)
    } else {
      hasAnyFailed = true
    }
  })

  cartEntries.value = successEntries
  hasError.value = hasAnyFailed
  isLoading.value = false
}

/**
 * 处理商品数量变更
 * @param {string} productId - 商品ID
 * @param {number} newQuantity - 新数量
 */
function handleUpdateQuantity(productId: string, newQuantity: number) {
  shoppingCartStore.setItemQuantity(productId, newQuantity)
  // 同步更新本地条目的数量
  const entry = cartEntries.value.find(e => e.productId === productId)
  if (entry) {
    entry.quantity = newQuantity
  }
}

/**
 * 处理商品移除
 * @param {string} productId - 要移除的商品ID
 */
function handleRemoveItem(productId: string) {
  shoppingCartStore.removeItem(productId)
  // 从本地列表中移除对应条目
  cartEntries.value = cartEntries.value.filter(e => e.productId !== productId)
}

// 组件挂载时加载商品详情
onMounted(loadProducts)

// 监听购物车商品种类数变化，当外部增删商品时重新加载
watch(
  () => shoppingCartStore.totalKinds,
  (newKinds, oldKinds) => {
    if (newKinds !== oldKinds) {
      loadProducts()
    }
  }
)
</script>

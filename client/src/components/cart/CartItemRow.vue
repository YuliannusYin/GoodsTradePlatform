<template>
  <!-- 购物车单行商品项，包含图片、名称、单价、数量控制、小计和删除按钮 -->
  <div class="bg-white rounded-xl shadow-md p-4 flex items-center gap-4 card-hover">
    <!-- 商品图片区域 -->
    <div class="w-24 h-24 shrink-0 cursor-pointer" @click="goToProduct">
      <img v-if="product.imageUrls && product.imageUrls.length > 0"
        :src="product.imageUrls[0]" :alt="product.name"
        class="w-full h-full object-contain rounded-lg"
        @error="handleImageError">
      <!-- 无图片时显示占位图 -->
      <div v-else class="w-full h-full bg-gray-100 rounded-lg flex items-center justify-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
        </svg>
      </div>
    </div>

    <!-- 商品信息区域 -->
    <div class="flex-1 min-w-0">
      <!-- 商品名称，点击可跳转详情 -->
      <h3 class="font-semibold text-gray-800 truncate cursor-pointer hover:text-primary-600 transition-colors"
        @click="goToProduct">
        {{ product.name }}
      </h3>
      <!-- 商品单价 -->
      <p class="text-sm text-gray-500 mt-1">单价：¥{{ product.price.toFixed(2) }}</p>
      <!-- 库存不足提示 -->
      <p v-if="product.quantity <= 0" class="text-xs text-red-500 mt-1">已售罄</p>
      <p v-else-if="quantity >= product.quantity" class="text-xs text-orange-500 mt-1">已达库存上限</p>
    </div>

    <!-- 数量控制区域 -->
    <div class="flex items-center gap-2 shrink-0">
      <!-- 减少数量按钮，数量为1时禁用 -->
      <button @click="decrementQuantity"
        :disabled="quantity <= 1"
        class="w-8 h-8 flex items-center justify-center rounded-lg border transition-colors"
        :class="quantity <= 1
          ? 'border-gray-200 text-gray-300 cursor-not-allowed'
          : 'border-gray-300 text-gray-600 hover:bg-primary-50 hover:border-primary-300 hover:text-primary-600'">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M20 12H4" />
        </svg>
      </button>
      <!-- 当前数量显示 -->
      <span class="w-10 text-center font-medium text-gray-800">{{ quantity }}</span>
      <!-- 增加数量按钮，达到库存上限时禁用 -->
      <button @click="incrementQuantity"
        :disabled="quantity >= product.quantity || product.quantity <= 0"
        class="w-8 h-8 flex items-center justify-center rounded-lg border transition-colors"
        :class="(quantity >= product.quantity || product.quantity <= 0)
          ? 'border-gray-200 text-gray-300 cursor-not-allowed'
          : 'border-gray-300 text-gray-600 hover:bg-primary-50 hover:border-primary-300 hover:text-primary-600'">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
      </button>
    </div>

    <!-- 小计金额 -->
    <div class="w-24 text-right shrink-0">
      <span class="text-lg font-bold text-accent-600">¥{{ subtotal.toFixed(2) }}</span>
    </div>

    <!-- 删除按钮 -->
    <button @click="emit('remove')"
      class="shrink-0 p-2 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors"
      title="移除商品">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
/**
 * @file CartItemRow.vue
 * @description 购物车单行商品组件，展示商品图片、名称、单价、数量控制、小计金额和删除操作
 * @input product: Product 商品详情, quantity: number 购买数量
 * @output update:quantity 数量变更事件, remove 删除商品事件
 */
import { computed } from 'vue'
import type { Product } from '@/types/product'
import { useRouter } from 'vue-router'

/**
 * 购物车商品行组件
 * 职责：展示单个购物车商品的完整信息和交互操作
 */
const props = defineProps<{
  /** 商品详情 */
  product: Product
  /** 当前购买数量 */
  quantity: number
}>()

const emit = defineEmits<{
  /** 数量变更事件，携带新数量 */
  (e: 'update:quantity', quantity: number): void
  /** 删除商品事件 */
  (e: 'remove'): void
}>()

const router = useRouter()

// 计算小计金额 = 单价 x 数量
const subtotal = computed(() => props.product.price * props.quantity)

/**
 * 增加商品数量，不超过库存上限
 */
function incrementQuantity() {
  if (props.quantity < props.product.quantity) {
    emit('update:quantity', props.quantity + 1)
  }
}

/**
 * 减少商品数量，最小为1
 */
function decrementQuantity() {
  if (props.quantity > 1) {
    emit('update:quantity', props.quantity - 1)
  }
}

/**
 * 跳转到商品详情页
 */
function goToProduct() {
  router.push({ name: 'productView', params: { productId: props.product.id } })
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
</script>

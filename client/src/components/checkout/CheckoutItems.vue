/**
 * @file CheckoutItems.vue
 * @description 结算商品清单组件（只读），展示商品图片、名称、数量、小计及合计信息
 * @input items: 订单商品列表（OrderItem[]）
 * @output 无
 */
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- 标题 -->
    <h2 class="text-lg font-semibold text-gray-800 mb-4">商品清单</h2>

    <!-- 商品列表 -->
    <div class="space-y-4">
      <div
        v-for="item in items"
        :key="item.id"
        class="flex items-center gap-4 py-3 border-b border-gray-100 last:border-b-0"
      >
        <!-- 商品图片 -->
        <img
          :src="getItemImage(item)"
          :alt="item.product.name"
          class="w-16 h-16 object-cover rounded-md border border-gray-200 flex-shrink-0"
        />
        <!-- 商品信息 -->
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-800 truncate">{{ item.product.name }}</p>
          <p class="text-xs text-gray-500 mt-1">单价：¥{{ item.product.price.toFixed(2) }}</p>
        </div>
        <!-- 数量 -->
        <div class="text-sm text-gray-600 flex-shrink-0">
          x{{ item.amount }}
        </div>
        <!-- 小计 -->
        <div class="text-sm font-semibold text-gray-800 flex-shrink-0 w-24 text-right">
          ¥{{ item.price.toFixed(2) }}
        </div>
      </div>
    </div>

    <!-- 底部汇总 -->
    <div class="mt-4 pt-4 border-t border-gray-200 flex items-center justify-between">
      <!-- 返回购物车链接 -->
      <router-link
        to="/cart"
        class="text-sm text-blue-500 hover:text-blue-600 hover:underline transition-colors"
      >
        需要修改？返回购物车
      </router-link>
      <!-- 总件数和合计金额 -->
      <div class="text-right">
        <span class="text-sm text-gray-500">共 {{ totalAmount }} 件商品</span>
        <span class="ml-4 text-lg font-bold text-gray-800">合计：¥{{ totalPrice.toFixed(2) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 结算商品清单组件（只读）
 * 职责：展示结算时的商品列表、总件数和合计金额
 */
import { computed } from 'vue'
import type { OrderItem } from '@/types/order'

const props = defineProps<{
  /** 订单商品列表 */
  items: OrderItem[]
}>()

/**
 * 计算商品总件数
 */
const totalAmount = computed(() => {
  return props.items.reduce((sum, item) => sum + item.amount, 0)
})

/**
 * 计算合计金额
 */
const totalPrice = computed(() => {
  return props.items.reduce((sum, item) => sum + item.price, 0)
})

/**
 * 获取商品图片URL，无图片时返回占位图
 * @param {OrderItem} item - 订单项
 * @returns {string} 图片URL
 */
function getItemImage(item: OrderItem): string {
  // 优先使用商品的第一张图片，无图片时使用占位图
  if (item.product.imageUrls && item.product.imageUrls.length > 0) {
    return item.product.imageUrls[0]
  }
  return 'https://via.placeholder.com/64?text=No+Image'
}
</script>

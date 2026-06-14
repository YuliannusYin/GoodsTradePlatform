<template>
  <!-- 购物车汇总面板，展示商品总数、合计金额及操作按钮 -->
  <div class="bg-white rounded-xl shadow-md p-6 sticky top-4">
    <h3 class="text-lg font-bold text-gray-800 mb-4">订单汇总</h3>

    <!-- 汇总信息 -->
    <div class="space-y-3 mb-6">
      <div class="flex justify-between text-sm text-gray-600">
        <span>商品数量</span>
        <span>{{ totalQuantity }} 件</span>
      </div>
      <div class="border-t border-gray-100 pt-3 flex justify-between items-center">
        <span class="font-semibold text-gray-800">合计</span>
        <span class="text-2xl font-bold text-accent-600">¥{{ totalPrice.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 操作按钮区域 -->
    <div class="space-y-3">
      <!-- 去结算按钮，未登录时跳转登录页 -->
      <button @click="handleCheckout"
        :disabled="totalQuantity === 0"
        class="w-full py-3 rounded-lg font-medium text-white transition-colors"
        :class="totalQuantity === 0
          ? 'bg-gray-300 cursor-not-allowed'
          : 'bg-primary-500 hover:bg-primary-600'">
        去结算
      </button>
      <!-- 继续购物链接 -->
      <router-link to="/shop"
        class="block w-full py-2.5 rounded-lg font-medium text-center border border-primary-500 text-primary-500 hover:bg-primary-50 transition-colors">
        继续购物
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file CartSummary.vue
 * @description 购物车汇总面板组件，展示商品总数、合计金额，提供去结算和继续购物操作
 * @input totalPrice: number 合计金额, totalQuantity: number 商品总数量
 * @output 无（通过路由跳转实现导航）
 */
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/network/accountStore'

/**
 * 购物车汇总组件
 * 职责：展示订单汇总信息，处理结算和继续购物操作
 */
const props = defineProps<{
  /** 合计金额 */
  totalPrice: number
  /** 商品总数量 */
  totalQuantity: number
}>()

const router = useRouter()
const accountStore = useAccountStore()

/**
 * 处理去结算操作
 * 未登录用户跳转到登录页，已登录用户跳转到结算页
 */
function handleCheckout() {
  if (props.totalQuantity === 0) {
    // 购物车为空时不执行跳转
    return
  }
  if (!accountStore.isAuthenticated) {
    // 未登录时跳转到登录页
    router.push('/login')
  } else {
    // 已登录时跳转到结算页
    router.push('/checkout')
  }
}
</script>

/**
 * @file OrderSuccess.vue
 * @description 下单成功页面组件，展示下单成功提示、订单信息和操作按钮
 * @input orderId: 订单ID, totalPrice: 支付金额, remainingBalance: 剩余余额
 * @output 无
 */
<template>
  <div class="flex flex-col items-center justify-center py-12">
    <!-- 成功图标：大勾号 -->
    <div class="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center mb-6">
      <svg class="w-12 h-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2.5"
          d="M5 13l4 4L19 7"
        />
      </svg>
    </div>

    <!-- 成功标题 -->
    <h2 class="text-2xl font-bold text-gray-800 mb-6">下单成功！</h2>

    <!-- 订单信息卡片 -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 w-full max-w-md mb-8">
      <div class="space-y-3 text-sm">
        <!-- 订单号 -->
        <div class="flex justify-between">
          <span class="text-gray-500">订单号</span>
          <span class="text-gray-800 font-medium">{{ orderId }}</span>
        </div>
        <!-- 支付方式 -->
        <div class="flex justify-between">
          <span class="text-gray-500">支付方式</span>
          <span class="text-gray-800 font-medium">账户余额</span>
        </div>
        <!-- 支付金额 -->
        <div class="flex justify-between">
          <span class="text-gray-500">支付金额</span>
          <span class="text-gray-800 font-semibold">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <!-- 当前余额 -->
        <div class="flex justify-between pt-2 border-t border-gray-100">
          <span class="text-gray-500">当前余额</span>
          <span class="text-green-600 font-semibold">¥{{ remainingBalance.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="flex gap-4">
      <!-- 查看订单按钮 -->
      <router-link
        to="/account/orders"
        class="px-6 py-2.5 bg-blue-500 hover:bg-blue-600 text-white rounded-md text-sm font-medium transition-colors duration-200"
      >
        查看订单
      </router-link>
      <!-- 继续购物按钮 -->
      <router-link
        to="/shop"
        class="px-6 py-2.5 border border-gray-300 hover:border-gray-400 text-gray-700 rounded-md text-sm font-medium transition-colors duration-200"
      >
        继续购物
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 下单成功页面组件
 * 职责：展示下单成功提示、订单摘要信息和后续操作入口
 */

defineProps<{
  /** 订单ID */
  orderId: string
  /** 支付金额 */
  totalPrice: number
  /** 剩余余额 */
  remainingBalance: number
}>()
</script>

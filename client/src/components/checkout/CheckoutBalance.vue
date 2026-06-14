/**
 * @file CheckoutBalance.vue
 * @description 余额支付信息组件，显示账户余额、应付金额及余额是否充足的状态提示
 * @input totalPrice: 应付总金额
 * @output 无
 */
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- 标题 -->
    <h2 class="text-lg font-semibold text-gray-800 mb-4">支付信息</h2>

    <div class="space-y-3">
      <!-- 支付方式 -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600">支付方式</span>
        <span class="text-sm font-medium text-gray-800">账户余额支付</span>
      </div>

      <!-- 账户余额 -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600">账户余额</span>
        <span class="text-sm font-semibold text-gray-800">¥{{ balance.toFixed(2) }}</span>
      </div>

      <!-- 应付金额 -->
      <div class="flex items-center justify-between">
        <span class="text-sm text-gray-600">应付金额</span>
        <span class="text-sm font-semibold text-gray-800">¥{{ totalPrice.toFixed(2) }}</span>
      </div>

      <!-- 余额状态提示 -->
      <div class="pt-3 border-t border-gray-200">
        <div v-if="isBalanceSufficient" class="flex items-center gap-2">
          <!-- 余额充足图标 -->
          <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span class="text-sm font-medium text-green-600">余额充足</span>
        </div>
        <div v-else class="flex items-center gap-2">
          <!-- 余额不足图标 -->
          <svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span class="text-sm font-medium text-red-600">余额不足，请联系管理员充值</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 余额支付信息组件
 * 职责：展示账户余额、应付金额及余额充足状态
 */
import { computed } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'

const props = defineProps<{
  /** 应付总金额 */
  totalPrice: number
}>()

const accountStore = useAccountStore()

/** 当前账户余额，安全访问避免 user 为 null 的情况 */
const balance = computed(() => {
  return accountStore.balance ?? 0
})

/** 余额是否充足 */
const isBalanceSufficient = computed(() => {
  return balance.value >= props.totalPrice
})
</script>

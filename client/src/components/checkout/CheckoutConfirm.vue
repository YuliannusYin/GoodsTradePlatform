/**
 * @file CheckoutConfirm.vue
 * @description 订单确认汇总组件，展示所有订单信息并提供确认下单按钮
 * @input items: 订单商品列表, address: 收货地址, deliveryMethod: 配送方式, totalPrice: 总价, balance: 账户余额
 * @output confirm: 用户点击确认下单时触发
 */
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- 标题 -->
    <h2 class="text-lg font-semibold text-gray-800 mb-6">确认订单</h2>

    <!-- 收货信息汇总 -->
    <div class="mb-6 pb-4 border-b border-gray-200">
      <h3 class="text-sm font-semibold text-gray-700 mb-2">收货信息</h3>
      <div class="text-sm text-gray-600 space-y-1">
        <p>收货人：{{ address.receiverName }}</p>
        <p>联系电话：{{ address.receiverPhone }}</p>
        <p>收货地址：{{ address.region }} {{ address.detailAddress }}</p>
        <p>配送方式：{{ deliveryMethodLabel }}</p>
      </div>
    </div>

    <!-- 商品信息汇总 -->
    <div class="mb-6 pb-4 border-b border-gray-200">
      <h3 class="text-sm font-semibold text-gray-700 mb-3">商品信息</h3>
      <div class="space-y-2">
        <div
          v-for="item in items"
          :key="item.id"
          class="flex items-center justify-between text-sm"
        >
          <div class="flex items-center gap-2 flex-1 min-w-0">
            <img
              :src="getItemImage(item)"
              :alt="item.product.name"
              class="w-10 h-10 object-cover rounded border border-gray-200 flex-shrink-0"
            />
            <span class="text-gray-700 truncate">{{ item.product.name }}</span>
            <span class="text-gray-400 flex-shrink-0">x{{ item.amount }}</span>
          </div>
          <span class="text-gray-800 font-medium flex-shrink-0 ml-4">¥{{ item.price.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 支付信息汇总 -->
    <div class="mb-6 pb-4 border-b border-gray-200">
      <h3 class="text-sm font-semibold text-gray-700 mb-2">支付信息</h3>
      <div class="text-sm text-gray-600 space-y-1">
        <p>支付方式：账户余额</p>
        <p>应付金额：<span class="font-semibold text-gray-800">¥{{ totalPrice.toFixed(2) }}</span></p>
        <p>当前余额：<span class="font-semibold text-gray-800">¥{{ balance.toFixed(2) }}</span></p>
        <!-- 下单后余额预览 -->
        <p class="pt-1">
          下单后余额：
          <span
            class="font-semibold"
            :class="remainingBalance >= 0 ? 'text-green-600' : 'text-red-600'"
          >
            ¥{{ remainingBalance.toFixed(2) }}
          </span>
        </p>
      </div>
    </div>

    <!-- 确认下单按钮 -->
    <div class="flex justify-end">
      <button
        class="px-8 py-3 rounded-md text-white font-medium transition-colors duration-200"
        :class="isBalanceSufficient
          ? 'bg-blue-500 hover:bg-blue-600'
          : 'bg-gray-400 cursor-not-allowed'"
        :disabled="!isBalanceSufficient"
        @click="emit('confirm')"
      >
        确认下单
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 订单确认汇总组件
 * 职责：展示订单所有信息汇总，提供确认下单入口
 */
import { computed } from 'vue'
import type { OrderItem } from '@/types/order'
import type { AddressInfo } from '@/types/order'
import { DELIVERY_METHODS } from '@/types/order'

const props = defineProps<{
  /** 订单商品列表 */
  items: OrderItem[]
  /** 收货地址信息 */
  address: AddressInfo
  /** 配送方式枚举值 */
  deliveryMethod: string
  /** 应付总金额 */
  totalPrice: number
  /** 账户余额 */
  balance: number
}>()

const emit = defineEmits<{
  /** 用户点击确认下单时触发 */
  confirm: []
}>()

/** 配送方式的中文显示名称 */
const deliveryMethodLabel = computed(() => {
  return DELIVERY_METHODS[props.deliveryMethod] || props.deliveryMethod
})

/** 下单后剩余余额 */
const remainingBalance = computed(() => {
  return props.balance - props.totalPrice
})

/** 余额是否充足 */
const isBalanceSufficient = computed(() => {
  return props.balance >= props.totalPrice
})

/**
 * 获取商品图片URL，无图片时返回占位图
 * @param {OrderItem} item - 订单项
 * @returns {string} 图片URL
 */
function getItemImage(item: OrderItem): string {
  if (item.product.imageUrls && item.product.imageUrls.length > 0) {
    return item.product.imageUrls[0]
  }
  return 'https://via.placeholder.com/40?text=No+Image'
}
</script>

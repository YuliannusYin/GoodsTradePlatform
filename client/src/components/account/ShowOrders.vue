<template>
  <div class="p-4 bg-white rounded shadow">
    <h2 class="text-xl font-semibold mb-4">我的订单</h2>
    <ul>
      <li v-for="order in orders" :key="order.received" class="mb-4 p-4 border rounded">
        <div class="text-sm text-gray-500 border-b pb-3">
          状态：{{ order.status }} | 下单时间：{{ order.received }}
        </div>
        <div class="flex justify-between">
          <div>
            <div v-for="item in order.items" :key="item.product.id">
              <div class="mb-1 py-2">
                <img v-if="item.product.imageUrls && item.product.imageUrls.length > 0"
                  :src="item.product.imageUrls[0]" alt="商品图片" class="w-8 h-8 inline-block mr-2"
                  @error="(e: Event) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2232%22 height=%2232%22 viewBox=%220 0 32 32%22><rect fill=%22%23f3f4f6%22 width=%2232%22 height=%2232%22/></svg>') }" />
                <div v-else class="w-8 h-8 bg-gray-100 inline-block mr-2 rounded"></div>
                <span class="font-bold">{{ item.product.name }}</span>
                - {{ item.product.price }}
                <span v-if="item.amount > 1" class="font-semibold text-blue-700"> x {{ item.amount }}</span>
              </div>
            </div>
          </div>
          <div class="font-bold">{{ order.price }}</div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ShowOrders.vue
 * @description 用户订单列表展示组件，加载并显示当前用户的所有已下单订单
 */
import { onMounted, ref } from 'vue'
import { useOrderStore } from '@/stores/network/orderStore'
import type { PlacedOrder } from '@/types/order'

const orderStore = useOrderStore()
// 用户已下单的订单列表
const orders = ref<PlacedOrder[]>([])

// 组件挂载时获取用户已下单的订单数据
onMounted(async () => {
  orders.value = await orderStore.getPlacedOrders()
})
</script>

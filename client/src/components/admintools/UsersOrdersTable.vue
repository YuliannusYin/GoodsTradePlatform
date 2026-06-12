<template>
  <section>
    <SmallViewTitle :text="generateHeader()" class="mb-2" />
    <table class="border max-w-full w-full">
      <thead class="text-left text-xs whitespace-nowrap">
        <tr class="bg-gray-200">
          <th class="border p-2 hidden md:table-cell">用户邮箱</th>
          <th class="border p-2 hidden md:table-cell">价格</th>
          <th class="border p-2">状态</th>
          <th class="border p-2">下单时间</th>
          <th class="border p-2">预计送达</th>
          <th class="border p-2 hidden md:table-cell">商品列表</th>
        </tr>
      </thead>
      <tbody class="bg-gray-50 whitespace-normal text-s md:text-base">
        <tr v-for="order in orders" :key="order.id" class="cursor-pointer" :class="{
          'bg-white border-2': order === selectedOrder,
          'hover:bg-white': order !== selectedOrder
        }" @click="() => showUserOrderAside(order)">
          <td class="border p-2 hidden md:table-cell">{{ order.userEmail }}</td>
          <td class="border p-2 hidden md:table-cell">{{ order.price }}</td>
          <td class="border p-2 text-left">
            <span :class="['py-1 px-1 mr-2 rounded-full',
              order.status === 'PENDING' ? 'bg-yellow-300' : 'bg-green-300']"></span>
            {{ order.status }}
          </td>
          <td class="border p-2">{{ formatDateTime(order.received) }}</td>
          <td v-if="order.expectedDelivery" class="border p-2">{{ formatDateTime(order.expectedDelivery) }}</td>
          <td v-else class="border p-2 text-left">
            <span class="bg-yellow-300 py-1 px-1 mr-2 rounded-full"></span>
            <span>暂无</span>
          </td>
          <td class="border p-2 hidden md:table-cell">
            <div v-for="item in order.items" :key="item.product.id">
              <div class="py-2 flex items-center justify-start">
                <img :src="item.product.imageUrls?.[0]" class="w-8 h-8 mr-2 hidden sm:inline-block" />
                <div class="font-bold text-xs hidden lg:table-cell mr-2">{{ item.product.name }}</div>
                <div v-if="item.amount > 1" class="font-semibold text-blue-700 text-xs"> x {{ item.amount }}</div>
              </div>
            </div>
          </td>
        </tr>
      </tbody>
      <UserOrderAside v-if="selectedOrder" :order="selectedOrder" :onSend="sendOrder" :onUpdate="changeExpectedDelivery"
        :onClose="hideUserOrderAside" />
    </table>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import UserOrderAside from '@/components/admintools/UserOrderAside.vue'
import { OrderStatus, orderStatusToString, type UserOrder } from '@/types/order'
import { useRoute } from 'vue-router'
import SmallViewTitle from '../SmallViewTitle.vue'

const adminToolsStore = useAdminToolsStore()
const orders = ref<UserOrder[]>([])
const selectedOrder = ref<UserOrder | null>(null)
const route = useRoute()

async function getAllOrders() {
  orders.value = await adminToolsStore.getAllOrders()
}

async function getAllOrdersByStatus(status: OrderStatus) {
  orders.value = await adminToolsStore.getAllOrdersWithStatus(orderStatusToString(status))
}

function generateHeader(): string {
  if (route.name === 'PendingOrders') return '待发货订单'
  else if (route.name === 'SentOrders') return '已发货订单'
  else return '全部订单'
}

async function loadOrders() {
  if (route.name === 'AllOrders') getAllOrders()
  else if (route.name === 'PendingOrders') getAllOrdersByStatus(OrderStatus.PENDING)
  else if (route.name === 'SentOrders') getAllOrdersByStatus(OrderStatus.SHIPPED)
}

onMounted(() => {
  loadOrders()
})

watch(() => route.name, () => {
  loadOrders()
  hideUserOrderAside()
})

function showUserOrderAside(order: UserOrder) {
  selectedOrder.value = order
}

async function hideUserOrderAside() {
  selectedOrder.value = null
}

async function sendOrder(orderId: string, expectedDelivery: string) {
  await adminToolsStore.sendOrder(orderId, expectedDelivery)
  loadOrders()
  hideUserOrderAside()
}

async function changeExpectedDelivery(orderId: string, newExpectedDelivery: string) {
  await adminToolsStore.changeExpectedDelivery(orderId, newExpectedDelivery)
  loadOrders()
  hideUserOrderAside()
}

function formatDateTime(dateTime: string) {
  return formatDate(dateTime) + " - " + formatTime(dateTime)
}

function formatDate(dateTime: string) {
  return dateTime.split('T')[0]
}

function formatTime(dateTime: string) {
  return dateTime.split('T')[1].slice(0, 5)
}
</script>

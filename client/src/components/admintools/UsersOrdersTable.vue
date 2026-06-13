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
                <img v-if="item.product.imageUrls && item.product.imageUrls.length > 0"
                  :src="item.product.imageUrls[0]" class="w-8 h-8 mr-2 hidden sm:inline-block"
                  @error="(e: Event) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2232%22 height=%2232%22 viewBox=%220 0 32 32%22><rect fill=%22%23f3f4f6%22 width=%2232%22 height=%2232%22/></svg>') }" />
                <div v-else class="w-8 h-8 bg-gray-100 mr-2 hidden sm:inline-block rounded"></div>
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
/**
 * @file UsersOrdersTable.vue
 * @description 用户订单管理表格组件，管理员可查看、筛选订单并执行发货或修改送达时间操作
 */
import { onMounted, ref, watch } from 'vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import UserOrderAside from '@/components/admintools/UserOrderAside.vue'
import { OrderStatus, orderStatusToString, type UserOrder } from '@/types/order'
import { useRoute } from 'vue-router'
import SmallViewTitle from '../SmallViewTitle.vue'

const adminToolsStore = useAdminToolsStore()
// 订单列表数据
const orders = ref<UserOrder[]>([])
// 当前选中的订单（用于侧边栏展示）
const selectedOrder = ref<UserOrder | null>(null)
const route = useRoute()

// 获取所有订单
async function getAllOrders() {
  orders.value = await adminToolsStore.getAllOrders()
}

/**
 * 根据订单状态获取订单列表
 * @param {OrderStatus} status - 订单状态枚举值
 */
async function getAllOrdersByStatus(status: OrderStatus) {
  orders.value = await adminToolsStore.getAllOrdersWithStatus(orderStatusToString(status))
}

/**
 * 根据当前路由生成表格标题
 * @returns {string} 表格标题文本
 */
function generateHeader(): string {
  if (route.name === 'PendingOrders') return '待发货订单'
  else if (route.name === 'SentOrders') return '已发货订单'
  else return '全部订单'
}

/**
 * 根据当前路由加载对应的订单数据
 */
async function loadOrders() {
  if (route.name === 'AllOrders') getAllOrders()
  else if (route.name === 'PendingOrders') getAllOrdersByStatus(OrderStatus.PENDING)
  else if (route.name === 'SentOrders') getAllOrdersByStatus(OrderStatus.SHIPPED)
}

// 组件挂载时加载订单
onMounted(() => {
  loadOrders()
})

// 监听路由变化，重新加载订单并关闭侧边栏
watch(() => route.name, () => {
  loadOrders()
  hideUserOrderAside()
})

// 显示订单详情侧边栏
function showUserOrderAside(order: UserOrder) {
  selectedOrder.value = order
}

// 隐藏订单详情侧边栏
async function hideUserOrderAside() {
  selectedOrder.value = null
}

/**
 * 发货操作，发送订单后刷新列表
 * @param {string} orderId - 订单ID
 * @param {string} expectedDelivery - 预计送达时间
 */
async function sendOrder(orderId: string, expectedDelivery: string) {
  await adminToolsStore.sendOrder(orderId, expectedDelivery)
  loadOrders()
  hideUserOrderAside()
}

/**
 * 修改预计送达时间后刷新列表
 * @param {string} orderId - 订单ID
 * @param {string} newExpectedDelivery - 新的预计送达时间
 */
async function changeExpectedDelivery(orderId: string, newExpectedDelivery: string) {
  await adminToolsStore.changeExpectedDelivery(orderId, newExpectedDelivery)
  loadOrders()
  hideUserOrderAside()
}

/**
 * 格式化日期时间字符串为可读格式
 * @param {string} dateTime - ISO 格式日期时间字符串
 * @returns {string} 格式化后的日期时间文本
 */
function formatDateTime(dateTime: string) {
  return formatDate(dateTime) + " - " + formatTime(dateTime)
}

// 提取日期部分
function formatDate(dateTime: string) {
  return dateTime.split('T')[0]
}

// 提取时间部分（时:分）
function formatTime(dateTime: string) {
  return dateTime.split('T')[1].slice(0, 5)
}
</script>

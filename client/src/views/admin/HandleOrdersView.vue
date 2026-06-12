<template>
  <section>
    <div class="flex space-x-8 border-b p-2 sm:p-4 justify-center sm:justify-start">
      <router-link :to="{ name: 'PendingOrders' }" class="text-black hover:text-gray-500 font-semibold"
        :class="{ 'text-blue-600': isOnPendingOrdersRoute, 'bg-white': !isOnPendingOrdersRoute }">
        待发货
      </router-link>
      <p class="text-gray-400">|</p>
      <router-link :to="{ name: 'SentOrders' }" class="text-black hover:text-gray-500 font-semibold"
        :class="{ 'text-blue-600': isOnSentOrdersRoute, 'bg-white': !isOnSentOrdersRoute }">
        已发货
      </router-link>
      <p class="text-gray-400">|</p>
      <router-link :to="{ name: 'AllOrders' }" class="text-black hover:text-gray-500 font-semibold"
        :class="{ 'text-blue-600': isOnAllOrdersRoute, 'bg-white': !isOnAllOrdersRoute }">
        全部
      </router-link>
    </div>

    <RouterView class="overflow-x-scroll p-4" />
  </section>
</template>

<script setup lang="ts">
/**
 * @file HandleOrdersView.vue
 * @description 订单管理视图，提供待发货、已发货、全部订单的标签页导航及子路由展示
 */
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 各标签页的高亮状态
const isOnPendingOrdersRoute = ref(false) // 是否处于待发货路由
const isOnSentOrdersRoute = ref(false)    // 是否处于已发货路由
const isOnAllOrdersRoute = ref(false)     // 是否处于全部订单路由

/**
 * 根据当前路由名称更新标签页的高亮状态
 */
function assignHighlightedButton() {
  isOnPendingOrdersRoute.value = ['PendingOrders'].includes(route.name as string)
  isOnSentOrdersRoute.value = ['SentOrders'].includes(route.name as string)
  isOnAllOrdersRoute.value = ['AllOrders'].includes(route.name as string)
}

// 组件挂载时初始化高亮状态
onMounted(() => {
  assignHighlightedButton()
})

// 路由变化时更新高亮状态
watch(() => route.name, () => {
  assignHighlightedButton()
})
</script>

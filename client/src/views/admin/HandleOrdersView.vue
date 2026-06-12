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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const isOnPendingOrdersRoute = ref(false)
const isOnSentOrdersRoute = ref(false)
const isOnAllOrdersRoute = ref(false)

function assignHighlightedButton() {
  isOnPendingOrdersRoute.value = ['PendingOrders'].includes(route.name as string)
  isOnSentOrdersRoute.value = ['SentOrders'].includes(route.name as string)
  isOnAllOrdersRoute.value = ['AllOrders'].includes(route.name as string)
}

onMounted(() => {
  assignHighlightedButton()
})

watch(() => route.name, () => {
  assignHighlightedButton()
})
</script>

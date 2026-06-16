<template>
  <section class="px-0 py-0 lg:p-10">
    <div class="bg-white rounded-md sm:flex sm:p-4">
      <div class="flex justify-evenly sm:flex-col sm:justify-start sm:items-center sm:mr-4 sm:space-y-4">
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnProductsRoute, 'bg-white': !isOnProductsRoute }">
          <i class="fas fa-cube"></i>
          <router-link :to="{ name: 'HandleProductsView' }" class="text-black font-semibold">
            商品管理
          </router-link>
        </button>
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnOrdersRoute, 'bg-white': !isOnOrdersRoute }">
          <i class="fas fa-truck sm:ml-[-0.6rem]"></i>
          <router-link :to="{ name: 'HandleOrdersView' }" class="text-black ml-1 font-semibold">
            订单管理
          </router-link>
        </button>
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnUsersRoute, 'bg-white': !isOnUsersRoute }">
          <i class="fas fa-users"></i>
          <router-link :to="{ name: 'UserManagementView' }" class="text-black font-semibold">
            用户管理
          </router-link>
        </button>
        <button class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnReviewsRoute, 'bg-white': !isOnReviewsRoute }">
          <i class="fas fa-clipboard-check"></i>
          <router-link :to="{ name: 'ProductReviewView' }" class="text-black font-semibold">
            商品审核
          </router-link>
        </button>
        <button v-if="isSuperAdmin" class="border p-2 w-full min-w-max sm:p-4 hover:bg-blue-50 hover:text-blue-600"
          :class="{ 'bg-blue-50 text-blue-600': isOnCommissionRoute, 'bg-white': !isOnCommissionRoute }">
          <i class="fas fa-percent"></i>
          <router-link :to="{ name: 'CommissionConfigView' }" class="text-black font-semibold">
            佣金配置
          </router-link>
        </button>
      </div>
      <RouterView class="w-full border" />
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file AdminToolsView.vue
 * @description 管理员工具主视图，提供商品管理、订单管理、用户管理、商品审核、佣金配置的侧边栏导航及子路由展示
 */
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 是否为超级管理员
const isSuperAdmin = computed(() => sessionStorage.getItem('userRole') === 'SUPER_ADMIN')

// 各导航按钮的高亮状态
const isOnProductsRoute = ref(false) // 是否处于商品管理路由
const isOnOrdersRoute = ref(false)   // 是否处于订单管理路由
const isOnUsersRoute = ref(false)    // 是否处于用户管理路由
const isOnReviewsRoute = ref(false)  // 是否处于商品审核路由
const isOnCommissionRoute = ref(false) // 是否处于佣金配置路由

/**
 * 根据当前路由名称更新侧边栏按钮的高亮状态
 */
function assignHighlightedButton() {
  // 商品管理路由高亮（已简化为单一路由，不再有 add/edit/delete 子路由）
  isOnProductsRoute.value = ['HandleProductsView'].includes(route.name as string)
  // 订单管理相关路由高亮
  isOnOrdersRoute.value = ['PendingOrders', 'SentOrders', 'AllOrders', 'HandleOrdersView'].includes(route.name as string)
  // 用户管理路由高亮
  isOnUsersRoute.value = ['UserManagementView'].includes(route.name as string)
  // 商品审核路由高亮
  isOnReviewsRoute.value = ['ProductReviewView'].includes(route.name as string)
  // 佣金配置路由高亮
  isOnCommissionRoute.value = ['CommissionConfigView'].includes(route.name as string)
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

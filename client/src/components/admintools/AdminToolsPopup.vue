<template>
  <div class="hidden sm:block absolute pt-2">
    <div class="flex flex-col justify-center items-center bg-white p-4 shadow-md rounded-md">
      <div class="text-left space-y-2 flex flex-col">
        <router-link :to="{ name: 'HandleProductsView' }" class="text-blue-700 hover:text-blue-500">
          管理商品
        </router-link>
        <router-link :to="{ name: 'HandleOrdersView' }" class="text-blue-700 hover:text-blue-500">
          管理订单
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file AdminToolsPopup.vue
 * @description 管理工具弹出菜单组件，提供管理商品和管理订单的快捷链接
 */
</script>

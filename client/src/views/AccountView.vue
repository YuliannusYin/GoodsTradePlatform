<template>
  <section>
    <div class="bg-white rounded-md shadow-sm font-semibold space-x-4 p-4 mb-1">
      <div class="flex justify-start items-center space-x-4 text-l">
        <router-link :to="{ name: 'EditAccountView' }" class="text-blue-700 hover:text-blue-500">
          编辑我的信息
        </router-link>
        <router-link :to="{ name: 'ShowAccountOrdersView' }" class="text-blue-700 hover:text-blue-500">
          查看我的订单
        </router-link>
        <router-link :to="{ name: 'MyProductsView' }" class="text-blue-700 hover:text-blue-500">
          我的商品
        </router-link>
      </div>
    </div>
    <RouterView class="p-4" />
  </section>
</template>

<script setup lang="ts">
/**
 * @file AccountView.vue
 * @description 账户视图，提供编辑信息、查看订单、我的商品的导航入口及子路由展示
 */
</script>

<template>
  <section>
    <div class="flex justify-center sm:justify-start space-x-8 border-b p-2 sm:p-4">
      <router-link :to="{ name: 'AddProduct' }" class="text-black hover:text-gray-500 font-semibold"
        :class="{ 'text-blue-600': isOnAddProductRoute, 'bg-white': !isOnAddProductRoute }">
        添加
      </router-link>
      <p class="text-gray-400">|</p>
      <router-link :to="{ name: 'EditProduct' }" class="text-black hover:text-gray-500 font-semibold"
        :class="{ 'text-blue-600': isOnEditProductRoute, 'bg-white': !isOnEditProductRoute }">
        编辑
      </router-link>
      <p class="text-gray-400">|</p>
      <router-link :to="{ name: 'DeleteProduct' }" class="text-black hover:text-gray-500 font-semibold"
        :class="{ 'text-blue-600': isOnDeleteProductRoute, 'bg-white': !isOnDeleteProductRoute }">
        删除
      </router-link>
    </div>

    <RouterView class="sm:p-4" />
  </section>
</template>

<script setup lang="ts">
/**
 * @file HandleProductsView.vue
 * @description 商品管理视图，提供添加、编辑、删除商品的标签页导航及子路由展示
 */
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 各标签页的高亮状态
const isOnAddProductRoute = ref(false)    // 是否处于添加商品路由
const isOnEditProductRoute = ref(false)   // 是否处于编辑商品路由
const isOnDeleteProductRoute = ref(false) // 是否处于删除商品路由

/**
 * 根据当前路由名称更新标签页的高亮状态
 */
function assignHighlightedButton() {
  isOnAddProductRoute.value = ['AddProduct'].includes(route.name as string)
  isOnEditProductRoute.value = ['EditProduct'].includes(route.name as string)
  isOnDeleteProductRoute.value = ['DeleteProduct'].includes(route.name as string)
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

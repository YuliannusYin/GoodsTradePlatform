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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const isOnAddProductRoute = ref(false)
const isOnEditProductRoute = ref(false)
const isOnDeleteProductRoute = ref(false)

function assignHighlightedButton() {
  isOnAddProductRoute.value = ['AddProduct'].includes(route.name as string)
  isOnEditProductRoute.value = ['EditProduct'].includes(route.name as string)
  isOnDeleteProductRoute.value = ['DeleteProduct'].includes(route.name as string)
}

onMounted(() => {
  assignHighlightedButton()
})

watch(() => route.name, () => {
  assignHighlightedButton()
})
</script>

<template>
  <section class="max-w-3xl mx-auto px-4 py-8">
    <ProductPublishForm @published="handlePublished" />

    <div class="mt-8">
      <MyProductsList ref="myProductsListRef" />
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file PublishProductView.vue
 * @description 发布商品视图，组合商品发布表单和已发布商品列表两个子组件
 */
import { ref } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'
import { useRouter } from 'vue-router'
import ProductPublishForm from '@/components/products/ProductPublishForm.vue'
import MyProductsList from '@/components/products/MyProductsList.vue'

const accountStore = useAccountStore()    // 账户状态管理
const router = useRouter()

// 已发布商品列表组件引用，用于发布成功后触发刷新
const myProductsListRef = ref<InstanceType<typeof MyProductsList> | null>(null)

// 未登录则跳转登录页
if (!accountStore.isAuthenticated) {
  router.push('/login')
}

/**
 * 商品发布成功后的回调，刷新已发布商品列表
 */
function handlePublished() {
  myProductsListRef.value?.loadMyProducts()
}
</script>

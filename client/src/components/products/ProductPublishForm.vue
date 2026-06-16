<template>
  <div class="bg-white rounded-2xl shadow-md p-8">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">发布闲置周边</h2>
    <form @submit.prevent="handleSubmit" class="space-y-5">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">商品名称</label>
        <input v-model="form.name" type="text" required
          class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none"
          placeholder="例如：初音未来 手办" />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">商品描述</label>
        <textarea v-model="form.description" rows="4" required
          class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none resize-none"
          placeholder="详细描述商品的情况..."></textarea>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">图片链接（每行一个）</label>
        <textarea v-model="imageUrlsText" rows="3"
          class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none resize-none"
          placeholder="https://example.com/image1.jpg"></textarea>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">价格（元）</label>
          <input v-model.number="form.price" type="number" step="0.01" min="0" required
            class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">数量</label>
          <input v-model.number="form.quantity" type="number" min="1" required
            class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none" />
        </div>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">分类</label>
          <select v-model="form.category" required
            class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none">
            <option value="" disabled>请选择分类</option>
            <option v-for="(label, key) in PRODUCT_CATEGORIES" :key="key" :value="key">
              {{ label }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">成色</label>
          <select v-model="form.condition" required
            class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-300 focus:border-primary-300 outline-none">
            <option v-for="(label, key) in PRODUCT_CONDITIONS" :key="key" :value="key">
              {{ label }}
            </option>
          </select>
        </div>
      </div>
      <div v-if="errorMessage" class="text-red-500 text-sm">{{ errorMessage }}</div>
      <div v-if="successMessage" class="text-mint-600 text-sm">{{ successMessage }}</div>
      <button type="submit" class="btn-primary w-full" :disabled="isSubmitting">
        {{ isSubmitting ? '发布中...' : '发布商品' }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ProductPublishForm.vue
 * @description 商品发布表单组件，提供商品信息填写和提交功能
 * 职责单一：仅负责商品发布表单的展示与提交逻辑
 */
import { ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import { useAccountStore } from '@/stores/network/accountStore'
import type { CreateProductDto } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS } from '@/types/product'
import { useRouter } from 'vue-router'

const productStore = useProductStore()    // 商品状态管理
const accountStore = useAccountStore()    // 账户状态管理
const router = useRouter()

// 定义事件：发布成功后通知父组件刷新列表
const emit = defineEmits<{
  published: []
}>()

const isSubmitting = ref(false)            // 提交中状态
const errorMessage = ref('')               // 错误提示信息
const successMessage = ref('')             // 成功提示信息
const imageUrlsText = ref('')              // 图片链接文本（每行一个）

// 商品发布表单数据
const form = ref<CreateProductDto>({
  name: '',
  description: '',
  imageUrls: [],
  price: 0,
  quantity: 1,
  category: '',
  condition: 'NEW',
  source: 'USER'
})

/**
 * 处理商品发布提交，验证登录状态后提交表单数据
 */
async function handleSubmit() {
  // 未登录则跳转登录页
  if (!accountStore.isAuthenticated) {
    router.push('/login')
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    // 将换行分隔的图片文本转为数组
    form.value.imageUrls = imageUrlsText.value
      .split('\n')
      .map(url => url.trim())
      .filter(url => url.length > 0)

    // 若无图片则使用占位图
    if (form.value.imageUrls.length === 0) {
      form.value.imageUrls = ['https://via.placeholder.com/300x300?text=No+Image']
    }

    await productStore.addMyProduct(form.value)
    successMessage.value = '商品发布成功！'
    // 重置表单
    form.value = {
      name: '',
      description: '',
      imageUrls: [],
      price: 0,
      quantity: 1,
      category: '',
      condition: 'NEW',
      source: 'USER'
    }
    imageUrlsText.value = ''
    // 通知父组件刷新商品列表
    emit('published')
  } catch (error: unknown) {
    errorMessage.value = '发布失败，请重试'
  } finally {
    isSubmitting.value = false
  }
}
</script>

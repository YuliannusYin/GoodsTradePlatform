<template>
  <section class="max-w-3xl mx-auto px-4 py-8">
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

    <div class="mt-8">
      <h3 class="text-xl font-bold text-gray-800 mb-4">我发布的商品</h3>
      <div v-if="myProducts.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="product in myProducts" :key="product.id"
          class="bg-white rounded-xl shadow-md p-4 flex gap-4">
          <img v-if="product.imageUrls && product.imageUrls.length > 0"
            :src="product.imageUrls[0]" :alt="product.name"
            class="w-20 h-20 object-contain rounded-lg"
            @error="(e: Event) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2280%22 height=%2280%22 viewBox=%220 0 80 80%22><rect fill=%22%23f3f4f6%22 width=%2280%22 height=%2280%22/><text x=%2240%22 y=%2245%22 text-anchor=%22middle%22 fill=%22%239ca3af%22 font-size=%2210%22>无图</text></svg>') }">
          <div v-else class="w-20 h-20 bg-gray-100 rounded-lg flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
            </svg>
          </div>
          <div class="flex-1">
            <h4 class="font-semibold text-gray-800">{{ product.name }}</h4>
            <span class="text-accent-600 font-bold">¥{{ product.price.toFixed(2) }}</span>
            <p class="text-xs text-gray-500 mt-1">库存：{{ product.quantity }}</p>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-8 text-gray-400">
        你还没有发布过商品
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file PublishProductView.vue
 * @description 发布商品视图，提供商品信息填写表单及已发布商品列表展示
 */
import { onMounted, ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import { useAccountStore } from '@/stores/network/accountStore'
import type { Product, CreateProductDto } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS } from '@/types/product'
import { useRouter } from 'vue-router'

const productStore = useProductStore()    // 商品状态管理
const accountStore = useAccountStore()    // 账户状态管理
const router = useRouter()
const myProducts = ref<Product[]>([])     // 用户已发布的商品列表
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
 * 加载当前用户已发布的商品列表
 */
async function loadMyProducts() {
  try {
    myProducts.value = await productStore.getMyProducts()
  } catch (error) {
    // 错误已由拦截器处理
  }
}

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
    await loadMyProducts()
  } catch (error: unknown) {
    errorMessage.value = '发布失败，请重试'
  } finally {
    isSubmitting.value = false
  }
}

// 组件挂载时检查登录状态并加载商品列表
onMounted(() => {
  if (!accountStore.isAuthenticated) {
    router.push('/login')
    return
  }
  loadMyProducts()
})
</script>

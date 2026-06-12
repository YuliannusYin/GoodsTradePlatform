<template>
  <section class="max-w-4xl mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">我的商品</h2>
      <router-link :to="{ name: 'publishProduct' }"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm font-medium">
        <i class="fas fa-plus mr-1"></i> 发布商品
      </router-link>
    </div>

    <div v-if="loading" class="text-center py-8 text-gray-500">
      <i class="fas fa-spinner fa-spin mr-2"></i>加载中...
    </div>

    <div v-if="error" class="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
      {{ error }}
    </div>

    <div v-if="!loading" class="space-y-4">
      <div v-for="product in products" :key="product.id"
        class="bg-white rounded-xl shadow-md p-4 flex gap-4">
        <div class="flex-shrink-0">
          <img v-if="product.imageUrls && product.imageUrls.length > 0"
            :src="product.imageUrls[0]" :alt="product.name"
            class="w-24 h-24 object-contain rounded-lg border" />
          <div v-else class="w-24 h-24 bg-gray-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-image text-gray-300 text-2xl"></i>
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between">
            <div>
              <h3 class="font-semibold text-gray-800">{{ product.name }}</h3>
              <p class="text-accent-600 font-bold">¥{{ product.price.toFixed(2) }}</p>
              <p class="text-xs text-gray-500 mt-1">
                库存：{{ product.quantity }}
                <span class="mx-1 text-gray-300">|</span>
                {{ PRODUCT_CATEGORIES[product.category] || product.category }}
                <span class="mx-1 text-gray-300">|</span>
                {{ PRODUCT_CONDITIONS[product.condition] || product.condition }}
              </p>
            </div>
            <span :class="PRODUCT_STATUS_COLORS[product.status] || 'bg-gray-100 text-gray-800'"
              class="text-xs px-2 py-0.5 rounded flex-shrink-0 ml-2">
              {{ PRODUCT_STATUSES[product.status] || product.status }}
            </span>
          </div>
          <div v-if="product.status === 'REJECTED' && product.rejectReason"
            class="mt-2 bg-red-50 text-red-700 text-sm p-2 rounded">
            <i class="fas fa-exclamation-circle mr-1"></i>拒绝原因：{{ product.rejectReason }}
          </div>
          <div class="mt-3 flex gap-2">
            <button @click="openEditDialog(product)"
              class="text-blue-600 hover:text-blue-800 text-xs font-medium">
              <i class="fas fa-edit mr-1"></i>编辑
            </button>
            <button v-if="product.status === 'REJECTED'" @click="handleResubmit(product)"
              class="text-green-600 hover:text-green-800 text-xs font-medium">
              <i class="fas fa-redo mr-1"></i>重新提交
            </button>
            <button @click="handleDelete(product)"
              class="text-red-600 hover:text-red-800 text-xs font-medium">
              <i class="fas fa-trash mr-1"></i>删除
            </button>
          </div>
        </div>
      </div>
      <div v-if="products.length === 0" class="text-center py-8 text-gray-400">
        你还没有发布过商品
      </div>
    </div>

    <div v-if="showEditDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeEditDialog">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h3 class="text-lg font-bold text-gray-800 mb-4">编辑商品</h3>
          <form @submit.prevent="handleEditSubmit" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">商品名称</label>
              <input v-model="editForm.name" type="text" required
                class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">商品描述</label>
              <textarea v-model="editForm.description" rows="3" required
                class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none resize-none"></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">图片链接（每行一个）</label>
              <textarea v-model="editImageUrlsText" rows="2"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none resize-none"></textarea>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">价格（元）</label>
                <input v-model.number="editForm.price" type="number" step="0.01" min="0" required
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">数量</label>
                <input v-model.number="editForm.quantity" type="number" min="1" required
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none" />
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">分类</label>
                <select v-model="editForm.category" required
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none">
                  <option v-for="(label, key) in PRODUCT_CATEGORIES" :key="key" :value="key">
                    {{ label }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">成色</label>
                <select v-model="editForm.condition" required
                  class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none">
                  <option v-for="(label, key) in PRODUCT_CONDITIONS" :key="key" :value="key">
                    {{ label }}
                  </option>
                </select>
              </div>
            </div>
            <div v-if="dialogError" class="text-red-500 text-sm">{{ dialogError }}</div>
            <div class="flex justify-end gap-3">
              <button type="button" @click="closeEditDialog"
                class="px-4 py-2 text-gray-600 hover:text-gray-800 text-sm">
                取消
              </button>
              <button type="submit" :disabled="submitting"
                class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm font-medium disabled:opacity-50">
                {{ submitting ? '提交中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useProductStore } from '@/stores/network/productStore'
import type { Product, CreateProductDto } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS, PRODUCT_STATUSES, PRODUCT_STATUS_COLORS } from '@/types/product'

const productStore = useProductStore()
const products = ref<Product[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const showEditDialog = ref(false)
const editingProductId = ref<string | null>(null)
const editForm = ref<CreateProductDto>({
  name: '',
  description: '',
  imageUrls: [],
  price: 0,
  quantity: 1,
  category: '',
  condition: 'NEW',
  source: 'USER'
})
const editImageUrlsText = ref('')
const submitting = ref(false)
const dialogError = ref('')

async function loadProducts() {
  loading.value = true
  error.value = null
  try {
    products.value = await productStore.getMyProducts()
  } catch (e: any) {
    error.value = '加载商品失败'
  } finally {
    loading.value = false
  }
}

function openEditDialog(product: Product) {
  editingProductId.value = product.id
  editForm.value = {
    name: product.name,
    description: product.description,
    imageUrls: product.imageUrls,
    price: product.price,
    quantity: product.quantity,
    category: product.category,
    condition: product.condition,
    source: product.source || 'USER'
  }
  editImageUrlsText.value = product.imageUrls.join('\n')
  dialogError.value = ''
  showEditDialog.value = true
}

function closeEditDialog() {
  showEditDialog.value = false
  editingProductId.value = null
  dialogError.value = ''
}

async function handleEditSubmit() {
  if (!editingProductId.value) return
  submitting.value = true
  dialogError.value = ''
  try {
    editForm.value.imageUrls = editImageUrlsText.value
      .split('\n')
      .map(url => url.trim())
      .filter(url => url.length > 0)
    if (editForm.value.imageUrls.length === 0) {
      editForm.value.imageUrls = ['https://via.placeholder.com/300x300?text=No+Image']
    }
    await productStore.editMyProduct(editingProductId.value, editForm.value)
    closeEditDialog()
    await loadProducts()
  } catch (e: any) {
    dialogError.value = e?.response?.data?.message || '编辑失败'
  } finally {
    submitting.value = false
  }
}

async function handleResubmit(product: Product) {
  if (!confirm(`确定重新提交商品「${product.name}」吗？提交后将重新进入审核流程。`)) return
  try {
    const dto: CreateProductDto = {
      name: product.name,
      description: product.description,
      imageUrls: product.imageUrls,
      price: product.price,
      quantity: product.quantity,
      category: product.category,
      condition: product.condition,
      source: product.source || 'USER'
    }
    await productStore.editMyProduct(product.id, dto)
    await loadProducts()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '重新提交失败'
  }
}

async function handleDelete(product: Product) {
  if (!confirm(`确定删除商品「${product.name}」吗？此操作不可撤销。`)) return
  try {
    await productStore.deleteMyProduct(product.id)
    await loadProducts()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '删除失败'
  }
}

onMounted(() => {
  loadProducts()
})
</script>

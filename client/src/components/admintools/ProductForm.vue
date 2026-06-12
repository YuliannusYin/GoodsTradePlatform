<template>
  <div class="flex w-full sm:space-x-5">
    <ConfirmDialogue
      :isPasswordRequired="false"
      :header="confirmHeader"
      :text="confirmText"
      v-if="isConfirmationVisible"
      :onConfirm="handleConfirm"
      :onCancel="closeConfirmation"
    />

    <ProductPreview v-if="formMode !== 'delete'" :product="product" />
    <ProductPreview v-else-if="selectedProduct" :product="selectedProduct" />

    <div class="p-4 bg-white rounded shadow w-full sm:max-w-[50%] sm:min-w-[50%]">
      <SmallViewTitle :text="formTitle" class="mb-2" />

      <!-- Product selector for edit/delete -->
      <div v-if="formMode !== 'add'" class="mb-4">
        <label for="products" class="block text-gray-700 font-bold mb-2">商品</label>
        <select v-model="selectedProductId" class="border w-full p-2 rounded" @change="loadProduct">
          <option v-for="p in products" :key="p.id" :value="p.id">
            {{ p.name }}
          </option>
        </select>
      </div>

      <!-- Form fields for add/edit -->
      <form v-if="formMode !== 'delete'" @submit.prevent="openConfirmation">
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">名称</label>
          <input v-model="product.name" type="text" class="border w-full p-2 rounded" />
        </div>
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">描述</label>
          <textarea v-model="product.description" class="border w-full p-2 rounded"></textarea>
        </div>
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">图片链接</label>
          <input v-model="imageUrlInput" type="text" class="border w-full p-2 rounded" />
        </div>
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">价格</label>
          <input v-model="product.price" type="number" step="0.01" class="border w-full p-2 rounded" />
        </div>
        <div class="mb-4">
          <label class="block text-gray-700 font-bold mb-2">数量</label>
          <input v-model="product.quantity" type="number" class="border w-full p-2 rounded" />
        </div>
        <button type="submit" :class="formMode === 'add' ? 'bg-blue-500 hover:bg-blue-600' : 'bg-blue-500 hover:bg-blue-600'"
          class="text-white py-2 px-4 rounded">{{ formMode === 'add' ? '添加商品' : '保存修改' }}</button>
      </form>

      <!-- Delete button -->
      <form v-else @submit.prevent="openConfirmation">
        <button type="submit" class="bg-red-500 hover:bg-red-600 text-white py-2 px-4 rounded">删除商品</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ProductForm.vue
 * @description 商品管理表单组件，支持添加、编辑和删除商品操作
 */
import { ref, computed, onMounted } from 'vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import { useProductStore } from '@/stores/network/productStore'
import type { Product, CreateProductDto } from '@/types/product'
import ConfirmDialogue from '../ConfirmDialogue.vue'
import SmallViewTitle from '../SmallViewTitle.vue'
import ProductPreview from '../ProductPreview.vue'

const props = defineProps<{
  formMode: 'add' | 'edit' | 'delete'
}>()

const adminToolsStore = useAdminToolsStore()
const productStore = useProductStore()

// 所有商品列表（编辑/删除时使用）
const products = ref<Product[]>([])
// 当前选中的商品ID
const selectedProductId = ref('')
// 当前选中的商品详情
const selectedProduct = ref<Product | null>(null)
// 图片链接输入值
const imageUrlInput = ref('')
// 确认对话框是否可见
const isConfirmationVisible = ref(false)

// 商品表单数据
const product = ref<CreateProductDto>({
  name: '',
  description: '',
  imageUrls: [],
  price: 0,
  quantity: 0,
  category: '',
  condition: 'NEW',
  source: 'PLATFORM'
})

// 根据表单模式计算标题
const formTitle = computed(() => {
  switch (props.formMode) {
    case 'add': return '添加商品'
    case 'edit': return '编辑商品'
    case 'delete': return '删除商品'
  }
})

// 根据表单模式计算确认对话框标题
const confirmHeader = computed(() => {
  switch (props.formMode) {
    case 'add': return '确认添加商品'
    case 'edit': return '确认编辑商品'
    case 'delete': return '确认删除商品'
  }
})

// 根据表单模式计算确认对话框提示文本
const confirmText = computed(() => {
  switch (props.formMode) {
    case 'add': return '确定要添加此商品吗？'
    case 'edit': return '确定要编辑此商品吗？'
    case 'delete': return '确定要删除此商品吗？'
  }
})

// 组件挂载时，非添加模式需加载商品列表
onMounted(async () => {
  if (props.formMode !== 'add') {
    products.value = await productStore.getAllProducts()
    // 默认选中第一个商品
    if (products.value.length > 0) {
      selectedProductId.value = products.value[0].id
      await loadProduct()
    }
  }
})

/**
 * 加载选中商品的详情数据并填充到表单
 */
async function loadProduct() {
  if (!selectedProductId.value) return
  const response = await productStore.getProduct(selectedProductId.value)
  selectedProduct.value = response
  product.value.name = response.name
  product.value.description = response.description
  product.value.imageUrls = response.imageUrls || []
  imageUrlInput.value = response.imageUrls?.[0] || ''
  product.value.price = response.price
  product.value.quantity = response.quantity
}

// 显示确认对话框
function openConfirmation() {
  isConfirmationVisible.value = true
}

// 关闭确认对话框
function closeConfirmation() {
  isConfirmationVisible.value = false
}

/**
 * 确认操作处理函数
 * 根据表单模式执行添加、编辑或删除商品操作
 */
async function handleConfirm() {
  switch (props.formMode) {
    case 'add':
      // 添加商品：将图片链接转为数组格式
      product.value.imageUrls = imageUrlInput.value ? [imageUrlInput.value] : []
      await adminToolsStore.addProduct({ ...product.value })
      resetProduct()
      break
    case 'edit':
      // 编辑商品：更新商品信息
      product.value.imageUrls = imageUrlInput.value ? [imageUrlInput.value] : []
      await adminToolsStore.editProduct(selectedProductId.value, { ...product.value })
      break
    case 'delete':
      // 删除商品：删除后刷新商品列表
      await adminToolsStore.deleteProduct(selectedProductId.value)
      products.value = await productStore.getAllProducts()
      if (products.value.length > 0) {
        selectedProductId.value = products.value[0].id
        await loadProduct()
      }
      break
  }
  closeConfirmation()
}

// 重置商品表单数据为初始值
function resetProduct() {
  product.value = {
    name: '',
    description: '',
    imageUrls: [],
    price: 0,
    quantity: 0,
    category: '',
    condition: 'NEW',
    source: 'PLATFORM'
  }
  imageUrlInput.value = ''
}
</script>

/**
 * @file HandleProductsView.vue
 * @description 商品管理主视图，提供商品搜索、条件筛选、列表展示和增删改操作入口
 * @input 无
 * @output 商品管理页面，包含搜索栏、筛选器、商品表格和操作按钮
 */
<template>
  <section class="p-4 sm:p-6">
    <!-- 页面标题和添加按钮 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-gray-800">商品管理</h2>
      <button @click="openAddForm"
        class="inline-flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium">
        <i class="fas fa-plus"></i>
        添加商品
      </button>
    </div>

    <!-- 搜索栏和筛选器 -->
    <div class="bg-white rounded-lg shadow-sm border p-4 mb-4">
      <div class="flex flex-col sm:flex-row gap-3">
        <!-- 搜索输入框 -->
        <div class="relative flex-1">
          <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"></i>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索商品名称..."
            class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none text-sm"
            @input="handleSearch"
          />
        </div>

        <!-- 分类筛选 -->
        <Listbox v-model="selectedCategory" @update:model-value="handleFilter">
          <div class="relative">
            <ListboxButton
              class="relative w-full min-w-[140px] cursor-pointer rounded-lg border border-gray-200 bg-white py-2 pl-3 pr-10 text-left text-sm focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none">
              <span class="block truncate">{{ selectedCategoryLabel }}</span>
              <span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                <i class="fas fa-chevron-down text-gray-400 text-xs"></i>
              </span>
            </ListboxButton>
            <transition
              leave-active-class="transition duration-100 ease-in"
              leave-from-class="opacity-100"
              leave-to-class="opacity-0">
              <ListboxOptions
                class="absolute z-20 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-sm shadow-lg ring-1 ring-black/5 focus:outline-none">
                <ListboxOption v-for="cat in categoryOptions" :key="cat.value" :value="cat.value"
                  v-slot="{ active, selected }">
                  <li :class="[active ? 'bg-blue-50 text-blue-700' : 'text-gray-700', 'relative cursor-pointer select-none py-2 pl-8 pr-4']">
                    <span v-if="selected" class="absolute inset-y-0 left-0 flex items-center pl-1.5 text-blue-600">
                      <i class="fas fa-check text-xs"></i>
                    </span>
                    {{ cat.label }}
                  </li>
                </ListboxOption>
              </ListboxOptions>
            </transition>
          </div>
        </Listbox>

        <!-- 状态筛选 -->
        <Listbox v-model="selectedStatus" @update:model-value="handleFilter">
          <div class="relative">
            <ListboxButton
              class="relative w-full min-w-[140px] cursor-pointer rounded-lg border border-gray-200 bg-white py-2 pl-3 pr-10 text-left text-sm focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none">
              <span class="block truncate">{{ selectedStatusLabel }}</span>
              <span class="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                <i class="fas fa-chevron-down text-gray-400 text-xs"></i>
              </span>
            </ListboxButton>
            <transition
              leave-active-class="transition duration-100 ease-in"
              leave-from-class="opacity-100"
              leave-to-class="opacity-0">
              <ListboxOptions
                class="absolute z-20 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-sm shadow-lg ring-1 ring-black/5 focus:outline-none">
                <ListboxOption v-for="st in statusOptions" :key="st.value" :value="st.value"
                  v-slot="{ active, selected }">
                  <li :class="[active ? 'bg-blue-50 text-blue-700' : 'text-gray-700', 'relative cursor-pointer select-none py-2 pl-8 pr-4']">
                    <span v-if="selected" class="absolute inset-y-0 left-0 flex items-center pl-1.5 text-blue-600">
                      <i class="fas fa-check text-xs"></i>
                    </span>
                    {{ st.label }}
                  </li>
                </ListboxOption>
              </ListboxOptions>
            </transition>
          </div>
        </Listbox>

        <!-- 重置筛选按钮 -->
        <button v-if="hasActiveFilters" @click="resetFilters"
          class="inline-flex items-center gap-1 text-gray-500 hover:text-gray-700 text-sm px-3 py-2 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
          <i class="fas fa-times-circle"></i>
          重置
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-12 text-gray-500">
      <i class="fas fa-spinner fa-spin mr-2"></i>加载中...
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
      <i class="fas fa-exclamation-circle mr-1"></i>{{ error }}
    </div>

    <!-- 商品列表表格 -->
    <ProductTable
      v-if="!loading"
      :products="filteredProducts"
      @edit="openEditForm"
      @delete="openDeleteConfirm"
      @view="openDetail"
    />

    <!-- 空状态 -->
    <div v-if="!loading && filteredProducts.length === 0" class="text-center py-12 text-gray-400">
      <i class="fas fa-box-open text-4xl mb-3 block"></i>
      <p>{{ hasActiveFilters ? '没有找到匹配的商品' : '暂无商品' }}</p>
    </div>

    <!-- 添加/编辑商品侧滑面板 -->
    <ProductFormSlideOver
      v-if="isFormOpen"
      :mode="formMode"
      :product="editingProduct"
      @close="closeForm"
      @saved="handleSaved"
    />

    <!-- 商品详情侧滑面板 -->
    <ProductDetailSlideOver
      v-if="isDetailOpen"
      :product="detailProduct"
      @close="closeDetail"
    />

    <!-- 删除确认对话框 -->
    <ConfirmDialogue
      v-if="isDeleteConfirmOpen"
      :isPasswordRequired="false"
      header="确认删除商品"
      :text="`确定要删除商品「${deletingProduct?.name}」吗？此操作不可撤销。`"
      :loading="deleteLoading"
      :onConfirm="handleDelete"
      :onCancel="closeDeleteConfirm"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Listbox, ListboxButton, ListboxOptions, ListboxOption } from '@headlessui/vue'
import { useProductStore } from '@/stores/network/productStore'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import { useToastStore } from '@/stores/toastStore'
import type { Product } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_STATUSES } from '@/types/product'
import ProductTable from '@/components/admintools/ProductTable.vue'
import ProductFormSlideOver from '@/components/admintools/ProductFormSlideOver.vue'
import ProductDetailSlideOver from '@/components/admintools/ProductDetailSlideOver.vue'
import ConfirmDialogue from '@/components/ConfirmDialogue.vue'

const productStore = useProductStore()
const adminToolsStore = useAdminToolsStore()
const toast = useToastStore()

// 商品列表数据
const products = ref<Product[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

// 搜索和筛选状态
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')

// 分类下拉选项（含"全部分类"）
const categoryOptions = computed(() => [
  { value: '', label: '全部分类' },
  ...Object.entries(PRODUCT_CATEGORIES).map(([value, label]) => ({ value, label }))
])

// 状态下拉选项（含"全部状态"）
const statusOptions = computed(() => [
  { value: '', label: '全部状态' },
  ...Object.entries(PRODUCT_STATUSES).map(([value, label]) => ({ value, label }))
])

// 当前选中分类的显示标签
const selectedCategoryLabel = computed(() => {
  if (!selectedCategory.value) return '全部分类'
  return PRODUCT_CATEGORIES[selectedCategory.value] || '全部分类'
})

// 当前选中状态的显示标签
const selectedStatusLabel = computed(() => {
  if (!selectedStatus.value) return '全部状态'
  return PRODUCT_STATUSES[selectedStatus.value] || '全部状态'
})

// 是否有激活的筛选条件
const hasActiveFilters = computed(() =>
  searchQuery.value !== '' || selectedCategory.value !== '' || selectedStatus.value !== ''
)

/**
 * 根据搜索关键词和筛选条件过滤商品列表
 * 搜索匹配商品名称，筛选匹配分类和状态
 */
const filteredProducts = computed(() => {
  let result = products.value

  // 按名称搜索
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.trim().toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(query))
  }

  // 按分类筛选
  if (selectedCategory.value) {
    result = result.filter(p => p.category === selectedCategory.value)
  }

  // 按状态筛选
  if (selectedStatus.value) {
    result = result.filter(p => p.status === selectedStatus.value)
  }

  return result
})

// 表单侧滑面板状态
const isFormOpen = ref(false)
const formMode = ref<'add' | 'edit'>('add')
const editingProduct = ref<Product | null>(null)

// 详情侧滑面板状态
const isDetailOpen = ref(false)
const detailProduct = ref<Product | null>(null)

// 删除确认对话框状态
const isDeleteConfirmOpen = ref(false)
const deletingProduct = ref<Product | null>(null)
const deleteLoading = ref(false)

/**
 * 加载全部商品列表
 */
async function loadProducts() {
  loading.value = true
  error.value = null
  try {
    products.value = await productStore.getAllProducts()
  } catch (e: any) {
    error.value = '加载商品列表失败'
    toast.addToast('加载商品列表失败', 'error')
  } finally {
    loading.value = false
  }
}

// 搜索输入防抖处理（简单实现，直接触发 computed 过滤）
function handleSearch() {
  // filteredProducts 是 computed，自动响应 searchQuery 变化
}

// 筛选条件变更处理
function handleFilter() {
  // filteredProducts 是 computed，自动响应筛选条件变化
}

// 重置所有筛选条件
function resetFilters() {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedStatus.value = ''
}

// 打开添加商品表单
function openAddForm() {
  formMode.value = 'add'
  editingProduct.value = null
  isFormOpen.value = true
}

// 打开编辑商品表单
function openEditForm(product: Product) {
  formMode.value = 'edit'
  editingProduct.value = product
  isFormOpen.value = true
}

// 关闭表单面板
function closeForm() {
  isFormOpen.value = false
  editingProduct.value = null
}

// 表单保存成功后的回调
function handleSaved() {
  closeForm()
  loadProducts()
}

// 打开商品详情面板
function openDetail(product: Product) {
  detailProduct.value = product
  isDetailOpen.value = true
}

// 关闭详情面板
function closeDetail() {
  isDetailOpen.value = false
  detailProduct.value = null
}

// 打开删除确认对话框
function openDeleteConfirm(product: Product) {
  deletingProduct.value = product
  isDeleteConfirmOpen.value = true
}

// 关闭删除确认对话框
function closeDeleteConfirm() {
  isDeleteConfirmOpen.value = false
  deletingProduct.value = null
}

/**
 * 执行删除商品操作
 */
async function handleDelete() {
  if (!deletingProduct.value) return
  deleteLoading.value = true
  try {
    await adminToolsStore.deleteProduct(deletingProduct.value.id)
    toast.addToast(`商品「${deletingProduct.value.name}」已删除`, 'success')
    closeDeleteConfirm()
    await loadProducts()
  } catch (e: any) {
    toast.addToast('删除商品失败：' + (e?.message || '未知错误'), 'error')
  } finally {
    deleteLoading.value = false
  }
}

// 组件挂载时加载商品列表
onMounted(() => {
  loadProducts()
})
</script>

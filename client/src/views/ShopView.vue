<template>
  <section class="max-w-7xl mx-auto px-4 py-6">
    <div class="flex flex-col md:flex-row gap-6">
      <aside class="w-full md:w-56 shrink-0">
        <div class="bg-white rounded-xl shadow-md p-4 sticky top-4">
          <h3 class="text-lg font-bold text-gray-800 mb-3">商品分类</h3>
          <div class="space-y-1">
            <button @click="selectCategory('')"
              class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
              :class="!selectedCategory ? 'bg-primary-500 text-white font-medium' : 'text-gray-600 hover:bg-primary-50'">
              全部
            </button>
            <button v-for="(label, key) in PRODUCT_CATEGORIES" :key="key"
              @click="selectCategory(key)"
              class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
              :class="selectedCategory === key ? 'bg-primary-500 text-white font-medium' : 'text-gray-600 hover:bg-primary-50'">
              {{ label }}
            </button>
          </div>
        </div>
      </aside>
      <div class="flex-1">
        <ProductCards :placeholderAmount="10" :products="products" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file ShopView.vue
 * @description 商城视图，提供商品分类筛选、搜索功能及商品列表展示
 */
import { onMounted, ref, watch } from 'vue'
import ProductCards from '@/components/products/ProductCards.vue'
import { useProductStore } from '@/stores/network/productStore'
import type { Product } from '@/types/product'
import { PRODUCT_CATEGORIES } from '@/types/product'
import { useRoute } from 'vue-router'

const productStore = useProductStore()
const products = ref<Product[]>([])       // 商品列表数据
const selectedCategory = ref('')           // 当前选中的分类，空字符串表示全部
const route = useRoute()

// 获取所有商品
async function getAllProducts() {
  products.value = await productStore.getAllProducts()
}

/**
 * 根据搜索条件获取筛选后的商品
 * @param {string} query - 搜索关键词
 * @param {any} filter - 筛选条件
 * @param {string} [category] - 商品分类
 */
async function getSearchedProducts(query: string, filter: any, category?: string) {
  products.value = await productStore.getSearchedProducts(query, filter, category)
}

/**
 * 判断搜索关键词是否为空
 * @param {string} query - 搜索关键词
 * @returns {boolean} 是否为空
 */
function isEmpty(query: string): boolean {
  return query === ''
}

/**
 * 判断筛选条件是否为空
 * @param {any} filter - 筛选条件
 * @returns {boolean} 是否无筛选条件
 */
function hasNoFilter(filter: any): boolean {
  return filter === null
}

/**
 * 统一搜索处理：无搜索条件时获取全部商品，否则按条件搜索
 * @param {string} query - 搜索关键词
 * @param {any} filter - 筛选条件
 * @param {string} [category] - 商品分类
 */
async function handleSearch(query: string, filter: any, category?: string) {
  if (isEmpty(query) && hasNoFilter(filter) && !category) {
    // 无搜索条件时加载全部商品
    getAllProducts()
  } else {
    // 有搜索条件时按条件搜索
    getSearchedProducts(query, filter, category)
  }
}

/**
 * 选择商品分类并触发搜索
 * @param {string} category - 选中的分类，空字符串表示全部
 */
function selectCategory(category: string) {
  selectedCategory.value = category
  const query = (route.query.query as string) || ''
  const filter = (route.query.filter as string) || 'none'
  handleSearch(query, filter, category || undefined)
}

// 组件挂载时根据URL参数初始化搜索
onMounted(async () => {
  const query = route.query.query as string
  const filter = route.query.filter as string
  const category = route.query.category as string

  // 从URL参数恢复分类选择
  if (category) {
    selectedCategory.value = category
  }

  const hasNoSearchQuery = query == undefined
  const hasNoFilter = filter == undefined

  if (hasNoSearchQuery || hasNoFilter) {
    // 无搜索参数时，根据分类加载商品
    if (category) {
      handleSearch('', 'none', category)
    } else {
      getAllProducts()
    }
  } else {
    // 有搜索参数时按条件搜索
    handleSearch(query, filter, category || undefined)
  }
})

// 监听路由查询参数变化，重新执行搜索
watch(
  () => ({
    query: route.query.query as string,
    filter: route.query.filter as string,
    category: route.query.category as string,
  }),
  (newQuery) => {
    const { query, filter, category } = newQuery
    // 路由参数中有分类时更新选中状态
    if (category) {
      selectedCategory.value = category
    }
    handleSearch(query || '', filter || null, category || selectedCategory.value || undefined)
  }
)
</script>

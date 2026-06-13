<template>
  <section class="p-4 sm:p-6">
    <h2 class="text-xl font-bold text-gray-800 mb-6">商品审核</h2>

    <!-- Tabs -->
    <div class="flex space-x-1 border-b mb-6">
      <button v-for="tab in tabs" :key="tab.value" @click="switchTab(tab.value)"
        class="px-4 py-2 text-sm font-medium transition-colors"
        :class="activeTab === tab.value
          ? 'text-blue-600 border-b-2 border-blue-600'
          : 'text-gray-500 hover:text-gray-700'">
        {{ tab.label }}
        <span class="ml-1 text-xs bg-gray-100 px-1.5 py-0.5 rounded-full">
          {{ tabCounts[tab.value] || 0 }}
        </span>
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-8 text-gray-500">
      <i class="fas fa-spinner fa-spin mr-2"></i>加载中...
    </div>

    <!-- Error -->
    <div v-if="error" class="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
      {{ error }}
    </div>

    <!-- Product Cards -->
    <div v-if="!loading" class="space-y-4">
      <div v-for="product in products" :key="product.id"
        class="border rounded-lg p-4 hover:shadow-sm transition-shadow">
        <div class="flex gap-4">
          <!-- Product Image -->
          <div class="flex-shrink-0">
            <img v-if="product.imageUrls && product.imageUrls.length > 0"
              :src="product.imageUrls[0]" :alt="product.name"
              class="w-24 h-24 object-contain rounded-lg border"
              @error="(e: Event) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2296%22 height=%2296%22 viewBox=%220 0 96 96%22><rect fill=%22%23f3f4f6%22 width=%2296%22 height=%2296%22/><text x=%2248%22 y=%2252%22 text-anchor=%22middle%22 fill=%22%239ca3af%22 font-size=%2212%22>无图</text></svg>') }" />
            <div v-else class="w-24 h-24 bg-gray-100 rounded-lg flex items-center justify-center">
              <i class="fas fa-image text-gray-300 text-2xl"></i>
            </div>
          </div>

          <!-- Product Info -->
          <div class="flex-1 min-w-0">
            <div class="flex items-start justify-between">
              <div>
                <h3 class="font-semibold text-gray-800 truncate">{{ product.name }}</h3>
                <p class="text-sm text-gray-500 mt-0.5">
                  <span class="text-accent-600 font-bold">¥{{ product.price.toFixed(2) }}</span>
                  <span class="mx-2 text-gray-300">|</span>
                  库存：{{ product.quantity }}
                  <span class="mx-2 text-gray-300">|</span>
                  {{ PRODUCT_CATEGORIES[product.category] || product.category }}
                </p>
                <p v-if="product.seller" class="text-xs text-gray-400 mt-1">
                  卖家：{{ product.seller.username }}
                </p>
              </div>
              <span :class="PRODUCT_STATUS_COLORS[product.status] || 'bg-gray-100 text-gray-800'"
                class="text-xs px-2 py-0.5 rounded flex-shrink-0 ml-2">
                {{ PRODUCT_STATUSES[product.status] || product.status }}
              </span>
            </div>

            <p class="text-sm text-gray-600 mt-2 line-clamp-2">{{ product.description }}</p>

            <!-- Reject Reason -->
            <div v-if="product.status === 'REJECTED' && product.rejectReason"
              class="mt-2 bg-red-50 text-red-700 text-sm p-2 rounded">
              <i class="fas fa-exclamation-circle mr-1"></i>拒绝原因：{{ product.rejectReason }}
            </div>

            <!-- Actions -->
            <div class="mt-3 flex gap-2">
              <!-- Pending actions -->
              <template v-if="product.status === 'PENDING'">
                <button @click="handleApprove(product)"
                  class="bg-green-600 text-white px-3 py-1.5 rounded text-xs font-medium hover:bg-green-700">
                  <i class="fas fa-check mr-1"></i>通过
                </button>
                <button @click="openRejectDialog(product)"
                  class="bg-red-600 text-white px-3 py-1.5 rounded text-xs font-medium hover:bg-red-700">
                  <i class="fas fa-times mr-1"></i>拒绝
                </button>
              </template>

              <!-- Approved actions -->
              <template v-if="product.status === 'APPROVED'">
                <button @click="handleDisable(product)"
                  class="bg-yellow-600 text-white px-3 py-1.5 rounded text-xs font-medium hover:bg-yellow-700">
                  <i class="fas fa-ban mr-1"></i>禁用
                </button>
              </template>

              <!-- Disabled actions -->
              <template v-if="product.status === 'DISABLED'">
                <button @click="handleEnable(product)"
                  class="bg-green-600 text-white px-3 py-1.5 rounded text-xs font-medium hover:bg-green-700">
                  <i class="fas fa-check-circle mr-1"></i>重新启用
                </button>
              </template>

              <!-- Rejected actions -->
              <template v-if="product.status === 'REJECTED'">
                <button @click="handleApprove(product)"
                  class="bg-green-600 text-white px-3 py-1.5 rounded text-xs font-medium hover:bg-green-700">
                  <i class="fas fa-check mr-1"></i>重新审核通过
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div v-if="products.length === 0" class="text-center py-8 text-gray-400">
        暂无{{ PRODUCT_STATUSES[activeTab] || '' }}商品
      </div>
    </div>

    <!-- Reject Dialog -->
    <div v-if="showRejectDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeRejectDialog">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="p-6">
          <h3 class="text-lg font-bold text-gray-800 mb-4">拒绝商品</h3>
          <p class="text-sm text-gray-500 mb-3">商品：{{ rejectProduct?.name }}</p>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">拒绝原因</label>
            <textarea v-model="rejectReason" rows="3"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-red-300 focus:border-red-300 outline-none resize-none"
              placeholder="请输入拒绝原因..."></textarea>
          </div>

          <div v-if="dialogError" class="text-red-500 text-sm mt-3">{{ dialogError }}</div>

          <div class="flex justify-end gap-3 mt-6">
            <button @click="closeRejectDialog"
              class="px-4 py-2 text-gray-600 hover:text-gray-800 text-sm">
              取消
            </button>
            <button @click="handleReject" :disabled="submitting || !rejectReason.trim()"
              class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 text-sm font-medium disabled:opacity-50">
              {{ submitting ? '提交中...' : '确认拒绝' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * @file ProductReviewView.vue
 * @description 商品审核视图，管理员可按状态标签页查看商品列表，执行通过、拒绝、禁用、启用等审核操作
 */
import { ref, onMounted } from 'vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import type { Product } from '@/types/product'
import { PRODUCT_STATUSES, PRODUCT_STATUS_COLORS, PRODUCT_CATEGORIES } from '@/types/product'

const adminStore = useAdminToolsStore()
const products = ref<Product[]>([])       // 当前标签页下的商品列表
const loading = ref(false)                 // 加载状态
const error = ref<string | null>(null)     // 错误信息
const activeTab = ref('PENDING')           // 当前激活的标签页，默认待审核

// 标签页配置：各审核状态及对应中文标签
const tabs = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已拒绝' },
  { value: 'DISABLED', label: '已禁用' }
]

// 各标签页对应的商品数量
const tabCounts = ref<Record<string, number>>({
  PENDING: 0,
  APPROVED: 0,
  REJECTED: 0,
  DISABLED: 0
})

// 拒绝对话框相关状态
const showRejectDialog = ref(false)          // 是否显示拒绝对话框
const rejectProduct = ref<Product | null>(null) // 待拒绝的商品
const rejectReason = ref('')                  // 拒绝原因
const submitting = ref(false)                 // 提交中状态
const dialogError = ref('')                   // 对话框错误信息

/**
 * 加载当前标签页状态下的商品列表
 */
async function loadProducts() {
  loading.value = true
  error.value = null
  try {
    products.value = await adminStore.getProductsByStatus(activeTab.value)
  } catch (e: any) {
    error.value = '加载商品失败'
  } finally {
    loading.value = false
  }
}

/**
 * 加载各标签页的商品数量统计
 */
async function loadTabCounts() {
  try {
    const counts = { ...tabCounts.value }
    // 并发请求各状态的商品数量
    await Promise.all(
      tabs.map(async (tab) => {
        try {
          const result = await adminStore.getProductsByStatus(tab.value)
          counts[tab.value] = Array.isArray(result) ? result.length : 0
        } catch {
          counts[tab.value] = 0
        }
      })
    )
    tabCounts.value = counts
  } catch {
    // 静默失败，不影响主流程
  }
}

/**
 * 切换标签页并重新加载商品列表
 * @param {string} tab - 目标标签页的状态值
 */
function switchTab(tab: string) {
  activeTab.value = tab
  loadProducts()
}

/**
 * 审核通过商品
 * @param {Product} product - 待通过的商品
 */
async function handleApprove(product: Product) {
  if (!confirm(`确定通过商品「${product.name}」吗？`)) return
  try {
    await adminStore.approveProduct(product.id)
    await loadProducts()
    await loadTabCounts()
  } catch (e: any) {
    error.value = e?.message || '操作失败'
  }
}

/**
 * 打开拒绝对话框
 * @param {Product} product - 待拒绝的商品
 */
function openRejectDialog(product: Product) {
  rejectProduct.value = product
  rejectReason.value = ''
  dialogError.value = ''
  showRejectDialog.value = true
}

// 关闭拒绝对话框并重置状态
function closeRejectDialog() {
  showRejectDialog.value = false
  rejectProduct.value = null
  dialogError.value = ''
}

/**
 * 确认拒绝商品，提交拒绝原因
 */
async function handleReject() {
  if (!rejectProduct.value || !rejectReason.value.trim()) return
  submitting.value = true
  dialogError.value = ''
  try {
    await adminStore.rejectProduct(rejectProduct.value.id, rejectReason.value.trim())
    closeRejectDialog()
    await loadProducts()
    await loadTabCounts()
  } catch (e: any) {
    dialogError.value = e?.message || '操作失败'
  } finally {
    submitting.value = false
  }
}

/**
 * 禁用已通过的商品
 * @param {Product} product - 待禁用的商品
 */
async function handleDisable(product: Product) {
  if (!confirm(`确定禁用商品「${product.name}」吗？`)) return
  try {
    await adminStore.disableProduct(product.id)
    await loadProducts()
    await loadTabCounts()
  } catch (e: any) {
    error.value = e?.message || '操作失败'
  }
}

/**
 * 重新启用已禁用的商品
 * @param {Product} product - 待启用的商品
 */
async function handleEnable(product: Product) {
  if (!confirm(`确定重新启用商品「${product.name}」吗？`)) return
  try {
    await adminStore.enableProduct(product.id)
    await loadProducts()
    await loadTabCounts()
  } catch (e: any) {
    error.value = e?.message || '操作失败'
  }
}

// 组件挂载时加载商品列表和标签页数量
onMounted(() => {
  loadProducts()
  loadTabCounts()
})
</script>

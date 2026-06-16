/**
 * @file ProductTable.vue
 * @description 商品列表表格组件，展示商品信息并支持编辑、删除、查看详情操作
 * @input 商品列表数据
 * @output 渲染表格行，触发编辑/删除/查看事件
 */
<template>
  <!-- 桌面端表格视图 -->
  <div class="hidden sm:block bg-white rounded-lg shadow-sm border overflow-hidden">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">商品</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">分类</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">价格</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">库存</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
          <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-100">
        <tr v-for="product in products" :key="product.id" class="hover:bg-gray-50 transition-colors">
          <!-- 商品图片和名称 -->
          <td class="px-4 py-3 whitespace-nowrap">
            <div class="flex items-center gap-3">
              <img v-if="product.imageUrls && product.imageUrls.length > 0"
                :src="product.imageUrls[0]" :alt="product.name"
                class="w-10 h-10 object-contain rounded border bg-white"
                @error="handleImageError" />
              <div v-else class="w-10 h-10 bg-gray-100 rounded flex items-center justify-center border">
                <i class="fas fa-image text-gray-300 text-xs"></i>
              </div>
              <div class="min-w-0">
                <p class="text-sm font-medium text-gray-800 truncate max-w-[200px]">{{ product.name }}</p>
                <p v-if="product.seller" class="text-xs text-gray-400">{{ product.seller.username }}</p>
              </div>
            </div>
          </td>
          <!-- 分类 -->
          <td class="px-4 py-3 whitespace-nowrap">
            <span class="text-sm text-gray-600">{{ PRODUCT_CATEGORIES[product.category] || product.category }}</span>
          </td>
          <!-- 价格 -->
          <td class="px-4 py-3 whitespace-nowrap">
            <span class="text-sm font-semibold text-gray-800">¥{{ product.price.toFixed(2) }}</span>
          </td>
          <!-- 库存 -->
          <td class="px-4 py-3 whitespace-nowrap">
            <span :class="product.quantity <= 5 ? 'text-red-600' : 'text-gray-600'" class="text-sm">
              {{ product.quantity }}
            </span>
          </td>
          <!-- 状态 -->
          <td class="px-4 py-3 whitespace-nowrap">
            <span :class="PRODUCT_STATUS_COLORS[product.status] || 'bg-gray-100 text-gray-800'"
              class="inline-flex px-2 py-0.5 text-xs font-medium rounded">
              {{ PRODUCT_STATUSES[product.status] || product.status }}
            </span>
          </td>
          <!-- 操作按钮 -->
          <td class="px-4 py-3 whitespace-nowrap text-right">
            <div class="flex items-center justify-end gap-1">
              <button @click="$emit('view', product)" title="查看详情"
                class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors">
                <i class="fas fa-eye text-sm"></i>
              </button>
              <button @click="$emit('edit', product)" title="编辑"
                class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors">
                <i class="fas fa-pen text-sm"></i>
              </button>
              <button @click="$emit('delete', product)" title="删除"
                class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors">
                <i class="fas fa-trash text-sm"></i>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- 移动端卡片视图 -->
  <div class="sm:hidden space-y-3">
    <div v-for="product in products" :key="product.id"
      class="bg-white rounded-lg shadow-sm border p-3">
      <div class="flex gap-3">
        <!-- 商品缩略图 -->
        <img v-if="product.imageUrls && product.imageUrls.length > 0"
          :src="product.imageUrls[0]" :alt="product.name"
          class="w-16 h-16 object-contain rounded border bg-white flex-shrink-0"
          @error="handleImageError" />
        <div v-else class="w-16 h-16 bg-gray-100 rounded flex items-center justify-center border flex-shrink-0">
          <i class="fas fa-image text-gray-300"></i>
        </div>

        <!-- 商品信息 -->
        <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between">
            <p class="text-sm font-medium text-gray-800 truncate pr-2">{{ product.name }}</p>
            <span :class="PRODUCT_STATUS_COLORS[product.status] || 'bg-gray-100 text-gray-800'"
              class="text-xs px-1.5 py-0.5 rounded flex-shrink-0">
              {{ PRODUCT_STATUSES[product.status] || product.status }}
            </span>
          </div>
          <p class="text-xs text-gray-500 mt-0.5">
            {{ PRODUCT_CATEGORIES[product.category] || product.category }}
          </p>
          <div class="flex items-center justify-between mt-1">
            <span class="text-sm font-semibold text-gray-800">¥{{ product.price.toFixed(2) }}</span>
            <span :class="product.quantity <= 5 ? 'text-red-600' : 'text-gray-500'" class="text-xs">
              库存: {{ product.quantity }}
            </span>
          </div>
        </div>
      </div>

      <!-- 移动端操作按钮 -->
      <div class="flex justify-end gap-1 mt-2 pt-2 border-t">
        <button @click="$emit('view', product)"
          class="px-3 py-1 text-xs text-blue-600 hover:bg-blue-50 rounded">
          查看
        </button>
        <button @click="$emit('edit', product)"
          class="px-3 py-1 text-xs text-blue-600 hover:bg-blue-50 rounded">
          编辑
        </button>
        <button @click="$emit('delete', product)"
          class="px-3 py-1 text-xs text-red-600 hover:bg-red-50 rounded">
          删除
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ProductTable.vue
 * @description 商品列表表格组件，桌面端显示表格，移动端显示卡片
 */
import type { Product } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_STATUSES, PRODUCT_STATUS_COLORS } from '@/types/product'

defineProps<{
  /** 商品列表数据 */
  products: Product[]
}>()

defineEmits<{
  /** 编辑商品事件 */
  (e: 'edit', product: Product): void
  /** 删除商品事件 */
  (e: 'delete', product: Product): void
  /** 查看商品详情事件 */
  (e: 'view', product: Product): void
}>()

/**
 * 图片加载失败时替换为占位图
 * @param {Event} event - 图片错误事件
 */
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = 'data:image/svg+xml,' + encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40">' +
    '<rect fill="#f3f4f6" width="40" height="40"/>' +
    '<text x="20" y="24" text-anchor="middle" fill="#9ca3af" font-size="8">无图</text></svg>'
  )
}
</script>

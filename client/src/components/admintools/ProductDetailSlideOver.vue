/**
 * @file ProductDetailSlideOver.vue
 * @description 商品详情侧滑面板，展示商品的完整信息
 * @input 商品数据
 * @output 渲染商品详情，关闭时触发 close 事件
 */
<template>
  <TransitionRoot :show="true" as="template">
    <Dialog class="relative z-40" @close="$emit('close')">
      <!-- 背景遮罩 -->
      <TransitionChild
        enter="ease-in-out duration-300" enter-from="opacity-0" enter-to="opacity-100"
        leave="ease-in-out duration-300" leave-from="opacity-100" leave-to="opacity-0">
        <div class="fixed inset-0 bg-gray-500/30 transition-opacity" @click="$emit('close')" />
      </TransitionChild>

      <div class="fixed inset-0 overflow-hidden">
        <div class="absolute inset-0 overflow-hidden">
          <div class="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
            <!-- 侧滑面板 -->
            <TransitionChild
              enter="transform transition ease-in-out duration-300"
              enter-from="translate-x-full" enter-to="translate-x-0"
              leave="transform transition ease-in-out duration-300"
              leave-from="translate-x-0" leave-to="translate-x-full">
              <DialogPanel class="pointer-events-auto w-screen max-w-lg">
                <div class="flex h-full flex-col overflow-y-auto bg-white shadow-xl">
                  <!-- 面板头部 -->
                  <div class="bg-gray-50 px-4 py-4 border-b">
                    <div class="flex items-center justify-between">
                      <DialogTitle class="text-lg font-semibold text-gray-800">商品详情</DialogTitle>
                      <button @click="$emit('close')"
                        class="text-gray-400 hover:text-gray-600 p-1 rounded hover:bg-gray-100">
                        <i class="fas fa-times"></i>
                      </button>
                    </div>
                  </div>

                  <!-- 详情内容 -->
                  <div v-if="product" class="flex-1 px-4 py-6 space-y-6">
                    <!-- 商品图片 -->
                    <div v-if="product.imageUrls && product.imageUrls.length > 0" class="space-y-2">
                      <label class="block text-sm font-medium text-gray-700">商品图片</label>
                      <div class="grid grid-cols-2 gap-2">
                        <img v-for="(url, index) in product.imageUrls" :key="index"
                          :src="url" :alt="`${product.name} - 图片${index + 1}`"
                          class="w-full h-32 object-contain rounded-lg border bg-white p-1"
                          @error="handleImageError" />
                      </div>
                    </div>

                    <!-- 基本信息 -->
                    <div class="space-y-4">
                      <div>
                        <label class="block text-sm font-medium text-gray-500 mb-1">商品名称</label>
                        <p class="text-gray-800">{{ product.name }}</p>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-500 mb-1">商品描述</label>
                        <p class="text-gray-800 whitespace-pre-wrap">{{ product.description }}</p>
                      </div>

                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-500 mb-1">分类</label>
                          <p class="text-gray-800">{{ PRODUCT_CATEGORIES[product.category] || product.category }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-500 mb-1">成色</label>
                          <p class="text-gray-800">{{ PRODUCT_CONDITIONS[product.condition] || product.condition }}</p>
                        </div>
                      </div>

                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-500 mb-1">价格</label>
                          <p class="text-lg font-semibold text-gray-800">¥{{ product.price.toFixed(2) }}</p>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-500 mb-1">库存</label>
                          <p :class="product.quantity <= 5 ? 'text-red-600' : 'text-gray-800'" class="text-lg font-semibold">
                            {{ product.quantity }}
                          </p>
                        </div>
                      </div>

                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm font-medium text-gray-500 mb-1">状态</label>
                          <span :class="PRODUCT_STATUS_COLORS[product.status] || 'bg-gray-100 text-gray-800'"
                            class="inline-flex px-2 py-0.5 text-xs font-medium rounded">
                            {{ PRODUCT_STATUSES[product.status] || product.status }}
                          </span>
                        </div>
                        <div>
                          <label class="block text-sm font-medium text-gray-500 mb-1">来源</label>
                          <p class="text-gray-800">{{ product.source === 'PLATFORM' ? '平台' : '用户' }}</p>
                        </div>
                      </div>

                      <!-- 卖家信息 -->
                      <div v-if="product.seller">
                        <label class="block text-sm font-medium text-gray-500 mb-1">卖家</label>
                        <p class="text-gray-800">{{ product.seller.username }}</p>
                      </div>

                      <!-- 拒绝原因 -->
                      <div v-if="product.status === 'REJECTED' && product.rejectReason">
                        <label class="block text-sm font-medium text-gray-500 mb-1">拒绝原因</label>
                        <div class="bg-red-50 text-red-700 text-sm p-3 rounded-lg">
                          <i class="fas fa-exclamation-circle mr-1"></i>{{ product.rejectReason }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </DialogPanel>
            </TransitionChild>
          </div>
        </div>
      </div>
    </Dialog>
  </TransitionRoot>
</template>

<script setup lang="ts">
/**
 * @file ProductDetailSlideOver.vue
 * @description 商品详情侧滑面板，使用 Headless UI Dialog 实现遮罩和动画
 */
import {
  Dialog, DialogPanel, DialogTitle,
  TransitionRoot, TransitionChild
} from '@headlessui/vue'
import type { Product } from '@/types/product'
import { PRODUCT_CATEGORIES, PRODUCT_CONDITIONS, PRODUCT_STATUSES, PRODUCT_STATUS_COLORS } from '@/types/product'

defineProps<{
  /** 商品数据 */
  product: Product | null
}>()

defineEmits<{
  /** 关闭面板事件 */
  (e: 'close'): void
}>()

/**
 * 图片加载失败时替换为占位图
 * @param {Event} event - 图片错误事件
 */
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = 'data:image/svg+xml,' + encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128">' +
    '<rect fill="#f3f4f6" width="128" height="128"/>' +
    '<text x="64" y="68" text-anchor="middle" fill="#9ca3af" font-size="12">图片加载失败</text></svg>'
  )
}
</script>

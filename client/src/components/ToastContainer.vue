/**
 * @file ToastContainer.vue
 * @description 全局 Toast 通知容器组件，在页面右上角显示操作反馈通知
 * @input 通过 toastStore 注入通知数据
 * @output 渲染通知列表，支持手动关闭
 */
<template>
  <!-- 通知容器固定在右上角 -->
  <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 max-w-sm">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="toastClass(toast.type)"
        class="flex items-center gap-2 px-4 py-3 rounded-lg shadow-lg text-sm font-medium"
      >
        <!-- 通知图标 -->
        <i :class="toastIcon(toast.type)" class="flex-shrink-0"></i>
        <!-- 通知消息 -->
        <span class="flex-1">{{ toast.message }}</span>
        <!-- 手动关闭按钮 -->
        <button @click="removeToast(toast.id)" class="flex-shrink-0 opacity-60 hover:opacity-100">
          <i class="fas fa-times text-xs"></i>
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToastStore } from '@/stores/toastStore'
import type { ToastType } from '@/stores/toastStore'
import { storeToRefs } from 'pinia'

const toastStore = useToastStore()
const { toasts } = storeToRefs(toastStore)
const { removeToast } = toastStore

/**
 * 根据通知类型返回对应的样式类
 * @param {ToastType} type - 通知类型
 * @returns {string} Tailwind CSS 样式类
 */
function toastClass(type: ToastType): string {
  const map: Record<ToastType, string> = {
    success: 'bg-green-50 text-green-800 border border-green-200',
    error: 'bg-red-50 text-red-800 border border-red-200',
    warning: 'bg-yellow-50 text-yellow-800 border border-yellow-200',
    info: 'bg-blue-50 text-blue-800 border border-blue-200'
  }
  return map[type]
}

/**
 * 根据通知类型返回对应的图标类
 * @param {ToastType} type - 通知类型
 * @returns {string} FontAwesome 图标类名
 */
function toastIcon(type: ToastType): string {
  const map: Record<ToastType, string> = {
    success: 'fas fa-check-circle',
    error: 'fas fa-exclamation-circle',
    warning: 'fas fa-exclamation-triangle',
    info: 'fas fa-info-circle'
  }
  return map[type]
}
</script>

<style scoped>
/* 通知进入动画 */
.toast-enter-active {
  transition: all 0.3s ease-out;
}
/* 通知离开动画 */
.toast-leave-active {
  transition: all 0.2s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
.toast-move {
  transition: transform 0.2s ease;
}
</style>

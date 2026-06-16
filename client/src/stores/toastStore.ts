/**
 * @file toastStore.ts
 * @description Toast 通知状态管理，提供全局操作反馈通知的添加、移除和自动关闭功能
 * @input 通知消息、类型（成功/错误/警告/信息）
 * @output 通知列表及控制方法
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 通知类型：成功、错误、警告、信息 */
export type ToastType = 'success' | 'error' | 'warning' | 'info'

/** 通知项数据结构 */
export interface ToastItem {
  /** 唯一标识 */
  id: number
  /** 通知消息文本 */
  message: string
  /** 通知类型 */
  type: ToastType
}

/** 自增 ID 计数器 */
let nextId = 0

/**
 * Toast 通知状态管理 Store
 * 职责：管理全局通知列表，提供添加和移除通知的方法
 */
export const useToastStore = defineStore('toastStore', () => {
  // 当前显示的通知列表
  const toasts = ref<ToastItem[]>([])

  /** 各类型对应的默认显示时长（毫秒） */
  const DURATION: Record<ToastType, number> = {
    success: 3000,
    error: 5000,
    warning: 4000,
    info: 3000
  }

  /**
   * 添加一条通知并自动定时移除
   * @param {string} message - 通知消息
   * @param {ToastType} type - 通知类型，默认 success
   * @param {number} duration - 自定义显示时长（毫秒），不传则使用默认值
   */
  function addToast(message: string, type: ToastType = 'success', duration?: number) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    // 到时间后自动移除
    setTimeout(() => removeToast(id), duration ?? DURATION[type])
  }

  /**
   * 移除指定 ID 的通知
   * @param {number} id - 通知 ID
   */
  function removeToast(id: number) {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index !== -1) {
      toasts.value.splice(index, 1)
    }
  }

  return { toasts, addToast, removeToast }
})

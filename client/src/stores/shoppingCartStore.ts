/**
 * @file shoppingCartStore.ts
 * @description 购物车状态管理，使用Map结构管理商品数量，支持localStorage持久化
 * @input 无外部入参，内部从localStorage读取初始数据
 * @output 暴露购物车总数量、商品种类数及增删改查清空等操作方法
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { CartItem } from '@/types/cart'

/** localStorage 存储键名 */
const STORAGE_KEY = 'shopping_cart'

/**
 * 从localStorage加载购物车数据
 * @returns {CartItem[]} 已存储的购物车项数组，解析失败或无数据时返回空数组
 */
function loadCartItems(): CartItem[] {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch {
      // JSON解析失败时返回空数组，防止数据损坏导致异常
      return []
    }
  }
  return []
}

/**
 * 将购物车数据持久化到localStorage
 * @param {Map<string, CartItem>} items - 需要持久化的购物车Map
 */
function persistCartItems(items: Map<string, CartItem>): void {
  const arr = Array.from(items.values())
  localStorage.setItem(STORAGE_KEY, JSON.stringify(arr))
}

/**
 * 购物车状态管理Store
 * 职责：管理购物车商品Map、总数量和种类数，提供增删改查清空操作
 */
export const useShoppingCartStore = defineStore('shoppingCart', () => {
  // 初始化时从localStorage恢复已保存的购物车数据
  const initialItems = loadCartItems()
  // 购物车商品Map，key为商品ID，value为购物车项
  const cartItems = ref<Map<string, CartItem>>(new Map(
    initialItems.map(item => [item.productId, item])
  ))

  // 购物车中所有商品的总数量（每种商品的数量之和）
  const totalQuantity = computed(() => {
    let sum = 0
    for (const item of cartItems.value.values()) {
      sum += item.quantity
    }
    return sum
  })

  // 购物车中的商品种类数
  const totalKinds = computed(() => cartItems.value.size)

  /**
   * 向购物车添加商品，若已存在则数量+1
   * @param {string} productId - 要添加的商品ID
   */
  function addItem(productId: string): void {
    const existing = cartItems.value.get(productId)
    if (existing) {
      // 商品已存在，数量加1
      existing.quantity++
    } else {
      // 商品不存在，新增一条
      cartItems.value.set(productId, { productId, quantity: 1 })
    }
    // 触发响应式更新
    cartItems.value = new Map(cartItems.value)
    persistCartItems(cartItems.value)
  }

  /**
   * 设置购物车中指定商品的数量
   * @param {string} productId - 商品ID
   * @param {number} quantity - 目标数量
   */
  function setItemQuantity(productId: string, quantity: number): void {
    if (quantity <= 0) {
      // 数量<=0时移除商品
      removeItem(productId)
      return
    }
    const existing = cartItems.value.get(productId)
    if (existing) {
      existing.quantity = quantity
    } else {
      cartItems.value.set(productId, { productId, quantity })
    }
    // 触发响应式更新
    cartItems.value = new Map(cartItems.value)
    persistCartItems(cartItems.value)
  }

  /**
   * 从购物车移除指定商品
   * @param {string} productId - 要移除的商品ID
   */
  function removeItem(productId: string): void {
    cartItems.value.delete(productId)
    // 触发响应式更新
    cartItems.value = new Map(cartItems.value)
    persistCartItems(cartItems.value)
  }

  /**
   * 清空购物车中的所有商品
   */
  function clearCart(): void {
    cartItems.value.clear()
    cartItems.value = new Map(cartItems.value)
    localStorage.removeItem(STORAGE_KEY)
  }

  /**
   * 获取购物车中所有商品项列表
   * @returns {CartItem[]} 购物车项数组
   */
  function getAllItems(): CartItem[] {
    return Array.from(cartItems.value.values())
  }

  /**
   * 从后端数据恢复购物车（登录合并后使用）
   * @param {CartItem[]} items - 后端返回的购物车项列表
   */
  function restoreFromBackend(items: CartItem[]): void {
    cartItems.value = new Map(items.map(item => [item.productId, item]))
    persistCartItems(cartItems.value)
  }

  return {
    totalQuantity, totalKinds,
    addItem, setItemQuantity, removeItem, clearCart,
    getAllItems, restoreFromBackend,
    // 兼容旧属性名
    productAmount: totalQuantity
  }
})

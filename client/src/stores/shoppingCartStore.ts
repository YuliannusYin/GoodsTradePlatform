/**
 * @file shoppingCartStore.ts
 * @description 购物车状态管理，管理购物车中的商品ID列表及数量，支持sessionStorage持久化
 * @input 无外部入参，内部从sessionStorage读取初始数据
 * @output 暴露购物车商品ID列表、商品数量及增删查清空等操作方法
 */
import { ref } from 'vue'
import { defineStore } from 'pinia'

/**
 * 从sessionStorage加载购物车商品ID列表
 * @returns {string[]} 已存储的商品ID数组，解析失败或无数据时返回空数组
 */
function loadProductIds(): string[] {
  // 从sessionStorage中读取已保存的商品ID字符串
  const stored = sessionStorage.getItem('shoppingCart_productIds')
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
 * 将商品ID列表持久化到sessionStorage
 * @param {string[]} ids - 需要持久化的商品ID数组
 */
function persistProductIds(ids: string[]) {
  sessionStorage.setItem('shoppingCart_productIds', JSON.stringify(ids))
}

/**
 * 购物车状态管理Store
 * 职责：管理购物车商品ID列表、商品数量，提供增删查清空操作
 */
export const useShoppingCartStore = defineStore('shoppingCart', () => {
  // 初始化时从sessionStorage恢复已保存的商品ID
  const initialIds = loadProductIds()
  // 购物车中的商品ID列表
  const productIds = ref<string[]>(initialIds)
  // 购物车中的商品数量
  const productAmount = ref<number>(initialIds.length)

  /**
   * 向购物车添加商品ID
   * @param {string} productId - 要添加的商品ID
   * @returns {Promise<void>} 无返回值
   */
  async function addProductId(productId: string): Promise<void> {
    productIds.value.push(productId)
    productAmount.value++
    // 添加后立即持久化到sessionStorage
    persistProductIds(productIds.value)
  }

  /**
   * 从购物车移除指定商品ID
   * @param {string} productId - 要移除的商品ID
   */
  function removeProductId(productId: string): void {
    // 查找商品ID在列表中的索引位置
    const index = productIds.value.indexOf(productId)
    productIds.value.splice(index, 1)
    productAmount.value--
    // 移除后立即持久化到sessionStorage
    persistProductIds(productIds.value)
  }

  // 清空购物车中的所有商品
  function clearProductIds(): void {
    productIds.value = []
    productAmount.value = 0
    persistProductIds([])
  }

  // 获取购物车中所有商品ID列表
  function getAllProductIds(): string[] {
    return productIds.value
  }

  // 获取购物车中的商品总数量
  function getTotalItemsCount(): number {
    return productAmount.value
  }

  return {
    productIds, productAmount,
    addProductId, removeProductId, clearProductIds, getAllProductIds, getTotalItemsCount
  }
})

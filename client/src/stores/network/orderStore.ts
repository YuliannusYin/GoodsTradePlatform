/**
 * @file orderStore.ts
 * @description 订单状态管理，处理下单、查询进行中订单、已下订单及获取配送方式
 * @input 收货地址、配送方式等下单参数
 * @output 下单结果、订单列表、可用的配送方式
 */
import { defineStore } from 'pinia'
import { callPost, callGet } from './requests'
import { useShoppingCartStore } from '../shoppingCartStore'

/**
 * 从购物车构建商品ID数组（将数量展开为重复的productId）
 * @returns {string[]} 展开后的商品ID数组
 */
function buildProductIdsFromCart(): string[] {
  const shoppingCartStore = useShoppingCartStore()
  const productIds: string[] = []
  for (const item of shoppingCartStore.getAllItems()) {
    for (let i = 0; i < item.quantity; i++) {
      productIds.push(item.productId)
    }
  }
  return productIds
}

/**
 * 订单状态管理Store
 * 职责：封装订单相关的API调用，包括下单和查询订单
 */
export const useOrderStore = defineStore('orderStore', () => {
  /**
   * 提交订单，将购物车中的商品生成订单
   * 构建商品ID数组（同一商品ID按数量重复出现），发送到后端下单
   * @param {string} receiverName - 收货人姓名
   * @param {string} receiverPhone - 收货人联系电话
   * @param {string} region - 省/市/区
   * @param {string} detailAddress - 详细地址
   * @param {string} deliveryMethod - 配送方式枚举值
   * @returns 下单结果响应
   */
  async function placeOrder(
    receiverName: string,
    receiverPhone: string,
    region: string,
    detailAddress: string,
    deliveryMethod: string
  ) {
    const shoppingCartStore = useShoppingCartStore()
    // 使用共享函数构建商品ID数组
    const productIds = buildProductIdsFromCart()
    const response = await callPost('/api/orders/place', {
      productIds,
      receiverName,
      receiverPhone,
      region,
      detailAddress,
      deliveryMethod
    })
    if (response) {
      // 下单成功后清空购物车
      shoppingCartStore.clearCart()
    }
    return response
  }

  /**
   * 获取进行中的订单信息（基于当前购物车商品计算）
   * @returns 进行中订单的详情
   */
  async function getOngoingOrder() {
    // 使用共享函数构建商品ID数组
    const productIds = buildProductIdsFromCart()
    return callPost('/api/orders/ongoing', {
      productIds
    })
  }

  /**
   * 获取当前用户所有已下订单
   * @returns 已下订单列表
   */
  async function getPlacedOrders() {
    return callGet('/api/orders/all')
  }

  return {
    placeOrder, getOngoingOrder, getPlacedOrders
  }
})

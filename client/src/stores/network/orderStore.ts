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
 * 订单状态管理Store
 * 职责：封装订单相关的API调用，包括下单、查询订单和获取配送方式
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
    // 构建商品ID数组（包含数量，同一商品ID重复出现）
    const productIds: string[] = []
    for (const item of shoppingCartStore.getAllItems()) {
      for (let i = 0; i < item.quantity; i++) {
        productIds.push(item.productId)
      }
    }
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
    const shoppingCartStore = useShoppingCartStore()
    // 构建商品ID数组（同一商品ID按数量重复出现），与placeOrder保持一致
    const productIds: string[] = []
    for (const item of shoppingCartStore.getAllItems()) {
      for (let i = 0; i < item.quantity; i++) {
        productIds.push(item.productId)
      }
    }
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

  /**
   * 获取可用的配送方式列表
   * @returns 配送方式列表
   */
  async function getAvailableDeliveryMethods() {
    return callGet('/api/orders/delivery/methods')
  }

  return {
    placeOrder, getOngoingOrder, getPlacedOrders,
    getAvailableDeliveryMethods
  }
})

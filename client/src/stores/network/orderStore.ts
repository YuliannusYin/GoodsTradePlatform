/**
 * @file orderStore.ts
 * @description 订单状态管理，处理下单、查询进行中订单、已下订单及获取配送和支付方式
 * @input 收货地址、配送方式、支付方式等下单参数
 * @output 下单结果、订单列表、可用的配送和支付方式
 */
import { defineStore } from 'pinia'
import { callPost, callGet } from './requests'
import { useShoppingCartStore } from '../shoppingCartStore'

/**
 * 订单状态管理Store
 * 职责：封装订单相关的API调用，包括下单、查询订单和获取配送支付方式
 */
export const useOrderStore = defineStore('orderStore', () => {
  /**
   * 提交订单，将购物车中的商品生成订单
   * @param {string} address - 收货地址
   * @param {string} deliveryMethod - 配送方式
   * @param {string} paymentMethod - 支付方式
   */
  async function placeOrder(address: string, deliveryMethod: string, paymentMethod: string) {
    const shoppingCartStore = useShoppingCartStore()
    const response = await callPost('/api/orders/place', {
      // 从购物车获取所有商品ID作为订单商品列表
      productIds: shoppingCartStore.getAllProductIds(),
      address,
      deliveryMethod,
      paymentMethod
    })
    if (response) {
      // 下单成功后清空购物车
      shoppingCartStore.clearProductIds()
    }
    return response
  }

  /**
   * 获取进行中的订单信息（基于当前购物车商品计算）
   * @returns 进行中订单的详情
   */
  async function getOngoingOrder() {
    const shoppingCartStore = useShoppingCartStore()
    return callPost('/api/orders/ongoing', {
      productIds: shoppingCartStore.getAllProductIds()
    })
  }

  // 获取当前用户所有已下订单
  async function getPlacedOrders() {
    return callGet('/api/orders/all')
  }

  // 获取可用的配送方式列表
  async function getAvailableDeliveryMethods() {
    return callGet('/api/orders/delivery/methods')
  }

  // 获取可用的支付方式列表
  async function getAvailablePaymentMethods() {
    return callGet('/api/orders/payment/methods')
  }

  return {
    placeOrder, getOngoingOrder, getPlacedOrders,
    getAvailableDeliveryMethods, getAvailablePaymentMethods
  }
})

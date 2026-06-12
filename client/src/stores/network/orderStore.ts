import { defineStore } from 'pinia'
import { callPost, callGet } from './requests'
import { useShoppingCartStore } from '../shoppingCartStore'

export const useOrderStore = defineStore('orderStore', () => {
  async function placeOrder(address: string, deliveryMethod: string, paymentMethod: string) {
    const shoppingCartStore = useShoppingCartStore()
    const response = await callPost('/api/orders/place', {
      productIds: shoppingCartStore.getAllProductIds(),
      address,
      deliveryMethod,
      paymentMethod
    })
    if (response) {
      shoppingCartStore.clearProductIds()
    }
    return response
  }

  async function getOngoingOrder() {
    const shoppingCartStore = useShoppingCartStore()
    return callPost('/api/orders/ongoing', {
      productIds: shoppingCartStore.getAllProductIds()
    })
  }

  async function getPlacedOrders() {
    return callGet('/api/orders/all')
  }

  async function getAvailableDeliveryMethods() {
    return callGet('/api/orders/delivery/methods')
  }

  async function getAvailablePaymentMethods() {
    return callGet('/api/orders/payment/methods')
  }

  return {
    placeOrder, getOngoingOrder, getPlacedOrders,
    getAvailableDeliveryMethods, getAvailablePaymentMethods
  }
})

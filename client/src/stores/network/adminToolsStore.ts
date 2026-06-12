import { defineStore } from 'pinia'
import type { CreateProductDto, Product } from '@/types/product'
import { callGet, callPost, callPut, callPatch, callDelete } from './requests'
import type { UserOrder } from '@/types/order'

export const useAdminToolsStore = defineStore('adminToolsStore', () => {
  async function addProduct(dto: CreateProductDto): Promise<Product> {
    return callPost('/admin_tools/product/add', dto)
  }

  async function editProduct(productId: string, dto: CreateProductDto): Promise<Product> {
    return callPut(`/admin_tools/product/edit/${productId}`, dto)
  }

  async function deleteProduct(productId: string): Promise<Product> {
    return callDelete(`/admin_tools/product/delete/${productId}`)
  }

  async function getAllOrders(): Promise<UserOrder[]> {
    return callGet('/admin_tools/order/all')
  }

  async function getAllOrdersWithStatus(status: string): Promise<UserOrder[]> {
    return callGet(`/admin_tools/order/all/${status}`)
  }

  async function sendOrder(orderId: string, expectedDelivery: string) {
    return callPatch('/admin_tools/order/send', {
      orderId,
      expectedDelivery
    })
  }

  async function changeExpectedDelivery(orderId: string, newExpectedDelivery: string) {
    return callPatch('/admin_tools/order/expected_delivery', {
      orderId,
      newExpectedDelivery
    })
  }

  // Product Review APIs
  async function getPendingProducts(): Promise<Product[]> {
    return callGet('/admin_tools/product/pending')
  }

  async function getProductsByStatus(status: string): Promise<Product[]> {
    return callGet(`/admin_tools/product/status/${status}`)
  }

  async function approveProduct(productId: string): Promise<Product> {
    return callPatch(`/admin_tools/product/approve/${productId}`, {})
  }

  async function rejectProduct(productId: string, rejectReason: string): Promise<Product> {
    return callPatch(`/admin_tools/product/reject/${productId}?rejectReason=${encodeURIComponent(rejectReason)}`, {})
  }

  async function disableProduct(productId: string): Promise<Product> {
    return callPatch(`/admin_tools/product/disable/${productId}`, {})
  }

  async function enableProduct(productId: string): Promise<Product> {
    return callPatch(`/admin_tools/product/enable/${productId}`, {})
  }

  return {
    addProduct, editProduct, deleteProduct,
    getAllOrders, getAllOrdersWithStatus,
    sendOrder, changeExpectedDelivery,
    getPendingProducts, getProductsByStatus,
    approveProduct, rejectProduct, disableProduct, enableProduct
  }
})

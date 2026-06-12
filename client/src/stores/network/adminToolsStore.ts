import { defineStore } from 'pinia'
import type { CreateProductDto, EditProductDto, Product } from '@/types/product'
import { callGet, callPost, callPut, callPatch, callDelete } from './requests'
import type { UserOrder } from '@/types/order'

export const useAdminToolsStore = defineStore('adminToolsStore', () => {
  const API = {
    addProduct: async (dto: CreateProductDto): Promise<Product> =>
      await callPost(`/admin_tools/product/add`, dto),

    editProduct: async (productId: string, dto: EditProductDto): Promise<Product> =>
      await callPut(`/admin_tools/product/edit/${productId}`, dto),

    deleteProduct: async (productId: string): Promise<Product> =>
      await callDelete(`/admin_tools/product/delete/${productId}`),

    getAllOrders: async (): Promise<UserOrder[]> => await callGet(`/admin_tools/order/all`),

    getAllOrdersWithStatus: async (status: string): Promise<UserOrder[]> =>
      await callGet(`/admin_tools/order/all/${status}`),

    sendOrder: async (orderId: string, expectedDelivery: string) =>
      await callPatch(`/admin_tools/order/send`, {
        orderId: orderId,
        expectedDelivery: expectedDelivery
      }),

    changeExpectedDelivery: async (orderId: string, newExpectedDelivery: string) =>
      await callPatch(`/admin_tools/order/expected_delivery`, {
        orderId: orderId,
        newExpectedDelivery: newExpectedDelivery
      }),

    // Product Review APIs
    getPendingProducts: async (): Promise<Product[]> =>
      await callGet('/admin_tools/product/pending'),

    getProductsByStatus: async (status: string): Promise<Product[]> =>
      await callGet(`/admin_tools/product/status/${status}`),

    approveProduct: async (productId: string): Promise<Product> =>
      await callPatch(`/admin_tools/product/approve/${productId}`, {}),

    rejectProduct: async (productId: string, rejectReason: string): Promise<Product> =>
      await callPatch(`/admin_tools/product/reject/${productId}`, { rejectReason }),

    disableProduct: async (productId: string): Promise<Product> =>
      await callPatch(`/admin_tools/product/disable/${productId}`, {}),

    enableProduct: async (productId: string): Promise<Product> =>
      await callPatch(`/admin_tools/product/enable/${productId}`, {})
  }

  return {
    API
  }
})

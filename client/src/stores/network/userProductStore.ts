import { defineStore } from 'pinia'
import type { Product, CreateProductDto, EditProductDto } from '@/types/product'
import { callGet, callPost, callPut, callDelete } from './requests'

export const useUserProductStore = defineStore('userProductStore', () => {
  const API = {
    addProduct: async (dto: CreateProductDto): Promise<Product> =>
      await callPost('/user_products/add', dto),

    getMyProducts: async (): Promise<Product[]> =>
      await callGet('/user_products/my'),

    editProduct: async (productId: string, dto: EditProductDto): Promise<Product> =>
      await callPut(`/user_products/edit/${productId}`, dto),

    deleteProduct: async (productId: string): Promise<Product> =>
      await callDelete(`/user_products/delete/${productId}`)
  }

  return {
    API
  }
})

import { defineStore } from 'pinia'
import type { Product, CreateProductDto } from '@/types/product'
import { callGet, callPost } from './requests'

export const useUserProductStore = defineStore('userProductStore', () => {
  const API = {
    addProduct: async (dto: CreateProductDto): Promise<Product> =>
      await callPost('/user_products/add', dto),

    getMyProducts: async (): Promise<Product[]> =>
      await callGet('/user_products/my')
  }

  return {
    API
  }
})

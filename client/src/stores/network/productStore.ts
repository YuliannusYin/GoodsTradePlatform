import { defineStore } from 'pinia'
import type { Product } from '@/types/product'
import { callGet } from './requests'

export const useProductStore = defineStore('productStore', () => {
  const API = {
    getAllProducts: async (): Promise<Product[]> => await callGet('/products/all'),

    getProduct: async (productId: string | null): Promise<Product> =>
      await callGet(`/products/${productId}`),

    getSearchedProducts: async (searchInput: string, filter: string, category?: string): Promise<Product[]> => {
      let endpoint = `/products/search?query=${searchInput}&filter=${filter}`
      if (category) {
        endpoint += `&category=${category}`
      }
      return await callGet(endpoint)
    },

    getFeaturedProducts: async (): Promise<Product[]> => await callGet('/products/featured'),

    getProductsByCategory: async (category: string): Promise<Product[]> =>
      await callGet(`/products/category/${category}`),

    getCategories: async (): Promise<string[]> => await callGet('/products/categories'),

    getConditions: async (): Promise<string[]> => await callGet('/products/conditions')
  }

  return {
    API
  }
})

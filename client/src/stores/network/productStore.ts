import { defineStore } from 'pinia'
import type { Product, CreateProductDto } from '@/types/product'
import { callGet, callPost, callPut, callDelete } from './requests'

export const useProductStore = defineStore('productStore', () => {
  async function getAllProducts(): Promise<Product[]> {
    return callGet('/api/products/all')
  }

  async function getProduct(productId: string | null): Promise<Product> {
    return callGet(`/api/products/${productId}`)
  }

  async function getSearchedProducts(searchInput: string, filter: string, category?: string): Promise<Product[]> {
    let endpoint = `/api/products/search?query=${searchInput}&filter=${filter}`
    if (category) {
      endpoint += `&category=${category}`
    }
    return callGet(endpoint)
  }

  async function getFeaturedProducts(): Promise<Product[]> {
    return callGet('/api/products/featured')
  }

  async function getProductsByCategory(category: string): Promise<Product[]> {
    return callGet(`/api/products/category/${category}`)
  }

  async function getCategories(): Promise<string[]> {
    return callGet('/api/products/categories')
  }

  async function getConditions(): Promise<string[]> {
    return callGet('/api/products/conditions')
  }

  async function addMyProduct(dto: CreateProductDto): Promise<Product> {
    return callPost('/api/user_products/add', dto)
  }

  async function getMyProducts(): Promise<Product[]> {
    return callGet('/api/user_products/my')
  }

  async function editMyProduct(productId: string, dto: CreateProductDto): Promise<Product> {
    return callPut(`/api/user_products/edit/${productId}`, dto)
  }

  async function deleteMyProduct(productId: string): Promise<Product> {
    return callDelete(`/api/user_products/delete/${productId}`)
  }

  return {
    getAllProducts, getProduct, getSearchedProducts, getFeaturedProducts,
    getProductsByCategory, getCategories, getConditions,
    addMyProduct, getMyProducts, editMyProduct, deleteMyProduct
  }
})

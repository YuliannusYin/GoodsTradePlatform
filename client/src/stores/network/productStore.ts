/**
 * @file productStore.ts
 * @description 商品状态管理，提供商品的查询、搜索及商户自有商品的增删改操作
 * @input 商品ID、搜索关键词、分类名称、商品创建DTO等
 * @output 商品列表、单个商品详情等
 */
import { defineStore } from 'pinia'
import type { Product, CreateProductDto } from '@/types/product'
import { callGet, callPost, callPut, callDelete } from './requests'

/**
 * 商品状态管理Store
 * 职责：封装商品相关的API调用，包括公共商品查询和商户商品管理
 */
export const useProductStore = defineStore('productStore', () => {
  // 获取所有商品列表
  async function getAllProducts(): Promise<Product[]> {
    return callGet('/api/products/all')
  }

  /**
   * 根据商品ID获取单个商品详情
   * @param {string | null} productId - 商品ID
   * @returns {Promise<Product>} 商品详情
   */
  async function getProduct(productId: string | null): Promise<Product> {
    return callGet(`/api/products/${productId}`)
  }

  /**
   * 根据搜索关键词、筛选条件和分类搜索商品
   * @param {string} searchInput - 搜索关键词
   * @param {string} filter - 筛选条件
   * @param {string} [category] - 可选的商品分类
   * @returns {Promise<Product[]>} 搜索结果商品列表
   */
  async function getSearchedProducts(searchInput: string, filter: string, category?: string): Promise<Product[]> {
    let endpoint = `/api/products/search?query=${searchInput}&filter=${filter}`
    if (category) {
      // 指定了分类时，追加分类参数到查询路径
      endpoint += `&category=${category}`
    }
    return callGet(endpoint)
  }

  // 获取推荐/精选商品列表
  async function getFeaturedProducts(): Promise<Product[]> {
    return callGet('/api/products/featured')
  }

  /**
   * 随机获取推荐商品列表
   * @param {number} count - 获取数量，默认8个
   * @returns {Promise<Product[]>} 随机排序的商品列表
   */
  async function getRandomProducts(count: number = 8): Promise<Product[]> {
    return callGet(`/api/products/random?count=${count}`)
  }

  /**
   * 商户添加自有商品
   * @param {CreateProductDto} dto - 商品创建数据
   * @returns {Promise<Product>} 新创建的商品
   */
  async function addMyProduct(dto: CreateProductDto): Promise<Product> {
    return callPost('/api/user_products/add', dto)
  }

  // 获取商户自有商品列表
  async function getMyProducts(): Promise<Product[]> {
    return callGet('/api/user_products/my')
  }

  /**
   * 商户编辑自有商品
   * @param {string} productId - 要编辑的商品ID
   * @param {CreateProductDto} dto - 更新后的商品数据
   * @returns {Promise<Product>} 编辑后的商品
   */
  async function editMyProduct(productId: string, dto: CreateProductDto): Promise<Product> {
    return callPut(`/api/user_products/edit/${productId}`, dto)
  }

  /**
   * 商户删除自有商品
   * @param {string} productId - 要删除的商品ID
   * @returns {Promise<Product>} 被删除的商品
   */
  async function deleteMyProduct(productId: string): Promise<Product> {
    return callDelete(`/api/user_products/delete/${productId}`)
  }

  return {
    getAllProducts, getProduct, getSearchedProducts, getFeaturedProducts, getRandomProducts,
    addMyProduct, getMyProducts, editMyProduct, deleteMyProduct
  }
})

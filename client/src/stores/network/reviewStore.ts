/**
 * @file reviewStore.ts
 * @description 评价状态管理，提供商品评价查询、评分查询和添加评价操作
 * @input 商品ID、评价DTO
 * @output 评价列表、商品评分、评价操作结果
 */
import { defineStore } from 'pinia'
import type { Review, ProductRating, CreateReviewDto } from '@/types/review'
import { callGet, callPost } from './requests'

/**
 * 评价状态管理Store
 * 职责：封装商品评价相关的API调用，包括查询评价、评分和添加评价
 */
export const useReviewStore = defineStore('reviewStore', () => {
  /**
   * 获取指定商品的所有评价列表
   * @param {string} productId - 商品ID
   * @returns {Promise<Review[]>} 评价列表
   */
  async function getProductReviews(productId: string): Promise<Review[]> {
    return callGet(`/api/reviews/product/${productId}`)
  }

  /**
   * 获取指定商品的评分统计信息
   * @param {string} productId - 商品ID
   * @returns {Promise<ProductRating>} 评分统计数据
   */
  async function getProductRating(productId: string): Promise<ProductRating> {
    return callGet(`/api/reviews/product/${productId}/rating`)
  }

  /**
   * 添加商品评价
   * @param {CreateReviewDto} dto - 评价创建数据
   * @returns {Promise<Review>} 新创建的评价
   */
  async function addReview(dto: CreateReviewDto): Promise<Review> {
    return callPost('/api/reviews/add', dto)
  }

  return {
    getProductReviews, getProductRating, addReview
  }
})

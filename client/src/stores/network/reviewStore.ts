import { defineStore } from 'pinia'
import type { Review, ProductRating, CreateReviewDto } from '@/types/review'
import { callGet, callPost, callDelete } from './requests'

export const useReviewStore = defineStore('reviewStore', () => {
  async function getProductReviews(productId: string): Promise<Review[]> {
    return callGet(`/reviews/product/${productId}`)
  }

  async function getProductRating(productId: string): Promise<ProductRating> {
    return callGet(`/reviews/product/${productId}/rating`)
  }

  async function addReview(dto: CreateReviewDto): Promise<Review> {
    return callPost('/reviews/add', dto)
  }

  async function deleteReview(reviewId: string): Promise<void> {
    return callDelete(`/reviews/${reviewId}`)
  }

  return {
    getProductReviews, getProductRating, addReview, deleteReview
  }
})

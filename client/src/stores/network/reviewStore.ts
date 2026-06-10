import { defineStore } from 'pinia'
import type { Review, ProductRating, CreateReviewDto } from '@/types/review'
import { callGet, callPost, callDelete } from './requests'

export const useReviewStore = defineStore('reviewStore', () => {
  const API = {
    getProductReviews: async (productId: string): Promise<Review[]> =>
      await callGet(`/reviews/product/${productId}`),

    getProductRating: async (productId: string): Promise<ProductRating> =>
      await callGet(`/reviews/product/${productId}/rating`),

    addReview: async (dto: CreateReviewDto): Promise<Review> =>
      await callPost('/reviews/add', dto),

    deleteReview: async (reviewId: string): Promise<void> =>
      await callDelete(`/reviews/${reviewId}`)
  }

  return {
    API
  }
})

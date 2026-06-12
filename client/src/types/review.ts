export interface Review {
  id: string
  rating: number
  comment: string
  createdAt: string
  username: string
  productId: string
}

export interface ProductRating {
  averageRating: number
  reviewCount: number
}

export interface CreateReviewDto {
  productId: string
  rating: number
  comment: string
}

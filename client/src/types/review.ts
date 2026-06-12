/**
 * @file review.ts
 * @description 定义商品评价相关的数据类型，包括评价信息、评分统计及创建评价的数据传输对象
 * @input 无
 * @output Review, ProductRating, CreateReviewDto 接口
 */

/**
 * 商品评价数据结构，表示一条用户评价
 */
export interface Review {
  /** 评价 ID */
  id: string
  /** 评分（1-5） */
  rating: number
  /** 评价内容 */
  comment: string
  /** 评价创建时间 */
  createdAt: string
  /** 评价者用户名 */
  username: string
  /** 关联的商品 ID */
  productId: string
}

/**
 * 商品评分统计信息
 */
export interface ProductRating {
  /** 平均评分 */
  averageRating: number
  /** 评价总数 */
  reviewCount: number
}

/**
 * 创建评价的数据传输对象
 */
export interface CreateReviewDto {
  /** 商品 ID */
  productId: string
  /** 评分（1-5） */
  rating: number
  /** 评价内容 */
  comment: string
}

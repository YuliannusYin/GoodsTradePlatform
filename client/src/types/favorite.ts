/**
 * @file favorite.ts
 * @description 定义用户收藏（收藏夹）相关的数据类型
 * @input 无
 * @output Favorite 接口
 */

/**
 * 用户收藏项数据结构，表示一个被收藏的商品
 */
export interface Favorite {
  /** 收藏记录唯一标识 */
  id: string
  /** 收藏的商品 ID */
  productId: string
  /** 商品名称 */
  productName: string
  /** 商品图片 URL */
  imageUrl: string
  /** 商品价格 */
  price: number
  /** 收藏创建时间 */
  createdAt: string
}

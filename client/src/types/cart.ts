/**
 * @file cart.ts
 * @description 定义购物车相关的数据类型
 * @input 无
 * @output CartItem 接口
 */

/**
 * 购物车项，表示购物车中的单个商品条目
 */
export interface CartItem {
  /** 商品 ID */
  productId: string
  /** 购买数量 */
  quantity: number
}

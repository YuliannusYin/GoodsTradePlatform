/**
 * @file product.ts
 * @description 定义商品相关的数据类型，包括商品信息、商品状态、分类、成色等常量映射
 * @input 无
 * @output Product, UnavailableProduct, CreateProductDto 等类型及商品常量
 */

/** 商品审核状态类型：待审核、已通过、已拒绝、已禁用 */
export type ProductStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'DISABLED'

/**
 * 商品数据结构，表示一个完整的商品信息
 */
export interface Product {
  /** 商品 ID */
  id: string
  /** 商品名称 */
  name: string
  /** 商品描述 */
  description: string
  /** 商品图片 URL 列表 */
  imageUrls: string[]
  /** 商品价格 */
  price: number
  /** 库存数量 */
  quantity: number
  /** 商品分类 */
  category: string
  /** 商品成色/新旧程度 */
  condition: string
  /** 商品来源 */
  source: string
  /** 商品审核状态 */
  status: ProductStatus
  /** 拒绝原因，仅在状态为 REJECTED 时有值 */
  rejectReason: string | null
  /** 卖家信息，可能为空 */
  seller: {
    /** 卖家用户 ID */
    id: string
    /** 卖家用户名 */
    username: string
  } | null
}

/**
 * 不可用商品数据结构，表示库存不足等原因无法购买的商品
 */
export interface UnavailableProduct {
  /** 不可用原因说明 */
  message: string
  /** 商品 ID */
  productId: string
  /** 用户请求购买的数量 */
  requestedAmount: number
  /** 实际可用库存数量 */
  availableAmount: number
}

/**
 * 创建商品的数据传输对象
 */
export interface CreateProductDto {
  /** 商品名称 */
  name: string
  /** 商品描述 */
  description: string
  /** 商品图片 URL 列表 */
  imageUrls: string[]
  /** 商品价格 */
  price: number
  /** 库存数量 */
  quantity: number
  /** 商品分类 */
  category: string
  /** 商品成色 */
  condition: string
  /** 商品来源 */
  source: string
}

/** 商品分类映射：英文键 -> 中文显示名 */
export const PRODUCT_CATEGORIES: Record<string, string> = {
  ANIME_FIGURE: '手办',
  POSTER: '海报',
  KEYCHAIN: '钥匙扣',
  BADGE: '徽章',
  PILLOW: '抱枕',
  STAND: '立牌',
  CLOTHING: '服饰',
  ALBUM: '专辑',
  ACCESSORY: '配件',
  OTHER: '其他'
}

/** 商品成色映射：英文键 -> 中文显示名 */
export const PRODUCT_CONDITIONS: Record<string, string> = {
  NEW: '全新',
  LIKE_NEW: '几乎全新',
  GOOD: '良好',
  FAIR: '一般'
}

/** 商品审核状态映射：英文键 -> 中文显示名 */
export const PRODUCT_STATUSES: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  DISABLED: '已禁用'
}

/** 商品审核状态对应的 Tailwind CSS 样式类映射 */
export const PRODUCT_STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
  DISABLED: 'bg-gray-100 text-gray-800'
}

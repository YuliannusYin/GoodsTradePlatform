export type ProductStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'DISABLED'

export interface Product {
  id: string
  name: string
  description: string
  imageUrls: string[]
  price: number
  quantity: number
  category: string
  condition: string
  source: string
  status: ProductStatus
  rejectReason: string | null
  seller?: {
    id: string
    username: string
  }
}

export interface UnavailableProduct {
  message: string;
  productId: string;
  requestedAmount: number;
  availableAmount: number;
}

export interface CreateProductDto {
  name: string
  description: string
  imageUrls: string[]
  price: number
  quantity: number
  category: string
  condition: string
  source: string
}

export type EditProductDto = Partial<CreateProductDto>

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

export const PRODUCT_CONDITIONS: Record<string, string> = {
  NEW: '全新',
  LIKE_NEW: '几乎全新',
  GOOD: '良好',
  FAIR: '一般'
}

export const PRODUCT_STATUSES: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  DISABLED: '已禁用'
}

export const PRODUCT_STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  APPROVED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
  DISABLED: 'bg-gray-100 text-gray-800'
}

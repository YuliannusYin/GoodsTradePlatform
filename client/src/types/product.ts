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

export interface EditProductDto {
  name: string
  description: string
  imageUrls: string[]
  price: number
  quantity: number
  category: string
  condition: string
  source: string
}

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

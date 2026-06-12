import type { UnavailableProduct } from './product'

interface OrderItemProduct {
  id: string
  name: string
  price: number
  imageUrls: string[]
}

interface OrderItem {
  id: string
  amount: number
  price: number
  product: OrderItemProduct
}

export interface Order {
  id: string
  price: number
  status: string
  paymentMethod: string
  deliveryMethod: string
  address: string
  received: string
  expectedDelivery: string | null
  items: OrderItem[]
}

export interface OngoingOrder {
  items: OrderItem[]
  totalPrice: number
  unavailableProducts?: UnavailableProduct[]
}

export interface PlacedOrder {
  id: string
  price: number
  status: string
  address: string
  deliveryMethod: string
  paymentMethod: string
  received: string
  expectedDelivery: string | null
  items: OrderItem[]
}

export interface UserOrder {
  id: string
  userEmail: string
  username: string
  price: number
  status: string
  received: string
  expectedDelivery: string | null
  items: OrderItem[]
}

export enum OrderStatus {
  PENDING,
  SHIPPED,
  DELIVERED
}

export function orderStatusToString(status: OrderStatus): string {
  switch (status) {
    case OrderStatus.PENDING:
      return 'PENDING'
    case OrderStatus.SHIPPED:
      return 'SHIPPED'
    case OrderStatus.DELIVERED:
      return 'DELIVERED'
    default:
      return 'INVALID_STATUS'
  }
}

export const DELIVERY_METHODS: Record<string, string> = {
  STANDARD_DELIVERY: '普通快递',
  EXPRESS_DELIVERY: '加急快递'
}

export const PAYMENT_METHODS: Record<string, string> = {
  ALIPAY: '支付宝',
  WECHAT_PAY: '微信支付',
  CASH_ON_DELIVERY: '货到付款'
}

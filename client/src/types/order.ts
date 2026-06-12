/**
 * @file order.ts
 * @description 定义订单相关的数据类型，包括订单项、订单状态、配送方式及支付方式
 * @input 无
 * @output Order, OngoingOrder, PlacedOrder, UserOrder, OrderStatus 等类型及常量
 */

import type { UnavailableProduct } from './product'

/**
 * 订单项中的商品信息
 */
interface OrderItemProduct {
  /** 商品 ID */
  id: string
  /** 商品名称 */
  name: string
  /** 商品单价 */
  price: number
  /** 商品图片 URL 列表 */
  imageUrls: string[]
}

/**
 * 订单项，表示订单中的单个商品条目
 */
interface OrderItem {
  /** 订单项 ID */
  id: string
  /** 购买数量 */
  amount: number
  /** 订单项总价 */
  price: number
  /** 关联的商品信息 */
  product: OrderItemProduct
}

/**
 * 通用订单数据结构
 */
export interface Order {
  /** 订单 ID */
  id: string
  /** 订单总价 */
  price: number
  /** 订单状态 */
  status: string
  /** 支付方式 */
  paymentMethod: string
  /** 配送方式 */
  deliveryMethod: string
  /** 收货地址 */
  address: string
  /** 下单时间 */
  received: string
  /** 预计送达时间，可能为空 */
  expectedDelivery: string | null
  /** 订单商品列表 */
  items: OrderItem[]
}

/**
 * 进行中的订单（购物车结算时使用），包含不可用商品信息
 */
export interface OngoingOrder {
  /** 订单商品列表 */
  items: OrderItem[]
  /** 订单总价 */
  totalPrice: number
  /** 不可用商品列表（库存不足等） */
  unavailableProducts?: UnavailableProduct[]
}

/**
 * 已提交的订单数据结构
 */
export interface PlacedOrder {
  /** 订单 ID */
  id: string
  /** 订单总价 */
  price: number
  /** 订单状态 */
  status: string
  /** 收货地址 */
  address: string
  /** 配送方式 */
  deliveryMethod: string
  /** 支付方式 */
  paymentMethod: string
  /** 下单时间 */
  received: string
  /** 预计送达时间，可能为空 */
  expectedDelivery: string | null
  /** 订单商品列表 */
  items: OrderItem[]
}

/**
 * 管理员视角的用户订单数据结构，包含用户信息
 */
export interface UserOrder {
  /** 订单 ID */
  id: string
  /** 用户邮箱 */
  userEmail: string
  /** 用户名 */
  username: string
  /** 订单总价 */
  price: number
  /** 订单状态 */
  status: string
  /** 下单时间 */
  received: string
  /** 预计送达时间，可能为空 */
  expectedDelivery: string | null
  /** 订单商品列表 */
  items: OrderItem[]
}

/**
 * 订单状态枚举
 */
export enum OrderStatus {
  /** 待处理 */
  PENDING,
  /** 已发货 */
  SHIPPED,
  /** 已送达 */
  DELIVERED
}

/**
 * 将订单状态枚举转换为字符串
 * @param {OrderStatus} status - 订单状态枚举值
 * @returns {string} 状态对应的字符串表示
 */
export function orderStatusToString(status: OrderStatus): string {
  switch (status) {
    case OrderStatus.PENDING:
      // 待处理状态
      return 'PENDING'
    case OrderStatus.SHIPPED:
      // 已发货状态
      return 'SHIPPED'
    case OrderStatus.DELIVERED:
      // 已送达状态
      return 'DELIVERED'
    default:
      // 未知状态
      return 'INVALID_STATUS'
  }
}

/** 配送方式映射：英文键 -> 中文显示名 */
export const DELIVERY_METHODS: Record<string, string> = {
  STANDARD_DELIVERY: '普通快递',
  EXPRESS_DELIVERY: '加急快递'
}

/** 支付方式映射：英文键 -> 中文显示名 */
export const PAYMENT_METHODS: Record<string, string> = {
  ALIPAY: '支付宝',
  WECHAT_PAY: '微信支付',
  CASH_ON_DELIVERY: '货到付款'
}

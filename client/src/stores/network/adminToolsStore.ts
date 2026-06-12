/**
 * @file adminToolsStore.ts
 * @description 管理员工具状态管理，提供商品管理（增删改、审核、启用禁用）和订单管理（查询、发货、修改预计送达时间）等管理端操作
 * @input 商品DTO、商品ID、订单ID、状态、拒绝原因等管理操作参数
 * @output 操作结果、商品列表、订单列表等
 */
import { defineStore } from 'pinia'
import type { CreateProductDto, Product } from '@/types/product'
import { callGet, callPost, callPut, callPatch, callDelete } from './requests'
import type { UserOrder } from '@/types/order'

/**
 * 管理员工具状态管理Store
 * 职责：封装管理员专属的API调用，包括商品管理和订单管理操作
 */
export const useAdminToolsStore = defineStore('adminToolsStore', () => {
  /**
   * 管理员添加商品
   * @param {CreateProductDto} dto - 商品创建数据
   * @returns {Promise<Product>} 新创建的商品
   */
  async function addProduct(dto: CreateProductDto): Promise<Product> {
    return callPost('/api/admin_tools/product/add', dto)
  }

  /**
   * 管理员编辑商品
   * @param {string} productId - 商品ID
   * @param {CreateProductDto} dto - 更新后的商品数据
   * @returns {Promise<Product>} 编辑后的商品
   */
  async function editProduct(productId: string, dto: CreateProductDto): Promise<Product> {
    return callPut(`/api/admin_tools/product/edit/${productId}`, dto)
  }

  /**
   * 管理员删除商品
   * @param {string} productId - 要删除的商品ID
   * @returns {Promise<Product>} 被删除的商品
   */
  async function deleteProduct(productId: string): Promise<Product> {
    return callDelete(`/api/admin_tools/product/delete/${productId}`)
  }

  // 获取所有订单列表
  async function getAllOrders(): Promise<UserOrder[]> {
    return callGet('/api/admin_tools/order/all')
  }

  /**
   * 根据订单状态获取订单列表
   * @param {string} status - 订单状态筛选条件
   * @returns {Promise<UserOrder[]>} 符合状态的订单列表
   */
  async function getAllOrdersWithStatus(status: string): Promise<UserOrder[]> {
    return callGet(`/api/admin_tools/order/all/${status}`)
  }

  /**
   * 发货操作，设置订单为已发货并指定预计送达时间
   * @param {string} orderId - 订单ID
   * @param {string} expectedDelivery - 预计送达时间
   */
  async function sendOrder(orderId: string, expectedDelivery: string) {
    return callPatch('/api/admin_tools/order/send', {
      orderId,
      expectedDelivery
    })
  }

  /**
   * 修改订单的预计送达时间
   * @param {string} orderId - 订单ID
   * @param {string} newExpectedDelivery - 新的预计送达时间
   */
  async function changeExpectedDelivery(orderId: string, newExpectedDelivery: string) {
    return callPatch('/api/admin_tools/order/expected_delivery', {
      orderId,
      newExpectedDelivery
    })
  }

  // 获取待审核商品列表
  async function getPendingProducts(): Promise<Product[]> {
    return callGet('/api/admin_tools/product/pending')
  }

  /**
   * 根据商品审核状态获取商品列表
   * @param {string} status - 商品状态筛选条件
   * @returns {Promise<Product[]>} 符合状态的商品列表
   */
  async function getProductsByStatus(status: string): Promise<Product[]> {
    return callGet(`/api/admin_tools/product/status/${status}`)
  }

  /**
   * 审核通过商品
   * @param {string} productId - 商品ID
   * @returns {Promise<Product>} 审核后的商品
   */
  async function approveProduct(productId: string): Promise<Product> {
    return callPatch(`/api/admin_tools/product/approve/${productId}`, {})
  }

  /**
   * 审核拒绝商品
   * @param {string} productId - 商品ID
   * @param {string} rejectReason - 拒绝原因
   * @returns {Promise<Product>} 审核后的商品
   */
  async function rejectProduct(productId: string, rejectReason: string): Promise<Product> {
    // 拒绝原因需要进行URL编码，防止特殊字符导致请求异常
    return callPatch(`/api/admin_tools/product/reject/${productId}?rejectReason=${encodeURIComponent(rejectReason)}`, {})
  }

  /**
   * 禁用商品（下架）
   * @param {string} productId - 商品ID
   * @returns {Promise<Product>} 禁用后的商品
   */
  async function disableProduct(productId: string): Promise<Product> {
    return callPatch(`/api/admin_tools/product/disable/${productId}`, {})
  }

  /**
   * 启用商品（上架）
   * @param {string} productId - 商品ID
   * @returns {Promise<Product>} 启用后的商品
   */
  async function enableProduct(productId: string): Promise<Product> {
    return callPatch(`/api/admin_tools/product/enable/${productId}`, {})
  }

  return {
    addProduct, editProduct, deleteProduct,
    getAllOrders, getAllOrdersWithStatus,
    sendOrder, changeExpectedDelivery,
    getPendingProducts, getProductsByStatus,
    approveProduct, rejectProduct, disableProduct, enableProduct
  }
})

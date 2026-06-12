/**
 * @file favoriteStore.ts
 * @description 收藏状态管理，提供用户收藏列表查询、添加收藏、移除收藏及收藏状态检查
 * @input 商品ID
 * @output 收藏列表、收藏操作结果、收藏状态布尔值
 */
import { defineStore } from 'pinia'
import type { Favorite } from '@/types/favorite'
import { callGet, callPost, callDelete } from './requests'

/**
 * 收藏状态管理Store
 * 职责：封装用户收藏相关的API调用，包括查询、添加、移除和检查收藏状态
 */
export const useFavoriteStore = defineStore('favoriteStore', () => {
  // 获取当前用户的所有收藏列表
  async function getUserFavorites(): Promise<Favorite[]> {
    return callGet('/api/favorites/list')
  }

  /**
   * 添加商品到收藏
   * @param {string} productId - 要收藏的商品ID
   * @returns {Promise<any>} 操作结果
   */
  async function addFavorite(productId: string): Promise<any> {
    // 商品ID进行URL编码，防止特殊字符导致请求异常
    return callPost(`/api/favorites/add?productId=${encodeURIComponent(productId)}`)
  }

  /**
   * 从收藏中移除商品
   * @param {string} productId - 要移除收藏的商品ID
   * @returns {Promise<any>} 操作结果
   */
  async function removeFavorite(productId: string): Promise<any> {
    return callDelete(`/api/favorites/remove/${productId}`)
  }

  /**
   * 检查指定商品是否已被当前用户收藏
   * @param {string} productId - 商品ID
   * @returns {Promise<boolean>} 是否已收藏
   */
  async function isFavorite(productId: string): Promise<boolean> {
    return callGet(`/api/favorites/check/${productId}`)
  }

  return {
    getUserFavorites, addFavorite, removeFavorite, isFavorite
  }
})

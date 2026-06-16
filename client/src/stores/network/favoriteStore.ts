/**
 * @file favoriteStore.ts
 * @description 收藏状态管理，提供用户收藏列表查询、添加收藏、移除收藏及收藏状态检查
 * @input 商品ID
 * @output 收藏列表、收藏操作结果、收藏状态布尔值
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Favorite } from '@/types/favorite'
import { callGet, callPost, callDelete } from './requests'

/**
 * 收藏状态管理Store
 * 职责：封装用户收藏相关的API调用，包括查询、添加、移除和检查收藏状态
 * 同时维护收藏商品ID缓存，避免N+1请求问题
 */
export const useFavoriteStore = defineStore('favoriteStore', () => {
  // 已收藏的商品ID列表（缓存，用于批量判断收藏状态，避免逐个请求）
  const favoriteProductIds = ref<string[]>([])
  // 收藏缓存是否已加载
  const favoritesLoaded = ref(false)

  /**
   * 批量加载当前用户的所有收藏商品ID，填充缓存
   * 应在商品列表页面挂载时调用一次，后续 isFavorite 检查直接读缓存
   */
  async function loadFavoriteIds(): Promise<void> {
    const favorites = await callGet<Favorite[]>('/api/favorites/list')
    favoriteProductIds.value = favorites.map(f => f.productId)
    favoritesLoaded.value = true
  }

  /**
   * 获取当前用户的所有收藏列表
   * @returns {Promise<Favorite[]>} 收藏列表
   */
  async function getUserFavorites(): Promise<Favorite[]> {
    return callGet('/api/favorites/list')
  }

  /**
   * 添加商品到收藏，同时更新缓存
   * @param {string} productId - 要收藏的商品ID
   * @returns {Promise<void>} 操作结果
   */
  async function addFavorite(productId: string): Promise<void> {
    await callPost(`/api/favorites/add?productId=${encodeURIComponent(productId)}`)
    // 缓存已加载时同步更新，避免重新请求
    if (favoritesLoaded.value && !favoriteProductIds.value.includes(productId)) {
      favoriteProductIds.value.push(productId)
    }
  }

  /**
   * 从收藏中移除商品，同时更新缓存
   * @param {string} productId - 要移除收藏的商品ID
   * @returns {Promise<void>} 操作结果
   */
  async function removeFavorite(productId: string): Promise<void> {
    await callDelete(`/api/favorites/remove/${productId}`)
    // 缓存已加载时同步更新，避免重新请求
    if (favoritesLoaded.value) {
      favoriteProductIds.value = favoriteProductIds.value.filter(id => id !== productId)
    }
  }

  /**
   * 检查指定商品是否已被当前用户收藏
   * 优先使用缓存，缓存未加载时回退到单独API请求
   * @param {string} productId - 商品ID
   * @returns {Promise<boolean>} 是否已收藏
   */
  async function isFavorite(productId: string): Promise<boolean> {
    // 缓存已加载时直接从缓存读取，无需额外请求
    if (favoritesLoaded.value) {
      return favoriteProductIds.value.includes(productId)
    }
    // 缓存未加载时回退到单独检查接口
    return callGet(`/api/favorites/check/${productId}`)
  }

  return {
    favoriteProductIds, favoritesLoaded,
    loadFavoriteIds, getUserFavorites, addFavorite, removeFavorite, isFavorite
  }
})

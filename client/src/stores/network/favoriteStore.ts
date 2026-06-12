import { defineStore } from 'pinia'
import type { Favorite } from '@/types/favorite'
import { callGet, callPost, callDelete } from './requests'

export const useFavoriteStore = defineStore('favoriteStore', () => {
  async function getUserFavorites(): Promise<Favorite[]> {
    return callGet('/api/favorites/list')
  }

  async function addFavorite(productId: string): Promise<any> {
    return callPost(`/api/favorites/add?productId=${encodeURIComponent(productId)}`)
  }

  async function removeFavorite(productId: string): Promise<any> {
    return callDelete(`/api/favorites/remove/${productId}`)
  }

  async function isFavorite(productId: string): Promise<boolean> {
    return callGet(`/api/favorites/check/${productId}`)
  }

  return {
    getUserFavorites, addFavorite, removeFavorite, isFavorite
  }
})

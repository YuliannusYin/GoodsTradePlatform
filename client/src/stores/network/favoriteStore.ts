import { defineStore } from 'pinia'
import type { Favorite } from '@/types/favorite'
import { callGet, callPost, callDelete } from './requests'

export const useFavoriteStore = defineStore('favoriteStore', () => {
  async function getUserFavorites(): Promise<Favorite[]> {
    return callGet('/favorites/list')
  }

  async function addFavorite(productId: string): Promise<any> {
    return callPost(`/favorites/add?productId=${encodeURIComponent(productId)}`)
  }

  async function removeFavorite(productId: string): Promise<any> {
    return callDelete(`/favorites/remove/${productId}`)
  }

  async function isFavorite(productId: string): Promise<boolean> {
    return callGet(`/favorites/check/${productId}`)
  }

  return {
    getUserFavorites, addFavorite, removeFavorite, isFavorite
  }
})

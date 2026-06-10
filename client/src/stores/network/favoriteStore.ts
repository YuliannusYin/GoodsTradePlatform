import { defineStore } from 'pinia'
import type { Favorite } from '@/types/favorite'
import { callGet, callPost, callDelete } from './requests'

export const useFavoriteStore = defineStore('favoriteStore', () => {
  const API = {
    getUserFavorites: async (): Promise<Favorite[]> =>
      await callGet('/favorites/list'),

    addFavorite: async (productId: string): Promise<any> =>
      await callPost('/favorites/add', { productId }),

    removeFavorite: async (productId: string): Promise<any> =>
      await callDelete(`/favorites/remove/${productId}`),

    isFavorite: async (productId: string): Promise<boolean> =>
      await callGet(`/favorites/check/${productId}`)
  }

  return {
    API
  }
})

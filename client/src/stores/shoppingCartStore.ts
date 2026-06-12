import { ref } from 'vue'
import { defineStore } from 'pinia'

function loadProductIds(): string[] {
  const stored = sessionStorage.getItem('shoppingCart_productIds')
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch {
      return []
    }
  }
  return []
}

function persistProductIds(ids: string[]) {
  sessionStorage.setItem('shoppingCart_productIds', JSON.stringify(ids))
}

export const useShoppingCartStore = defineStore('shoppingCart', () => {
  const initialIds = loadProductIds()
  const states = {
    productIds: ref<string[]>(initialIds),
    productAmount: ref<number>(initialIds.length)
  }

  const methods = {
    addProductId: async (productId: string): Promise<void> => {
      states.productIds.value.push(productId)
      states.productAmount.value++
      persistProductIds(states.productIds.value)
    },

    removeProductId: (productId: string): void => {
      const index = states.productIds.value.indexOf(productId)
      states.productIds.value.splice(index, 1)
      states.productAmount.value--
      persistProductIds(states.productIds.value)
    },

    clearProductIds: (): void => {
      states.productIds.value = []
      states.productAmount.value = 0
      persistProductIds([])
    },

    getAllProductIds: (): string[] => states.productIds.value,

    getTotalItemsCount: (): number => states.productAmount.value
  }

  return {
    states,
    methods
  }
})

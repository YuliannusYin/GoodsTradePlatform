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
  const productIds = ref<string[]>(initialIds)
  const productAmount = ref<number>(initialIds.length)

  async function addProductId(productId: string): Promise<void> {
    productIds.value.push(productId)
    productAmount.value++
    persistProductIds(productIds.value)
  }

  function removeProductId(productId: string): void {
    const index = productIds.value.indexOf(productId)
    productIds.value.splice(index, 1)
    productAmount.value--
    persistProductIds(productIds.value)
  }

  function clearProductIds(): void {
    productIds.value = []
    productAmount.value = 0
    persistProductIds([])
  }

  function getAllProductIds(): string[] {
    return productIds.value
  }

  function getTotalItemsCount(): number {
    return productAmount.value
  }

  return {
    productIds, productAmount,
    addProductId, removeProductId, clearProductIds, getAllProductIds, getTotalItemsCount
  }
})

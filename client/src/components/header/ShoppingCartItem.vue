<template>
  <div :class="[additionalClass]">
    <RouterLink to="/cart">
      <div class="relative">
        <i class="fa-solid fa-cart-shopping text-gray-700 hover:text-primary-600"></i>
        <div v-if="itemsCount > 0" class="absolute top-4 ml-[-0.2rem] md:top-3 md:ml-[0.8rem] lg:top-5">
          <p class="p-3 w-3 h-3 bg-green-500 text-xs rounded-full text-white font-semibold flex justify-center items-center">
            {{ itemsCount }}
          </p>
        </div>
      </div>
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ShoppingCartItem.vue
 * @description 购物车图标组件，显示购物车中的商品数量角标
 */
import { computed } from 'vue'
import { useShoppingCartStore } from '@/stores/shoppingCartStore'

withDefaults(defineProps<{
  additionalClass?: string
}>(), {})

const shoppingCartStore = useShoppingCartStore()
// 计算购物车中的商品总数量
const itemsCount = computed(() => shoppingCartStore.getTotalItemsCount())
</script>

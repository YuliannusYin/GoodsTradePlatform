<template>
  <section>
    <OngoingOrder />
    <FeaturedProducts />
  </section>
</template>

<script setup lang="ts">
/**
 * @file CheckoutView.vue
 * @description 结算视图，展示进行中的订单和推荐商品
 */
import OngoingOrder from '@/components/products/OngoingOrder.vue'
import FeaturedProducts from '@/components/products/FeaturedProducts.vue'
</script>

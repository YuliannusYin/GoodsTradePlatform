<template>
  <div class="bg-white p-4 shadow flex-col w-full hidden sm:flex">
    <SmallViewTitle text="商品预览" class="mb-4" />
    <img :src="product.imageUrls?.[0]" class="mb-4 h-[12rem] inline-block object-scale-down cursor-pointer border p-4 w-full">
    <div class="space-y-5">
      <h3 class="text-l font-semibold">{{ product.name === '' ? "Title" : product.name }}</h3>
      <p>{{ product.description === '' ? "Description" : product.description }}</p>
      <div class="text-l font-semibold text-black">Price: {{ product.price }} :-</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import SmallViewTitle from './SmallViewTitle.vue'

defineProps<{
  product: any
}>()
</script>

<template>
  <div class="bg-white p-4 shadow flex-col w-full hidden sm:flex">
    <SmallViewTitle text="商品预览" class="mb-4" />
    <img v-if="product.imageUrls && product.imageUrls.length > 0"
      :src="product.imageUrls[0]"
      class="mb-4 h-[12rem] inline-block object-scale-down cursor-pointer border p-4 w-full"
      @error="(e: Event) => { (e.target as HTMLImageElement).src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22 viewBox=%220 0 200 200%22><rect fill=%22%23f3f4f6%22 width=%22200%22 height=%22200%22/><text x=%22100%22 y=%22110%22 text-anchor=%22middle%22 fill=%22%239ca3af%22 font-size=%2214%22>图片加载失败</text></svg>') }">
    <div v-else class="mb-4 h-[12rem] bg-gray-100 flex items-center justify-center border p-4 w-full">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
      </svg>
    </div>
    <div class="space-y-5">
      <h3 class="text-l font-semibold">{{ product.name === '' ? "Title" : product.name }}</h3>
      <p>{{ product.description === '' ? "Description" : product.description }}</p>
      <div class="text-l font-semibold text-black">Price: {{ product.price }} :-</div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ProductPreview.vue
 * @description 商品预览组件，在管理后台表单旁展示商品的图片、名称、描述和价格
 */
import SmallViewTitle from './SmallViewTitle.vue'

defineProps<{
  product: any
}>()
</script>

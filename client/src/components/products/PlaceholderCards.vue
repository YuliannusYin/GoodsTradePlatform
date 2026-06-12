<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 min-h-[15rem]">
    <div v-for="card in placeholderCards" :key="card.id"
      class="bg-white p-4 shadow-md rounded-lg flex justify-center items-center">
      <div class="spinner-border" role="status" />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file PlaceholderCards.vue
 * @description 商品加载占位卡片组件，在商品数据加载完成前显示旋转加载动画
 */
import { ref, onMounted } from 'vue'

// 占位卡片数据结构
interface PlaceHolderCard {
  id: string
}

const props = defineProps<{
  placeholderAmount: number
}>()

// 占位卡片列表
const placeholderCards = ref<PlaceHolderCard[]>([])

// 组件挂载时根据数量生成占位卡片
onMounted(() => {
  for (let amount = 0; amount < props.placeholderAmount; amount++) {
    placeholderCards.value.push({ id: `${amount}` })
  }
})
</script>

<style scoped>
.spinner-border {
  display: inline-block;
  width: 2rem;
  height: 2rem;
  border-width: .25em;
  border-color: gray;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spinner-border .75s linear infinite;
}

@keyframes spinner-border {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

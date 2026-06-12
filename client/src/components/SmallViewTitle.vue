<template>
  <div>
    <h2 :class="['text-xl font-semibold', additionalClass]">{{ text }}</h2>
    <div v-if="hasUnderline" class="border border-t-1 w-full mt-1"></div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file SmallViewTitle.vue
 * @description 小型视图标题组件，可配置标题文本、是否显示下划线和额外样式类
 */
withDefaults(defineProps<{
  text: string
  hasUnderline?: boolean
  additionalClass?: string
}>(), {
  hasUnderline: true
})
</script>

/**
 * @file CheckoutDelivery.vue
 * @description 配送方式选择组件，使用单选按钮组展示可用的配送方式
 * @input modelValue: 当前选中的配送方式枚举值
 * @output update:modelValue: 配送方式变更时触发
 */
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- 标题 -->
    <h2 class="text-lg font-semibold text-gray-800 mb-4">配送方式</h2>

    <!-- 配送方式单选按钮组 -->
    <div class="space-y-3">
      <label
        v-for="(label, key) in DELIVERY_METHODS"
        :key="key"
        class="flex items-center gap-3 p-3 border rounded-md cursor-pointer transition-all duration-200"
        :class="modelValue === key
          ? 'border-blue-500 bg-blue-50'
          : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'"
      >
        <!-- 单选按钮 -->
        <input
          type="radio"
          :value="key"
          :checked="modelValue === key"
          class="w-4 h-4 text-blue-500 focus:ring-blue-500"
          @change="emit('update:modelValue', key)"
        />
        <!-- 配送方式名称 -->
        <span
          class="text-sm font-medium"
          :class="modelValue === key ? 'text-blue-700' : 'text-gray-700'"
        >
          {{ label }}
        </span>
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 配送方式选择组件
 * 职责：展示配送方式选项，支持 v-model 双向绑定选中值
 */
import { DELIVERY_METHODS } from '@/types/order'

defineProps<{
  /** 当前选中的配送方式枚举值 */
  modelValue: string
}>()

const emit = defineEmits<{
  /** 配送方式变更时触发 */
  'update:modelValue': [value: string]
}>()
</script>

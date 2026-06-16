/**
 * @file CheckoutStepper.vue
 * @description 结算步骤指示器组件，横向展示步骤进度，支持点击已完成步骤回退；已完成步骤显示蓝色勾号，最后一步（完成步骤）显示绿色勾号
 * @input steps: 步骤名称数组, currentStep: 当前步骤索引（从0开始）
 * @output emit update:currentStep: 点击已完成步骤时触发，携带目标步骤索引
 */
<template>
  <div class="flex items-center justify-center w-full py-6">
    <div
      v-for="(step, index) in steps"
      :key="index"
      class="flex items-center"
    >
      <!-- 步骤节点：圆形指示器 + 步骤名称 -->
      <div
        class="flex flex-col items-center select-none"
        :class="{
          'cursor-pointer': index < currentStep,
          'cursor-default': index >= currentStep
        }"
        @click="handleStepClick(index)"
      >
        <!-- 圆形步骤图标 -->
        <div
          class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold border-2 transition-all duration-300"
          :class="getStepCircleClass(index)"
        >
          <!-- 已完成步骤（非最后一步）显示蓝色勾号 -->
          <svg
            v-if="index < currentStep && !isLastStep(index)"
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2.5"
              d="M5 13l4 4L19 7"
            />
          </svg>
          <!-- 最后一步（完成步骤）到达后显示绿色勾号 -->
          <svg
            v-else-if="index === currentStep && isLastStep(index)"
            class="w-5 h-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2.5"
              d="M5 13l4 4L19 7"
            />
          </svg>
          <!-- 当前步骤（非完成步骤）显示序号 -->
          <span v-else-if="index === currentStep">{{ index + 1 }}</span>
          <!-- 未来步骤显示序号 -->
          <span v-else>{{ index + 1 }}</span>
        </div>
        <!-- 步骤名称 -->
        <span
          class="mt-2 text-sm font-medium whitespace-nowrap transition-colors duration-300"
          :class="getStepLabelClass(index)"
        >
          {{ step }}
        </span>
      </div>
      <!-- 步骤之间的连接线（最后一个步骤不显示） -->
      <div
        v-if="index < steps.length - 1"
        class="h-0.5 mx-3 transition-colors duration-300"
        :class="index < currentStep ? 'bg-blue-500' : 'bg-gray-300'"
        style="width: 80px"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 结算步骤指示器
 * 职责：展示结算流程的步骤进度，支持点击已完成步骤回退，完成步骤高亮为绿色
 */

const props = defineProps<{
  /** 步骤名称数组 */
  steps: string[]
  /** 当前步骤索引（从0开始） */
  currentStep: number
}>()

const emit = defineEmits<{
  /** 点击已完成步骤时触发，携带目标步骤索引 */
  'update:currentStep': [step: number]
}>()

/**
 * 判断是否为最后一步（完成步骤）
 * @param {number} index - 步骤索引
 * @returns {boolean} 是否为最后一步
 */
function isLastStep(index: number): boolean {
  return index === props.steps.length - 1
}

/**
 * 获取步骤圆形图标的样式类
 * @param {number} index - 步骤索引
 * @returns {string} Tailwind CSS 类名
 */
function getStepCircleClass(index: number): string {
  // 最后一步（完成步骤）到达时显示绿色
  if (isLastStep(index) && index === props.currentStep) {
    return 'bg-green-500 border-green-500 text-white'
  }
  if (index < props.currentStep) {
    // 已完成步骤：蓝色背景 + 白色文字
    return 'bg-blue-500 border-blue-500 text-white'
  } else if (index === props.currentStep) {
    // 当前步骤：蓝色边框 + 蓝色文字
    return 'border-blue-500 text-blue-500 bg-white'
  } else {
    // 未来步骤：灰色边框 + 灰色文字
    return 'border-gray-300 text-gray-400 bg-white'
  }
}

/**
 * 获取步骤名称标签的样式类
 * @param {number} index - 步骤索引
 * @returns {string} Tailwind CSS 类名
 */
function getStepLabelClass(index: number): string {
  // 最后一步（完成步骤）到达时显示绿色
  if (isLastStep(index) && index === props.currentStep) {
    return 'text-green-600'
  }
  if (index <= props.currentStep) {
    // 已完成和当前步骤：深色文字
    return 'text-gray-800'
  } else {
    // 未来步骤：灰色文字
    return 'text-gray-400'
  }
}

/**
 * 处理步骤点击事件
 * 仅允许点击已完成的步骤进行回退（完成步骤不可回退）
 * @param {number} index - 被点击的步骤索引
 */
function handleStepClick(index: number) {
  // 只有已完成的步骤才允许点击回退，完成步骤不可回退
  if (index < props.currentStep && !isLastStep(index)) {
    emit('update:currentStep', index)
  }
}
</script>

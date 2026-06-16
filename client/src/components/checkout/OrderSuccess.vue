/**
 * @file OrderSuccess.vue
 * @description 下单成功页面组件，展示动态成功反馈、订单摘要信息和倒计时自动跳转
 * @input orderId: 订单ID, totalPrice: 支付金额, remainingBalance: 剩余余额
 * @output 无
 */
<template>
  <div class="flex flex-col items-center justify-center py-12">
    <!-- 成功图标：勾号圆圈缩放弹入动画 -->
    <div
      class="w-24 h-24 rounded-full bg-green-100 flex items-center justify-center mb-6 success-icon"
    >
      <svg class="w-14 h-14 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2.5"
          d="M5 13l4 4L19 7"
        />
      </svg>
    </div>

    <!-- 成功标题：淡入上移动画 -->
    <h2 class="text-2xl font-bold text-gray-800 mb-6 success-title">下单成功！</h2>

    <!-- 订单信息卡片：滑入动画 -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 w-full max-w-md mb-8 success-card">
      <div class="space-y-3 text-sm">
        <!-- 订单号 -->
        <div class="flex justify-between">
          <span class="text-gray-500">订单号</span>
          <span class="text-gray-800 font-medium">{{ orderId }}</span>
        </div>
        <!-- 支付方式 -->
        <div class="flex justify-between">
          <span class="text-gray-500">支付方式</span>
          <span class="text-gray-800 font-medium">账户余额</span>
        </div>
        <!-- 支付金额 -->
        <div class="flex justify-between">
          <span class="text-gray-500">支付金额</span>
          <span class="text-gray-800 font-semibold">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <!-- 当前余额 -->
        <div class="flex justify-between pt-2 border-t border-gray-100">
          <span class="text-gray-500">当前余额</span>
          <span class="text-green-600 font-semibold">¥{{ remainingBalance.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 操作按钮区域：淡入动画 -->
    <div class="flex flex-col items-center gap-3 success-actions">
      <!-- 查看订单按钮（含倒计时） -->
      <button
        class="px-8 py-2.5 bg-blue-500 hover:bg-blue-600 text-white rounded-md text-sm font-medium transition-colors duration-200 flex items-center gap-2"
        @click="goToOrders"
      >
        <i class="fas fa-clipboard-list"></i>
        <span>查看订单</span>
        <!-- 倒计时徽标 -->
        <span
          v-if="countdown > 0"
          class="bg-white bg-opacity-20 text-white text-xs px-1.5 py-0.5 rounded-full min-w-[22px] text-center"
        >
          {{ countdown }}s
        </span>
      </button>

      <!-- 继续购物按钮 -->
      <router-link
        to="/shop"
        class="px-6 py-2 text-gray-500 hover:text-gray-700 text-sm transition-colors duration-200"
        @click="cancelCountdown"
      >
        继续购物
      </router-link>
    </div>

    <!-- 倒计时提示文字 -->
    <p v-if="countdown > 0" class="text-xs text-gray-400 mt-4 success-hint">
      {{ countdown }}秒后自动跳转到我的订单
    </p>
  </div>
</template>

<script setup lang="ts">
/**
 * 下单成功页面组件
 * 职责：展示动态成功反馈、订单摘要信息，支持倒计时自动跳转到我的订单
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{
  /** 订单ID */
  orderId: string
  /** 支付金额 */
  totalPrice: number
  /** 剩余余额 */
  remainingBalance: number
}>()

const router = useRouter()

// 倒计时秒数，默认5秒
const countdown = ref(5)
// 倒计时定时器引用
let countdownTimer: ReturnType<typeof setInterval> | null = null

/**
 * 立即跳转到我的订单页面
 */
function goToOrders() {
  cancelCountdown()
  router.push('/account/orders')
}

/**
 * 取消倒计时定时器
 */
function cancelCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  // 清除倒计时显示
  countdown.value = 0
}

// 组件挂载时启动倒计时
onMounted(() => {
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      cancelCountdown()
      // 倒计时结束，自动跳转到我的订单
      router.push('/account/orders')
    }
  }, 1000)
})

// 组件卸载时清理定时器，防止内存泄漏
onUnmounted(() => {
  cancelCountdown()
})
</script>

<style scoped>
/* 勾号圆圈缩放弹入动画 */
.success-icon {
  animation: iconPop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
  opacity: 0;
  transform: scale(0);
}

/* 标题淡入上移动画 */
.success-title {
  animation: fadeSlideUp 0.4s ease-out 0.2s forwards;
  opacity: 0;
  transform: translateY(10px);
}

/* 信息卡片滑入动画 */
.success-card {
  animation: fadeSlideUp 0.4s ease-out 0.4s forwards;
  opacity: 0;
  transform: translateY(15px);
}

/* 操作按钮淡入动画 */
.success-actions {
  animation: fadeIn 0.3s ease-out 0.6s forwards;
  opacity: 0;
}

/* 倒计时提示文字淡入动画 */
.success-hint {
  animation: fadeIn 0.3s ease-out 0.8s forwards;
  opacity: 0;
}

/* 缩放弹入关键帧 */
@keyframes iconPop {
  0% {
    opacity: 0;
    transform: scale(0);
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 淡入上移关键帧 */
@keyframes fadeSlideUp {
  0% {
    opacity: 0;
    transform: translateY(15px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 淡入关键帧 */
@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
</style>

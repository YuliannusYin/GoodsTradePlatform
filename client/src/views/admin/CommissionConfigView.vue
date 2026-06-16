/**
 * @file CommissionConfigView.vue
 * @description 佣金配置管理视图，超级管理员可设置平台佣金类型和费率
 * @input 无
 * @output 无
 */
<template>
  <div class="p-6 max-w-2xl mx-auto">
    <h2 class="text-2xl font-bold mb-6">佣金配置</h2>

    <!-- 加载中状态 -->
    <div v-if="isLoading" class="flex items-center justify-center py-10">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      <span class="ml-3 text-gray-500">加载中...</span>
    </div>

    <!-- 配置表单 -->
    <div v-else class="space-y-6">
      <!-- 当前配置展示 -->
      <div class="bg-gray-50 rounded-lg p-4 border">
        <h3 class="font-semibold text-gray-700 mb-2">当前配置</h3>
        <p v-if="config.commissionType === 'PERCENTAGE'" class="text-gray-600">
          佣金模式：<span class="font-medium text-blue-600">百分比佣金</span>，
          佣金率：<span class="font-medium text-blue-600">{{ (config.commissionRate * 100).toFixed(1) }}%</span>
        </p>
        <p v-else class="text-gray-600">
          佣金模式：<span class="font-medium text-blue-600">固定金额</span>，
          每笔佣金：<span class="font-medium text-blue-600">¥{{ config.fixedAmount.toFixed(2) }}</span>
        </p>
      </div>

      <!-- 佣金类型选择 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">佣金类型</label>
        <div class="flex gap-4">
          <label class="flex items-center cursor-pointer">
            <input type="radio" v-model="form.commissionType" value="PERCENTAGE"
              class="mr-2 accent-blue-500" :disabled="!isSuperAdmin">
            <span>百分比佣金</span>
          </label>
          <label class="flex items-center cursor-pointer">
            <input type="radio" v-model="form.commissionType" value="FIXED"
              class="mr-2 accent-blue-500" :disabled="!isSuperAdmin">
            <span>固定金额</span>
          </label>
        </div>
      </div>

      <!-- 百分比佣金设置 -->
      <div v-if="form.commissionType === 'PERCENTAGE'">
        <label class="block text-sm font-medium text-gray-700 mb-2">佣金率（%）</label>
        <input type="number" v-model.number="form.commissionRate" min="0" max="100" step="0.1"
          class="w-full border border-gray-300 rounded-md px-3 py-2 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"
          :disabled="!isSuperAdmin" placeholder="如 5 表示 5%">
        <p class="text-sm text-gray-500 mt-1">范围 0~100，如输入 5 表示收取 5% 的佣金</p>
      </div>

      <!-- 固定金额设置 -->
      <div v-if="form.commissionType === 'FIXED'">
        <label class="block text-sm font-medium text-gray-700 mb-2">固定佣金金额（¥）</label>
        <input type="number" v-model.number="form.fixedAmount" min="0" step="0.01"
          class="w-full border border-gray-300 rounded-md px-3 py-2 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"
          :disabled="!isSuperAdmin" placeholder="如 5.00 表示每笔收取 ¥5">
        <p class="text-sm text-gray-500 mt-1">每笔订单收取的固定佣金金额</p>
      </div>

      <!-- 计算示例 -->
      <div class="bg-blue-50 rounded-lg p-4 border border-blue-200">
        <h3 class="font-semibold text-blue-700 mb-2">计算示例</h3>
        <p class="text-sm text-blue-600">
          假设订单金额 ¥100.00：
        </p>
        <p class="text-sm text-blue-600 mt-1">
          平台佣金：<span class="font-medium">¥{{ sampleCommission }}</span>，
          商户实收：<span class="font-medium">¥{{ sampleSellerIncome }}</span>
        </p>
      </div>

      <!-- 保存按钮 -->
      <div v-if="isSuperAdmin" class="flex justify-end">
        <button
          class="px-6 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed text-white rounded-md text-sm font-medium transition-colors duration-200 flex items-center"
          :disabled="isSaving"
          @click="saveConfig"
        >
          <!-- 保存中显示加载动画 -->
          <svg v-if="isSaving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ isSaving ? '保存中...' : '保存配置' }}
        </button>
      </div>

      <!-- 非超管提示 -->
      <div v-if="!isSuperAdmin" class="text-center text-gray-500 text-sm py-2">
        仅超级管理员可修改佣金配置
      </div>

      <!-- 操作结果提示 -->
      <div v-if="message" class="p-3 rounded-md text-sm"
        :class="messageType === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 佣金配置管理视图
 * 职责：展示和编辑平台佣金配置，支持百分比和固定金额两种模式
 */
import { ref, computed, onMounted } from 'vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import type { CommissionConfig } from '@/stores/network/adminToolsStore'
import { useAccountStore } from '@/stores/network/accountStore'

const adminToolsStore = useAdminToolsStore()
const accountStore = useAccountStore()

// 是否正在加载配置
const isLoading = ref(true)
// 是否正在保存配置
const isSaving = ref(false)
// 操作结果消息
const message = ref('')
// 消息类型：success 或 error
const messageType = ref<'success' | 'error'>('success')

// 当前配置（从后端加载）
const config = ref<CommissionConfig>({
  commissionType: 'PERCENTAGE',
  commissionRate: 0.05,
  fixedAmount: 0
})

// 表单数据（用于编辑）
const form = ref({
  commissionType: 'PERCENTAGE',
  commissionRate: 5,
  fixedAmount: 0
})

// 是否为超级管理员
const isSuperAdmin = computed(() => sessionStorage.getItem('userRole') === 'SUPER_ADMIN')

// 计算示例：基于 ¥100 订单
const sampleCommission = computed(() => {
  if (form.value.commissionType === 'PERCENTAGE') {
    return (100 * form.value.commissionRate / 100).toFixed(2)
  }
  return form.value.fixedAmount.toFixed(2)
})

const sampleSellerIncome = computed(() => {
  const commission = form.value.commissionType === 'PERCENTAGE'
    ? 100 * form.value.commissionRate / 100
    : form.value.fixedAmount
  return Math.max(0, 100 - commission).toFixed(2)
})

/**
 * 加载佣金配置
 */
async function loadConfig() {
  isLoading.value = true
  try {
    const data = await adminToolsStore.getCommissionConfig()
    config.value = data
    // 初始化表单数据
    form.value.commissionType = data.commissionType
    form.value.commissionRate = data.commissionRate * 100 // 后端存储 0.05，前端显示 5
    form.value.fixedAmount = data.fixedAmount
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '加载佣金配置失败'
    showMessage(msg, 'error')
  } finally {
    isLoading.value = false
  }
}

/**
 * 保存佣金配置
 */
async function saveConfig() {
  // 防止重复提交
  if (isSaving.value) return

  // 表单验证
  if (form.value.commissionType === 'PERCENTAGE') {
    if (form.value.commissionRate < 0 || form.value.commissionRate > 100) {
      showMessage('佣金率必须在 0~100 之间', 'error')
      return
    }
  } else {
    if (form.value.fixedAmount < 0) {
      showMessage('固定佣金金额不能为负数', 'error')
      return
    }
  }

  isSaving.value = true
  message.value = ''

  try {
    const updateData: Record<string, unknown> = {
      commissionType: form.value.commissionType
    }
    if (form.value.commissionType === 'PERCENTAGE') {
      // 前端显示 5，后端存储 0.05
      updateData.commissionRate = form.value.commissionRate / 100
      updateData.fixedAmount = 0
    } else {
      updateData.fixedAmount = form.value.fixedAmount
      updateData.commissionRate = 0
    }

    const data = await adminToolsStore.updateCommissionConfig(updateData as any)
    config.value = data
    form.value.commissionRate = data.commissionRate * 100
    form.value.fixedAmount = data.fixedAmount
    showMessage('佣金配置保存成功', 'success')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '保存佣金配置失败'
    showMessage(msg, 'error')
  } finally {
    isSaving.value = false
  }
}

/**
 * 显示操作结果消息
 * @param msg 消息内容
 * @param type 消息类型
 */
function showMessage(msg: string, type: 'success' | 'error') {
  message.value = msg
  messageType.value = type
  // 3秒后自动清除消息
  setTimeout(() => {
    if (message.value === msg) {
      message.value = ''
    }
  }, 3000)
}

// 组件挂载时加载配置
onMounted(() => {
  loadConfig()
})
</script>

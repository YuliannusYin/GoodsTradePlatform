/**
 * @file CheckoutAddress.vue
 * @description 收货地址表单组件，支持 v-model 双向绑定地址数据，包含表单验证
 * @input modelValue: 收货地址对象（receiverName, receiverPhone, region, detailAddress）
 * @output update:modelValue: 地址数据变更时触发
 */
<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- 标题 -->
    <h2 class="text-lg font-semibold text-gray-800 mb-4">收货地址</h2>

    <div class="space-y-4">
      <!-- 收货人姓名 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          收货人 <span class="text-red-500">*</span>
        </label>
        <input
          :value="modelValue.receiverName"
          type="text"
          placeholder="请输入收货人姓名"
          class="w-full border rounded-md px-3 py-2 text-sm outline-none transition-colors"
          :class="errors.receiverName ? 'border-red-400 focus:border-red-500' : 'border-gray-300 focus:border-blue-500'"
          @input="updateField('receiverName', ($event.target as HTMLInputElement).value)"
          @blur="validateField('receiverName')"
        />
        <!-- 收货人姓名错误提示 -->
        <p v-if="errors.receiverName" class="mt-1 text-xs text-red-500">{{ errors.receiverName }}</p>
      </div>

      <!-- 联系电话 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          联系电话 <span class="text-red-500">*</span>
        </label>
        <input
          :value="modelValue.receiverPhone"
          type="tel"
          placeholder="请输入联系电话"
          class="w-full border rounded-md px-3 py-2 text-sm outline-none transition-colors"
          :class="errors.receiverPhone ? 'border-red-400 focus:border-red-500' : 'border-gray-300 focus:border-blue-500'"
          @input="updateField('receiverPhone', ($event.target as HTMLInputElement).value)"
          @blur="validateField('receiverPhone')"
        />
        <!-- 联系电话错误提示 -->
        <p v-if="errors.receiverPhone" class="mt-1 text-xs text-red-500">{{ errors.receiverPhone }}</p>
      </div>

      <!-- 省/市/区 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          省/市/区 <span class="text-red-500">*</span>
        </label>
        <input
          :value="modelValue.region"
          type="text"
          placeholder="例如：北京市 朝阳区"
          class="w-full border rounded-md px-3 py-2 text-sm outline-none transition-colors"
          :class="errors.region ? 'border-red-400 focus:border-red-500' : 'border-gray-300 focus:border-blue-500'"
          @input="updateField('region', ($event.target as HTMLInputElement).value)"
          @blur="validateField('region')"
        />
        <!-- 省/市/区错误提示 -->
        <p v-if="errors.region" class="mt-1 text-xs text-red-500">{{ errors.region }}</p>
      </div>

      <!-- 详细地址 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">
          详细地址 <span class="text-red-500">*</span>
        </label>
        <textarea
          :value="modelValue.detailAddress"
          placeholder="请输入详细地址（街道、楼栋、门牌号等）"
          rows="3"
          class="w-full border rounded-md px-3 py-2 text-sm outline-none transition-colors resize-none"
          :class="errors.detailAddress ? 'border-red-400 focus:border-red-500' : 'border-gray-300 focus:border-blue-500'"
          @input="updateField('detailAddress', ($event.target as HTMLTextAreaElement).value)"
          @blur="validateField('detailAddress')"
        ></textarea>
        <!-- 详细地址错误提示 -->
        <p v-if="errors.detailAddress" class="mt-1 text-xs text-red-500">{{ errors.detailAddress }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 收货地址表单组件
 * 职责：收集收货地址信息，支持 v-model 双向绑定和表单验证
 */
import { reactive } from 'vue'
import type { AddressInfo } from '@/types/order'

const props = defineProps<{
  /** 地址表单数据，支持 v-model 绑定 */
  modelValue: AddressInfo
}>()

const emit = defineEmits<{
  /** 地址数据变更时触发 */
  'update:modelValue': [value: AddressInfo]
}>()

/** 各字段的验证错误信息 */
const errors = reactive<Record<keyof AddressInfo, string>>({
  receiverName: '',
  receiverPhone: '',
  region: '',
  detailAddress: ''
})

/**
 * 更新单个字段值并触发 v-model 更新
 * @param {keyof AddressInfo} field - 字段名
 * @param {string} value - 新值
 */
function updateField(field: keyof AddressInfo, value: string) {
  // 清除该字段的错误提示
  errors[field] = ''
  emit('update:modelValue', {
    ...props.modelValue,
    [field]: value
  })
}

/**
 * 验证单个字段
 * @param {keyof AddressInfo} field - 字段名
 */
function validateField(field: keyof AddressInfo) {
  errors[field] = ''

  switch (field) {
    case 'receiverName':
      // 收货人姓名不能为空
      if (!props.modelValue.receiverName.trim()) {
        errors.receiverName = '请输入收货人姓名'
      }
      break
    case 'receiverPhone':
      // 联系电话不能为空且需要符合手机号格式
      if (!props.modelValue.receiverPhone.trim()) {
        errors.receiverPhone = '请输入联系电话'
      } else if (!/^1[3-9]\d{9}$/.test(props.modelValue.receiverPhone.trim())) {
        errors.receiverPhone = '请输入正确的手机号码'
      }
      break
    case 'region':
      // 省/市/区不能为空
      if (!props.modelValue.region.trim()) {
        errors.region = '请输入省/市/区'
      }
      break
    case 'detailAddress':
      // 详细地址不能为空
      if (!props.modelValue.detailAddress.trim()) {
        errors.detailAddress = '请输入详细地址'
      }
      break
  }
}

/**
 * 验证所有字段，返回是否全部通过
 * @returns {boolean} 所有字段是否验证通过
 */
function validateAll(): boolean {
  const fields: (keyof AddressInfo)[] = ['receiverName', 'receiverPhone', 'region', 'detailAddress']
  let isValid = true
  for (const field of fields) {
    validateField(field)
    if (errors[field]) {
      isValid = false
    }
  }
  return isValid
}

// 暴露验证方法供父组件调用
defineExpose({ validateAll })
</script>

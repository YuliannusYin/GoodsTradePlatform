<template>
  <div class="fixed inset-0 flex items-center justify-center z-50 bg-black bg-opacity-50">
    <div class="bg-white rounded-md p-4 shadow-lg space-y-4">
      <div class="mb-4">
        <div class="flex items-center">
          <i class="fas fa-warning mr-2 text-red-600 text-xl" />
          <h2 class="text-xl font-semibold">{{ header }}</h2>
        </div>
        <div class="border border-t-1 w-full mt-1"></div>
      </div>
      <p class="text-gray-700">{{ text }}</p>
      <div class="flex flex-col sm:flex-row justify-start space-y-2 sm:space-y-0 sm:space-x-2">
        <input v-if="isPasswordRequired" v-model="password"
          class="border border-gray-300 rounded py-2 px-4 outline-none focus:border-blue-500 mb-2 sm:mb-0"
          placeholder="请输入密码" type="password">
        <button class="bg-gray-400 hover:bg-gray-500 text-white py-2 px-4 rounded" @click="cancel">取消</button>
        <button :disabled="isPasswordRequired && password.length <= 0"
          class="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-500 disabled:cursor-not-allowed text-white py-2 px-4 rounded"
          @click="handleConfirm">确认</button>
      </div>
      <div v-if="isConfirmationErrorResponse" class="flex justify-center font-semibold text-red-700 mt-2">
        <p>密码错误。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file ConfirmDialogue.vue
 * @description 确认对话框组件，支持可选的密码验证，用于操作前的二次确认
 */
import { ref } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'

const props = defineProps<{
  isPasswordRequired?: boolean
  header: string
  text: string
  onConfirm: (password?: string) => void
  onCancel: () => void
}>()

const accountStore = useAccountStore()
// 密码验证错误提示是否可见
const isConfirmationErrorResponse = ref(false)
// 用户输入的密码
const password = ref('')

/**
 * 确认操作处理函数
 * 需要密码验证时先校验密码，验证通过后执行确认回调；无需验证时直接执行确认回调
 */
async function handleConfirm() {
  if (props.isPasswordRequired) {
    try {
      // 验证用户输入的密码是否正确
      const confirmed = await accountStore.isValidCredentials(accountStore.email || '', password.value)
      if (confirmed) {
        props.onConfirm(password.value)
      } else {
        isConfirmationErrorResponse.value = true
      }
    } catch {
      isConfirmationErrorResponse.value = true
    }
  } else {
    // 无需密码验证，直接确认
    props.onConfirm(password.value)
  }
  props.onCancel()
}

// 取消操作，关闭对话框
function cancel() {
  props.onCancel()
}
</script>

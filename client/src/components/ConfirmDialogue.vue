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
const isConfirmationErrorResponse = ref(false)
const password = ref('')

async function handleConfirm() {
  if (props.isPasswordRequired) {
    try {
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
    props.onConfirm(password.value)
  }
  props.onCancel()
}

function cancel() {
  props.onCancel()
}
</script>

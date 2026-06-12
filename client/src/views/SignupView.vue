<template>
  <div class="flex justify-center items-center bg-gray-100">
    <div class="bg-white p-8 rounded shadow-lg w-[25rem]">
      <h2 class="text-2xl mb-4">注册</h2>
      <form @submit.prevent="handleSignup">
        <div class="mb-4">
          <label for="username" class="block text-gray-700 text-sm font-bold mb-2">用户名</label>
          <input v-model="username" type="text" id="username"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <div class="mb-4">
          <label for="email" class="block text-gray-700 text-sm font-bold mb-2">邮箱</label>
          <input v-model="email" type="email" id="email"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <div class="mb-4">
          <label for="password" class="block text-gray-700 text-sm font-bold mb-2">密码</label>
          <input v-model="password" type="password" id="password"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <div class="mb-6">
          <label for="confirmPassword" class="block text-gray-700 text-sm font-bold mb-2">确认密码</label>
          <input v-model="confirmPassword" type="password" id="confirmPassword"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <button type="submit" :disabled="password === '' || password != confirmPassword"
          class="w-full bg-blue-500 hover:bg-blue-700 disabled:bg-gray-500 disabled:hover:bg-gray-700 disabled:cursor-not-allowed text-white font-bold py-2 px-4 rounded focus:outline-none">注册</button>
      </form>
      <div v-if="responseMessage" :class="['flex', 'justify-center', 'font-semibold', 'mt-2', responseMessageColor]">
        <p>{{ responseMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file SignupView.vue
 * @description 注册视图，提供用户名、邮箱、密码输入表单，处理用户注册逻辑
 */
import { ref } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore()   // 账户状态管理实例
const username = ref('')                  // 用户名
const email = ref('')                     // 邮箱
const password = ref('')                  // 密码
const confirmPassword = ref('')           // 确认密码
const responseMessage = ref('')           // 响应提示信息
const responseMessageColor = ref('')      // 提示信息颜色样式

/**
 * 处理用户注册，成功后显示成功提示，失败显示错误信息
 */
async function handleSignup() {
  try {
    await accountStore.register(email.value, username.value, password.value)
    responseMessage.value = '注册成功'
    responseMessageColor.value = 'text-green-700' // 成功提示为绿色
  } catch (error: any) {
    // 注册失败时显示错误信息
    responseMessage.value = error?.message || error?.response?.data?.message || '注册失败'
    responseMessageColor.value = 'text-red-700' // 错误提示为红色
  }
}
</script>

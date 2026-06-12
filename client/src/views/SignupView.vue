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
import { ref } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore()
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const responseMessage = ref('')
const responseMessageColor = ref('')

async function handleSignup() {
  try {
    const response = await accountStore.register(email.value, username.value, password.value)
    responseMessage.value = response.message || '注册成功'
    responseMessageColor.value = 'text-green-700'
  } catch (error: any) {
    responseMessage.value = error?.response?.data?.message || '注册失败'
    responseMessageColor.value = 'text-red-700'
  }
}
</script>

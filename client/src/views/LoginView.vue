<template>
  <div class="flex justify-center items-center bg-gray-100 min-h-screen">
    <div class="bg-white p-8 rounded shadow-lg w-[25rem]">
      <h2 class="text-2xl mb-4">登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label for="email" class="block text-gray-700 text-sm font-bold mb-2">邮箱</label>
          <input v-model="email" type="email" id="email"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <div class="mb-6">
          <label for="password" class="block text-gray-700 text-sm font-bold mb-2">密码</label>
          <input v-model="password" type="password" id="password"
            class="w-full px-3 py-2 border rounded border-gray-400 focus:outline-none" required />
        </div>
        <button type="submit"
          class="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none">登录</button>
      </form>
      <div class="flex justify-center items-center mt-4">
        <h4 class="mr-1">新用户？</h4>
        <router-link to="/signup" class="text-blue-700 hover:text-blue-600 font-semibold">点击注册</router-link>
      </div>
      <div v-if="errorMessage" class="flex justify-center font-semibold text-red-700 mt-2">
        <p>{{ errorMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/network/accountStore'

const router = useRouter()
const accountStore = useAccountStore()
const email = ref('')
const password = ref('')
const errorMessage = ref('')

async function handleLogin() {
  try {
    errorMessage.value = ''
    await accountStore.login(email.value, password.value)
    router.push('/')
  } catch (error: any) {
    errorMessage.value = error?.message || error?.response?.data?.message || '登录失败'
  }
}
</script>

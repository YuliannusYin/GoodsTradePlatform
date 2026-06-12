<template>
  <div class="hidden absolute pt-2 md:block">
    <div class="flex flex-col flex-nowrap bg-white p-4 shadow-md rounded-md">
      <button class="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none transition duration-300">
        <router-link to="/login" class="text-md">登录</router-link>
      </button>
      <div class="flex justify-center items-center mt-4">
        <p class="mr-1 text-sm">新用户？</p>
        <router-link to="/signup" class="text-blue-700 hover:text-blue-400 text-sm">点击注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file LoginOrSignupPopup.vue
 * @description 登录/注册弹出菜单组件，提供登录按钮和注册链接入口
 */
</script>

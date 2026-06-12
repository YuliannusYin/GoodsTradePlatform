<template>
  <div class="cursor-pointer transition duration-300" @mouseover="showPopup" @mouseleave="hidePopup">
    <router-link to="/login" class="text-gray-700 hover:text-primary-600 font-medium">登录</router-link>
    <LoginOrSignupPopup v-if="isShowingPopup" />
  </div>
</template>

<script setup lang="ts">
/**
 * @file LoginItem.vue
 * @description 未登录用户的登录入口组件，悬停时显示登录/注册弹出菜单
 */
import { ref } from 'vue'
import LoginOrSignupPopup from '../LoginOrSignupPopup.vue'

// 控制弹出菜单是否可见
const isShowingPopup = ref(false)

// 鼠标悬停时显示弹出菜单
function showPopup() {
  isShowingPopup.value = true
}

// 鼠标离开时隐藏弹出菜单
function hidePopup() {
  isShowingPopup.value = false
}
</script>

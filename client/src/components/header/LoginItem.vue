<template>
  <div class="cursor-pointer transition duration-300" @mouseover="showPopup" @mouseleave="hidePopup">
    <router-link to="/login" class="text-gray-700 hover:text-primary-600 font-medium">登录</router-link>
    <LoginOrSignupPopup v-if="isShowingPopup" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LoginOrSignupPopup from '../LoginOrSignupPopup.vue'

const isShowingPopup = ref(false)

function showPopup() {
  isShowingPopup.value = true
}

function hidePopup() {
  isShowingPopup.value = false
}
</script>

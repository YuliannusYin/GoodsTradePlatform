<template>
  <div class="cursor-pointer transition duration-300" @mouseover="showPopup" @mouseleave="hidePopup">
    <router-link to="/admin_tools" class="text-blue-500">
      <i class="fas fa-id-card"></i>
    </router-link>
    <AdminToolsPopup v-if="isShowingPopup" />
  </div>
</template>

<script setup lang="ts">
/**
 * @file AdminToolsItem.vue
 * @description 管理后台入口图标组件，鼠标悬停时显示管理工具弹出菜单
 */
import { ref } from 'vue'
import AdminToolsPopup from './AdminToolsPopup.vue'

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

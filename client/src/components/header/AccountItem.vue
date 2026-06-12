<template>
  <div class="cursor-pointer transition duration-300" @mouseover="showPopup" @mouseleave="hidePopup">
    <router-link to="/account/edit" class="text-gray-700 hover:text-primary-600">
      <i class="fas fa-user"></i>
    </router-link>
    <div v-if="isShowingPopup" class="hidden sm:block absolute pt-2">
      <div class="flex flex-col justify-center items-center bg-white p-4 shadow-md rounded-md">
        <div class="text-left space-y-2 flex flex-col">
          <router-link :to="{ name: 'EditAccountView' }" class="text-blue-700 hover:text-blue-500">
            编辑信息
          </router-link>
          <router-link :to="{ name: 'ShowAccountOrdersView' }" class="text-blue-700 hover:text-blue-500">
            我的订单
          </router-link>
          <button @click="handleLogout" class="text-left text-red-600 hover:text-red-500">
            退出登录
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file AccountItem.vue
 * @description 已登录用户的账户图标组件，悬停显示编辑信息、我的订单和退出登录菜单
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore()
const router = useRouter()
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

// 退出登录并跳转到首页
function handleLogout() {
  accountStore.logout()
  router.push('/')
}
</script>

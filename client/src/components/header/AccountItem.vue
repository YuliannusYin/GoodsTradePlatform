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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore()
const router = useRouter()
const isShowingPopup = ref(false)

function showPopup() {
  isShowingPopup.value = true
}

function hidePopup() {
  isShowingPopup.value = false
}

function handleLogout() {
  accountStore.logout()
  router.push('/')
}
</script>

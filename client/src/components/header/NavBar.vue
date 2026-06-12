<template>
  <header
    class="bg-white py-3 px-6 lg:py-4 sticky top-0 z-50 border-b-2 border-primary-100 shadow-sm flex items-center justify-between md:justify-center space-x-6">
    <!-- Logo -->
    <router-link to="/" class="text-2xl font-black text-primary-600 tracking-tight">
      周边<span class="text-accent-500">商城</span>
    </router-link>

    <!-- Desktop Search -->
    <SearchBar class="hidden md:flex" />

    <!-- Desktop Nav Items -->
    <nav class="hidden md:flex space-x-6 justify-center items-center">
      <router-link to="/shop" class="text-gray-700 hover:text-primary-600 font-medium transition-colors">周边商城</router-link>
      <router-link to="/favorites" class="text-gray-700 hover:text-primary-600 font-medium transition-colors">收藏</router-link>
      <AccountItem v-if="accountStore.isAuthenticated" />
      <LoginItem v-else />
      <ShoppingCartItem />
      <AdminToolsItem v-if="accountStore.isAdmin()" />
    </nav>

    <!-- Mobile Icons -->
    <div class="md:hidden cursor-pointer space-x-6 text-l flex justify-center items-center">
      <div class="cursor-pointer" @click="toggleSearchInput">
        <i class="fas fa-search text-black"></i>
      </div>
      <div class="cursor-pointer" @click="toggleAsideVisibility">
        <i class="fas fa-bars text-black text-xl"></i>
      </div>
      <ShoppingCartItem />
    </div>

    <!-- Mobile Search Dropdown -->
    <div v-if="isSearchInputOpen"
      class="outside-search-components absolute top-full left-0 right-0 bg-white shadow-md p-4 z-50">
      <SearchBar :hasCloseSearchEnabled="true" @onClose="closeSearchInput" />
    </div>
  </header>

  <!-- Mobile Hamburger Dropdown -->
  <div v-if="isAsideOpen" class="outside-aside-components fixed inset-0 z-40" @click="handleClickOutsideAside">
    <div class="absolute right-0 top-[3.5rem] bg-white shadow-lg p-4 min-w-[12rem]">
      <nav class="flex flex-col space-y-3">
        <router-link to="/shop" class="text-gray-700 hover:text-primary-600 font-medium" @click="closeAside">周边商城</router-link>
        <router-link to="/favorites" class="text-gray-700 hover:text-primary-600 font-medium" @click="closeAside">收藏</router-link>
        <template v-if="accountStore.isAuthenticated">
          <router-link to="/account/edit" class="text-gray-700 hover:text-primary-600 font-medium" @click="closeAside">我的账户</router-link>
          <button @click="handleLogout" class="text-left text-red-600 hover:text-red-700 font-medium">退出登录</button>
        </template>
        <template v-else>
          <router-link to="/login" class="text-gray-700 hover:text-primary-600 font-medium" @click="closeAside">登录</router-link>
          <router-link to="/signup" class="text-gray-700 hover:text-primary-600 font-medium" @click="closeAside">注册</router-link>
        </template>
        <template v-if="accountStore.isAdmin()">
          <div class="border-t pt-2 mt-2">
            <router-link to="/admin_tools" class="text-blue-600 hover:text-blue-700 font-medium" @click="closeAside">管理后台</router-link>
          </div>
        </template>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * @file NavBar.vue
 * @description 顶部导航栏组件，包含 Logo、搜索栏、桌面端导航项和移动端汉堡菜单
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAccountStore } from '@/stores/network/accountStore'
import SearchBar from './SearchBar.vue'
import AccountItem from './AccountItem.vue'
import LoginItem from './LoginItem.vue'
import ShoppingCartItem from './ShoppingCartItem.vue'
import AdminToolsItem from '@/components/admintools/AdminToolsItem.vue'

const accountStore = useAccountStore()
const router = useRouter()

// 移动端侧边栏是否展开
const isAsideOpen = ref(false)
// 移动端搜索框是否展开
const isSearchInputOpen = ref(false)

// 切换移动端侧边栏的展开/收起状态
function toggleAsideVisibility() {
  isAsideOpen.value = !isAsideOpen.value
}

// 关闭移动端侧边栏
function closeAside() {
  isAsideOpen.value = false
}

// 点击侧边栏外部区域时关闭侧边栏
function handleClickOutsideAside(event: any) {
  if (event.target.className?.includes?.('outside-aside-components')) {
    closeAside()
  }
}

// 切换移动端搜索框的展开/收起状态
function toggleSearchInput() {
  isSearchInputOpen.value = !isSearchInputOpen.value
}

// 关闭移动端搜索框
function closeSearchInput() {
  isSearchInputOpen.value = false
}

// 退出登录并跳转到首页
function handleLogout() {
  accountStore.logout()
  closeAside()
  router.push('/')
}
</script>

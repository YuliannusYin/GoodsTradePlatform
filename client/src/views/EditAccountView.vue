<template>
  <section>
    <AccountInfo />
    <template v-if="!accountStore.isProtected">
      <AccountEditForm editType="username" />
      <AccountEditForm editType="email" />
      <AccountEditForm editType="password" />
      <AccountEditForm editType="delete" />
    </template>
  </section>
</template>

<script setup lang="ts">
/**
 * @file EditAccountView.vue
 * @description 编辑账户信息视图，展示账户信息及编辑表单（用户名、邮箱、密码、删除账户）
 */
import AccountInfo from '@/components/account/AccountInfo.vue'
import AccountEditForm from '@/components/account/AccountEditForm.vue'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore() // 账户状态管理实例
</script>

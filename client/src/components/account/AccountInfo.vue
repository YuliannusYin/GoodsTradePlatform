<template>
  <div class="p-4 bg-white rounded shadow">
    <h2 class="text-xl font-semibold mb-4">账户信息</h2>
    <div>
      <p><strong>用户名：</strong> {{ accountStore.username }}</p>
      <p><strong>邮箱：</strong> {{ accountStore.email }}</p>
      <p><strong>余额：</strong> ${{ formattedBalance }}</p>
      <p v-if="accountStore.isProtected" class="mt-2 text-sm text-amber-600 font-medium">
        这是系统账户，用户名、邮箱、密码和删除操作受到限制。
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore()

const formattedBalance = computed(() => {
  return (accountStore.balance || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})
</script>

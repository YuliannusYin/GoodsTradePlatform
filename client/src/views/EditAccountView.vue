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
import AccountInfo from '@/components/account/AccountInfo.vue'
import AccountEditForm from '@/components/account/AccountEditForm.vue'
import { useAccountStore } from '@/stores/network/accountStore'

const accountStore = useAccountStore()
</script>

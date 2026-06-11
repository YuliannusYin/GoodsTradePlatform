<template>
    <section>
      <AccountInfo />
      <template v-if="!isProtected">
        <EditInformation />
        <ChangePassword />
        <DeleteAccount />
      </template>
    </section>
  </template>
  
  <script lang="ts">
  import AccountInfo from '@/components/account/AccountInfo.vue'
  import EditInformation from '@/components/account/EditInformation.vue'
  import ChangePassword from '@/components/account/ChangePassword.vue'
  import DeleteAccount from '@/components/account/DeleteAccount.vue'
  import { useAccountStore } from '@/stores/network/accountStore'
  import { computed, defineComponent } from 'vue'
  
  export default defineComponent({
    name: 'EditAccountView',
    components: { AccountInfo, EditInformation, ChangePassword, DeleteAccount },
    setup() {
      const accountStore = useAccountStore()
      const isProtected = computed(() => accountStore.states.isProtected)
      return { isProtected }
    }
  })
  </script>
  
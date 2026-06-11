<template>
  <ConfirmDialogue :isPasswordRequired="true" header="确认删除账户"
    text="确定要删除账户吗？此操作不可撤销" v-if="isConfirmationVisible"
    :onConfirm="handleDeleteAccount" :onCancel="closeConfirmation" />

  <div class="p-4 bg-white rounded shadow">
    <h2 class="text-xl font-semibold mb-4">删除账户</h2>
    <div v-if="deleteAccountResponse" :class="['flex', 'justify-center', 'font-semibold', 'my-2', responseMessageColor]">
      <p>{{ deleteAccountResponse.message }}</p>
    </div>
    <form @submit.prevent="openConfirmation">

      <button type="submit" class="bg-red-500 text-white py-2 px-4 rounded">删除账户</button>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useAccountStore } from '@/stores/network/accountStore';
import { useConnectionStore } from '@/stores/network/connectionStore';
import ConfirmDialogue from '@/components/ConfirmDialogue.vue'

export default defineComponent({
  name: 'DeleteAccount',
  setup() {
    const accountStore = useAccountStore();
    const connectionStore = useConnectionStore();

    const deleteAccountResponse = ref<any | null>(null);
    const responseMessageColor = ref<string | null>(null);

    const isConfirmationVisible = ref<boolean>(false);

    function openConfirmation() {
      isConfirmationVisible.value = true;
    }

    function closeConfirmation() {
      isConfirmationVisible.value = false;
    }

    async function handleDeleteAccount() {
      const response = await accountStore.API.deleteAccount();
      handleResponseMessage()

      if ('success' in response) {
        setTimeout(async () => {
          await connectionStore.API.submitLogout();;
        }, 2000);
      }
      closeConfirmation();
    }

    function handleResponseMessage() {
      deleteAccountResponse.value = accountStore.states.deleteAccountResponse

      if (deleteAccountResponse.value.error) {
        responseMessageColor.value = "text-red-700"
      }
      else {
        responseMessageColor.value = "text-green-700"
      }
    }

    return { openConfirmation, closeConfirmation, handleDeleteAccount, deleteAccountResponse, responseMessageColor, isConfirmationVisible }
  },
  components: { ConfirmDialogue }
});
</script>

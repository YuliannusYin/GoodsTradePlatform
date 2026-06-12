<template>
  <div class="p-4 bg-white rounded shadow">
    <h2 class="text-xl font-semibold mb-4">{{ title }}</h2>
    <div v-if="responseMessage" :class="['flex', 'justify-center', 'font-semibold', 'my-2', responseMessageColor]">
      <p>{{ responseMessage }}</p>
    </div>

    <!-- Username form -->
    <form v-if="editType === 'username'" @submit.prevent="handleSubmit">
      <div class="mb-4">
        <label class="block text-gray-700 font-bold mb-2">新用户名</label>
        <input v-model="newValue" type="text" class="border w-full p-2 rounded" />
      </div>
      <button type="submit" class="bg-blue-500 text-white py-2 px-4 rounded">保存</button>
    </form>

    <!-- Email form -->
    <form v-if="editType === 'email'" @submit.prevent="handleSubmit">
      <div class="mb-4">
        <label class="block text-gray-700 font-bold mb-2">新邮箱</label>
        <input v-model="newValue" type="email" class="border w-full p-2 rounded" />
      </div>
      <button type="submit" class="bg-blue-500 text-white py-2 px-4 rounded">保存</button>
    </form>

    <!-- Password form -->
    <form v-if="editType === 'password'" @submit.prevent="handleSubmit">
      <div class="mb-4">
        <label class="block text-gray-700 font-bold mb-2">当前密码</label>
        <input v-model="currentPassword" type="password" class="border w-full p-2 rounded" />
      </div>
      <div class="mb-4">
        <label class="block text-gray-700 font-bold mb-2">新密码</label>
        <input v-model="newValue" type="password" class="border w-full p-2 rounded" />
      </div>
      <button type="submit" class="bg-blue-500 text-white py-2 px-4 rounded">保存</button>
    </form>

    <!-- Delete account -->
    <form v-if="editType === 'delete'" @submit.prevent="openConfirmation">
      <button type="submit" class="bg-red-500 text-white py-2 px-4 rounded">删除账户</button>
    </form>

    <ConfirmDialogue
      :isPasswordRequired="editType === 'delete'"
      :header="confirmHeader"
      :text="confirmText"
      v-if="isConfirmationVisible"
      :onConfirm="handleConfirm"
      :onCancel="closeConfirmation"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'
import { useRouter } from 'vue-router'
import ConfirmDialogue from '@/components/ConfirmDialogue.vue'

const props = defineProps<{
  editType: 'username' | 'email' | 'password' | 'delete'
}>()

const accountStore = useAccountStore()
const router = useRouter()

const newValue = ref('')
const currentPassword = ref('')
const responseMessage = ref<string | null>(null)
const responseMessageColor = ref('')
const isConfirmationVisible = ref(false)

const title = computed(() => {
  switch (props.editType) {
    case 'username': return '修改用户名'
    case 'email': return '修改邮箱'
    case 'password': return '修改密码'
    case 'delete': return '删除账户'
  }
})

const confirmHeader = computed(() => {
  switch (props.editType) {
    case 'username': return '确认修改用户名'
    case 'email': return '确认修改邮箱'
    case 'password': return '确认修改密码'
    case 'delete': return '确认删除账户'
  }
})

const confirmText = computed(() => {
  switch (props.editType) {
    case 'username': return '确定要修改用户名吗？'
    case 'email': return '确定要修改邮箱吗？'
    case 'password': return '确定要修改密码吗？'
    case 'delete': return '确定要删除账户吗？此操作不可撤销'
  }
})

function openConfirmation() {
  isConfirmationVisible.value = true
}

function closeConfirmation() {
  isConfirmationVisible.value = false
}

async function handleSubmit() {
  openConfirmation()
}

async function handleConfirm(password?: string) {
  try {
    let response: any
    switch (props.editType) {
      case 'username':
        response = await accountStore.changeUsername(newValue.value)
        break
      case 'email':
        response = await accountStore.changeEmail(newValue.value)
        break
      case 'password':
        response = await accountStore.changePassword(currentPassword.value, newValue.value)
        break
      case 'delete':
        response = await accountStore.deleteAccount()
        if (response) {
          setTimeout(() => {
            router.push('/')
          }, 2000)
        }
        break
    }
    if (response !== undefined && response !== null) {
      responseMessage.value = '操作成功'
      responseMessageColor.value = 'text-green-700'
    }
  } catch (error: any) {
    responseMessage.value = error?.message || error?.response?.data?.message || '操作失败'
    responseMessageColor.value = 'text-red-700'
  }
  closeConfirmation()
}
</script>

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
/**
 * @file AccountEditForm.vue
 * @description 账户信息编辑表单组件，支持修改用户名、邮箱、密码及删除账户操作
 */
import { ref, computed } from 'vue'
import { useAccountStore } from '@/stores/network/accountStore'
import { useRouter } from 'vue-router'
import ConfirmDialogue from '@/components/ConfirmDialogue.vue'

const props = defineProps<{
  editType: 'username' | 'email' | 'password' | 'delete'
}>()

const accountStore = useAccountStore()
const router = useRouter()

// 新值输入（用户名/邮箱/新密码）
const newValue = ref('')
// 当前密码（修改密码时需要验证）
const currentPassword = ref('')
// 操作响应消息
const responseMessage = ref<string | null>(null)
// 响应消息颜色类名
const responseMessageColor = ref('')
// 确认对话框是否可见
const isConfirmationVisible = ref(false)

// 根据编辑类型计算表单标题
const title = computed(() => {
  switch (props.editType) {
    case 'username': return '修改用户名'
    case 'email': return '修改邮箱'
    case 'password': return '修改密码'
    case 'delete': return '删除账户'
  }
})

// 根据编辑类型计算确认对话框标题
const confirmHeader = computed(() => {
  switch (props.editType) {
    case 'username': return '确认修改用户名'
    case 'email': return '确认修改邮箱'
    case 'password': return '确认修改密码'
    case 'delete': return '确认删除账户'
  }
})

// 根据编辑类型计算确认对话框提示文本
const confirmText = computed(() => {
  switch (props.editType) {
    case 'username': return '确定要修改用户名吗？'
    case 'email': return '确定要修改邮箱吗？'
    case 'password': return '确定要修改密码吗？'
    case 'delete': return '确定要删除账户吗？此操作不可撤销'
  }
})

// 显示确认对话框
function openConfirmation() {
  isConfirmationVisible.value = true
}

// 关闭确认对话框
function closeConfirmation() {
  isConfirmationVisible.value = false
}

// 提交表单时打开确认对话框
async function handleSubmit() {
  openConfirmation()
}

/**
 * 确认操作处理函数
 * @param {string} password - 可选的确认密码
 * 根据编辑类型调用对应的账户操作接口，成功显示绿色提示，失败显示红色错误信息
 */
async function handleConfirm(password?: string) {
  try {
    let response: any
    switch (props.editType) {
      case 'username':
        // 修改用户名
        response = await accountStore.changeUsername(newValue.value)
        break
      case 'email':
        // 修改邮箱
        response = await accountStore.changeEmail(newValue.value)
        break
      case 'password':
        // 修改密码，需提供当前密码和新密码
        response = await accountStore.changePassword(currentPassword.value, newValue.value)
        break
      case 'delete':
        // 删除账户，成功后延迟跳转到首页
        response = await accountStore.deleteAccount()
        if (response) {
          setTimeout(() => {
            router.push('/')
          }, 2000)
        }
        break
    }
    // 操作成功时显示绿色提示
    if (response !== undefined && response !== null) {
      responseMessage.value = '操作成功'
      responseMessageColor.value = 'text-green-700'
    }
  } catch (error: any) {
    // 操作失败时显示红色错误信息
    responseMessage.value = error?.message || error?.response?.data?.message || '操作失败'
    responseMessageColor.value = 'text-red-700'
  }
  closeConfirmation()
}
</script>

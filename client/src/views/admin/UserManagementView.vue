<template>
  <section class="p-4 sm:p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">用户管理</h2>
      <button @click="loadData" class="text-blue-600 hover:text-blue-800 text-sm">
        <i class="fas fa-sync-alt mr-1"></i>刷新
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-8 text-gray-500">
      <i class="fas fa-spinner fa-spin mr-2"></i>加载中...
    </div>

    <!-- Error -->
    <div v-if="error" class="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
      {{ error }}
    </div>

    <!-- Users Table -->
    <div v-if="!loading" class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="bg-gray-50 text-left">
            <th class="px-4 py-3 font-medium text-gray-600">邮箱</th>
            <th class="px-4 py-3 font-medium text-gray-600">用户名</th>
            <th class="px-4 py-3 font-medium text-gray-600">角色</th>
            <th class="px-4 py-3 font-medium text-gray-600">状态</th>
            <th class="px-4 py-3 font-medium text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id" class="border-b hover:bg-gray-50">
            <td class="px-4 py-3">{{ user.email }}</td>
            <td class="px-4 py-3">{{ user.username }}</td>
            <td class="px-4 py-3">
              <span class="bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded">
                {{ user.role || '无角色' }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span :class="user.enabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="text-xs px-2 py-0.5 rounded">
                {{ user.enabled ? '启用' : '禁用' }}
              </span>
              <span v-if="user.protected"
                class="bg-yellow-100 text-yellow-800 text-xs px-2 py-0.5 rounded ml-1">受保护</span>
            </td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <button @click="openRoleDialog(user)"
                  class="text-blue-600 hover:text-blue-800 text-xs" title="分配角色">
                  <i class="fas fa-user-tag"></i> 角色
                </button>
                <button v-if="!user.protected" @click="handleToggleEnabled(user)"
                  class="text-yellow-600 hover:text-yellow-800 text-xs"
                  :title="user.enabled ? '禁用用户' : '启用用户'">
                  <i :class="user.enabled ? 'fas fa-ban' : 'fas fa-check-circle'"></i>
                  {{ user.enabled ? '禁用' : '启用' }}
                </button>
                <button v-if="!user.protected" @click="handleDeleteUser(user)"
                  class="text-red-600 hover:text-red-800 text-xs" title="删除用户">
                  <i class="fas fa-trash"></i> 删除
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="5" class="text-center py-8 text-gray-400">暂无用户数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Assign Role Dialog -->
    <div v-if="showRoleDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeRoleDialog">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="p-6">
          <h3 class="text-lg font-bold text-gray-800 mb-2">分配角色</h3>
          <p class="text-sm text-gray-500 mb-4">
            用户：{{ selectedUser?.username }} ({{ selectedUser?.email }})
          </p>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">选择角色</label>
            <select v-model="selectedRole"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none">
              <option v-for="role in availableRoles" :key="role" :value="role">
                {{ role }}
              </option>
            </select>
          </div>

          <div v-if="dialogError" class="text-red-500 text-sm mt-3">{{ dialogError }}</div>

          <div class="flex justify-end gap-3 mt-6">
            <button @click="closeRoleDialog"
              class="px-4 py-2 text-gray-600 hover:text-gray-800 text-sm">
              取消
            </button>
            <button @click="handleAssignRole" :disabled="submitting"
              class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm font-medium disabled:opacity-50">
              {{ submitting ? '提交中...' : '确认' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminToolsStore } from '@/stores/network/adminToolsStore'
import { callGet, callPatch, callDelete } from '@/stores/network/requests'

interface AdminUser {
  id: string
  email: string
  username: string
  role: string
  enabled: boolean
  protected: boolean
}

const adminStore = useAdminToolsStore()
const users = ref<AdminUser[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const availableRoles = ['USER', 'MERCHANT', 'ADMIN']

const showRoleDialog = ref(false)
const selectedUser = ref<AdminUser | null>(null)
const selectedRole = ref('USER')
const submitting = ref(false)
const dialogError = ref('')

async function loadData() {
  loading.value = true
  error.value = null
  try {
    users.value = await callGet<AdminUser[]>('/admin_tools/users/all')
  } catch (e: any) {
    error.value = '加载数据失败'
  } finally {
    loading.value = false
  }
}

function openRoleDialog(user: AdminUser) {
  selectedUser.value = user
  selectedRole.value = user.role || 'USER'
  dialogError.value = ''
  showRoleDialog.value = true
}

function closeRoleDialog() {
  showRoleDialog.value = false
  selectedUser.value = null
  dialogError.value = ''
}

async function handleAssignRole() {
  if (!selectedUser.value) return
  submitting.value = true
  dialogError.value = ''
  try {
    await callPatch(`/admin_tools/users/${selectedUser.value.id}/role?role=${selectedRole.value}`, {})
    closeRoleDialog()
    await loadData()
  } catch (e: any) {
    dialogError.value = e?.data?.message || '分配角色失败'
  } finally {
    submitting.value = false
  }
}

async function handleToggleEnabled(user: AdminUser) {
  const action = user.enabled ? '禁用' : '启用'
  if (!confirm(`确定${action}用户「${user.username}」吗？`)) return
  try {
    await callPatch(`/admin_tools/users/${user.id}/toggle-enabled`, {})
    await loadData()
  } catch (e: any) {
    error.value = e?.data?.message || '操作失败'
  }
}

async function handleDeleteUser(user: AdminUser) {
  if (!confirm(`确定删除用户「${user.username}」吗？此操作不可撤销。`)) return
  try {
    await callDelete(`/admin_tools/users/${user.id}`)
    await loadData()
  } catch (e: any) {
    error.value = e?.data?.message || '删除失败'
  }
}

onMounted(() => {
  loadData()
})
</script>

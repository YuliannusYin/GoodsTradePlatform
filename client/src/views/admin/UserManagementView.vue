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
              <span :class="user.isEnabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
                class="text-xs px-2 py-0.5 rounded">
                {{ user.isEnabled ? '启用' : '禁用' }}
              </span>
              <span v-if="user.isProtected"
                class="bg-yellow-100 text-yellow-800 text-xs px-2 py-0.5 rounded ml-1">受保护</span>
            </td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <button @click="openRoleDialog(user)"
                  class="text-blue-600 hover:text-blue-800 text-xs" title="分配角色">
                  <i class="fas fa-user-tag"></i> 角色
                </button>
                <button v-if="!user.isProtected" @click="handleToggleEnabled(user)"
                  class="text-yellow-600 hover:text-yellow-800 text-xs"
                  :title="user.isEnabled ? '禁用用户' : '启用用户'">
                  <i :class="user.isEnabled ? 'fas fa-ban' : 'fas fa-check-circle'"></i>
                  {{ user.isEnabled ? '禁用' : '启用' }}
                </button>
                <button v-if="!user.isProtected" @click="handleDeleteUser(user)"
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
/**
 * @file UserManagementView.vue
 * @description 用户管理视图，管理员可查看用户列表、分配角色、启用/禁用用户、删除用户
 */
import { ref, onMounted } from 'vue'
import { callGet, callPut, callPatch, callDelete } from '@/stores/network/requests'

/**
 * 管理员用户信息接口，字段名需与后端UserDTO的JSON属性名一致
 */
interface AdminUser {
  id: string
  email: string
  username: string
  role: string
  /** 后端UserDTO中isProtected序列化为isProtected */
  isProtected: boolean
  /** 后端UserDTO中isEnabled序列化为isEnabled */
  isEnabled: boolean
}

const users = ref<AdminUser[]>([])       // 用户列表数据
const loading = ref(false)                // 加载状态
const error = ref<string | null>(null)    // 错误信息

// 可分配的角色列表
const availableRoles = ['USER', 'MERCHANT', 'ADMIN']

// 角色分配对话框相关状态
const showRoleDialog = ref(false)               // 是否显示角色分配对话框
const selectedUser = ref<AdminUser | null>(null) // 当前选中的用户
const selectedRole = ref('USER')                  // 选中的角色
const submitting = ref(false)                     // 提交中状态
const dialogError = ref('')                       // 对话框错误信息

/**
 * 加载所有用户数据
 */
async function loadData() {
  loading.value = true
  error.value = null
  try {
    users.value = await callGet<AdminUser[]>('/api/admin/users/all')
  } catch (e: any) {
    error.value = '加载数据失败'
  } finally {
    loading.value = false
  }
}

/**
 * 打开角色分配对话框
 * @param {AdminUser} user - 待分配角色的用户
 */
function openRoleDialog(user: AdminUser) {
  selectedUser.value = user
  selectedRole.value = user.role || 'USER' // 默认选中用户当前角色
  dialogError.value = ''
  showRoleDialog.value = true
}

// 关闭角色分配对话框并重置状态
function closeRoleDialog() {
  showRoleDialog.value = false
  selectedUser.value = null
  dialogError.value = ''
}

/**
 * 确认分配角色，提交角色变更请求
 */
async function handleAssignRole() {
  if (!selectedUser.value) return
  submitting.value = true
  dialogError.value = ''
  try {
    // 后端UserManagementController使用PUT方法分配角色
    await callPut(`/api/admin/users/${selectedUser.value.id}/role`, { role: selectedRole.value })
    closeRoleDialog()
    await loadData()
  } catch (e: any) {
    dialogError.value = e?.message || '分配角色失败'
  } finally {
    submitting.value = false
  }
}

/**
 * 切换用户的启用/禁用状态
 * @param {AdminUser} user - 待操作的用户
 */
async function handleToggleEnabled(user: AdminUser) {
  const action = user.isEnabled ? '禁用' : '启用'
  if (!confirm(`确定${action}用户「${user.username}」吗？`)) return
  try {
    await callPatch(`/api/admin/users/${user.id}/toggle-enabled`, {})
    await loadData()
  } catch (e: any) {
    error.value = e?.message || '操作失败'
  }
}

/**
 * 删除用户（不可撤销）
 * @param {AdminUser} user - 待删除的用户
 */
async function handleDeleteUser(user: AdminUser) {
  if (!confirm(`确定删除用户「${user.username}」吗？此操作不可撤销。`)) return
  try {
    await callDelete(`/api/admin/users/${user.id}`)
    await loadData()
  } catch (e: any) {
    error.value = e?.message || '删除失败'
  }
}

// 组件挂载时加载用户数据
onMounted(() => {
  loadData()
})
</script>

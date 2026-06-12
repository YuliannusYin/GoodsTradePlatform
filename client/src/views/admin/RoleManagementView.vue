<template>
  <section class="p-4 sm:p-6">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">角色管理</h2>
      <button @click="openCreateDialog"
        class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm font-medium">
        <i class="fas fa-plus mr-1"></i> 新建角色
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

    <!-- Roles List -->
    <div v-if="!loading" class="space-y-4">
      <div v-for="role in roles" :key="role.id"
        class="border rounded-lg p-4 hover:shadow-sm transition-shadow">
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center gap-2">
              <h3 class="font-semibold text-gray-800">{{ role.name }}</h3>
              <span v-if="role.builtIn"
                class="bg-gray-100 text-gray-600 text-xs px-2 py-0.5 rounded">内置</span>
            </div>
            <p class="text-sm text-gray-500 mt-1">{{ role.description || '无描述' }}</p>
            <div class="mt-2 flex flex-wrap gap-1">
              <span v-for="perm in role.permissions" :key="perm.id"
                class="bg-blue-50 text-blue-700 text-xs px-2 py-0.5 rounded">
                {{ perm.name }}
              </span>
              <span v-if="role.permissions.length === 0" class="text-xs text-gray-400">无权限</span>
            </div>
          </div>
          <div class="flex gap-2 ml-4">
            <button @click="openEditDialog(role)"
              class="text-blue-600 hover:text-blue-800 text-sm">
              <i class="fas fa-edit"></i>
            </button>
            <button v-if="!role.builtIn" @click="handleDeleteRole(role)"
              class="text-red-600 hover:text-red-800 text-sm">
              <i class="fas fa-trash"></i>
            </button>
          </div>
        </div>
      </div>
      <div v-if="roles.length === 0" class="text-center py-8 text-gray-400">
        暂无角色数据
      </div>
    </div>

    <!-- Create/Edit Dialog -->
    <div v-if="showDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="closeDialog">
      <div class="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <h3 class="text-lg font-bold text-gray-800 mb-4">
            {{ isEditing ? '编辑角色' : '新建角色' }}
          </h3>

          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">角色名称</label>
              <input v-model="formData.name" type="text"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none"
                placeholder="输入角色名称" />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">角色描述</label>
              <textarea v-model="formData.description" rows="2"
                class="w-full px-3 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-300 outline-none resize-none"
                placeholder="输入角色描述"></textarea>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">权限选择</label>
              <div v-for="(perms, module) in permissionsByModule" :key="module" class="mb-3">
                <p class="text-sm font-semibold text-gray-600 mb-1">{{ getModuleLabel(module as string) }}</p>
                <div class="flex flex-wrap gap-2">
                  <label v-for="perm in perms" :key="perm.id"
                    class="flex items-center gap-1 text-sm bg-gray-50 px-2 py-1 rounded cursor-pointer hover:bg-gray-100">
                    <input type="checkbox" :value="perm.id" v-model="formData.permissionIds"
                      class="rounded text-blue-600" />
                    {{ perm.name }}
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div v-if="dialogError" class="text-red-500 text-sm mt-3">{{ dialogError }}</div>

          <div class="flex justify-end gap-3 mt-6">
            <button @click="closeDialog"
              class="px-4 py-2 text-gray-600 hover:text-gray-800 text-sm">
              取消
            </button>
            <button @click="handleSubmit" :disabled="submitting"
              class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm font-medium disabled:opacity-50">
              {{ submitting ? '提交中...' : '确认' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, computed } from 'vue';
import { useRoleManagementStore } from '@/stores/network/roleManagementStore';
import type { Role, Permission } from '@/stores/network/roleManagementStore';

export default defineComponent({
  name: 'RoleManagementView',

  setup() {
    const store = useRoleManagementStore();
    const roles = ref<Role[]>([]);
    const permissions = ref<Permission[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    const showDialog = ref(false);
    const isEditing = ref(false);
    const editingRoleId = ref<string | null>(null);
    const submitting = ref(false);
    const dialogError = ref('');

    const formData = ref({
      name: '',
      description: '',
      permissionIds: [] as string[]
    });

    const permissionsByModule = computed(() => {
      const grouped: Record<string, Permission[]> = {};
      for (const perm of permissions.value) {
        if (!grouped[perm.module]) {
          grouped[perm.module] = [];
        }
        grouped[perm.module].push(perm);
      }
      return grouped;
    });

    function getModuleLabel(module: string): string {
      const labels: Record<string, string> = {
        PRODUCT: '商品管理',
        ORDER: '订单管理',
        USER: '用户管理',
        ROLE: '角色管理',
        REVIEW: '审核管理',
        SYSTEM: '系统管理'
      };
      return labels[module] || module;
    }

    async function loadData() {
      loading.value = true;
      error.value = null;
      try {
        const [rolesResult, permsResult] = await Promise.all([
          store.API.getAllRoles(),
          store.API.getAllPermissions()
        ]);
        roles.value = rolesResult;
        permissions.value = permsResult;
      } catch (e: any) {
        error.value = '加载数据失败';
      } finally {
        loading.value = false;
      }
    }

    function openCreateDialog() {
      isEditing.value = false;
      editingRoleId.value = null;
      formData.value = { name: '', description: '', permissionIds: [] };
      dialogError.value = '';
      showDialog.value = true;
    }

    function openEditDialog(role: Role) {
      isEditing.value = true;
      editingRoleId.value = role.id;
      formData.value = {
        name: role.name,
        description: role.description,
        permissionIds: role.permissions.map(p => p.id)
      };
      dialogError.value = '';
      showDialog.value = true;
    }

    function closeDialog() {
      showDialog.value = false;
      dialogError.value = '';
    }

    async function handleSubmit() {
      if (!formData.value.name.trim()) {
        dialogError.value = '角色名称不能为空';
        return;
      }
      submitting.value = true;
      dialogError.value = '';
      try {
        if (isEditing.value && editingRoleId.value) {
          await store.API.updateRole(editingRoleId.value, {
            name: formData.value.name,
            description: formData.value.description,
            permissionIds: formData.value.permissionIds
          });
        } else {
          await store.API.createRole({
            name: formData.value.name,
            description: formData.value.description,
            permissionIds: formData.value.permissionIds
          });
        }
        closeDialog();
        await loadData();
      } catch (e: any) {
        dialogError.value = e?.data?.message || '操作失败';
      } finally {
        submitting.value = false;
      }
    }

    async function handleDeleteRole(role: Role) {
      if (!confirm(`确定删除角色「${role.name}」吗？此操作不可撤销。`)) return;
      try {
        await store.API.deleteRole(role.id);
        await loadData();
      } catch (e: any) {
        error.value = e?.data?.message || '删除失败';
      }
    }

    onMounted(() => {
      loadData();
    });

    return {
      roles, permissions, loading, error,
      showDialog, isEditing, formData, submitting, dialogError,
      permissionsByModule, getModuleLabel,
      openCreateDialog, openEditDialog, closeDialog, handleSubmit, handleDeleteRole
    };
  }
});
</script>

import { defineStore } from 'pinia'
import { callGet, callPost, callPut, callDelete, callPatch } from './requests'
import { ref } from 'vue'

export interface Permission {
  id: string
  name: string
  description: string
  module: string
}

export interface Role {
  id: string
  name: string
  description: string
  builtIn: boolean
  permissions: Permission[]
}

export interface AdminUser {
  id: string
  email: string
  username: string
  enabled: boolean
  protected: boolean
  roles: Role[]
}

export const useRoleManagementStore = defineStore('roleManagementStore', () => {
  const states = {
    roles: ref<Role[]>([]),
    permissions: ref<Permission[]>([]),
    users: ref<AdminUser[]>([]),
    loading: ref<boolean>(false),
    error: ref<string | null>(null)
  }

  const API = {
    // Role Management
    getAllRoles: async (): Promise<Role[]> => {
      const result = await callGet('/admin/roles/all')
      states.roles.value = result
      return result
    },

    getRoleById: async (id: string): Promise<Role> =>
      await callGet(`/admin/roles/${id}`),

    createRole: async (data: { name: string; description: string; permissionIds: string[] }): Promise<Role> =>
      await callPost('/admin/roles/add', data),

    updateRole: async (id: string, data: { name: string; description: string; permissionIds: string[] }): Promise<Role> =>
      await callPut(`/admin/roles/edit/${id}`, data),

    deleteRole: async (id: string): Promise<void> =>
      await callDelete(`/admin/roles/delete/${id}`),

    // Permission Management
    getAllPermissions: async (): Promise<Permission[]> => {
      const result = await callGet('/admin/roles/permissions/all')
      states.permissions.value = result
      return result
    },

    getPermissionsByModule: async (module: string): Promise<Permission[]> =>
      await callGet(`/admin/roles/permissions/module/${module}`),

    // User Management
    getAllUsers: async (): Promise<AdminUser[]> => {
      const result = await callGet('/admin/users/all')
      states.users.value = result
      return result
    },

    getUserById: async (id: string): Promise<AdminUser> =>
      await callGet(`/admin/users/${id}`),

    assignUserRoles: async (userId: string, roleIds: string[]): Promise<AdminUser> =>
      await callPut(`/admin/users/${userId}/roles`, { roleIds }),

    toggleUserEnabled: async (userId: string): Promise<AdminUser> =>
      await callPatch(`/admin/users/${userId}/toggle-enabled`, {}),

    deleteUser: async (userId: string): Promise<void> =>
      await callDelete(`/admin/users/${userId}`)
  }

  return {
    states,
    API
  }
})

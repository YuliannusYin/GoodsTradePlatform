import { defineStore } from 'pinia'
import { ref } from 'vue'
import { callGet, callPost, callPut, callDelete } from './requests'

export interface LoginResponse {
  success: boolean
  message: string
  userRoles: string[]
  token: string
}

export interface AccountResponse {
  success: boolean
  message: string
  email?: string
  username?: string
  balance?: number
  isProtected?: boolean
  role?: string
}

export const useAccountStore = defineStore('accountStore', () => {
  // State
  const isAuthenticated = ref(false)
  const jwtToken = ref<string | null>(null)
  const userRole = ref<string | null>(null)
  const email = ref<string | null>(null)
  const username = ref<string | null>(null)
  const balance = ref<number>(0)
  const isProtected = ref(false)

  // Auth methods
  async function login(loginEmail: string, password: string): Promise<LoginResponse> {
    const response = await callPost<LoginResponse>('/api/account/login', {
      email: loginEmail,
      password
    })
    if (response.token) {
      jwtToken.value = response.token
      userRole.value = response.userRoles?.[0] || 'USER'
      isAuthenticated.value = true
      sessionStorage.setItem('jwtToken', response.token)
      sessionStorage.setItem('userRole', userRole.value)
      // Fetch user details after login
      await fetchUserDetails()
    }
    return response
  }

  function logout() {
    jwtToken.value = null
    userRole.value = null
    isAuthenticated.value = false
    email.value = null
    username.value = null
    balance.value = 0
    isProtected.value = false
    sessionStorage.removeItem('jwtToken')
    sessionStorage.removeItem('userRole')
  }

  function restoreSession() {
    const token = sessionStorage.getItem('jwtToken')
    const role = sessionStorage.getItem('userRole')
    if (token && role) {
      jwtToken.value = token
      userRole.value = role
      isAuthenticated.value = true
      fetchUserDetails()
    }
  }

  async function fetchUserDetails() {
    try {
      const response = await callGet<AccountResponse>('/api/account/details')
      email.value = response.email || null
      username.value = response.username || null
      balance.value = response.balance || 0
      isProtected.value = response.isProtected || false
      userRole.value = response.role || userRole.value
    } catch (e) {
      // ignore
    }
  }

  // Account methods
  async function register(userEmail: string, userName: string, password: string) {
    return callPost('/api/account/register', {
      email: userEmail,
      username: userName,
      password
    })
  }

  async function changeUsername(newUsername: string) {
    const response = await callPut('/api/account/username', { newUsername })
    if (response.success) username.value = newUsername
    return response
  }

  async function changeEmail(newEmail: string) {
    const response = await callPut('/api/account/email', { newEmail })
    if (response.success) email.value = newEmail
    return response
  }

  async function changePassword(currentPassword: string, newPassword: string) {
    return callPut('/api/account/password', { currentPassword, newPassword })
  }

  async function deleteAccount() {
    const response = await callDelete('/api/account/delete')
    if (response.success) logout()
    return response
  }

  async function isValidCredentials(emailAddress: string, password: string) {
    return callPost('/api/account/confirm', { email: emailAddress, password })
  }

  async function getOrders() {
    return callGet('/api/account/orders/all')
  }

  // Helper
  function getJwtToken(): string | null {
    return jwtToken.value || sessionStorage.getItem('jwtToken')
  }

  function hasRole(role: string): boolean {
    return userRole.value === role
  }

  function isAdmin(): boolean {
    return userRole.value === 'SUPER_ADMIN' || userRole.value === 'ADMIN'
  }

  function isMerchant(): boolean {
    return userRole.value === 'MERCHANT'
  }

  return {
    // State
    isAuthenticated, jwtToken, userRole, email, username, balance, isProtected,
    // Auth methods
    login, logout, restoreSession, getJwtToken, hasRole, isAdmin, isMerchant,
    // Account methods
    register, fetchUserDetails, changeUsername, changeEmail, changePassword, deleteAccount, isValidCredentials, getOrders
  }
})

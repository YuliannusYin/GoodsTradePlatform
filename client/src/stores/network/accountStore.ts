/**
 * @file accountStore.ts
 * @description 用户账户状态管理，处理登录、注册、登出、会话恢复及用户信息修改等操作
 * @input 登录/注册等函数接收用户凭证信息
 * @output 暴露认证状态、用户信息及账户操作方法
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { callGet, callPost, callPut, callDelete } from './requests'
import { useShoppingCartStore } from '../shoppingCartStore'

/** 登录响应数据结构，包含用户角色列表和JWT令牌 */
export interface LoginResponse {
  userRoles: string[]
  token: string
}

/** 用户账户详情数据结构，包含邮箱、用户名、余额、保护状态和角色 */
export interface AccountDetails {
  email: string
  username: string
  balance: number
  isProtected: boolean
  role: string
}

/**
 * 用户账户状态管理Store
 * 职责：管理用户认证状态、JWT令牌、用户信息，提供登录注册及信息修改操作
 */
export const useAccountStore = defineStore('accountStore', () => {
  // 是否已通过认证
  const isAuthenticated = ref(false)
  // 当前用户的邮箱
  const email = ref<string | null>(null)
  // 当前用户的用户名
  const username = ref<string | null>(null)
  // 当前用户的账户余额
  const balance = ref<number>(0)
  // 当前账户是否已设置密码保护
  const isProtected = ref(false)

  /**
   * 用户登录，验证凭证并保存令牌和角色信息
   * 登录成功后合并本地购物车到后端
   * @param {string} loginEmail - 登录邮箱
   * @param {string} password - 登录密码
   * @returns {Promise<LoginResponse>} 登录响应，包含令牌和角色
   */
  async function login(loginEmail: string, password: string): Promise<LoginResponse> {
    const response = await callPost<LoginResponse>('/api/account/login', {
      email: loginEmail,
      password
    })
    if (response.token) {
      // 登录成功：保存令牌、角色到sessionStorage
      sessionStorage.setItem('jwtToken', response.token)
      // 取第一个角色，无角色时默认为USER
      sessionStorage.setItem('userRole', response.userRoles?.[0] || 'USER')
      isAuthenticated.value = true
      // 登录后立即拉取用户详细信息
      await fetchUserDetails()
      // 登录后合并本地购物车到后端
      const shoppingCartStore = useShoppingCartStore()
      await shoppingCartStore.mergeCartAfterLogin()
    }
    return response
  }

  /**
   * 用户登出，清除所有认证状态和用户信息，并清空购物车（含后端同步）
   */
  async function logout() {
    // 重置所有状态为初始值
    isAuthenticated.value = false
    email.value = null
    username.value = null
    balance.value = 0
    isProtected.value = false
    // 清除sessionStorage中的认证信息
    sessionStorage.removeItem('jwtToken')
    sessionStorage.removeItem('userRole')
    // 退出登录时清空购物车（含后端同步），防止未登录用户看到残留数据
    const shoppingCartStore = useShoppingCartStore()
    await shoppingCartStore.clearCart()
  }

  /**
   * 从sessionStorage恢复用户会话状态
   * 在页面刷新后调用，验证令牌有效性后再恢复认证状态
   */
  async function restoreSession() {
    const token = sessionStorage.getItem('jwtToken')
    const role = sessionStorage.getItem('userRole')
    if (token && role) {
      try {
        // 验证令牌有效性：尝试拉取用户详情，失败则说明令牌已过期
        await fetchUserDetails()
        // 拉取成功，确认认证状态
        isAuthenticated.value = true
      } catch (e) {
        // 令牌无效或过期，清除登录状态，避免"假登录"
        logout()
      }
    }
  }

  /**
   * 拉取当前用户的详细信息并更新状态
   * 认证失败（401）时抛出异常，供restoreSession判断令牌有效性
   */
  async function fetchUserDetails() {
    try {
      const response = await callGet<AccountDetails>('/api/account/details')
      email.value = response.email || null
      username.value = response.username || null
      balance.value = response.balance || 0
      isProtected.value = response.isProtected || false
      // 优先使用接口返回的角色信息
      sessionStorage.setItem('userRole', response.role || sessionStorage.getItem('userRole') || 'USER')
    } catch (e: unknown) {
      // 401表示令牌无效或过期，向上抛出以便restoreSession处理
      if (e instanceof Error && 'response' in e && (e as { response?: { status?: number } }).response?.status === 401) {
        throw e
      }
      // 其他错误（如网络波动）静默忽略，不中断用户操作
    }
  }

  /**
   * 用户注册新账户
   * @param {string} userEmail - 注册邮箱
   * @param {string} userName - 注册用户名
   * @param {string} password - 注册密码
   */
  async function register(userEmail: string, userName: string, password: string) {
    return callPost('/api/account/register', {
      email: userEmail,
      username: userName,
      password
    })
  }

  /**
   * 修改用户名
   * @param {string} newUsername - 新用户名
   * @returns 修改结果，成功时同步更新本地状态
   */
  async function changeUsername(newUsername: string) {
    const response = await callPut('/api/account/username', { newUsername })
    // 修改成功时同步更新本地用户名
    if (response) username.value = newUsername
    return response
  }

  /**
   * 修改邮箱
   * @param {string} newEmail - 新邮箱地址
   * @returns 修改结果，成功时同步更新本地状态
   */
  async function changeEmail(newEmail: string) {
    const response = await callPut('/api/account/email', { newEmail })
    // 修改成功时同步更新本地邮箱
    if (response) email.value = newEmail
    return response
  }

  /**
   * 修改密码
   * @param {string} currentPassword - 当前密码
   * @param {string} newPassword - 新密码
   */
  async function changePassword(currentPassword: string, newPassword: string) {
    return callPut('/api/account/password', { currentPassword, newPassword })
  }

  /**
   * 删除当前用户账户
   * 删除成功后自动执行登出操作
   */
  async function deleteAccount() {
    const response = await callDelete('/api/account/delete')
    // 删除成功后自动登出
    if (response) logout()
    return response
  }

  /**
   * 验证用户凭证是否有效（用于敏感操作前的二次确认）
   * @param {string} emailAddress - 用户邮箱
   * @param {string} password - 用户密码
   */
  async function isValidCredentials(emailAddress: string, password: string) {
    return callPost('/api/account/confirm', { email: emailAddress, password })
  }

  /**
   * 判断当前用户是否为管理员（超级管理员或普通管理员）
   * @returns {boolean} 是否为管理员角色
   */
  function isAdmin(): boolean {
    const role = sessionStorage.getItem('userRole')
    return role === 'SUPER_ADMIN' || role === 'ADMIN'
  }

  /**
   * 判断当前用户是否为商户
   * @returns {boolean} 是否为商户角色
   */
  function isMerchant(): boolean {
    return sessionStorage.getItem('userRole') === 'MERCHANT'
  }

  return {
    isAuthenticated, email, username, balance, isProtected,
    login, logout, restoreSession, isAdmin, isMerchant,
    register, fetchUserDetails, changeUsername, changeEmail, changePassword, deleteAccount, isValidCredentials
  }
})

/**
 * @file user.ts
 * @description 定义用户数据结构
 * @input 无
 * @output User 接口
 */

/**
 * 用户数据结构，表示系统中的用户信息
 */
export interface User {
  /** 用户 ID */
  id: string
  /** 用户邮箱 */
  email: string
  /** 用户名 */
  username: string
  /** 用户角色（如 USER、ADMIN） */
  role: string
  /** 用户头像 URL，可能为空 */
  avatarUrl: string | null
  /** 个人简介，可能为空 */
  bio: string | null
  /** 账户余额 */
  balance: number
  /** 是否受保护账户（不可被管理员删除） */
  isProtected: boolean
  /** 账户是否启用 */
  isEnabled: boolean
}

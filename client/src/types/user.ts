export interface User {
  id: string
  email: string
  username: string
  role: string
  balance: number
  isProtected: boolean
  isEnabled: boolean
  avatarUrl?: string
  bio?: string
}

export interface User {
  id: string
  email: string
  username: string
  role: string
  avatarUrl: string | null
  bio: string | null
  balance: number
  isProtected: boolean
  isEnabled: boolean
}

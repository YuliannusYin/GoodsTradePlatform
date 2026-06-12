export interface ApiResponse {
  success: boolean
  message: string
  status?: number
}

export interface LoginResponse {
  success: boolean
  message: string
  userRoles: string[]
  token: string
}

export interface UserDetails {
  email: string
  username: string
  balance: number
  isProtected: boolean
  role: string
}

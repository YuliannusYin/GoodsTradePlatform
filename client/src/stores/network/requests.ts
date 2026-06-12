import axios from 'axios'
import router from '@/router'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
})

// 请求拦截器 - 自动注入 JWT
apiClient.interceptors.request.use(config => {
  const token = sessionStorage.getItem('jwtToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 统一错误处理
apiClient.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('jwtToken')
      router.push('/login')
    }
    throw error
  }
)

export async function callGet<T = any>(endpoint: string): Promise<T> {
  return apiClient.get(endpoint)
}

export async function callPost<T = any>(endpoint: string, data?: any): Promise<T> {
  return apiClient.post(endpoint, data)
}

export async function callPut<T = any>(endpoint: string, data?: any): Promise<T> {
  return apiClient.put(endpoint, data)
}

export async function callPatch<T = any>(endpoint: string, data?: any): Promise<T> {
  return apiClient.patch(endpoint, data)
}

export async function callDelete<T = any>(endpoint: string): Promise<T> {
  return apiClient.delete(endpoint)
}

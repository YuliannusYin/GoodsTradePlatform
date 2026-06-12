import axios from 'axios'
import router from '@/router'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || ''
})

apiClient.interceptors.request.use(config => {
  const token = sessionStorage.getItem('jwtToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  response => {
    const apiResponse = response.data
    // If the response follows ApiResponse format, extract the data field
    if (apiResponse && typeof apiResponse === 'object' && 'success' in apiResponse && 'data' in apiResponse) {
      if (!apiResponse.success) {
        throw new Error(apiResponse.message || 'Request failed')
      }
      return apiResponse.data
    }
    // Fallback for non-standard responses
    return apiResponse
  },
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

/**
 * @file requests.ts
 * @description HTTP请求封装模块，基于axios创建API客户端，统一处理请求拦截（JWT注入）和响应拦截（ApiResponse解包、401跳转）
 * @input 各请求函数接收endpoint路径和可选的data参数
 * @output 返回经过响应拦截器解包后的API数据
 */
import axios from 'axios'
import router from '@/router'

// 创建axios实例，baseURL从环境变量VITE_API_BASE_URL读取
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || ''
})

// 请求拦截器：自动在请求头中注入JWT令牌
apiClient.interceptors.request.use(config => {
  // 从sessionStorage获取已保存的JWT令牌
  const token = sessionStorage.getItem('jwtToken')
  if (token) {
    // 令牌存在时，添加Bearer认证头
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：解包ApiResponse格式数据，处理401未授权
apiClient.interceptors.response.use(
  response => {
    const apiResponse = response.data
    // 判断响应是否遵循ApiResponse标准格式（包含success和data字段）
    if (apiResponse && typeof apiResponse === 'object' && 'success' in apiResponse && 'data' in apiResponse) {
      if (!apiResponse.success) {
        // 业务逻辑失败时，抛出错误信息
        throw new Error(apiResponse.message || 'Request failed')
      }
      // 成功时提取data字段返回
      return apiResponse.data
    }
    // 非标准ApiResponse格式，直接返回原始数据
    return apiResponse
  },
  error => {
    if (error.response?.status === 401) {
      // 401未授权：清除本地令牌并跳转到登录页
      sessionStorage.removeItem('jwtToken')
      sessionStorage.removeItem('userRole')
      router.push('/login')
    } else if (error.response?.status === 403) {
      // 403禁止访问：权限不足，提示用户
      console.error('权限不足，无法访问该资源')
    }
    throw error
  }
)

/**
 * 发送GET请求
 * @param {string} endpoint - API端点路径
 * @returns {Promise<T>} 响应数据
 */
export async function callGet<T = any>(endpoint: string): Promise<T> {
  return apiClient.get(endpoint)
}

/**
 * 发送POST请求
 * @param {string} endpoint - API端点路径
 * @param {any} data - 请求体数据
 * @returns {Promise<T>} 响应数据
 */
export async function callPost<T = any>(endpoint: string, data?: any): Promise<T> {
  return apiClient.post(endpoint, data)
}

/**
 * 发送PUT请求
 * @param {string} endpoint - API端点路径
 * @param {any} data - 请求体数据
 * @returns {Promise<T>} 响应数据
 */
export async function callPut<T = any>(endpoint: string, data?: any): Promise<T> {
  return apiClient.put(endpoint, data)
}

/**
 * 发送PATCH请求
 * @param {string} endpoint - API端点路径
 * @param {any} data - 请求体数据
 * @returns {Promise<T>} 响应数据
 */
export async function callPatch<T = any>(endpoint: string, data?: any): Promise<T> {
  return apiClient.patch(endpoint, data)
}

/**
 * 发送DELETE请求
 * @param {string} endpoint - API端点路径
 * @returns {Promise<T>} 响应数据
 */
export async function callDelete<T = any>(endpoint: string): Promise<T> {
  return apiClient.delete(endpoint)
}

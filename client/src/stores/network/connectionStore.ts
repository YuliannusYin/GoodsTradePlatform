import { defineStore } from 'pinia'
import { callPost, ApiError } from './requests'
import { ref } from 'vue'
import { useAuthenticationStore } from '../authenticationStore'
import { useAccountStore } from './accountStore'

export interface LoginResponseSuccess {
  success: boolean
  userRoles: string[]
  token: string
}

export interface ResponseError {
  error: boolean
  message: string
}

export interface ResponseSuccess {
  success: boolean
  message: string
}

export interface LoginDiagnostics {
  timestamp: string
  httpStatus: number
  backendMessage: string
  fullResponseData: any
  requestUrl: string
  requestMethod: string
  requestPayload: { email: string }
  errorType: 'backend' | 'network' | 'unknown'
}

export const useConnectionStore = defineStore('connectionStore', () => {
  const states = {
    loginErrorResponse: ref<ResponseError | null>(null),
    loginDiagnostics: ref<LoginDiagnostics | null>(null)
  }

  const API = {
    submitLogin: async (email: string, password: string): Promise<LoginResponseSuccess | ResponseError> => {
      states.loginDiagnostics.value = null
      try {
        const response: LoginResponseSuccess = await callPost('/account/login', {
          email: email,
          password: password
        })
        states.loginErrorResponse.value = null
        useAuthenticationStore().methods.handleAuthentication(response)

        if (!useAccountStore().states.username || !useAccountStore().states.email) {
          useAccountStore().API.getUserDetails()
        }

        return response
      } catch (error) {
        if (error instanceof ApiError) {
          const errorResponse: ResponseError = {
            error: true,
            message: error.data?.message || '登录失败'
          }
          states.loginErrorResponse.value = errorResponse
          states.loginDiagnostics.value = {
            timestamp: new Date().toISOString(),
            httpStatus: error.status,
            backendMessage: error.data?.message || '无消息',
            fullResponseData: error.data,
            requestUrl: error.url,
            requestMethod: error.method,
            requestPayload: { email },
            errorType: error.status === 0 ? 'network' : 'backend'
          }
          return errorResponse
        }
        const errorResponse: ResponseError = { error: true, message: '网络错误' }
        states.loginErrorResponse.value = errorResponse
        states.loginDiagnostics.value = {
          timestamp: new Date().toISOString(),
          httpStatus: 0,
          backendMessage: '网络错误 - 无法连接服务器',
          fullResponseData: null,
          requestUrl: '/api/account/login',
          requestMethod: 'POST',
          requestPayload: { email },
          errorType: 'network'
        }
        return errorResponse
      }
    },

    submitRelog: async (password: string, routerOriginName: string): Promise<LoginResponseSuccess | ResponseError> => {
      const email = useAccountStore().states.email as string
      const response = await API.submitLogin(email, password)

      if ('success' in response && response.success) {
        useAuthenticationStore().methods.handleAuthentication(response, routerOriginName)
      }
      return response
    },

    submitLogout: async () => useAuthenticationStore().methods.handleRevokeAuthentication(),

    testBackendConnection: async (): Promise<{ reachable: boolean; status?: number; message: string }> => {
      try {
        const response = await callPost('/account/login', {})
        return { reachable: true, status: 200, message: '后端可访问（返回了意外的成功响应）' }
      } catch (error) {
        if (error instanceof ApiError) {
          if (error.status === 0) {
            return { reachable: false, message: '无法连接后端服务器' }
          }
          return { reachable: true, status: error.status, message: `后端可访问，返回 HTTP ${error.status}` }
        }
        return { reachable: false, message: '测试后端连接时出现未知错误' }
      }
    }
  }

  return {
    states,
    API
  }
})

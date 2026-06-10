import { defineStore } from 'pinia'
import { callPost, ApiError } from './requests'
import { ref } from 'vue'
import { useAuthenticationStore } from '../authenticationStore'
import { useAccountStore } from './accountStore'

export interface LoginResponseSuccess {
  success: boolean
  userRole: string
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

export const useConnectionStore = defineStore('connectionStore', () => {
  const states = {
    loginErrorResponse: ref<ResponseError | null>(null)
  }

  const API = {
    submitLogin: async (email: string, password: string): Promise<LoginResponseSuccess | ResponseError> => {
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
            message: error.data?.message || 'Login failed'
          }
          states.loginErrorResponse.value = errorResponse
          return errorResponse
        }
        const errorResponse: ResponseError = { error: true, message: 'Network error' }
        states.loginErrorResponse.value = errorResponse
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

    submitLogout: async () => useAuthenticationStore().methods.handleRevokeAuthentication()
  }

  return {
    states,
    API
  }
})

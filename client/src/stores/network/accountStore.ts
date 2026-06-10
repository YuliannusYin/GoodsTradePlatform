import { defineStore } from 'pinia'
import { callPost, callGet, callPut, callDelete, ApiError } from './requests'
import { ref, watch } from 'vue'
import { useAuthenticationStore } from '../authenticationStore'

export interface ResponseError {
  error: boolean
  message: string
}

export interface ResponseSuccess {
  success: boolean
  message: string
}

export const useAccountStore = defineStore('accountStore', () => {
  const states = {
    signupResponse: ref<ResponseSuccess | ResponseError | null>(null),

    changeUsernameResponse: ref<ResponseSuccess | ResponseError | null>(null),

    changeEmailResponse: ref<ResponseSuccess | ResponseError | null>(null),

    changePasswordResponse: ref<ResponseSuccess | ResponseError | null>(null),

    deleteAccountResponse: ref<ResponseSuccess | ResponseError | null>(null),

    isConfirmationErrorResponse: ref<boolean>(false),

    username: ref<string | null>(null),
    email: ref<string | null>(null),
    orders: ref<any | null>(null)
  }

  watch(
    async () => useAuthenticationStore().states.isAuthenticated,
    async (change) => {
      const isAuthenticated: boolean = await change
      if (isAuthenticated) {
        await API.getUserDetails()
      }
    }
  )

  const API = {
    submitSignup: async (username: string, email: string, password: string): Promise<ResponseSuccess | ResponseError> => {
      try {
        const response: ResponseSuccess = await callPost('/account/register', {
          username: username,
          email: email,
          password: password
        })
        states.signupResponse.value = { success: true, message: response.message }
        return response
      } catch (error) {
        const errorResponse = toResponseError(error)
        states.signupResponse.value = errorResponse
        return errorResponse
      }
    },

    confirmCredentials: async (password: string): Promise<boolean> => {
      try {
        const response: any = await callPost('/account/confirm', {
          email: useAccountStore().states.email,
          password: password
        })
        return !!response
      } catch (error) {
        states.isConfirmationErrorResponse.value = true
        return false
      }
    },

    getUserDetails: async () => {
      try {
        const response = await callGet('/account/details')
        if (response.success) {
          states.username.value = response.username
          states.email.value = response.email
        }
      } catch (error) {
        // silently fail - user details can be fetched again
      }
    },

    changeUsername: async (newUsername: string): Promise<ResponseSuccess | ResponseError> => {
      try {
        const response: ResponseSuccess = await callPut('/account/username', {
          newUsername: newUsername
        })
        states.changeUsernameResponse.value = { success: true, message: response.message }
        return response
      } catch (error) {
        const errorResponse = toResponseError(error)
        states.changeUsernameResponse.value = errorResponse
        return errorResponse
      }
    },

    changeEmail: async (newEmail: string): Promise<ResponseSuccess | ResponseError> => {
      try {
        const response: ResponseSuccess = await callPut('/account/email', {
          newEmail: newEmail
        })
        states.changeEmailResponse.value = { success: true, message: response.message }
        return response
      } catch (error) {
        const errorResponse = toResponseError(error)
        states.changeEmailResponse.value = errorResponse
        return errorResponse
      }
    },

    changePassword: async (currentPassword: string, newPassword: string): Promise<ResponseSuccess | ResponseError> => {
      try {
        const response: ResponseSuccess = await callPut('/account/password', {
          currentPassword: currentPassword,
          newPassword: newPassword
        })
        states.changePasswordResponse.value = { success: true, message: response.message }
        return response
      } catch (error) {
        const errorResponse = toResponseError(error)
        states.changePasswordResponse.value = errorResponse
        return errorResponse
      }
    },

    getOrders: () => callGet('/account/orders/all'),

    deleteAccount: async (): Promise<ResponseSuccess | ResponseError> => {
      try {
        const response: ResponseSuccess = await callDelete('/account/delete')
        states.deleteAccountResponse.value = { success: true, message: response.message }
        return response
      } catch (error) {
        const errorResponse = toResponseError(error)
        states.deleteAccountResponse.value = errorResponse
        return errorResponse
      }
    }
  }

  function toResponseError(error: unknown): ResponseError {
    if (error instanceof ApiError) {
      return { error: true, message: error.data?.message || 'Request failed' }
    }
    return { error: true, message: 'Network error' }
  }

  return {
    states,
    API
  }
})

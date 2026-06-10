import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginResponseSuccess } from './network/connectionStore'
import navigationProvider from '../router/navigationProvider'

function getStoredUserRole(): string | null {
  return sessionStorage.getItem('userRole')
}

export const useAuthenticationStore = defineStore('authenticationStore', () => {
  const storedToken = sessionStorage.getItem('jwtToken')
  const storedRole = getStoredUserRole()

  const states = {
    isAuthenticated: ref<boolean>(!!storedToken),
    isAdmin: ref<boolean>(storedRole === 'ADMIN')
  }

  const methods = {
    handleAuthentication: async (response: LoginResponseSuccess, routerOriginName?: string) => {
      if (response.success && response.token) {
        storeJwtToken(response.token)
        states.isAuthenticated.value = true

        if (response.userRole === 'ADMIN') {
          states.isAdmin.value = true
        } else {
          states.isAdmin.value = false
        }
        storeUserRole(response.userRole)

        navigationProvider.navigateOnCondition(
          states.isAuthenticated.value,
          routerOriginName ? routerOriginName : 'home',
          'login'
        )
      }
    },

    handleRevokeAuthentication: () => {
      clearJwtToken()
      clearUserRole()
      revokeAuthentication()
      navigationProvider.navigate('home')
    },

    getJwtToken: () => {
      return sessionStorage.getItem('jwtToken')
    }
  }

  function revokeAuthentication() {
    states.isAuthenticated.value = false
    states.isAdmin.value = false
  }

  function storeJwtToken(token: string) {
    sessionStorage.setItem('jwtToken', token)
  }

  function storeUserRole(role: string) {
    sessionStorage.setItem('userRole', role)
  }

  function clearJwtToken() {
    sessionStorage.removeItem('jwtToken')
  }

  function clearUserRole() {
    sessionStorage.removeItem('userRole')
  }

  return {
    states,
    methods
  }
})

import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { LoginResponseSuccess } from './network/connectionStore'
import navigationProvider from '../router/navigationProvider'

function getStoredUserRoles(): string[] {
  const stored = sessionStorage.getItem('userRoles')
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch {
      return []
    }
  }
  return []
}

export const useAuthenticationStore = defineStore('authenticationStore', () => {
  const storedToken = sessionStorage.getItem('jwtToken')
  const storedRoles = getStoredUserRoles()

  const states = {
    isAuthenticated: ref<boolean>(!!storedToken),
    roles: ref<string[]>(storedRoles),
    isAdmin: computed<boolean>(() =>
      states.roles.value.includes('SUPER_ADMIN') || states.roles.value.includes('ADMIN')
    ),
    isSuperAdmin: computed<boolean>(() =>
      states.roles.value.includes('SUPER_ADMIN')
    )
  }

  const methods = {
    handleAuthentication: async (response: LoginResponseSuccess, routerOriginName?: string) => {
      if (response.success && response.token) {
        storeJwtToken(response.token)
        states.isAuthenticated.value = true

        if (response.userRoles && Array.isArray(response.userRoles)) {
          states.roles.value = response.userRoles
        } else {
          states.roles.value = []
        }
        storeUserRoles(states.roles.value)

        navigationProvider.navigateOnCondition(
          states.isAuthenticated.value,
          routerOriginName ? routerOriginName : 'home',
          'login'
        )
      }
    },

    handleRevokeAuthentication: () => {
      clearJwtToken()
      clearUserRoles()
      revokeAuthentication()
      navigationProvider.navigate('home')
    },

    getJwtToken: () => {
      return sessionStorage.getItem('jwtToken')
    }
  }

  function revokeAuthentication() {
    states.isAuthenticated.value = false
    states.roles.value = []
  }

  function storeJwtToken(token: string) {
    sessionStorage.setItem('jwtToken', token)
  }

  function storeUserRoles(roles: string[]) {
    sessionStorage.setItem('userRoles', JSON.stringify(roles))
  }

  function clearJwtToken() {
    sessionStorage.removeItem('jwtToken')
  }

  function clearUserRoles() {
    sessionStorage.removeItem('userRoles')
  }

  return {
    states,
    methods
  }
})

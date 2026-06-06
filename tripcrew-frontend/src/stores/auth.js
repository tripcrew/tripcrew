import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { authApi } from '@/api/auth'
import { tokenStorage } from '@/api/http'

/**
 * 인증 상태 store. 토큰의 단일 출처는 localStorage(tokenStorage)이고,
 * store 는 화면 반응성을 위해 그것을 미러링한다.
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(tokenStorage.getAccess())
  const user = ref(null)

  const isAuthenticated = computed(() => !!accessToken.value)

  async function signup(payload) {
    return authApi.signup(payload)
  }

  async function login(credentials) {
    const tokens = await authApi.login(credentials)
    tokenStorage.set(tokens.accessToken, tokens.refreshToken)
    accessToken.value = tokens.accessToken
    return tokens
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 서버 로그아웃 실패해도 로컬 토큰은 비운다
    }
    tokenStorage.clear()
    accessToken.value = null
    user.value = null
  }

  return { accessToken, user, isAuthenticated, signup, login, logout }
})

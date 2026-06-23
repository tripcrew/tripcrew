import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { authApi } from '@/api/auth'
import { tokenStorage } from '@/api/http'

const USER_KEY = 'tripcrew.user'

function readStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

function writeStoredUser(user) {
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
  else localStorage.removeItem(USER_KEY)
}

/**
 * 인증 상태 store. 토큰의 단일 출처는 localStorage(tokenStorage)이고,
 * store 는 화면 반응성을 위해 그것을 미러링한다.
 */
export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(tokenStorage.getAccess())
  const user = ref(readStoredUser())

  const isAuthenticated = computed(() => !!accessToken.value)

  async function signup(payload) {
    return authApi.signup(payload)
  }

  async function login(credentials) {
    const tokens = await authApi.login(credentials)
    tokenStorage.set(tokens.accessToken, tokens.refreshToken)
    accessToken.value = tokens.accessToken
    user.value = tokens.user || null
    writeStoredUser(user.value)
    return tokens
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 서버 로그아웃 실패해도 로컬 토큰은 비운다
    }
    tokenStorage.clear()
    writeStoredUser(null)
    accessToken.value = null
    user.value = null
  }

  async function updateNickname(nickname, currentPassword) {
    const updatedUser = await authApi.updateNickname(nickname, currentPassword)
    user.value = updatedUser
    writeStoredUser(updatedUser)
    return updatedUser
  }

  async function verifyPassword(currentPassword) {
    await authApi.verifyPassword(currentPassword)
  }

  async function withdraw(currentPassword) {
    await authApi.withdraw(currentPassword)
    tokenStorage.clear()
    writeStoredUser(null)
    accessToken.value = null
    user.value = null
  }

  return { accessToken, user, isAuthenticated, signup, login, logout, verifyPassword, updateNickname, withdraw }
})

import axios from 'axios'

/**
 * 백엔드 REST API 공통 axios 인스턴스.
 *
 * - 요청: access token 을 Authorization 헤더에 자동 첨부
 * - 응답: 401 이면 refresh token 으로 1회 재발급 후 원요청 재시도,
 *         재발급도 실패하면 토큰 비우고 로그인 화면으로 보냄
 *
 * 토큰은 localStorage 를 단일 출처로 둔다. (store 와의 순환 import 회피 +
 * 새로고침 후에도 유지)
 */
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

/**
 * 정적 리소스(업로드 이미지 등) origin. API baseURL 에서 끝의 `/api` 를 떼어낸 값.
 * 예: http://localhost:8080/api → http://localhost:8080
 * 백엔드가 내려주는 상대 경로(/uploads/...)는 이 값을 앞에 붙여 절대 URL 로 만든다.
 */
export const assetBaseURL = baseURL.replace(/\/api\/?$/, '')

/** 업로드 이미지 상대경로(/uploads/...)를 절대 URL 로. 이미 절대 URL 이면 그대로 둔다. */
export function toAssetUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  return assetBaseURL + path
}

const ACCESS_KEY = 'tripcrew.accessToken'
const REFRESH_KEY = 'tripcrew.refreshToken'

export const tokenStorage = {
  getAccess: () => localStorage.getItem(ACCESS_KEY),
  getRefresh: () => localStorage.getItem(REFRESH_KEY),
  set: (accessToken, refreshToken) => {
    localStorage.setItem(ACCESS_KEY, accessToken)
    if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken)
  },
  clear: () => {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}

export const http = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
})

// 요청 인터셉터: access token 자동 첨부
http.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 동시에 여러 요청이 401 을 받아도 재발급은 1번만 (single-flight)
let refreshingPromise = null

async function reissueAccessToken() {
  const refreshToken = tokenStorage.getRefresh()
  if (!refreshToken) throw new Error('no refresh token')
  // 인터셉터 루프 방지를 위해 기본 axios 로 직접 호출
  const { data } = await axios.post(`${baseURL}/auth/reissue`, { refreshToken })
  tokenStorage.set(data.accessToken, data.refreshToken)
  return data.accessToken
}

// 응답 인터셉터: 401 → 재발급 → 재시도
http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error
    const isAuthCall = config?.url?.includes('/auth/')

    if (response?.status !== 401 || config?._retry || isAuthCall) {
      return Promise.reject(error)
    }

    config._retry = true
    try {
      refreshingPromise = refreshingPromise || reissueAccessToken()
      const newAccessToken = await refreshingPromise
      refreshingPromise = null
      config.headers.Authorization = `Bearer ${newAccessToken}`
      return http(config)
    } catch (reissueError) {
      refreshingPromise = null
      tokenStorage.clear()
      if (typeof window !== 'undefined') window.location.assign('/auth')
      return Promise.reject(reissueError)
    }
  },
)

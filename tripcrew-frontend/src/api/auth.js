import { http } from './http'

/**
 * 인증(F01) API. 백엔드 엔드포인트와 1:1 대응.
 *   POST /api/auth/signup | /login | /reissue | /logout
 */
export const authApi = {
  signup: (payload) => http.post('/auth/signup', payload).then((r) => r.data),
  login: (payload) => http.post('/auth/login', payload).then((r) => r.data),
  reissue: (refreshToken) =>
    http.post('/auth/reissue', { refreshToken }).then((r) => r.data),
  logout: () => http.post('/auth/logout'),
}

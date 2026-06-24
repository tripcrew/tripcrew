import { http } from './http'

/**
 * 인증(F01) API. 백엔드 엔드포인트와 1:1 대응.
 *   POST /api/auth/signup | /login | /reissue | /logout
 *   POST /api/auth/me/verify-password | PATCH /api/auth/me/nickname | PATCH /api/auth/me/password | DELETE /api/auth/me
 */
export const authApi = {
  signup: (payload) => http.post('/auth/signup', payload).then((r) => r.data),
  login: (payload) => http.post('/auth/login', payload).then((r) => r.data),
  reissue: (refreshToken) =>
    http.post('/auth/reissue', { refreshToken }).then((r) => r.data),
  logout: () => http.post('/auth/logout'),
  verifyPassword: (currentPassword) => http.post('/auth/me/verify-password', { currentPassword }),
  updateNickname: (nickname, currentPassword) =>
    http.patch('/auth/me/nickname', { nickname, currentPassword }).then((r) => r.data),
  updatePassword: (currentPassword, newPassword) =>
    http.patch('/auth/me/password', { currentPassword, newPassword }),
  withdraw: (currentPassword) => http.delete('/auth/me', { data: { currentPassword } }),
}

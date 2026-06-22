import { http } from './http'

/**
 * 공지사항(F10) API. 백엔드 엔드포인트와 1:1 대응.
 *
 * 공개(조회):
 *   GET /api/notices         목록(고정 우선·최신순)
 *   GET /api/notices/{id}    상세 (조회 시 조회수 +1)
 *
 * 관리자(작성/수정/삭제) — /api/admin/notices, 서버 SecurityConfig 에서 ROLE_ADMIN 전용:
 *   POST   /api/admin/notices        작성 → 201
 *   PUT    /api/admin/notices/{id}   수정
 *   DELETE /api/admin/notices/{id}   삭제 → 204
 */
export const noticeApi = {
  list: () => http.get('/notices').then((r) => r.data),
  detail: (id) => http.get(`/notices/${id}`).then((r) => r.data),
}

export const adminNoticeApi = {
  create: (payload) => http.post('/admin/notices', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/admin/notices/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/admin/notices/${id}`),
}

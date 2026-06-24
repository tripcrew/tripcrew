import { http } from './http'

/**
 * 1:1 문의(Q&A) API — 사용자용(모두 인증). 관리자용(목록/답변)은 admin.js 의 adminApi 참고.
 *   POST /api/inquiries       문의 작성
 *   GET  /api/me/inquiries    내 문의 목록(답변 포함, 본인 스코프)
 *
 * 문의 한 건: { id, title, content, status('OPEN'|'ANSWERED'), answer, answeredAt, createdAt }
 */
export const inquiryApi = {
  create: (payload) => http.post('/inquiries', payload).then((r) => r.data),
  myList: () => http.get('/me/inquiries').then((r) => r.data),
}

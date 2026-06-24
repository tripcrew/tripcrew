import { http } from './http'

/**
 * 관리자(F09) - 사용자 관리 API. 백엔드 엔드포인트와 1:1 대응.
 *   GET   /api/admin/users             사용자 목록 (password 미포함) — ROLE_ADMIN 전용
 *   PATCH /api/admin/users/{id}/role   권한 변경 (USER ↔ ADMIN) → 204 — ROLE_SUPER_ADMIN 전용
 *   PATCH /api/admin/users/{id}/ban    계정 제재 → 204 — ROLE_ADMIN 전용
 *   PATCH /api/admin/users/{id}/unban  제재 해제 → 204 — ROLE_ADMIN 전용
 *   GET   /api/admin/reports                신고 목록(이메일+후기내용, 보통 OPEN만) — ROLE_ADMIN 전용
 *   PATCH /api/admin/reports/{id}/resolve   처리완료(누적+1, 3회시 자동제재) → 204 — ROLE_ADMIN 전용
 *   PATCH /api/admin/reports/{id}/dismiss   기각(카운트 없음) → 204 — ROLE_ADMIN 전용
 *
 * /api/admin/** 은 서버 SecurityConfig 에서 ROLE_ADMIN 전용, 그중 role 변경은
 * SUPER_ADMIN 전용이라 일반 USER 는 401/403, ADMIN 도 role 변경엔 403 이 떨어진다
 * (프론트 가드와 무관한 진짜 방어선). SUPER_ADMIN 부여는 API 로 불가(서버가 400).
 * 밴 대상 제한(본인·SUPER_ADMIN 불가, ADMIN 은 SUPER_ADMIN 만)도 서버가 400 으로 막는다.
 */
export const adminApi = {
  dashboard: () => http.get('/admin/dashboard').then((r) => r.data),
  listUsers: () => http.get('/admin/users').then((r) => r.data),
  updateRole: (id, role) => http.patch(`/admin/users/${id}/role`, { role }),
  ban: (id) => http.patch(`/admin/users/${id}/ban`),
  unban: (id) => http.patch(`/admin/users/${id}/unban`),
  listReports: (status) =>
    http.get('/admin/reports', { params: status ? { status } : {} }).then((r) => r.data),
  resolveReport: (id) => http.patch(`/admin/reports/${id}/resolve`),
  dismissReport: (id) => http.patch(`/admin/reports/${id}/dismiss`),
}

import { http } from './http'

/**
 * 관리자(F09) - 사용자 관리 API. 백엔드 엔드포인트와 1:1 대응.
 *   GET   /api/admin/users            사용자 목록 (password 미포함) — ROLE_ADMIN 전용
 *   PATCH /api/admin/users/{id}/role  권한 변경 (USER ↔ ADMIN) → 204 — ROLE_SUPER_ADMIN 전용
 *
 * /api/admin/** 은 서버 SecurityConfig 에서 ROLE_ADMIN 전용, 그중 role 변경은
 * SUPER_ADMIN 전용이라 일반 USER 는 401/403, ADMIN 도 role 변경엔 403 이 떨어진다
 * (프론트 가드와 무관한 진짜 방어선). SUPER_ADMIN 부여는 API 로 불가(서버가 400).
 */
export const adminApi = {
  listUsers: () => http.get('/admin/users').then((r) => r.data),
  updateRole: (id, role) => http.patch(`/admin/users/${id}/role`, { role }),
}

import { http } from './http'

/**
 * 여행계획(F03) API. 백엔드 엔드포인트와 1:1 대응.
 *   GET    /api/trip-plans        내 계획 목록
 *   GET    /api/trip-plans/{id}   단건 조회
 *   POST   /api/trip-plans        생성
 *   PUT    /api/trip-plans/{id}   수정 (낙관적 락: payload 에 version 포함)
 *   DELETE /api/trip-plans/{id}   삭제
 *   GET    /api/trip-plans/{id}/places
 *   GET    /api/trip-plans/{id}/places/driving-route?visitDay={day}
 *   POST   /api/trip-plans/{id}/places
 *   PUT    /api/trip-plans/{id}/places/{placeId}/schedule
 *   PUT    /api/trip-plans/{id}/places/reorder
 *   POST   /api/trip-plans/{id}/places/optimize
 *   DELETE /api/trip-plans/{id}/places/{placeId}
 *   GET    /api/trip-plans/{id}/members            멤버 목록(공동편집)
 *   POST   /api/trip-plans/{id}/members            이메일 초대 {email, role}
 *   PATCH  /api/trip-plans/{id}/members/{userId}   역할 변경 {role}
 *   DELETE /api/trip-plans/{id}/members/{userId}   멤버 제거 / 본인 탈퇴
 *
 * 수정 시 마지막으로 읽은 version 을 함께 보내야 하며, 서버 version 과
 * 다르면 409(OptimisticLockConflict) 가 떨어진다. 호출측에서 처리한다.
 */
export const tripPlanApi = {
  list: () => http.get('/trip-plans').then((r) => r.data),
  get: (id) => http.get(`/trip-plans/${id}`).then((r) => r.data),
  create: (payload) => http.post('/trip-plans', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/trip-plans/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/trip-plans/${id}`),
  listPlaces: (id) => http.get(`/trip-plans/${id}/places`).then((r) => r.data),
  getDrivingRoute: (id, visitDay) =>
    http.get(`/trip-plans/${id}/places/driving-route`, { params: { visitDay } }).then((r) => r.data),
  addPlace: (id, payload) => http.post(`/trip-plans/${id}/places`, payload).then((r) => r.data),
  schedulePlace: (id, placeId, payload) =>
    http.put(`/trip-plans/${id}/places/${placeId}/schedule`, payload).then((r) => r.data),
  reorderPlaces: (id, payload) =>
    http.put(`/trip-plans/${id}/places/reorder`, payload).then((r) => r.data),
  optimizePlaces: (id, payload) =>
    http.post(`/trip-plans/${id}/places/optimize`, payload).then((r) => r.data),
  removePlace: (id, placeId) => http.delete(`/trip-plans/${id}/places/${placeId}`),

  // F06 공동편집 — 멤버(협업자) 관리
  listMembers: (id) => http.get(`/trip-plans/${id}/members`).then((r) => r.data),
  inviteMember: (id, payload) => http.post(`/trip-plans/${id}/members`, payload).then((r) => r.data),
  updateMemberRole: (id, userId, role) =>
    http.patch(`/trip-plans/${id}/members/${userId}`, { role }),
  removeMember: (id, userId) => http.delete(`/trip-plans/${id}/members/${userId}`),

  // F06 P4 — 받은 초대(수락 대기). 초대 시 PENDING → 수락=ACCEPTED 전환, 거절=행 삭제
  //   초대 한 건: { planId, planTitle, inviterNickname, role, invitedAt }
  listInvites: () => http.get('/me/invites').then((r) => r.data),
  acceptInvite: (planId) => http.post(`/me/invites/${planId}/accept`),
  rejectInvite: (planId) => http.delete(`/me/invites/${planId}`),
}

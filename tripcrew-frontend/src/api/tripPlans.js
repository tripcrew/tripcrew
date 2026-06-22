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
}

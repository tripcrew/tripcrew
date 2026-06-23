import { http } from './http'

/**
 * 후기/평점(F08) API. 백엔드 엔드포인트와 1:1 대응.
 *   GET    /api/reviews?targetType&targetId&page&size&sort   대상별 후기 목록(공개, 페이징+요약)
 *   POST   /api/reviews                                       후기 작성(인증) → 201
 *   PUT    /api/reviews/{id}                                  후기 수정(본인만)
 *   DELETE /api/reviews/{id}                                  후기 삭제(본인만) → 204
 *
 * 대상은 폴리모픽(targetType: ATTRACTION | TRIP_PLAN, targetId: 대상 PK).
 * 목록 응답: { content, page, size, totalElements, totalPages, summary: { average, count, distribution } }
 *   sort: LATEST(기본) | RATING_HIGH | RATING_LOW
 */
export const reviewApi = {
  listByTarget: (targetType, targetId, { page = 0, size = 10, sort = 'LATEST' } = {}) =>
    http
      .get('/reviews', { params: { targetType, targetId, page, size, sort } })
      .then((r) => r.data),
  create: (payload) => http.post('/reviews', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/reviews/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/reviews/${id}`).then((r) => r.data),
}

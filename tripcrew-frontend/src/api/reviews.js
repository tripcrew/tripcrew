import { http } from './http'

/**
 * 후기/평점(F08) API. 백엔드 엔드포인트와 1:1 대응.
 *   GET  /api/reviews?targetType=ATTRACTION&targetId={id}   대상별 후기 목록(공개)
 *   POST /api/reviews                                        후기 작성(인증 필요) → 201
 *
 * 대상은 폴리모픽(targetType: ATTRACTION | TRIP_PLAN, targetId: 대상 PK).
 */
export const reviewApi = {
  listByTarget: (targetType, targetId) =>
    http.get('/reviews', { params: { targetType, targetId } }).then((r) => r.data),
  create: (payload) => http.post('/reviews', payload).then((r) => r.data),
}

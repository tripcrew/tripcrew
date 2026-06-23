import { http } from './http'

/**
 * 신고(F09 Phase 2) API. 백엔드 엔드포인트와 1:1 대응.
 *   POST /api/reports   신고 생성(인증 필요) → 201
 *
 * 대상은 폴리모픽(targetType: REVIEW | USER, targetId: 대상 PK).
 * 같은 대상을 중복 신고하면 서버가 409 로 막는다.
 */
export const reportApi = {
  create: (payload) => http.post('/reports', payload),
}

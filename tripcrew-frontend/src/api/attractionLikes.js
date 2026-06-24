import { http } from './http'

/**
 * 관광지 좋아요(찜) API. 응답은 항상 { liked, likeCount }.
 *   GET    /api/attractions/{no}/likes  상태 조회(공개, 비로그인 liked=false)
 *   POST   /api/attractions/{no}/likes  좋아요(인증)
 *   DELETE /api/attractions/{no}/likes  좋아요 취소(인증)
 *   GET    /api/me/likes                내 찜 목록(인증) — 카드+평점+총 찜 수, 최근 찜 순
 */
export const attractionLikeApi = {
  status: (no) => http.get(`/attractions/${no}/likes`).then((r) => r.data),
  like: (no) => http.post(`/attractions/${no}/likes`).then((r) => r.data),
  unlike: (no) => http.delete(`/attractions/${no}/likes`).then((r) => r.data),
  mine: () => http.get('/me/likes').then((r) => r.data),
  // 카드용 배치: 여러 관광지의 총 찜 수 + 내 찜 여부 → [{ no, likeCount, liked }]
  counts: (nos) =>
    http.get('/attractions/likes/counts', { params: { nos: nos.join(',') } }).then((r) => r.data),
}

import { http } from './http'

/**
 * 관광지 좋아요(찜) API. 응답은 항상 { liked, likeCount }.
 *   GET    /api/attractions/{no}/likes  상태 조회(공개, 비로그인 liked=false)
 *   POST   /api/attractions/{no}/likes  좋아요(인증)
 *   DELETE /api/attractions/{no}/likes  좋아요 취소(인증)
 */
export const attractionLikeApi = {
  status: (no) => http.get(`/attractions/${no}/likes`).then((r) => r.data),
  like: (no) => http.post(`/attractions/${no}/likes`).then((r) => r.data),
  unlike: (no) => http.delete(`/attractions/${no}/likes`).then((r) => r.data),
}

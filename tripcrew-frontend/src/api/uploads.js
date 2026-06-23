import { http } from './http'

/**
 * 이미지 업로드(F08 후기 첨부) API.
 *   POST /api/uploads/images  (multipart, 인증) → { urls: [...] }
 *
 * 후기 작성/수정 전에 파일을 먼저 올려 공개 URL 목록을 받고,
 * 그 URL 들을 후기 작성/수정 요청의 imageUrls 로 보낸다.
 */
export const uploadApi = {
  /**
   * @param {File[]} files 업로드할 이미지 파일들(최대 5장)
   * @returns {Promise<string[]>} 저장된 공개 상대 URL 목록(/uploads/reviews/...)
   */
  images: (files) => {
    const form = new FormData()
    files.forEach((f) => form.append('files', f))
    // FormData 를 보낼 땐 Content-Type 을 비워 axios/브라우저가 boundary 를 채우게 한다
    // (http 인스턴스 기본값 application/json 을 덮어쓴다).
    return http
      .post('/uploads/images', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((r) => r.data.urls || [])
  },
}

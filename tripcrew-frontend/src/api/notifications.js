import { http } from './http'

/**
 * 회원 알림 API(모두 인증). 헤더 벨 뱃지·드롭다운에서 사용.
 *   GET   /api/me/notifications               최근 알림 목록(최신순)
 *   GET   /api/me/notifications/unread-count  미읽음 개수 → { count }
 *   PATCH /api/me/notifications/{id}/read     한 건 읽음
 *   PATCH /api/me/notifications/read-all      전부 읽음
 *   DELETE /api/me/notifications              전체 삭제
 *
 * 알림 한 건: { id, type, refId, message, read, createdAt }
 */
export const notificationApi = {
  list: () => http.get('/me/notifications').then((r) => r.data),
  unreadCount: () => http.get('/me/notifications/unread-count').then((r) => r.data.count),
  markRead: (id) => http.patch(`/me/notifications/${id}/read`).then((r) => r.data),
  markAllRead: () => http.patch('/me/notifications/read-all').then((r) => r.data),
  remove: (id) => http.delete(`/me/notifications/${id}`).then((r) => r.data),
  removeAll: () => http.delete('/me/notifications').then((r) => r.data),
}

/**
 * 알림 type → 클릭 시 이동할 라우트(없으면 null = 이동 없이 읽음만).
 * refId 의 의미는 type 마다 다르다(신고 id / 여행계획 id 등).
 */
export function notificationRoute(notification) {
  switch (notification.type) {
    case 'REVIEW_NUDGE':
      // 여행 종료 후 후기 격려 → 해당 계획 상세(후기 작성 가능 화면)
      return notification.refId ? `/plans/${notification.refId}` : null
    case 'INVITE':
      // 공동편집 초대 → 내 계획 화면의 '받은 초대' 섹션(수락 전엔 계획 자체엔 접근 불가)
      return '/plans'
    case 'INQUIRY_ANSWERED':
      // 1:1 문의 답변 등록 → 내 문의 화면(/support)에서 답변 확인
      return '/support'
    case 'SANCTION_REVIEW_REQUIRED':
      // (관리자 수신) 신고 누적 최고 임계 도달 → 회원 관리에서 대상 확인·수동 영구정지
      return '/admin/users'
    case 'SANCTION_APPLIED':
      // 내 계정에 단계 제재가 적용됨 → 전용 도착 화면 없이 읽음 처리만(문구로 안내)
      return null
    case 'REPORT_RESOLVED':
    default:
      // 신고 처리완료는 전용 도착 화면이 없어 읽음 처리만 한다.
      return null
  }
}

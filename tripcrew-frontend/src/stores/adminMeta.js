import { defineStore } from 'pinia'
import { ref } from 'vue'

import { adminApi } from '@/api/admin'

/**
 * 관리자 화면 공통 메타 상태. 사이드바 배지(미처리 OPEN 건수)를 어느 관리자 페이지에서든
 * 항상 보여주기 위해 별도 store 로 둔다. 신고/문의를 처리한 뒤 refresh 를 호출하면 즉시 갱신된다.
 *   - openReportCount  : 미처리(OPEN) 신고 수
 *   - openInquiryCount : 미답변(OPEN) 1:1 문의 수
 */
export const useAdminMetaStore = defineStore('adminMeta', () => {
  const openReportCount = ref(0)
  const openInquiryCount = ref(0)

  async function refreshOpenReportCount() {
    try {
      const list = await adminApi.listReports('OPEN')
      openReportCount.value = list.length
    } catch {
      // 권한 없음(403) 등은 무시 — 배지는 표시상의 편의일 뿐
    }
  }

  async function refreshOpenInquiryCount() {
    try {
      const list = await adminApi.listInquiries('OPEN')
      openInquiryCount.value = list.length
    } catch {
      // 권한 없음(403) 등은 무시 — 배지는 표시상의 편의일 뿐
    }
  }

  return { openReportCount, openInquiryCount, refreshOpenReportCount, refreshOpenInquiryCount }
})

import { defineStore } from 'pinia'
import { ref } from 'vue'

import { adminApi } from '@/api/admin'

/**
 * 관리자 화면 공통 메타 상태. 사이드바의 "신고 관리" 배지(미처리 OPEN 건수)를
 * 어느 관리자 페이지에서든 항상 보여주기 위해 별도 store 로 둔다.
 * 신고를 처리/기각한 뒤 refresh() 를 호출하면 배지가 즉시 갱신된다.
 */
export const useAdminMetaStore = defineStore('adminMeta', () => {
  const openReportCount = ref(0)

  async function refreshOpenReportCount() {
    try {
      const list = await adminApi.listReports('OPEN')
      openReportCount.value = list.length
    } catch {
      // 권한 없음(403) 등은 무시 — 배지는 표시상의 편의일 뿐
    }
  }

  return { openReportCount, refreshOpenReportCount }
})

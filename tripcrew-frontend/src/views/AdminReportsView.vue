<template>
  <AdminLayout active="reports">
    <nav class="admin-breadcrumb">
      관리자 › <strong>신고 관리</strong>
    </nav>

    <header class="admin-page-head">
      <div>
        <h1 class="t-h1">신고 관리</h1>
        <p class="head-sub t-caption">미처리 신고 {{ reports.length }}건 · 처리완료/기각하면 목록에서 사라집니다</p>
      </div>
      <div class="head-actions">
        <BaseButton variant="secondary" :disabled="loading" @click="load">새로고침</BaseButton>
      </div>
    </header>

    <!-- 403: 권한 거부 -->
    <section v-if="forbidden" class="state-panel state-panel--error">
      <strong>접근 권한이 없습니다 (403)</strong>
      <p>이 화면은 관리자 전용입니다. 관리자 계정으로 다시 로그인해 주세요.</p>
    </section>

    <section v-else-if="error" class="state-panel state-panel--error">
      <strong>목록을 불러오지 못했습니다</strong>
      <p>{{ error }}</p>
      <BaseButton variant="secondary" @click="load">다시 시도</BaseButton>
    </section>

    <section v-else class="table-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>신고일</th>
            <th>대상</th>
            <th>신고자</th>
            <th>신고 사유 / 신고자 메모</th>
            <th>신고된 내용 (원문)</th>
            <th>피신고 유저</th>
            <th>조치</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="table-empty">불러오는 중…</td>
          </tr>
          <tr v-else-if="reports.length === 0">
            <td colspan="7" class="table-empty">미처리 신고가 없습니다. 👍</td>
          </tr>
          <tr v-for="rp in reports" :key="rp.id">
            <td class="t-mono">{{ formatDate(rp.createdAt) }}</td>
            <td><span :class="['type-chip', `type--${rp.targetType.toLowerCase()}`]">{{ targetLabel(rp.targetType) }}</span></td>
            <td class="email-cell">{{ rp.reporterEmail }}</td>
            <td class="reason-cell">
              <span class="reason">{{ reasonLabel(rp.reason) }}</span>
              <p v-if="rp.detail" class="reporter-note">“{{ rp.detail }}”</p>
              <p v-else class="reporter-note muted">상세 메모 없음</p>
            </td>
            <td class="content-cell">
              <span v-if="rp.reviewContent">{{ rp.reviewContent }}</span>
              <span v-else class="muted">— (사용자 직접 신고 · 원문 없음)</span>
            </td>
            <td class="email-cell">
              <span v-if="rp.reportedUserEmail">{{ rp.reportedUserEmail }}</span>
              <span v-else class="muted">—</span>
            </td>
            <td class="action-cell">
              <button
                class="action-btn action-btn--promote"
                :disabled="busyId === rp.id"
                @click="resolve(rp)"
              >{{ busyId === rp.id ? '처리 중…' : '처리완료' }}</button>
              <button
                class="action-btn"
                :disabled="busyId === rp.id"
                @click="dismiss(rp)"
              >기각</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <p v-if="notice" :class="['toast', notice.type === 'error' ? 'toast--error' : 'toast--ok']">
      {{ notice.text }}
    </p>

  </AdminLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { adminApi } from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { useAdminMetaStore } from '@/stores/adminMeta'

const adminMeta = useAdminMetaStore()

const reports = ref([])
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)
const busyId = ref(null)
const notice = ref(null)

const REASON_LABELS = {
  SPAM: '스팸/도배',
  ABUSE: '욕설/비방',
  ADVERTISING: '광고/홍보',
  INAPPROPRIATE: '부적절한 내용',
  OTHER: '기타',
}
function reasonLabel(code) {
  return REASON_LABELS[code] || code
}
function targetLabel(code) {
  return code === 'REVIEW' ? '후기' : '사용자'
}
function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10).replaceAll('-', '.')
}
function flash(type, text) {
  notice.value = { type, text }
}

async function load() {
  loading.value = true
  error.value = ''
  forbidden.value = false
  try {
    reports.value = await adminApi.listReports('OPEN')
  } catch (e) {
    if (e.response?.status === 403) forbidden.value = true
    else error.value = e.response?.data?.message || e.message || '알 수 없는 오류'
  } finally {
    loading.value = false
  }
}

// 처리/기각 후 목록에서 제거 + 사이드바 배지 갱신
function removeRow(id) {
  reports.value = reports.value.filter((r) => r.id !== id)
  adminMeta.refreshOpenReportCount()
}

async function resolve(report) {
  busyId.value = report.id
  notice.value = null
  try {
    await adminApi.resolveReport(report.id)
    removeRow(report.id)
    const who = report.reportedUserEmail || '대상 유저'
    flash('ok', `신고 #${report.id} 처리완료. (${who} 누적 +1 · 3회 이상이면 자동 제재)`)
  } catch (e) {
    flash('error', e.response?.data?.message || `처리 실패 (${e.response?.status || e.message})`)
  } finally {
    busyId.value = null
  }
}

async function dismiss(report) {
  busyId.value = report.id
  notice.value = null
  try {
    await adminApi.dismissReport(report.id)
    removeRow(report.id)
    flash('ok', `신고 #${report.id}를 기각했습니다.`)
  } catch (e) {
    flash('error', e.response?.data?.message || `처리 실패 (${e.response?.status || e.message})`)
  } finally {
    busyId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.admin-breadcrumb {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 16px;
}

.admin-breadcrumb strong { color: var(--ink); }

.admin-page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.head-sub { margin-top: 6px; color: var(--ink-soft); }
.head-actions { display: flex; gap: 8px; }

.table-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
}

.admin-table { width: 100%; }

.admin-table thead {
  background: var(--bg-soft);
  border-bottom: 1px solid var(--line);
}

.admin-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-soft);
}

.admin-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--line);
  font-size: 13px;
  color: var(--ink-2);
  vertical-align: middle;
}

.admin-table tbody tr { transition: background 0.15s; }
.admin-table tbody tr:hover { background: var(--bg-soft); }

.muted { color: var(--muted); }

.email-cell { font-family: var(--font-mono); font-size: 12px; }

/* 신고된 후기 원문 — 검토용으로 전문을 보여주되 너무 넓어지지 않게 */
.content-cell {
  max-width: 320px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
}

/* 신고 사유 + 신고자가 적은 메모 */
.reason-cell { max-width: 240px; }
.reporter-note {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--ink-soft);
  white-space: pre-wrap;
  word-break: break-word;
}
.reporter-note.muted { color: var(--muted); font-style: italic; }

.type-chip {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
}

.type--review { background: var(--teal-soft); color: var(--teal-3); }
.type--user { background: #FFE5E8; color: #B12C3A; }

.reason { font-weight: 600; }

.action-cell { white-space: nowrap; }

.action-btn {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-3);
  margin-right: 4px;
}

.action-btn:hover { background: var(--bg-2); }
.action-btn[disabled] { opacity: 0.5; cursor: not-allowed; }

.action-btn--promote { background: var(--teal); color: white; }
.action-btn--promote:hover { background: var(--teal-3); }

.table-empty { text-align: center; padding: 40px 16px; color: var(--muted); }

.state-panel {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}

.state-panel--error {
  border-color: #FBEAE2;
  background: linear-gradient(135deg, #FFF5F5 0%, white 100%);
}

.state-panel strong { font-size: 16px; color: var(--ink); }
.state-panel p { font-size: 13px; color: var(--ink-soft); }

.toast {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.toast--ok { background: #E1F5EA; color: #1A7A4A; }
.toast--error { background: #FFE5E8; color: #B12C3A; }

.api-note {
  margin-top: 20px;
  font-size: 11px;
  color: var(--muted);
  padding: 10px 14px;
  background: var(--bg-2);
  border-radius: 6px;
}
</style>

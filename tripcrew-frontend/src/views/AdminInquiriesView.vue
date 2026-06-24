<template>
  <AdminLayout active="inquiries">
    <nav class="admin-breadcrumb">
      관리자 › <strong>1:1 문의</strong>
    </nav>

    <header class="admin-page-head">
      <div>
        <h1 class="t-h1">1:1 문의</h1>
        <p class="head-sub t-caption">{{ headSub }}</p>
      </div>
      <div class="head-actions">
        <div class="filter-tabs" role="tablist">
          <button
            v-for="t in TABS"
            :key="t.value"
            :class="['filter-tab', { active: statusFilter === t.value }]"
            @click="setFilter(t.value)"
          >{{ t.label }}</button>
        </div>
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

    <template v-else>
      <p v-if="loading" class="list-empty">불러오는 중…</p>
      <p v-else-if="inquiries.length === 0" class="list-empty">{{ emptyText }}</p>

      <ul v-else class="iq-list">
        <li v-for="iq in pagedInquiries" :key="iq.id" class="iq-card">
          <div class="iq-card__top">
            <div class="iq-card__headinfo">
              <span :class="['status-chip', iq.status === 'ANSWERED' ? 'status-chip--answered' : 'status-chip--open']">
                {{ iq.status === 'ANSWERED' ? '답변완료' : '미답변' }}
              </span>
              <h2 class="iq-card__title">{{ iq.title }}</h2>
            </div>
            <div class="iq-card__by">
              <span class="iq-card__email">{{ iq.userEmail }}</span>
              <span class="iq-card__date">{{ formatDate(iq.createdAt) }}</span>
            </div>
          </div>

          <p class="iq-card__content">{{ iq.content }}</p>

          <!-- 답변완료 + 비편집: 기존 답변 표시 + 수정 진입 -->
          <div v-if="iq.status === 'ANSWERED' && !isEditing(iq.id)" class="iq-answer">
            <div class="iq-answer__head">
              <span class="iq-answer__label">관리자 답변 <em v-if="iq.answeredAt">· {{ formatDate(iq.answeredAt) }}</em></span>
              <button class="link-btn" @click="startEdit(iq)">수정</button>
            </div>
            <p class="iq-answer__text">{{ iq.answer }}</p>
          </div>

          <!-- 미답변 또는 수정 중: 답변 폼 -->
          <div v-else class="iq-form">
            <textarea
              v-model.trim="drafts[iq.id]"
              class="iq-textarea"
              maxlength="5000"
              rows="4"
              placeholder="답변을 입력하세요"
              :disabled="busyId === iq.id"
            ></textarea>
            <div class="iq-form__actions">
              <button
                v-if="isEditing(iq.id)"
                class="link-btn"
                :disabled="busyId === iq.id"
                @click="cancelEdit(iq.id)"
              >취소</button>
              <button
                class="action-btn action-btn--primary"
                :disabled="busyId === iq.id || !(drafts[iq.id] && drafts[iq.id].length > 0)"
                @click="answer(iq)"
              >{{ busyId === iq.id ? '저장 중…' : (iq.status === 'ANSWERED' ? '답변 수정' : '답변 등록') }}</button>
            </div>
          </div>
        </li>
      </ul>

      <BasePagination v-if="inquiries.length > 0" v-model="page" :total="inquiries.length" :page-size="PAGE_SIZE" />
    </template>

    <p v-if="notice" :class="['toast', notice.type === 'error' ? 'toast--error' : 'toast--ok']">
      {{ notice.text }}
    </p>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'

import { adminApi } from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { useAdminMetaStore } from '@/stores/adminMeta'

const adminMeta = useAdminMetaStore()

const TABS = [
  { value: 'OPEN', label: '미답변' },
  { value: 'ANSWERED', label: '답변완료' },
  { value: '', label: '전체' },
]

const inquiries = ref([])
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)
const statusFilter = ref('OPEN')
const busyId = ref(null)
const notice = ref(null)
const drafts = ref({})
const editingIds = ref(new Set())
const page = ref(1)
const PAGE_SIZE = 10  // 문의 카드는 신고 표보다 커서 한 페이지에 10개

const pagedInquiries = computed(() => inquiries.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
watch(() => inquiries.value.length, (len) => {
  const maxPage = Math.max(1, Math.ceil(len / PAGE_SIZE))
  if (page.value > maxPage) page.value = maxPage
})

const headSub = computed(() => {
  if (statusFilter.value === 'OPEN') return `미답변 문의 ${inquiries.value.length}건 · 답변하면 답변완료로 이동합니다`
  if (statusFilter.value === 'ANSWERED') return `답변완료 문의 ${inquiries.value.length}건`
  return `전체 문의 ${inquiries.value.length}건`
})
const emptyText = computed(() => {
  if (statusFilter.value === 'OPEN') return '미답변 문의가 없습니다. 👍'
  if (statusFilter.value === 'ANSWERED') return '답변완료된 문의가 없습니다.'
  return '문의가 없습니다.'
})

function isEditing(id) {
  return editingIds.value.has(id)
}
function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10).replaceAll('-', '.')
}
function flash(type, text) {
  notice.value = { type, text }
}

function setFilter(value) {
  if (statusFilter.value === value) return
  statusFilter.value = value
  page.value = 1
  load()
}

function startEdit(iq) {
  drafts.value = { ...drafts.value, [iq.id]: iq.answer || '' }
  editingIds.value = new Set(editingIds.value).add(iq.id)
}
function cancelEdit(id) {
  const next = new Set(editingIds.value)
  next.delete(id)
  editingIds.value = next
}

async function load() {
  loading.value = true
  error.value = ''
  forbidden.value = false
  try {
    inquiries.value = await adminApi.listInquiries(statusFilter.value || undefined)
  } catch (e) {
    if (e.response && e.response.status === 403) forbidden.value = true
    else error.value = (e.response && e.response.data && e.response.data.message) || e.message || '알 수 없는 오류'
  } finally {
    loading.value = false
  }
}

async function answer(iq) {
  const text = drafts.value[iq.id]
  if (!text || text.length === 0) return
  busyId.value = iq.id
  notice.value = null
  const wasOpen = iq.status === 'OPEN'
  try {
    await adminApi.answerInquiry(iq.id, text)
    // 미답변 필터에서 답변하면 목록에서 제거, 그 외(답변완료/전체)는 그 자리에서 갱신
    if (statusFilter.value === 'OPEN') {
      inquiries.value = inquiries.value.filter((x) => x.id !== iq.id)
    } else {
      iq.status = 'ANSWERED'
      iq.answer = text
      iq.answeredAt = new Date().toISOString()
    }
    cancelEdit(iq.id)
    if (wasOpen) adminMeta.refreshOpenInquiryCount()
    flash('ok', wasOpen ? `문의 #${iq.id}에 답변을 등록했습니다. (작성자에게 알림 전송)` : `문의 #${iq.id} 답변을 수정했습니다.`)
  } catch (e) {
    flash('error', (e.response && e.response.data && e.response.data.message) || `처리 실패 (${(e.response && e.response.status) || e.message})`)
  } finally {
    busyId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.admin-breadcrumb { font-size: 13px; color: var(--ink-soft); margin-bottom: 16px; }
.admin-breadcrumb strong { color: var(--ink); }

.admin-page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.head-sub { margin-top: 6px; color: var(--ink-soft); }
.head-actions { display: flex; align-items: center; gap: 10px; }

/* 상태 필터 탭 */
.filter-tabs {
  display: inline-flex;
  background: var(--bg-2);
  border-radius: 8px;
  padding: 3px;
  gap: 2px;
}
.filter-tab {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-3);
  transition: all 0.15s;
}
.filter-tab:hover { color: var(--ink); }
.filter-tab.active { background: var(--surface); color: var(--teal-ink); box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.list-empty { text-align: center; padding: 48px 16px; color: var(--muted); }

.iq-list { display: grid; gap: 14px; }
.iq-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
}
.iq-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.iq-card__headinfo { display: flex; align-items: center; gap: 10px; min-width: 0; }
.iq-card__title { font-size: 16px; font-weight: 700; color: var(--ink); word-break: break-word; }
.iq-card__by { display: flex; flex-direction: column; align-items: flex-end; gap: 3px; flex-shrink: 0; }
.iq-card__email { font-family: var(--font-mono); font-size: 12px; color: var(--ink-3); }
.iq-card__date { font-family: var(--font-mono); font-size: 12px; color: var(--muted); }

.iq-card__content {
  font-size: 14px;
  line-height: 1.65;
  color: var(--ink-2);
  white-space: pre-wrap;
  word-break: break-word;
  padding: 14px 16px;
  background: var(--bg-soft);
  border-radius: var(--r-md);
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}
.status-chip--open { background: var(--coral-soft); color: var(--coral-ink); }
.status-chip--answered { background: var(--teal-soft); color: var(--teal-ink); }

/* 기존 답변 표시 */
.iq-answer {
  margin-top: 14px;
  padding: 14px 16px;
  background: var(--teal-soft);
  border-radius: var(--r-md);
}
.iq-answer__head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.iq-answer__label { font-size: 12px; font-weight: 700; color: var(--teal-ink); }
.iq-answer__label em { font-style: normal; font-weight: 600; color: var(--ink-soft); }
.iq-answer__text { font-size: 14px; line-height: 1.65; color: var(--ink-2); white-space: pre-wrap; word-break: break-word; }

/* 답변 폼 */
.iq-form { margin-top: 14px; }
.iq-textarea {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink);
  resize: vertical;
  transition: border-color 0.15s;
}
.iq-textarea:focus { outline: none; border-color: var(--teal); }
.iq-form__actions { display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-top: 10px; }

.link-btn { font-size: 13px; font-weight: 600; color: var(--ink-3); }
.link-btn:hover:not(:disabled) { color: var(--ink); text-decoration: underline; }
.link-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.action-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-3);
}
.action-btn:hover:not(:disabled) { background: var(--bg-2); }
.action-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.action-btn--primary { background: var(--teal); color: white; }
.action-btn--primary:hover:not(:disabled) { background: var(--teal-3); }

.state-panel {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}
.state-panel--error { border-color: var(--coral-soft); background: linear-gradient(135deg, var(--coral-tint) 0%, var(--surface) 100%); }
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

@media (max-width: 640px) {
  .iq-card__top { flex-direction: column; gap: 8px; }
  .iq-card__by { align-items: flex-start; }
}
</style>

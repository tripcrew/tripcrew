<template>
  <AdminLayout active="notices">
    <nav class="admin-breadcrumb">
      관리자 › <strong>공지사항</strong>
    </nav>

    <header class="admin-page-head">
      <div>
        <h1 class="t-h1">공지사항 관리</h1>
        <p class="head-sub t-caption">총 {{ notices.length }}건 · 고정 공지가 목록 상단에 표시됩니다</p>
      </div>
      <div class="head-actions">
        <BaseButton variant="secondary" :disabled="loading" @click="load">새로고침</BaseButton>
        <BaseButton variant="primary" @click="openCreate">+ 새 공지</BaseButton>
      </div>
    </header>

    <!-- 403 -->
    <section v-if="forbidden" class="state-panel state-panel--error">
      <strong>접근 권한이 없습니다 (403)</strong>
      <p>이 화면은 관리자 전용입니다. 관리자 계정으로 다시 로그인해 주세요.</p>
    </section>

    <template v-else>
      <!-- 작성/수정 폼 -->
      <section v-if="formOpen" class="form-card">
        <h2 class="form-title">{{ editingId ? '공지 수정' : '새 공지 작성' }}</h2>
        <div class="form-row">
          <label class="form-label">제목</label>
          <input v-model="form.title" class="form-input" maxlength="200" placeholder="공지 제목" />
        </div>
        <div class="form-row">
          <label class="form-label">내용</label>
          <textarea v-model="form.content" class="form-textarea" rows="6" placeholder="공지 내용"></textarea>
        </div>
        <div class="form-row form-row--inline">
          <label class="check-label">
            <input v-model="form.pinned" type="checkbox" />
            상단 고정
          </label>
        </div>
        <div class="form-actions">
          <BaseButton variant="secondary" :disabled="saving" @click="closeForm">취소</BaseButton>
          <BaseButton variant="primary" :disabled="saving || !canSave" @click="save">
            {{ saving ? '저장 중…' : (editingId ? '수정 저장' : '등록') }}
          </BaseButton>
        </div>
      </section>

      <section v-if="error" class="state-panel state-panel--error">
        <strong>목록을 불러오지 못했습니다</strong>
        <p>{{ error }}</p>
        <BaseButton variant="secondary" @click="load">다시 시도</BaseButton>
      </section>

      <section v-else class="table-card">
        <table class="admin-table">
          <thead>
            <tr>
              <th>고정</th>
              <th>제목</th>
              <th>작성자</th>
              <th>작성일</th>
              <th>조회수</th>
              <th>조치</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="6" class="table-empty">불러오는 중…</td>
            </tr>
            <tr v-else-if="notices.length === 0">
              <td colspan="6" class="table-empty">등록된 공지가 없습니다.</td>
            </tr>
            <tr v-for="n in notices" :key="n.id">
              <td>{{ n.pinned ? '📌' : '—' }}</td>
              <td class="title-cell">{{ n.title }}</td>
              <td class="email-cell">{{ n.authorNickname || '—' }}</td>
              <td class="t-mono">{{ formatDate(n.createdAt) }}</td>
              <td class="t-mono">{{ n.viewCount }}</td>
              <td class="action-cell">
                <button class="action-btn" :disabled="busyId === n.id" @click="openEdit(n)">수정</button>
                <button class="action-btn action-btn--danger" :disabled="busyId === n.id" @click="remove(n)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>

    <p v-if="notice" :class="['toast', notice.type === 'error' ? 'toast--error' : 'toast--ok']">
      {{ notice.text }}
    </p>

  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { noticeApi, adminNoticeApi } from '@/api/notices'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const notices = ref([])
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)
const busyId = ref(null)
const notice = ref(null)

const formOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = reactive({ title: '', content: '', pinned: false })

const canSave = computed(() => form.title.trim().length > 0 && form.content.trim().length > 0)

function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10).replaceAll('-', '.')
}
function flash(type, text) {
  notice.value = { type, text }
}

function openCreate() {
  editingId.value = null
  form.title = ''
  form.content = ''
  form.pinned = false
  formOpen.value = true
  notice.value = null
}
function openEdit(n) {
  editingId.value = n.id
  form.title = n.title
  form.content = n.content
  form.pinned = n.pinned
  formOpen.value = true
  notice.value = null
}
function closeForm() {
  formOpen.value = false
  editingId.value = null
}

async function load() {
  loading.value = true
  error.value = ''
  forbidden.value = false
  try {
    notices.value = await noticeApi.list()
  } catch (e) {
    if (e.response?.status === 403) forbidden.value = true
    else error.value = e.response?.data?.message || e.message || '알 수 없는 오류'
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!canSave.value) return
  saving.value = true
  notice.value = null
  const payload = { title: form.title.trim(), content: form.content.trim(), pinned: form.pinned }
  try {
    if (editingId.value) {
      await adminNoticeApi.update(editingId.value, payload)
      flash('ok', '공지를 수정했습니다.')
    } else {
      await adminNoticeApi.create(payload)
      flash('ok', '공지를 등록했습니다.')
    }
    closeForm()
    await load()
  } catch (e) {
    flash('error', e.response?.data?.message || `저장 실패 (${e.response?.status || e.message})`)
  } finally {
    saving.value = false
  }
}

async function remove(n) {
  if (!window.confirm(`"${n.title}" 공지를 삭제할까요?`)) return
  busyId.value = n.id
  notice.value = null
  try {
    await adminNoticeApi.remove(n.id)
    notices.value = notices.value.filter((x) => x.id !== n.id)
    flash('ok', '공지를 삭제했습니다.')
  } catch (e) {
    flash('error', e.response?.data?.message || `삭제 실패 (${e.response?.status || e.message})`)
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
  margin-bottom: 24px;
}
.head-sub { margin-top: 6px; color: var(--ink-soft); }
.head-actions { display: flex; gap: 8px; }

/* 작성/수정 폼 */
.form-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 24px;
  margin-bottom: 20px;
}
.form-title { font-size: 16px; font-weight: 700; color: var(--ink); margin-bottom: 16px; }
.form-row { margin-bottom: 16px; }
.form-row--inline { display: flex; align-items: center; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: var(--ink-soft); margin-bottom: 6px; }
.form-input, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 14px;
  color: var(--ink);
}
.form-textarea { resize: vertical; line-height: 1.6; }
.form-input:focus, .form-textarea:focus { outline: none; border-color: var(--teal); }
.check-label { display: inline-flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink-2); cursor: pointer; }
.form-actions { display: flex; justify-content: flex-end; gap: 8px; }

.table-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
}
.admin-table { width: 100%; }
.admin-table thead { background: var(--bg-soft); border-bottom: 1px solid var(--line); }
.admin-table th { padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 700; color: var(--ink-soft); }
.admin-table td { padding: 14px 16px; border-bottom: 1px solid var(--line); font-size: 13px; color: var(--ink-2); vertical-align: middle; }
.admin-table tbody tr { transition: background 0.15s; }
.admin-table tbody tr:hover { background: var(--bg-soft); }

.title-cell { font-weight: 600; color: var(--ink); max-width: 360px; }
.email-cell { font-family: var(--font-mono); font-size: 12px; }

.action-cell { white-space: nowrap; }
.action-btn { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; color: var(--ink-3); margin-right: 4px; }
.action-btn:hover { background: var(--bg-2); }
.action-btn[disabled] { opacity: 0.5; cursor: not-allowed; }
.action-btn--danger { color: #B12C3A; }
.action-btn--danger:hover { background: #FFE5E8; }

.table-empty { text-align: center; padding: 40px 16px; color: var(--muted); }

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
.state-panel--error { border-color: #FBEAE2; background: linear-gradient(135deg, #FFF5F5 0%, white 100%); }
.state-panel strong { font-size: 16px; color: var(--ink); }
.state-panel p { font-size: 13px; color: var(--ink-soft); }

.toast { margin-top: 16px; padding: 12px 16px; border-radius: 8px; font-size: 13px; font-weight: 600; }
.toast--ok { background: #E1F5EA; color: #1A7A4A; }
.toast--error { background: #FFE5E8; color: #B12C3A; }

.api-note { margin-top: 20px; font-size: 11px; color: var(--muted); padding: 10px 14px; background: var(--bg-2); border-radius: 6px; }
</style>

<template>
  <AdminLayout active="banned">
    <nav class="admin-breadcrumb">
      관리자 › <strong>정지된 계정</strong>
    </nav>

    <header class="admin-page-head">
      <div>
        <h1 class="t-h1">정지된 계정</h1>
        <p class="head-sub t-caption">제재(BANNED)된 계정만 표시 · 신고 누적 3회 이상이면 자동 제재됩니다</p>
      </div>
      <div class="head-actions">
        <BaseButton variant="secondary" :disabled="loading" @click="load">새로고침</BaseButton>
      </div>
    </header>

    <section v-if="forbidden" class="state-panel state-panel--error">
      <strong>접근 권한이 없습니다 (403)</strong>
      <p>이 화면은 ADMIN 전용입니다. 서버 인가 규칙(<span class="t-mono">/api/admin/**</span>)이 요청을 거부했습니다.</p>
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
            <th>ID</th>
            <th>이메일</th>
            <th>닉네임</th>
            <th>role</th>
            <th>신고 누적</th>
            <th>가입일</th>
            <th>조치</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="table-empty">불러오는 중…</td>
          </tr>
          <tr v-else-if="bannedUsers.length === 0">
            <td colspan="7" class="table-empty">정지된 계정이 없습니다. 👍</td>
          </tr>
          <tr v-for="u in bannedUsers" :key="u.id">
            <td class="t-mono">{{ u.id }}</td>
            <td class="email-cell">{{ u.email }}</td>
            <td><strong>{{ u.nickname }}</strong></td>
            <td><span :class="['role-chip', `role--${u.role.toLowerCase()}`]">{{ u.role }}</span></td>
            <td class="t-mono">
              <span :class="{ 'count-hot': u.reportCount >= 3 }">{{ u.reportCount }}회</span>
            </td>
            <td class="t-mono">{{ formatDate(u.createdAt) }}</td>
            <td>
              <button
                class="action-btn action-btn--promote"
                :disabled="busyId === u.id"
                @click="unban(u)"
              >{{ busyId === u.id ? '처리 중…' : '제재 해제' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <p v-if="notice" :class="['toast', notice.type === 'error' ? 'toast--error' : 'toast--ok']">
      {{ notice.text }}
    </p>

    <p class="api-note t-mono">
      GET /api/admin/users 중 status=BANNED 필터 · PATCH /admin/users/{id}/unban (ROLE_ADMIN)
    </p>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import { adminApi } from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const users = ref([])
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)
const busyId = ref(null)
const notice = ref(null)

const bannedUsers = computed(() => users.value.filter((u) => u.status === 'BANNED'))

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
    users.value = await adminApi.listUsers()
  } catch (e) {
    if (e.response?.status === 403) forbidden.value = true
    else error.value = e.response?.data?.message || e.message || '알 수 없는 오류'
  } finally {
    loading.value = false
  }
}

async function unban(user) {
  busyId.value = user.id
  notice.value = null
  try {
    await adminApi.unban(user.id)
    user.status = 'ACTIVE' // 목록(bannedUsers)에서 자동 제외
    flash('ok', `${user.nickname}님의 제재를 해제했습니다.`)
  } catch (e) {
    const status = e.response?.status
    if (status === 404) flash('error', '대상 사용자를 찾을 수 없습니다 (404).')
    else flash('error', e.response?.data?.message || `처리 실패 (${status || e.message})`)
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

.email-cell { font-family: var(--font-mono); font-size: 12px; }

.role-chip {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
}

.role--user { background: var(--bg-2); color: var(--ink-3); }
.role--admin { background: var(--coral); color: white; }
.role--super_admin { background: var(--ink); color: white; }

.count-hot { color: var(--danger); font-weight: 800; }

.action-btn {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-3);
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

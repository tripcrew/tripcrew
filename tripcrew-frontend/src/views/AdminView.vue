<template>
  <div class="admin-app">
    <!-- Top bar -->
    <header class="admin-top">
      <div class="admin-brand">
        <span class="logo">TripCrew<span class="dot">.</span></span>
        <span class="admin-badge">Admin <span class="t-mono">v.2026.05</span></span>
      </div>

      <div class="system-status">
        <span class="status-label">SYSTEM</span>
        <span class="status-item"><span class="sd sd--ok"></span>API · 정상</span>
        <span class="status-item"><span class="sd sd--ok"></span>Redis · 정상</span>
        <span class="status-item"><span class="sd sd--warn"></span>TourAPI · HALF-OPEN</span>
        <span class="status-item"><span class="sd sd--ok"></span>Gemini · 정상</span>
      </div>

      <div class="admin-user">
        <div class="avatar" style="background: var(--teal-3);">{{ userInitial }}</div>
      </div>
    </header>

    <div class="admin-layout">
      <!-- Sidebar -->
      <aside class="admin-sidebar">
        <nav class="admin-nav">
          <h4 class="nav-title">관리</h4>
          <a class="nav-item active">
            <span class="nav-icon">👥</span>
            회원 관리
            <span class="nav-count">12,482</span>
          </a>
          <a class="nav-item">
            <span class="nav-icon">📝</span>
            후기 모더레이션
            <span class="nav-count nav-count--alert">4</span>
          </a>
          <a class="nav-item">
            <span class="nav-icon">📢</span>
            공지사항
          </a>
          <a class="nav-item">
            <span class="nav-icon">📍</span>
            관광지 관리
          </a>

          <h4 class="nav-title">모니터링</h4>
          <a class="nav-item">
            <span class="nav-icon">📊</span>
            통계 대시보드
          </a>
          <a class="nav-item">
            <span class="nav-icon">⚙️</span>
            시스템 상태
          </a>
        </nav>
      </aside>

      <!-- Main -->
      <main class="admin-main">
        <nav class="admin-breadcrumb">
          관리자 › <strong>회원 관리</strong>
        </nav>

        <header class="admin-page-head">
          <h1 class="t-h1">회원 관리</h1>
          <div class="head-actions">
            <BaseButton variant="secondary" :disabled="loading" @click="load">새로고침</BaseButton>
          </div>
        </header>

        <!-- Stat cards (실제 목록에서 집계) -->
        <div class="stat-grid">
          <article class="stat-card">
            <span class="stat-label">전체 회원</span>
            <strong class="stat-value">{{ users.length }}</strong>
            <span class="stat-delta">GET /api/admin/users</span>
          </article>
          <article class="stat-card">
            <span class="stat-label">관리자 (ADMIN)</span>
            <strong class="stat-value">{{ adminCount }}</strong>
          </article>
          <article class="stat-card">
            <span class="stat-label">일반 회원 (USER)</span>
            <strong class="stat-value">{{ userCount }}</strong>
          </article>
          <article class="stat-card">
            <span class="stat-label">표시 중</span>
            <strong class="stat-value">{{ filteredUsers.length }}</strong>
          </article>
        </div>

        <!-- 403: 일반 USER 가 접근한 경우 (서버 인가 거부) -->
        <section v-if="forbidden" class="state-panel state-panel--error">
          <strong>접근 권한이 없습니다 (403)</strong>
          <p>이 화면은 ADMIN 전용입니다. 서버 인가 규칙(<span class="t-mono">/api/admin/**</span>)이
            요청을 거부했습니다.</p>
        </section>

        <!-- 그 외 로드 실패 -->
        <section v-else-if="error" class="state-panel state-panel--error">
          <strong>목록을 불러오지 못했습니다</strong>
          <p>{{ error }}</p>
          <BaseButton variant="secondary" @click="load">다시 시도</BaseButton>
        </section>

        <!-- Table -->
        <section v-else class="table-card">
          <div class="table-head">
            <div class="table-search">
              <span>🔍</span>
              <input v-model="query" type="text" placeholder="이메일 또는 닉네임 검색" />
            </div>
            <div class="table-filters">
              <select v-model="roleFilter">
                <option value="">전체</option>
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <span class="t-caption table-count">
              {{ users.length }}명 중 {{ filteredUsers.length }}명 표시
            </span>
          </div>

          <table class="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>이메일</th>
                <th>닉네임</th>
                <th>role</th>
                <th>가입일</th>
                <th>권한 변경</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="table-empty">불러오는 중…</td>
              </tr>
              <tr v-else-if="filteredUsers.length === 0">
                <td colspan="6" class="table-empty">표시할 회원이 없습니다.</td>
              </tr>
              <tr v-for="u in filteredUsers" :key="u.id">
                <td class="t-mono">{{ u.id }}</td>
                <td>{{ u.email }}</td>
                <td><strong>{{ u.nickname }}</strong></td>
                <td>
                  <span :class="['role-chip', `role--${u.role.toLowerCase()}`]">{{ u.role }}</span>
                </td>
                <td class="t-mono">{{ formatDate(u.createdAt) }}</td>
                <td>
                  <button
                    v-if="u.id === currentUserId"
                    class="action-btn"
                    disabled
                    title="본인 권한은 변경할 수 없습니다"
                  >본인</button>
                  <button
                    v-else
                    class="action-btn"
                    :class="u.role === 'ADMIN' ? 'action-btn--danger' : 'action-btn--promote'"
                    :disabled="savingId === u.id"
                    @click="toggleRole(u)"
                  >
                    {{ savingId === u.id ? '변경 중…' : (u.role === 'ADMIN' ? '→ USER 강등' : '→ ADMIN 승격') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <p v-if="notice" :class="['toast', notice.type === 'error' ? 'toast--error' : 'toast--ok']">
          {{ notice.text }}
        </p>

        <p class="api-note t-mono">
          GET /api/admin/users · PATCH /api/admin/users/{id}/role · ROLE_ADMIN 전용
        </p>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import { adminApi } from '@/api/admin'
import BaseButton from '@/components/common/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const users = ref([])
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)
const savingId = ref(null)
const notice = ref(null)

const query = ref('')
const roleFilter = ref('')

const currentUserId = computed(() => auth.user && auth.user.id)
const userInitial = computed(() => (auth.user && auth.user.nickname ? auth.user.nickname : 'A').charAt(0))

const adminCount = computed(() => users.value.filter((u) => u.role === 'ADMIN').length)
const userCount = computed(() => users.value.filter((u) => u.role === 'USER').length)

const filteredUsers = computed(() => {
  const q = query.value.trim().toLowerCase()
  return users.value.filter((u) => {
    if (roleFilter.value && u.role !== roleFilter.value) return false
    if (!q) return true
    return (
      u.email?.toLowerCase().includes(q) || u.nickname?.toLowerCase().includes(q)
    )
  })
})

function formatDate(value) {
  if (!value) return '-'
  // 백엔드 LocalDateTime("2026-01-14T10:00:00") → "2026.01.14"
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
    if (e.response?.status === 403) {
      forbidden.value = true
    } else {
      error.value = e.response?.data?.message || e.message || '알 수 없는 오류'
    }
  } finally {
    loading.value = false
  }
}

async function toggleRole(user) {
  const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  savingId.value = user.id
  notice.value = null
  try {
    await adminApi.updateRole(user.id, nextRole)
    user.role = nextRole // 204 (본문 없음) → 로컬 반영
    flash('ok', `${user.nickname}님의 권한을 ${nextRole}로 변경했습니다.`)
  } catch (e) {
    const status = e.response?.status
    const msg = e.response?.data?.message
    if (status === 403) flash('error', '권한이 없어 변경할 수 없습니다 (403).')
    else if (status === 404) flash('error', '대상 사용자를 찾을 수 없습니다 (404).')
    else flash('error', msg || `변경 실패 (${status || e.message})`)
  } finally {
    savingId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.admin-app {
  min-height: 100vh;
  background: var(--bg-soft);
  font-size: 14px;
}

/* Top bar */
.admin-top {
  height: 60px;
  background: var(--ink);
  color: white;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 32px;
  border-bottom: 1px solid #2A323D;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  font-size: 18px;
  font-weight: 800;
  color: white;
  letter-spacing: -0.5px;
}

.logo .dot { color: var(--coral); }

.admin-badge {
  padding: 4px 10px;
  background: var(--coral);
  color: white;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.admin-badge .t-mono {
  font-size: 10px;
  opacity: 0.75;
}

.system-status {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
}

.status-label {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  color: rgba(255,255,255,0.4);
  letter-spacing: 1.2px;
}

.status-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: rgba(255,255,255,0.78);
}

.sd {
  width: 6px; height: 6px;
  border-radius: 50%;
}

.sd--ok { background: var(--success); animation: blink 2s infinite; }
.sd--warn { background: var(--warning); animation: blink 1.4s infinite; }

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.admin-user .avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 14px;
  border: 2px solid white;
}

/* Layout */
.admin-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: calc(100vh - 60px);
}

/* Sidebar */
.admin-sidebar {
  background: white;
  border-right: 1px solid var(--line);
  padding: 24px 16px;
}

.nav-title {
  font-size: 11px;
  font-weight: 700;
  color: var(--muted);
  letter-spacing: 1.2px;
  margin: 16px 12px 8px;
  text-transform: uppercase;
}

.nav-title:first-child { margin-top: 0; }

.admin-nav {
  display: flex;
  flex-direction: column;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-3);
  cursor: pointer;
  transition: all 0.15s;
}

.nav-item:hover { background: var(--bg-soft); color: var(--ink); }

.nav-item.active {
  background: var(--teal-soft);
  color: var(--teal-3);
}

.nav-icon { font-size: 16px; }

.nav-count {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--muted);
  background: var(--bg-2);
  padding: 2px 7px;
  border-radius: 999px;
}

.nav-count--alert {
  background: var(--coral);
  color: white;
  font-weight: 700;
}

/* Main */
.admin-main {
  padding: 32px 40px;
}

.admin-breadcrumb {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 16px;
}

.admin-breadcrumb strong { color: var(--ink); }

.admin-page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.head-actions {
  display: flex;
  gap: 8px;
}

/* Stats */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-card--alert {
  background: linear-gradient(135deg, #FFF5F5 0%, white 100%);
  border-color: #FBEAE2;
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-soft);
}

.stat-value {
  font-family: var(--font-mono);
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -1px;
  color: var(--ink);
}

.stat-delta {
  font-size: 12px;
  font-weight: 600;
}

.delta--up { color: var(--success); }
.delta--alert { color: var(--coral); }

/* Table */
.table-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
}

.table-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
  background: var(--bg-soft);
}

.table-search {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 0 14px;
  max-width: 380px;
}

.table-search input {
  flex: 1;
  padding: 9px 0;
  border: none;
  outline: none;
  background: none;
  font-size: 13px;
}

.table-filters {
  display: flex;
  gap: 6px;
}

.table-filters select {
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
}

.table-count {
  margin-left: auto;
  font-size: 12px;
  color: var(--ink-soft);
}

.admin-table {
  width: 100%;
}

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
  letter-spacing: 0.2px;
}

.admin-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--line);
  font-size: 13px;
  color: var(--ink-2);
}

.admin-table tbody tr {
  transition: background 0.15s;
}

.admin-table tbody tr:hover { background: var(--bg-soft); }
.admin-table tr.is-selected { background: var(--teal-tint); }

.admin-table input[type="checkbox"] { accent-color: var(--teal); }

.muted { color: var(--muted); }

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

.status-chip {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.status--active { background: #E1F5EA; color: #1A7A4A; }
.status--locked { background: #FFE5E8; color: #B12C3A; }
.status--dormant { background: var(--bg-2); color: var(--ink-soft); }

.action-btn {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-3);
}

.action-btn:hover { background: var(--bg-2); }

.action-btn[disabled] {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn--danger {
  background: var(--danger);
  color: white;
}

.action-btn--danger:hover { background: #B12C3A; }

.action-btn--promote {
  background: var(--teal);
  color: white;
}

.action-btn--promote:hover { background: var(--teal-3); }

.table-empty {
  text-align: center;
  padding: 40px 16px;
  color: var(--muted);
}

/* 상태 패널 (403 / 에러) */
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

/* 작업 결과 토스트 */
.toast {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.toast--ok { background: #E1F5EA; color: #1A7A4A; }
.toast--error { background: #FFE5E8; color: #B12C3A; }

.table-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: var(--bg-soft);
}

.bulk-info { font-size: 13px; color: var(--ink-3); }
.link-teal { color: var(--teal); font-weight: 600; }

.pagination {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination button {
  min-width: 32px;
  height: 32px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-3);
}

.pagination button:hover { background: white; }

.pagination .active {
  background: var(--teal);
  color: white;
}

.pagination span { color: var(--muted); padding: 0 4px; }

.api-note {
  margin-top: 20px;
  font-size: 11px;
  color: var(--muted);
  padding: 10px 14px;
  background: var(--bg-2);
  border-radius: 6px;
}
</style>

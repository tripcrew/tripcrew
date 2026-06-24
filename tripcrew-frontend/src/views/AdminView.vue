<template>
  <AdminLayout active="users">
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
            <span class="stat-delta">현재 등록된 계정 기준</span>
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
          <p>이 화면은 관리자 전용입니다. 관리자 계정으로 다시 로그인해 주세요.</p>
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
                <option value="SUPER_ADMIN">SUPER_ADMIN</option>
              </select>
            </div>
            <span class="t-caption table-count">
              {{ users.length }}명 중 {{ filteredUsers.length }}명 표시
            </span>
          </div>

          <table class="admin-table">
            <thead>
              <tr>
                <th>#</th>
                <th>ID</th>
                <th>이메일</th>
                <th>닉네임</th>
                <th>role</th>
                <th>상태</th>
                <th>신고누적</th>
                <th>가입일</th>
                <th>권한 변경</th>
                <th>제재</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="10" class="table-empty">불러오는 중…</td>
              </tr>
              <tr v-else-if="filteredUsers.length === 0">
                <td colspan="10" class="table-empty">표시할 회원이 없습니다.</td>
              </tr>
              <tr v-for="(u, idx) in pagedUsers" :key="u.id">
                <td class="t-mono row-no">{{ (page - 1) * PAGE_SIZE + idx + 1 }}</td>
                <td class="t-mono id-cell">{{ u.id }}</td>
                <td>{{ u.email }}</td>
                <td><strong>{{ u.nickname }}</strong></td>
                <td>
                  <span :class="['role-chip', `role--${u.role.toLowerCase()}`]">{{ u.role }}</span>
                </td>
                <td>
                  <span :class="['status-chip', u.status === 'BANNED' ? 'status--locked' : 'status--active']">
                    {{ u.status === 'BANNED' ? '영구정지' : '정상' }}
                  </span>
                  <div v-if="u.activeRestrictions && u.activeRestrictions.length" class="restriction-chips">
                    <span
                      v-for="r in u.activeRestrictions"
                      :key="r.type"
                      class="restriction-chip"
                      :title="restrictionTitle(r)"
                    >{{ restrictionLabel(r) }}</span>
                  </div>
                </td>
                <td>
                  <span :class="['report-count', sanctionFlag(u) ? 'report-count--flag' : '']">
                    {{ u.reportCount || 0 }}
                  </span>
                  <span v-if="sanctionFlag(u)" class="flag-badge" title="신고 누적 15회 이상 — 영구정지 검토 필요">검토</span>
                </td>
                <td class="t-mono">{{ formatDate(u.createdAt) }}</td>
                <td>
                  <button
                    v-if="!canManageRoles"
                    class="action-btn"
                    disabled
                    title="권한 변경은 최고관리자(SUPER_ADMIN)만 가능합니다"
                  >읽기 전용</button>
                  <button
                    v-else-if="u.id === currentUserId"
                    class="action-btn"
                    disabled
                    title="본인 권한은 변경할 수 없습니다"
                  >본인</button>
                  <button
                    v-else-if="u.role === 'SUPER_ADMIN'"
                    class="action-btn"
                    disabled
                    title="최고관리자 권한은 이 화면에서 변경할 수 없습니다"
                  >최고관리자</button>
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
                <td class="sanction-cell">
                  <!-- 영구정지/해제: 누적 자동 단계제재와 별개로 관리자가 즉시 영구정지(BANNED) 가능 -->
                  <button
                    v-if="u.status === 'BANNED'"
                    class="action-btn action-btn--promote"
                    :disabled="banningId === u.id"
                    @click="unban(u)"
                  >{{ banningId === u.id ? '해제 중…' : '정지 해제' }}</button>
                  <button
                    v-else-if="!canSanction(u)"
                    class="action-btn"
                    disabled
                    :title="sanctionDisabledReason(u)"
                  >—</button>
                  <template v-else-if="confirmBanId === u.id">
                    <button
                      class="action-btn action-btn--danger"
                      :disabled="banningId === u.id"
                      @click="ban(u)"
                    >{{ banningId === u.id ? '정지 중…' : '영구정지 확정' }}</button>
                    <button class="action-btn" @click="confirmBanId = null">취소</button>
                  </template>
                  <button
                    v-else
                    class="action-btn action-btn--danger"
                    @click="confirmBanId = u.id"
                  >영구정지</button>

                  <!-- 단계 제재 해제: 활성 제재가 있으면(밴 여부와 무관) 즉시 전부 해제 -->
                  <button
                    v-if="u.activeRestrictions && u.activeRestrictions.length"
                    class="action-btn action-btn--lift"
                    :disabled="clearingId === u.id"
                    title="후기/계획 금지·임시정지 등 단계 제재를 즉시 모두 해제합니다"
                    @click="clearRestrictions(u)"
                  >{{ clearingId === u.id ? '해제 중…' : '제재 해제' }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </section>

        <BasePagination v-if="!forbidden && !error" v-model="page" :total="filteredUsers.length" :page-size="PAGE_SIZE" />

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
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const users = ref([])
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)
const savingId = ref(null)
const banningId = ref(null)
const confirmBanId = ref(null)
const clearingId = ref(null)
const notice = ref(null)

const query = ref('')
const roleFilter = ref('')
const page = ref(1)
const PAGE_SIZE = 15

const currentUserId = computed(() => auth.user && auth.user.id)
// role 변경은 SUPER_ADMIN 만(서버 인가와 짝). ADMIN 은 목록만 보고 토글은 '읽기 전용'.
const canManageRoles = computed(() => !!(auth.user && auth.user.role === 'SUPER_ADMIN'))
const isSuperAdmin = computed(() => !!(auth.user && auth.user.role === 'SUPER_ADMIN'))

const RESTRICTION_LABELS = {
  REVIEW_WRITE: '후기금지',
  PLAN_CREATE: '계획금지',
  ACCOUNT_SUSPEND: '계정정지',
}
function restrictionLabel(r) {
  const base = RESTRICTION_LABELS[r.type] || r.type
  const d = daysLeft(r.until)
  return d == null ? base : `${base} D-${d}`
}
function restrictionTitle(r) {
  if (!r.until) return `${RESTRICTION_LABELS[r.type] || r.type} · 영구`
  return `${RESTRICTION_LABELS[r.type] || r.type} · 해제 ${String(r.until).slice(0, 16).replace('T', ' ')}`
}
function daysLeft(until) {
  if (!until) return null
  const ms = new Date(until).getTime() - Date.now()
  return ms <= 0 ? 0 : Math.ceil(ms / 86400000)
}

// 신고 누적 15회 이상이면 영구정지 검토 플래그(서버 SanctionService 임계와 동일).
function sanctionFlag(u) {
  return u.status !== 'BANNED' && (u.reportCount || 0) >= 15
}

// 수동 영구정지 가능 여부: 본인·SUPER_ADMIN 불가, ADMIN 대상은 SUPER_ADMIN 만(서버 가드와 짝).
function canSanction(u) {
  if (u.id === currentUserId.value) return false
  if (u.role === 'SUPER_ADMIN') return false
  if (u.role === 'ADMIN') return isSuperAdmin.value
  return true
}
function sanctionDisabledReason(u) {
  if (u.id === currentUserId.value) return '본인 계정은 제재할 수 없습니다'
  if (u.role === 'SUPER_ADMIN') return '최고관리자는 제재할 수 없습니다'
  if (u.role === 'ADMIN') return 'ADMIN 계정은 최고관리자만 제재할 수 있습니다'
  return ''
}

const adminCount = computed(() => users.value.filter((u) => u.role === 'ADMIN').length)
const userCount = computed(() => users.value.filter((u) => u.role === 'USER').length)

// 정렬 우선순위: SUPER_ADMIN → ADMIN → USER, 동일 role 은 id 오름차순
const ROLE_RANK = { SUPER_ADMIN: 0, ADMIN: 1, USER: 2 }
const filteredUsers = computed(() => {
  const q = query.value.trim().toLowerCase()
  return users.value
    .filter((u) => {
      if (roleFilter.value && u.role !== roleFilter.value) return false
      if (!q) return true
      return (
        u.email?.toLowerCase().includes(q) || u.nickname?.toLowerCase().includes(q)
      )
    })
    .slice()
    .sort((a, b) => {
      const ra = ROLE_RANK[a.role] ?? 99
      const rb = ROLE_RANK[b.role] ?? 99
      return ra !== rb ? ra - rb : a.id - b.id
    })
})

const pagedUsers = computed(() => filteredUsers.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
// 검색·필터로 결과가 줄어 현재 페이지가 범위를 벗어나면 1페이지로
watch(filteredUsers, (list) => {
  const maxPage = Math.max(1, Math.ceil(list.length / PAGE_SIZE))
  if (page.value > maxPage) page.value = 1
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

async function ban(user) {
  banningId.value = user.id
  notice.value = null
  try {
    await adminApi.ban(user.id)
    user.status = 'BANNED' // 204 → 로컬 반영
    confirmBanId.value = null
    flash('ok', `${user.nickname}님을 영구정지했습니다.`)
  } catch (e) {
    const status = e.response?.status
    const msg = e.response?.data?.message
    flash('error', msg || `정지 실패 (${status || e.message})`)
  } finally {
    banningId.value = null
  }
}

async function unban(user) {
  banningId.value = user.id
  notice.value = null
  try {
    await adminApi.unban(user.id)
    user.status = 'ACTIVE'
    flash('ok', `${user.nickname}님의 정지를 해제했습니다. (단계 제재는 만료 시각까지 유지)`)
  } catch (e) {
    const status = e.response?.status
    const msg = e.response?.data?.message
    flash('error', msg || `해제 실패 (${status || e.message})`)
  } finally {
    banningId.value = null
  }
}

async function clearRestrictions(user) {
  clearingId.value = user.id
  notice.value = null
  try {
    await adminApi.clearRestrictions(user.id)
    user.activeRestrictions = [] // 204 → 로컬 반영(칩 제거)
    flash('ok', `${user.nickname}님의 단계 제재를 모두 해제했습니다.`)
  } catch (e) {
    const status = e.response?.status
    const msg = e.response?.data?.message
    flash('error', msg || `해제 실패 (${status || e.message})`)
  } finally {
    clearingId.value = null
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
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-card--alert {
  background: linear-gradient(135deg, var(--coral-tint) 0%, var(--surface) 100%);
  border-color: var(--coral-soft);
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
  background: var(--surface);
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
  background: var(--surface);
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
  background: var(--surface);
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

/* 표시용 순번(정렬을 따라 1..N) — 실제 PK(ID)와 별개, 이게 시각 기준 */
.row-no { font-weight: 700; color: var(--ink-2); }
/* 실제 ID(PK)는 안 바뀌는 대리키라 들쭉날쭉 정상 — 보조로 약하게 */
.id-cell { color: var(--muted); }

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
.role--super_admin { background: var(--surface-inverse); color: white; }

.status-chip {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.status--active { background: var(--teal-soft); color: var(--teal-ink); }
.status--locked { background: var(--coral-soft); color: var(--coral-ink); }
.status--dormant { background: var(--bg-2); color: var(--ink-soft); }

/* 활성 단계 제재 칩 (상태 셀 하단) */
.restriction-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.restriction-chip {
  display: inline-block;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  background: color-mix(in srgb, var(--amber) 16%, var(--surface));
  color: color-mix(in srgb, var(--amber) 75%, var(--ink));
}

/* 신고 누적 카운트 + 검토 플래그 */
.report-count { font-family: var(--font-mono); font-weight: 700; }
.report-count--flag { color: var(--coral); }

.flag-badge {
  margin-left: 6px;
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  background: var(--coral-soft);
  color: var(--coral-ink);
}

.sanction-cell {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

/* 단계 제재 해제(완화 액션) — 위험(빨강)과 구분되는 차분한 톤 */
.action-btn--lift {
  background: var(--bg-2);
  color: var(--ink-2);
  border: 1px solid var(--line);
}
.action-btn--lift:hover:not([disabled]) { background: var(--bg-soft); }

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

.action-btn--danger:hover { background: color-mix(in srgb, var(--danger) 85%, black); }

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
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}

.state-panel--error {
  border-color: var(--coral-soft);
  background: linear-gradient(135deg, var(--coral-tint) 0%, var(--surface) 100%);
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

.toast--ok { background: var(--teal-soft); color: var(--teal-ink); }
.toast--error { background: var(--coral-soft); color: var(--coral-ink); }

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

.pagination button:hover { background: var(--surface); }

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

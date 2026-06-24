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
        <span class="status-item"><span class="sd" :class="healthDotClass"></span>{{ healthText }}</span>
      </div>

      <button class="logout-btn" title="로그아웃" @click="handleLogout">
        <svg class="logout-ico" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" y1="12" x2="9" y2="12" />
        </svg>
        로그아웃
      </button>

      <div class="admin-user">
        <div class="avatar" style="background: var(--teal-3);">{{ userInitial }}</div>
      </div>
    </header>

    <div class="admin-layout">
      <!-- Sidebar -->
      <aside class="admin-sidebar">
        <nav class="admin-nav">
          <h4 class="nav-title">관리</h4>
          <RouterLink class="nav-item" :class="{ active: active === 'users' }" to="/admin/users">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            회원 관리
          </RouterLink>
          <RouterLink class="nav-item" :class="{ active: active === 'reports' }" to="/admin/reports">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M5 22V4"/><path d="M5 5c5-4 8 4 14 0v10c-6 4-9-4-14 0"/></svg>
            신고 관리
            <span v-if="openReportCount > 0" class="nav-count nav-count--alert">{{ openReportCount }}</span>
          </RouterLink>
          <RouterLink class="nav-item" :class="{ active: active === 'banned' }" to="/admin/banned">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="m5.6 5.6 12.8 12.8"/></svg>
            정지된 계정
          </RouterLink>
          <RouterLink class="nav-item" :class="{ active: active === 'notices' }" to="/admin/notices">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m3 11 17-5v12L3 13v-2Z"/><path d="M11 15v4a2 2 0 0 1-4 0v-5"/><path d="M22 9v6"/></svg>
            공지사항
          </RouterLink>
          <span class="nav-item nav-item--soon" title="준비 중인 메뉴입니다" aria-disabled="true">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="2.5"/></svg>
            관광지 관리
            <span class="nav-soon">준비 중</span>
          </span>

          <h4 class="nav-title">모니터링</h4>
          <span class="nav-item nav-item--soon" title="준비 중인 메뉴입니다" aria-disabled="true">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2"/></svg>
            통계 대시보드
            <span class="nav-soon">준비 중</span>
          </span>
          <span class="nav-item nav-item--soon" title="준비 중인 메뉴입니다" aria-disabled="true">
            <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="4" width="18" height="6" rx="1"/><rect x="3" y="14" width="18" height="6" rx="1"/><path d="M7 7h.01M7 17h.01"/></svg>
            시스템 상태
            <span class="nav-soon">준비 중</span>
          </span>
        </nav>
      </aside>

      <!-- Main -->
      <main class="admin-main">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'

import { http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { useAdminMetaStore } from '@/stores/adminMeta'

defineProps({
  /** 활성 사이드바 항목('users' | 'reports' | 'banned') */
  active: { type: String, default: '' },
})

const router = useRouter()
const auth = useAuthStore()
const userInitial = computed(() => (auth.user && auth.user.nickname ? auth.user.nickname : 'A').charAt(0))

// 시스템 상태: 프론트가 실제로 확인 가능한 건 백엔드 생존뿐이라 /api/health 를 핑한다.
// (Redis/외부 API 별 상태는 전용 헬스 엔드포인트가 생기면 확장 — 그전까지 날조 표시 안 함)
const apiHealthy = ref(null) // null=확인 중, true=정상, false=연결 끊김
const healthText = computed(() =>
  apiHealthy.value === null ? 'API · 확인 중' : apiHealthy.value ? 'API · 정상' : 'API · 연결 끊김',
)
const healthDotClass = computed(() =>
  apiHealthy.value === null ? 'sd--warn' : apiHealthy.value ? 'sd--ok' : 'sd--down',
)
async function checkHealth() {
  try {
    const { data } = await http.get('/health')
    apiHealthy.value = data && data.status === 'UP'
  } catch {
    apiHealthy.value = false
  }
}

async function handleLogout() {
  if (!window.confirm('로그아웃하시겠어요?')) return
  await auth.logout()
  router.replace('/')
}

// 신고 관리 배지(미처리 OPEN 수)는 어느 관리자 페이지에서든 보이도록 store 에서 가져온다.
const adminMeta = useAdminMetaStore()
const { openReportCount } = storeToRefs(adminMeta)
onMounted(() => {
  adminMeta.refreshOpenReportCount()
  checkHealth()
})
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

/* 로그아웃 — 다크 톱바용 ghost 버튼. hover 시 '나가기'를 암시하는 coral 톤 */
.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: transparent;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.15s;
}

.logout-btn:hover {
  background: rgba(224, 106, 79, 0.16);
  border-color: var(--coral);
  color: white;
}

.logout-ico { display: block; }

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
.sd--down { background: var(--danger); }

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
  text-decoration: none;
}

.nav-item:hover { background: var(--bg-soft); color: var(--ink); }

.nav-item.active {
  background: var(--teal-soft);
  color: var(--teal-3);
}

/* 아직 라우트가 없는(준비 중) 메뉴 — 클릭 불가, 깨진 링크처럼 보이지 않게 명시 */
.nav-item--soon {
  color: var(--muted);
  cursor: default;
}

.nav-item--soon:hover {
  background: none;
  color: var(--muted);
}

.nav-soon {
  margin-left: auto;
  font-size: 10px;
  font-weight: 700;
  color: var(--muted);
  background: var(--bg-2);
  padding: 2px 7px;
  border-radius: 999px;
}

.nav-icon {
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

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
</style>

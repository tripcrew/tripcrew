<template>
  <div class="admin-app">
    <!-- Top bar -->
    <header class="admin-top">
      <div class="admin-brand">
        <RouterLink to="/home" class="logo" title="사이트 홈으로">TripCrew<span class="dot">.</span></RouterLink>
        <span class="admin-badge">Admin <span class="t-mono">v.2026.05</span></span>
      </div>

      <div class="system-status">
        <span class="status-label">SYSTEM</span>
        <span class="status-item"><span class="sd sd--ok"></span>API · 정상</span>
        <span class="status-item"><span class="sd sd--ok"></span>Redis · 정상</span>
        <span class="status-item"><span class="sd sd--warn"></span>TourAPI · HALF-OPEN</span>
        <span class="status-item"><span class="sd sd--ok"></span>Gemini · 정상</span>
      </div>

      <RouterLink to="/home" class="home-btn" title="사용자 사이트 홈으로 이동">
        <svg class="home-ico" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M3 11.5 12 4l9 7.5" />
          <path d="M5 10v9h5v-5h4v5h5v-9" />
        </svg>
        사이트 홈
      </RouterLink>

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
            <span class="nav-icon">👥</span>
            회원 관리
          </RouterLink>
          <RouterLink class="nav-item" :class="{ active: active === 'reports' }" to="/admin/reports">
            <span class="nav-icon">🚩</span>
            신고 관리
            <span v-if="openReportCount > 0" class="nav-count nav-count--alert">{{ openReportCount }}</span>
          </RouterLink>
          <RouterLink class="nav-item" :class="{ active: active === 'banned' }" to="/admin/banned">
            <span class="nav-icon">🚫</span>
            정지된 계정
          </RouterLink>
          <RouterLink class="nav-item" :class="{ active: active === 'notices' }" to="/admin/notices">
            <span class="nav-icon">📢</span>
            공지사항
          </RouterLink>
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
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'

import { useAuthStore } from '@/stores/auth'
import { useAdminMetaStore } from '@/stores/adminMeta'

defineProps({
  /** 활성 사이드바 항목('users' | 'reports' | 'banned') */
  active: { type: String, default: '' },
})

const auth = useAuthStore()
const userInitial = computed(() => (auth.user && auth.user.nickname ? auth.user.nickname : 'A').charAt(0))

// 신고 관리 배지(미처리 OPEN 수)는 어느 관리자 페이지에서든 보이도록 store 에서 가져온다.
const adminMeta = useAdminMetaStore()
const { openReportCount } = storeToRefs(adminMeta)
onMounted(adminMeta.refreshOpenReportCount)
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
  text-decoration: none;
  transition: opacity 0.15s;
}

.logo:hover { opacity: 0.8; }

.logo .dot { color: var(--coral); }

/* 사이트(사용자) 홈으로 — 다크 톱바에 어울리는 ghost 버튼 */
.home-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.15s;
}

.home-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.36);
  color: white;
}

.home-ico { display: block; }

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
  text-decoration: none;
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
</style>

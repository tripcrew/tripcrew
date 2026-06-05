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
        <span class="status-item"><span class="sd sd--ok"></span>OpenAI · 정상</span>
      </div>

      <div class="admin-user">
        <div class="avatar" style="background: var(--teal-3);">A</div>
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
            <BaseButton variant="secondary">CSV 내보내기</BaseButton>
            <BaseButton variant="primary">+ 회원 추가</BaseButton>
          </div>
        </header>

        <!-- Stat cards -->
        <div class="stat-grid">
          <article class="stat-card">
            <span class="stat-label">전체 회원</span>
            <strong class="stat-value">12,482</strong>
            <span class="stat-delta delta--up">+1.2% 이번 주</span>
          </article>
          <article class="stat-card">
            <span class="stat-label">활성 (30일)</span>
            <strong class="stat-value">8,140</strong>
            <span class="stat-delta delta--up">+3.4%</span>
          </article>
          <article class="stat-card">
            <span class="stat-label">신규 (7일)</span>
            <strong class="stat-value">320</strong>
            <span class="stat-delta delta--up">+12%</span>
          </article>
          <article class="stat-card stat-card--alert">
            <span class="stat-label">잠금 / 신고</span>
            <strong class="stat-value">4</strong>
            <span class="stat-delta delta--alert">처리 대기</span>
          </article>
        </div>

        <!-- Table -->
        <section class="table-card">
          <div class="table-head">
            <div class="table-search">
              <span>🔍</span>
              <input type="text" placeholder="이메일 또는 닉네임 검색" />
            </div>
            <div class="table-filters">
              <select><option>전체</option><option>USER</option><option>ADMIN</option></select>
              <select><option>상태</option><option>활성</option><option>잠금</option><option>휴면</option></select>
            </div>
            <span class="t-caption table-count">12,482개 중 1–10</span>
          </div>

          <table class="admin-table">
            <thead>
              <tr>
                <th><input type="checkbox" /></th>
                <th>ID</th>
                <th>이메일</th>
                <th>닉네임</th>
                <th>role</th>
                <th>상태</th>
                <th>가입일</th>
                <th>최근 접속</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id" :class="{ 'is-selected': u.selected }">
                <td><input type="checkbox" :checked="u.selected" /></td>
                <td class="t-mono">{{ u.id }}</td>
                <td>{{ u.email }}</td>
                <td><strong>{{ u.nickname }}</strong></td>
                <td>
                  <span :class="['role-chip', `role--${u.role.toLowerCase()}`]">{{ u.role }}</span>
                </td>
                <td>
                  <span :class="['status-chip', `status--${u.statusKey}`]">{{ u.status }}</span>
                </td>
                <td class="t-mono">{{ u.signedUp }}</td>
                <td class="t-mono muted">{{ u.lastSeen }}</td>
                <td>
                  <button v-if="u.statusKey === 'locked'" class="action-btn action-btn--danger">제재</button>
                  <button v-else class="action-btn">⋯</button>
                </td>
              </tr>
            </tbody>
          </table>

          <footer class="table-foot">
            <div class="bulk-info">
              <strong>선택 1개</strong> · <a href="#" class="link-teal">일괄 작업</a>
            </div>
            <nav class="pagination">
              <button>‹</button>
              <button class="active">1</button>
              <button>2</button>
              <button>3</button>
              <span>…</span>
              <button>›</button>
            </nav>
          </footer>
        </section>

        <p class="api-note t-mono">
          GET /api/admin/users?role=&amp;status=&amp;q= · @PreAuthorize ADMIN
        </p>
      </main>
    </div>
  </div>
</template>

<script setup>
import BaseButton from '@/components/common/BaseButton.vue'

const users = [
  { id: 10482, email: 'minji@crew.kr', nickname: '민지', role: 'USER', status: '활성', statusKey: 'active', signedUp: '2026.01.14', lastSeen: '방금 전', selected: true },
  { id: 10481, email: 'spammer@xyz.com', nickname: '스팸유저', role: 'USER', status: '잠금', statusKey: 'locked', signedUp: '2026.05.18', lastSeen: '2시간 전' },
  { id: 10480, email: 'jiwon@crew.kr', nickname: '지원', role: 'ADMIN', status: '활성', statusKey: 'active', signedUp: '2025.11.02', lastSeen: '5분 전' },
  { id: 10479, email: 'hyunwoo@crew.kr', nickname: '현우', role: 'USER', status: '활성', statusKey: 'active', signedUp: '2026.03.18', lastSeen: '어제' },
  { id: 10478, email: 'test@example.com', nickname: '테스트', role: 'USER', status: '휴면', statusKey: 'dormant', signedUp: '2025.06.21', lastSeen: '90일 전' },
  { id: 10477, email: 'hyemi@crew.kr', nickname: '혜미', role: 'USER', status: '활성', statusKey: 'active', signedUp: '2026.02.04', lastSeen: '3시간 전' },
  { id: 10476, email: 'park@crew.kr', nickname: '박지원', role: 'USER', status: '활성', statusKey: 'active', signedUp: '2025.09.30', lastSeen: '1일 전' }
]
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

.action-btn--danger {
  background: var(--danger);
  color: white;
}

.action-btn--danger:hover { background: #B12C3A; }

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

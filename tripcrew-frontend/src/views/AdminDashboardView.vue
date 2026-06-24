<template>
  <AdminLayout active="dashboard">
    <nav class="admin-breadcrumb">
      관리자 › <strong>대시보드</strong>
    </nav>

    <header class="admin-page-head">
      <div>
        <h1 class="t-h1">대시보드</h1>
        <p class="page-sub">서비스 현황과 미처리 업무를 한눈에 확인하세요.</p>
      </div>
      <div class="head-actions">
        <BaseButton variant="secondary" :disabled="loading" @click="load">새로고침</BaseButton>
      </div>
    </header>

    <!-- 403: 일반 USER 가 접근한 경우 (서버 인가 거부) -->
    <section v-if="forbidden" class="state-panel state-panel--error">
      <strong>접근 권한이 없습니다 (403)</strong>
      <p>이 화면은 관리자 전용입니다. 관리자 계정으로 다시 로그인해 주세요.</p>
    </section>

    <section v-else-if="error" class="state-panel state-panel--error">
      <strong>대시보드를 불러오지 못했습니다</strong>
      <p>{{ error }}</p>
    </section>

    <template v-else>
      <!-- TODAY: 메인 집계 카드(클릭 시 해당 관리 페이지로) -->
      <h2 class="section-label">TODAY</h2>
      <div class="card-grid">
        <RouterLink class="stat-card stat-card--link" to="/admin/users">
          <div class="stat-top">
            <span class="stat-label">사용자</span>
            <span class="stat-ico stat-ico--teal" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </span>
          </div>
          <strong class="stat-value">{{ display(summary.userCount) }}</strong>
          <span class="stat-delta">전체 등록 계정 · 회원 관리 →</span>
        </RouterLink>

        <RouterLink class="stat-card stat-card--link" :class="{ 'stat-card--alert': hasOpenReports }" to="/admin/reports">
          <div class="stat-top">
            <span class="stat-label">사용자 요청 사항</span>
            <span class="stat-ico" :class="hasOpenReports ? 'stat-ico--coral' : 'stat-ico--mute'" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M5 22V4"/><path d="M5 5c5-4 8 4 14 0v10c-6 4-9-4-14 0"/></svg>
            </span>
          </div>
          <strong class="stat-value" :class="{ 'stat-value--alert': hasOpenReports }">{{ display(summary.openReportCount) }}</strong>
          <span class="stat-delta" :class="{ 'delta--alert': hasOpenReports }">미처리 신고 · 신고 관리 →</span>
        </RouterLink>

        <article class="stat-card stat-card--soon">
          <div class="stat-top">
            <span class="stat-label">챗봇 사용 현황</span>
            <span class="stat-ico stat-ico--mute" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </span>
          </div>
          <strong class="stat-value stat-value--soon">준비 중</strong>
          <span class="stat-delta">집계 출처 연동 예정</span>
        </article>

        <RouterLink class="stat-card stat-card--link" to="/admin/notices">
          <div class="stat-top">
            <span class="stat-label">공지사항 / Q&amp;A</span>
            <span class="stat-ico stat-ico--teal" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="m3 11 17-5v12L3 13v-2Z"/><path d="M11 15v4a2 2 0 0 1-4 0v-5"/><path d="M22 9v6"/></svg>
            </span>
          </div>
          <strong class="stat-value">{{ display(summary.noticeCount) }}</strong>
          <span class="stat-delta">공지 {{ display(summary.noticeCount) }}건 · Q&amp;A 준비 중 →</span>
        </RouterLink>
      </div>

      <!-- 하단: 바로가기(정지된 계정 등) + 시스템 상태 -->
      <div class="dash-cols">
        <section class="panel">
          <h2 class="panel-title">관리 바로가기</h2>
          <div class="quick-list">
            <RouterLink class="quick-row" to="/admin/banned">
              <span class="quick-ico" aria-hidden="true">
                <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="m5.6 5.6 12.8 12.8"/></svg>
              </span>
              <span class="quick-text">
                <strong>정지된 계정</strong>
                <em>제재된 계정 확인 · 해제</em>
              </span>
              <span class="quick-count" :class="{ 'quick-count--alert': hasBanned }">{{ display(summary.bannedUserCount) }}</span>
            </RouterLink>
            <RouterLink class="quick-row" to="/admin/reports">
              <span class="quick-ico" aria-hidden="true">
                <svg viewBox="0 0 24 24"><path d="M5 22V4"/><path d="M5 5c5-4 8 4 14 0v10c-6 4-9-4-14 0"/></svg>
              </span>
              <span class="quick-text">
                <strong>신고 처리</strong>
                <em>미처리 신고 검토 · 제재</em>
              </span>
              <span class="quick-count" :class="{ 'quick-count--alert': hasOpenReports }">{{ display(summary.openReportCount) }}</span>
            </RouterLink>
          </div>
        </section>

        <section id="system" class="panel">
          <h2 class="panel-title">시스템 상태</h2>
          <ul class="health-list">
            <li class="health-row">
              <span class="health-name">백엔드 API</span>
              <span class="health-state">
                <span class="hd" :class="healthDotClass"></span>{{ healthText }}
              </span>
            </li>
          </ul>
          <p class="health-note">
            Redis · 외부 API(관광/날씨/Gemini)별 상태는 전용 헬스 엔드포인트가 마련되면 확장합니다.
          </p>
        </section>
      </div>
    </template>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import { http } from '@/api/http'
import { adminApi } from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const summary = ref({
  userCount: null,
  bannedUserCount: null,
  openReportCount: null,
  noticeCount: null,
  chatbotUsageCount: null,
  qnaCount: null,
})
const loading = ref(false)
const error = ref('')
const forbidden = ref(false)

const hasOpenReports = computed(() => (summary.value.openReportCount || 0) > 0)
const hasBanned = computed(() => (summary.value.bannedUserCount || 0) > 0)

// 숫자가 아직 안 왔으면(로딩/null) 대시 표시
function display(value) {
  return value === null || value === undefined ? '—' : value
}

async function load() {
  loading.value = true
  error.value = ''
  forbidden.value = false
  try {
    summary.value = await adminApi.dashboard()
  } catch (e) {
    if (e.response && e.response.status === 403) {
      forbidden.value = true
    } else {
      error.value = (e.response && e.response.data && e.response.data.message) || e.message || '알 수 없는 오류'
    }
  } finally {
    loading.value = false
  }
}

// 시스템 상태: 프론트가 실제로 확인 가능한 건 백엔드 생존뿐이라 /api/health 를 핑한다.
const apiHealthy = ref(null) // null=확인 중, true=정상, false=연결 끊김
const healthText = computed(() =>
  apiHealthy.value === null ? '확인 중' : apiHealthy.value ? '정상' : '연결 끊김',
)
const healthDotClass = computed(() =>
  apiHealthy.value === null ? 'hd--warn' : apiHealthy.value ? 'hd--ok' : 'hd--down',
)
async function checkHealth() {
  try {
    const { data } = await http.get('/health')
    apiHealthy.value = !!(data && data.status === 'UP')
  } catch {
    apiHealthy.value = false
  }
}

onMounted(() => {
  load()
  checkHealth()
})
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

.page-sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--ink-soft);
}

.head-actions { display: flex; gap: 8px; }

.section-label {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.4px;
  color: var(--muted);
  margin-bottom: 12px;
}

/* 메인 카드 그리드 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-decoration: none;
  color: inherit;
}

.stat-card--link {
  transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;
}

.stat-card--link:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md, 0 6px 18px rgba(0, 0, 0, 0.06));
  border-color: var(--teal);
}

.stat-card--alert {
  background: linear-gradient(135deg, #FFF5F5 0%, white 100%);
  border-color: #FBEAE2;
}

.stat-card--alert:hover { border-color: var(--coral); }

.stat-card--soon { background: var(--bg-soft); }

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.stat-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-soft);
}

.stat-ico {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  display: grid;
  place-items: center;
}

.stat-ico svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.stat-ico--teal { background: var(--teal-soft); color: var(--teal-3); }
.stat-ico--coral { background: #FBEAE2; color: var(--coral); }
.stat-ico--mute { background: var(--bg-2); color: var(--muted); }

.stat-value {
  font-family: var(--font-mono);
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -1px;
  color: var(--ink);
}

.stat-value--alert { color: var(--coral); }
.stat-value--soon { font-size: 20px; color: var(--muted); letter-spacing: 0; }

.stat-delta {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-soft);
}

.delta--alert { color: var(--coral); }

/* 하단 2단 */
.dash-cols {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 14px;
}

/* 바로가기 */
.quick-list { display: flex; flex-direction: column; gap: 8px; }

.quick-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  transition: background 0.15s, border-color 0.15s;
}

.quick-row:hover { background: var(--bg-soft); border-color: var(--teal); }

.quick-ico {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: var(--bg-2);
  color: var(--ink-3);
  display: grid;
  place-items: center;
  flex: 0 0 36px;
}

.quick-ico svg {
  width: 18px; height: 18px;
  fill: none; stroke: currentColor;
  stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round;
}

.quick-text { display: flex; flex-direction: column; gap: 2px; }
.quick-text strong { font-size: 13px; color: var(--ink); }
.quick-text em { font-size: 12px; color: var(--ink-soft); font-style: normal; }

.quick-count {
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 18px;
  font-weight: 800;
  color: var(--ink-3);
}

.quick-count--alert { color: var(--coral); }

/* 시스템 상태 */
.health-list { display: flex; flex-direction: column; gap: 8px; }

.health-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
}

.health-name { font-size: 13px; font-weight: 600; color: var(--ink-2); }

.health-state {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-3);
}

.hd { width: 8px; height: 8px; border-radius: 50%; }
.hd--ok { background: var(--success); }
.hd--warn { background: var(--warning); }
.hd--down { background: var(--danger); }

.health-note {
  margin-top: 12px;
  font-size: 12px;
  color: var(--muted);
  line-height: 1.5;
}

@media (max-width: 980px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); }
  .dash-cols { grid-template-columns: 1fr; }
}
</style>

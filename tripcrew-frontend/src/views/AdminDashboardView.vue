<template>
  <AdminLayout active="dashboard">
   <div class="dash">
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
      <!-- 현황: 핵심 집계 카드(누적/스냅샷, 클릭 시 해당 관리 페이지로) -->
      <h2 class="section-label">현황</h2>
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

        <article class="stat-card">
          <div class="stat-top">
            <span class="stat-label">챗봇 사용 현황</span>
            <span class="stat-ico stat-ico--teal" aria-hidden="true">
              <svg viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            </span>
          </div>
          <strong class="stat-value">{{ display(summary.chatbotUsageCount) }}</strong>
          <span class="stat-delta">누적 챗봇 대화 요청</span>
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

      <!-- 통계: 실제 도메인 데이터(가입/후기/신고/회원 분포) 기반 차트 -->
      <h2 class="section-label">통계 <span class="section-note">실데이터</span></h2>
      <div class="chart-grid">
        <section class="panel">
          <div class="panel-head">
            <h3 class="panel-title">가입 추이</h3>
            <div class="period-picker">
              <select v-model.number="signupYear" class="period-select" aria-label="연도 선택" @change="loadSignups">
                <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
              </select>
              <select v-model.number="signupMonth" class="period-select" aria-label="월 선택" @change="loadSignups">
                <option v-for="m in 12" :key="m" :value="m">{{ m }}월</option>
              </select>
            </div>
          </div>
          <MiniBarChart :data="signupSeries" unit="명" :aria-label="`${signupYear}년 ${signupMonth}월 가입 추이`" />
        </section>
        <section class="panel">
          <div class="panel-head">
            <h3 class="panel-title">콘텐츠 활동 추이</h3>
            <span class="panel-sub">최근 14일</span>
          </div>
          <MiniLineChart :series="activitySeries" unit="건" aria-label="최근 14일 후기·신고 추이" />
        </section>
        <section class="panel">
          <h3 class="panel-title">회원 역할 분포</h3>
          <MiniDonutChart :data="roleSegments" aria-label="회원 역할 분포" />
        </section>
        <section class="panel">
          <h3 class="panel-title">회원 상태 분포</h3>
          <MiniDonutChart :data="statusSegments" aria-label="회원 상태 분포" />
        </section>
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
   </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import { http } from '@/api/http'
import { adminApi } from '@/api/admin'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import MiniBarChart from '@/components/admin/charts/MiniBarChart.vue'
import MiniLineChart from '@/components/admin/charts/MiniLineChart.vue'
import MiniDonutChart from '@/components/admin/charts/MiniDonutChart.vue'

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

// 차트 데이터(최근 14일 활동 추이 + 역할/상태 분포)
const stats = ref({ reviews: [], reports: [], chat: [], roleDistribution: [], statusDistribution: [] })

function toSeries(rows) {
  return (rows || []).map((r) => ({ label: r.day, value: r.count }))
}

// 가입 추이 — 연/월 선택형(별도 엔드포인트). 기본=이번 달.
const now = new Date()
const signupYear = ref(now.getFullYear())
const signupMonth = ref(now.getMonth() + 1)
const signupDays = ref([])
// 서비스 시작 연도(2026)부터 올해까지 선택 가능
const yearOptions = computed(() => {
  const years = []
  for (let y = now.getFullYear(); y >= 2026; y--) years.push(y)
  return years
})
const signupSeries = computed(() => toSeries(signupDays.value))

const activitySeries = computed(() => [
  { name: '후기', color: 'var(--teal)', data: toSeries(stats.value.reviews) },
  { name: '신고', color: 'var(--coral)', data: toSeries(stats.value.reports) },
  { name: '챗봇', color: 'var(--ink-3)', data: toSeries(stats.value.chat) },
])

// enum name → 한글 라벨 + 색. 미정의 라벨은 원문 + 회색으로 폴백.
const ROLE_META = {
  USER: { label: '일반 회원', color: 'var(--teal)' },
  ADMIN: { label: '관리자', color: 'var(--coral)' },
  SUPER_ADMIN: { label: '최고관리자', color: 'var(--ink)' },
}
const STATUS_META = {
  ACTIVE: { label: '정상', color: 'var(--success)' },
  BANNED: { label: '정지', color: 'var(--danger)' },
  WITHDRAWN: { label: '탈퇴', color: 'var(--muted)' },
}

function toSegments(rows, metaMap) {
  return (rows || []).map((r) => {
    const meta = metaMap[r.label] || { label: r.label, color: 'var(--ink-3)' }
    return { label: meta.label, value: r.count, color: meta.color }
  })
}

const roleSegments = computed(() => toSegments(stats.value.roleDistribution, ROLE_META))
const statusSegments = computed(() => toSegments(stats.value.statusDistribution, STATUS_META))

// 숫자가 아직 안 왔으면(로딩/null) 대시 표시
function display(value) {
  return value === null || value === undefined ? '—' : value
}

// 가입 추이만 별도 로드(연/월 변경 시 재호출). 차트만 갱신하고 전체 에러 상태는 건드리지 않음.
async function loadSignups() {
  try {
    signupDays.value = await adminApi.dashboardSignups(signupYear.value, signupMonth.value)
  } catch {
    signupDays.value = []
  }
}

async function load() {
  loading.value = true
  error.value = ''
  forbidden.value = false
  try {
    const [summaryData, statsData] = await Promise.all([adminApi.dashboard(), adminApi.dashboardStats()])
    summary.value = summaryData
    stats.value = statsData
    await loadSignups()
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
/* 다른 관리자 페이지(회원관리 등)와 동일하게 admin-main 폭을 그대로 꽉 채운다(유동).
   차트는 높이 고정(ResizeObserver 픽셀 렌더)이라 폭이 넓어져도 세로로 길어지지 않으므로
   상한 없이 디스플레이에 맞춰 늘어나고, 화면을 줄이면 아래 브레이크포인트로 자연스럽게 접힌다. */
.dash {
  width: 100%;
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
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-sub {
  margin-top: 6px;
  font-size: 14px;
  color: var(--ink-soft);
}

.head-actions { display: flex; gap: 8px; }

.section-label {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.4px;
  color: var(--muted);
  margin-bottom: 12px;
}

.section-note {
  font-family: var(--font-sans, inherit);
  font-weight: 600;
  letter-spacing: 0;
  color: var(--ink-soft);
  text-transform: none;
}

/* 차트 2x2 그리드 */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}

.chart-grid .panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink-2);
  margin-bottom: 14px;
}

/* 차트 패널 헤더(제목 + 기간 선택/부제) */
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  min-height: 32px;
}
.panel-head .panel-title { margin-bottom: 0; }

.panel-sub {
  font-size: 12px;
  font-weight: 600;
  color: var(--muted);
}

.period-picker { display: flex; gap: 6px; }

.period-select {
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-2);
  cursor: pointer;
}
.period-select:focus { outline: none; border-color: var(--teal); }

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
  /* 그리드 칸이 내용(숫자/아이콘)보다 작게도 줄어들 수 있게 — 가로 오버플로 방지 */
  min-width: 0;
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

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.stat-label {
  font-size: 13px;
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

.stat-delta {
  font-size: 13px;
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
  /* 차트 SVG 의 고정 픽셀 width 가 그리드 칸 최소폭을 키워 가로 오버플로를 만드는 것을 막는다.
     이게 있어야 창을 줄일 때 칸→차트가 같이 줄고 ResizeObserver 가 재계산한다. */
  min-width: 0;
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
.quick-text strong { font-size: 14px; color: var(--ink); }
.quick-text em { font-size: 13px; color: var(--ink-soft); font-style: normal; }

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

.health-name { font-size: 14px; font-weight: 600; color: var(--ink-2); }

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

/* 화면을 줄이면 칸 수를 단계적으로 줄여 자연스럽게 접힌다 (회원관리 등과 동일한 유동 동작) */
@media (max-width: 1200px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 1024px) {
  .chart-grid { grid-template-columns: 1fr; }
  .dash-cols { grid-template-columns: 1fr; }
}
@media (max-width: 560px) {
  .card-grid { grid-template-columns: 1fr; }
}
</style>

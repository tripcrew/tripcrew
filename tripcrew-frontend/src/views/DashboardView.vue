<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container dashboard">
      <!-- Welcome -->
      <section class="welcome">
        <div>
          <h1 class="welcome__title">안녕하세요, {{ displayName }} 님</h1>
          <p class="welcome__sub">
            {{ welcomeMessage }}
          </p>
        </div>
        <BaseButton variant="primary" size="lg" @click="$router.push('/plans')">+ 새 계획 만들기</BaseButton>
      </section>

      <!-- Active plans -->
      <section class="block">
        <div class="block__head">
          <h2 class="t-h2">진행 중인 여행 계획</h2>
          <span class="t-mono muted">{{ dashboardPlans.length }}개</span>
        </div>

        <p v-if="plansLoading" class="plan-state">여행 계획을 불러오는 중입니다.</p>
        <p v-else-if="plansError" class="plan-state plan-state--error">{{ plansError }}</p>

        <div v-else-if="dashboardPlans.length === 0" class="plan-empty">
          <div class="plan-empty__mark">+</div>
          <div>
            <h3>아직 진행 중인 여행 계획이 없어요</h3>
            <p>새 계획을 만들고 관광지를 담으면 이곳에서 바로 이어서 편집할 수 있습니다.</p>
          </div>
          <BaseButton variant="secondary" @click="$router.push('/plans')">계획 만들기</BaseButton>
        </div>

        <div
          v-else
          ref="planRail"
          class="plan-grid"
          @pointerdown="startPlanDrag"
          @pointermove="movePlanDrag"
          @pointerup="endPlanDrag"
          @pointerleave="endPlanDrag"
        >
          <article
            v-for="p in dashboardPlans"
            :key="p.id"
            class="plan-card"
            @click="openPlan(p.id)"
          >
            <div class="plan-card__top">
              <span :class="['status-chip', `status--${p.statusKey}`]">{{ p.status }}</span>
              <span v-if="p.ddayLabel" class="dday">{{ p.ddayLabel }}</span>
            </div>
            <h3 class="plan-card__title">{{ p.title }}</h3>
            <p class="plan-card__meta">{{ p.dates }}</p>

            <div class="plan-card__detail">
              <span>{{ p.duration }}</span>
              <span>수정 {{ p.updatedLabel }}</span>
            </div>

          </article>
        </div>
      </section>

      <!-- Two columns -->
      <div class="two-col">
        <!-- Recommendations -->
        <section class="block">
          <div class="block__head">
            <h2 class="t-h2">취향에 맞는 추천 여행지</h2>
            <span class="t-caption">자연 · 해변 위주 · 12개</span>
          </div>

          <div class="rec-grid">
            <article v-for="r in recommendations" :key="r.id" class="rec-card">
              <div class="rec-card__thumb"></div>
              <div class="rec-card__body">
                <h4>{{ r.name }}</h4>
                <p class="t-caption">★ {{ r.rating }} · {{ r.tag }}</p>
              </div>
            </article>
          </div>
        </section>

        <!-- Right column -->
        <aside class="side-col">
          <!-- Ranking -->
          <section class="block block--soft">
            <div class="block__head">
              <h2 class="t-h2">실시간 랭킹</h2>
              <span class="live-pill">
                <span class="live-dot"></span> LIVE · 1h
              </span>
            </div>

            <p v-if="rankingLoading" class="ranking-state">랭킹을 불러오는 중입니다.</p>
            <p v-else-if="rankingError" class="ranking-state">{{ rankingError }}</p>
            <p v-else-if="ranking.length === 0" class="ranking-state">아직 최근 1시간 내 랭킹 데이터가 없습니다.</p>

            <ol v-else class="ranking-list">
              <li v-for="item in ranking" :key="item.id">
                <button type="button" class="ranking-list__button" @click="goToAttraction(item.id)">
                  <span class="rank-no">{{ item.rank }}</span>
                  <span class="rank-info">
                    <strong>{{ item.title }}</strong>
                    <span class="t-caption">{{ item.region }}</span>
                  </span>
                  <span :class="['trend', `trend--${item.trend}`]">
                    <template v-if="item.trend === 'up'">▲ {{ item.delta }}</template>
                    <template v-else-if="item.trend === 'down'">▼ {{ item.delta }}</template>
                    <template v-else-if="item.trend === 'new'">NEW</template>
                    <template v-else>─</template>
                  </span>
                </button>
              </li>
            </ol>
          </section>

          <!-- Activity -->
          <section class="block block--soft">
            <div class="block__head">
              <h2 class="t-h2">최근 활동</h2>
            </div>
            <p v-if="activitiesLoading" class="activity-state">최근 활동을 불러오는 중입니다.</p>
            <p v-else-if="activitiesError" class="activity-state">{{ activitiesError }}</p>
            <p v-else-if="activities.length === 0" class="activity-state">아직 최근 활동이 없습니다.</p>
            <ul v-else class="activity">
              <li v-for="activity in activities" :key="activity.id">
                <div :class="['activity-dot', `activity-dot--${activity.activityType}`]"></div>
                <div>
                  <p>{{ activityMessage(activity) }}</p>
                  <span class="t-caption">{{ formatRelative(activity.createdAt) }}</span>
                </div>
              </li>
            </ul>
          </section>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { activityApi } from '@/api/activities'
import { rankingApi } from '@/api/rankings'
import { tripPlanApi } from '@/api/tripPlans'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const displayName = computed(() => authStore.user?.nickname || '여행자')
const rawPlans = ref([])
const plansLoading = ref(true)
const plansError = ref('')
const ranking = ref([])
const rankingLoading = ref(true)
const rankingError = ref('')
const activities = ref([])
const activitiesLoading = ref(true)
const activitiesError = ref('')
const planRail = ref(null)
const planDrag = ref({
  active: false,
  moved: false,
  startX: 0,
  scrollLeft: 0,
})
const suppressPlanClick = ref(false)
let rankingTimer = null

const today = startOfDay(new Date())

const dashboardPlans = computed(() =>
  rawPlans.value
    .filter((plan) => getStatus(plan).key !== 'done')
    .map(toDashboardPlan)
    .sort((a, b) => a.sortTime - b.sortTime),
)

const welcomeMessage = computed(() => {
  if (plansLoading.value) return '내 여행 계획을 확인하고 있습니다.'
  if (dashboardPlans.value.length === 0) return '새 여행 계획을 만들고 다음 여정을 준비해 보세요.'
  const next = dashboardPlans.value[0]
  return next.ddayLabel
    ? `${next.ddayLabel} · ${next.title} 계획이 기다리고 있습니다.`
    : `${next.title} 계획을 이어서 정리해 보세요.`
})

const recommendations = [
  { id: 1, name: '통영 동피랑', rating: 4.6, tag: '자연' },
  { id: 2, name: '속초 영금정', rating: 4.5, tag: '자연' },
  { id: 3, name: '남해 독일마을', rating: 4.4, tag: '문화' },
  { id: 4, name: '가평 자라섬', rating: 4.3, tag: '자연' }
]

async function loadPlans() {
  plansLoading.value = true
  plansError.value = ''
  try {
    rawPlans.value = await tripPlanApi.list()
  } catch (error) {
    rawPlans.value = []
    plansError.value = error?.response?.data?.message || '여행 계획을 불러오지 못했습니다.'
  } finally {
    plansLoading.value = false
  }
}

async function loadRanking() {
  try {
    ranking.value = await rankingApi.getAttractions()
    rankingError.value = ''
  } catch {
    ranking.value = []
    rankingError.value = ''
  } finally {
    rankingLoading.value = false
  }
}

async function loadActivities() {
  try {
    activities.value = await activityApi.getRecent()
    activitiesError.value = ''
  } catch {
    activities.value = []
    activitiesError.value = ''
  } finally {
    activitiesLoading.value = false
  }
}

function activityMessage(activity) {
  const planTitle = activity.tripPlanTitle || '여행 계획'
  if (activity.activityType === 'PLAN_CREATED') return `${planTitle} 계획을 만들었어요.`
  if (activity.activityType === 'PLACE_ADDED') return `${planTitle}에 ${activity.placeName}을(를) 추가했어요.`
  if (activity.activityType === 'PLACE_SCHEDULED') {
    return activity.visitDay
      ? `${activity.placeName}을(를) Day ${activity.visitDay}에 배치했어요.`
      : `${activity.placeName}을(를) 보관함으로 옮겼어요.`
  }
  if (activity.activityType === 'ROUTE_OPTIMIZED') return `${planTitle} Day ${activity.visitDay} 동선을 최적화했어요.`
  return '여행 계획을 업데이트했어요.'
}

function openPlan(planId) {
  if (suppressPlanClick.value) {
    suppressPlanClick.value = false
    return
  }
  router.push(`/plans/${planId}/edit`)
}

function goToAttraction(id) {
  router.push(`/attractions/${id}`)
}

function startPlanDrag(event) {
  const rail = planRail.value
  if (!rail) return
  suppressPlanClick.value = false
  planDrag.value = {
    active: true,
    moved: false,
    startX: event.clientX,
    scrollLeft: rail.scrollLeft,
  }
}

function movePlanDrag(event) {
  const rail = planRail.value
  if (!rail || !planDrag.value.active) return
  const delta = event.clientX - planDrag.value.startX
  if (Math.abs(delta) > 4) {
    planDrag.value.moved = true
    suppressPlanClick.value = true
  }
  rail.scrollLeft = planDrag.value.scrollLeft - delta
}

function endPlanDrag() {
  if (!planDrag.value.active) return
  planDrag.value.active = false
}

function toDashboardPlan(plan) {
  const status = getStatus(plan)
  return {
    id: plan.id,
    title: plan.title || '제목 없음',
    dates: formatDates(plan.startDate, plan.endDate),
    duration: formatDuration(plan.startDate, plan.endDate),
    updatedLabel: formatRelative(plan.updatedAt),
    status: status.label,
    statusKey: status.key,
    ddayLabel: getDdayLabel(plan.startDate),
    sortTime: getSortTime(plan),
  }
}

function getStatus(plan) {
  const start = parseDate(plan.startDate)
  const end = parseDate(plan.endDate)
  if (!start || !end) return { key: 'draft', label: '날짜 미정' }
  if (today < start) return { key: 'ready', label: '예정' }
  if (today > end) return { key: 'done', label: '완료' }
  return { key: 'active', label: '진행 중' }
}

function getDdayLabel(startDate) {
  const start = parseDate(startDate)
  if (!start) return ''
  const diff = Math.round((start - today) / 86400000)
  if (diff > 0) return `D-${diff}`
  if (diff === 0) return 'D-Day'
  return `D+${Math.abs(diff)}`
}

function getSortTime(plan) {
  const start = parseDate(plan.startDate)
  const updated = plan.updatedAt ? new Date(plan.updatedAt).getTime() : 0
  return start ? start.getTime() : Number.MAX_SAFE_INTEGER - updated
}

function formatDates(startDate, endDate) {
  if (!startDate && !endDate) return '날짜를 정하면 일정 진행률이 표시됩니다.'
  if (startDate && endDate) return `${formatPlanDate(startDate)} - ${formatPlanEndDate(endDate)}`
  return formatPlanDate(startDate || endDate)
}

function formatDuration(startDate, endDate) {
  const start = parseDate(startDate)
  const end = parseDate(endDate)
  if (!start || !end || end < start) return '기간 미정'
  const days = Math.round((end - start) / 86400000) + 1
  const nights = Math.max(0, days - 1)
  return nights > 0 ? `${nights}박 ${days}일` : '당일 여행'
}

function formatRelative(iso) {
  if (!iso) return '방금 전'
  const then = new Date(iso).getTime()
  const diffMin = Math.round((Date.now() - then) / 60000)
  if (diffMin < 1) return '방금 전'
  if (diffMin < 60) return `${diffMin}분 전`
  const diffHr = Math.round(diffMin / 60)
  if (diffHr < 24) return `${diffHr}시간 전`
  return `${Math.round(diffHr / 24)}일 전`
}

function parseDate(value) {
  if (!value) return null
  const [year, month, day] = value.split('-').map(Number)
  const date = new Date(year, month - 1, day)
  return Number.isNaN(date.getTime()) ? null : date
}

function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

function formatPlanDate(value) {
  const date = parseDate(value)
  if (!date) return '날짜 미정'
  return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}`
}

function formatPlanEndDate(value) {
  const date = parseDate(value)
  if (!date) return '날짜 미정'
  return `${pad(date.getMonth() + 1)}.${pad(date.getDate())}`
}

function pad(value) {
  return String(value).padStart(2, '0')
}

onMounted(() => {
  loadPlans()
  loadRanking()
  loadActivities()
  rankingTimer = window.setInterval(loadRanking, 30_000)
})

onBeforeUnmount(() => {
  if (rankingTimer) window.clearInterval(rankingTimer)
})
</script>

<style scoped>
.dashboard {
  padding: 40px var(--space-6) 80px;
}

.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.welcome__title {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.8px;
  margin-bottom: 8px;
}

.welcome__sub {
  font-size: 16px;
  color: var(--ink-3);
}

.welcome__sub strong {
  color: var(--coral);
}

.block {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 28px;
  margin-bottom: 24px;
}

.block--soft {
  background: var(--bg-soft);
  border: 1px solid var(--line);
}

.block__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.muted { color: var(--ink-soft); }

.plan-state {
  padding: 36px 0;
  text-align: center;
  color: var(--ink-soft);
  font-weight: 700;
}

.plan-state--error {
  color: var(--coral);
}

.plan-empty {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  padding: 22px;
  background: var(--bg-soft);
  border: 1px dashed var(--line-2);
  border-radius: var(--r-lg);
}

.plan-empty__mark {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: white;
  color: var(--teal);
  border: 1px solid var(--line);
  font-size: 28px;
  font-weight: 800;
}

.plan-empty h3 {
  font-size: 17px;
  font-weight: 800;
  margin-bottom: 4px;
}

.plan-empty p {
  color: var(--ink-soft);
  font-size: 14px;
}

/* Plan cards */
.plan-grid {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  overscroll-behavior-x: contain;
  scroll-snap-type: x mandatory;
  scroll-padding: 2px;
  padding: 2px 2px 12px;
  cursor: grab;
  -webkit-overflow-scrolling: touch;
}

.plan-grid:active {
  cursor: grabbing;
}

.plan-grid::-webkit-scrollbar {
  height: 8px;
}

.plan-grid::-webkit-scrollbar-track {
  background: var(--bg-soft);
  border-radius: 999px;
}

.plan-grid::-webkit-scrollbar-thumb {
  background: var(--line-2);
  border-radius: 999px;
}

.plan-card {
  flex: 0 0 calc((100% - 16px) / 2);
  min-height: 190px;
  display: flex;
  flex-direction: column;
  scroll-snap-align: start;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 22px;
  cursor: pointer;
  transition: all 0.2s;
}

.plan-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--sh-2);
  border-color: var(--teal);
}

.plan-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-chip {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}

.status--active { background: var(--teal); color: white; }
.status--ready { background: var(--coral-tint); color: var(--coral); }
.status--draft { background: var(--bg-2); color: var(--ink-3); }
.status--done { background: var(--success); color: white; }

.dday {
  font-family: var(--font-mono);
  font-size: 18px;
  font-weight: 800;
  color: var(--coral);
}

.plan-card__title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: -0.4px;
  margin-bottom: 6px;
  overflow-wrap: anywhere;
}

.plan-card__meta {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 14px;
}

.plan-card__detail {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
  margin-bottom: 16px;
}

.plan-card__detail span {
  padding: 5px 9px;
  border-radius: 999px;
  background: var(--bg-soft);
  color: var(--ink-3);
  font-size: 12px;
  font-weight: 700;
}

.progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress__bar {
  flex: 1;
  height: 6px;
  background: var(--bg-2);
  border-radius: 3px;
  overflow: hidden;
}

.progress__fill {
  height: 100%;
  background: linear-gradient(90deg, var(--teal), var(--coral));
  border-radius: 3px;
}

.progress__label {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--teal);
  min-width: 36px;
  text-align: right;
}

/* Two columns */
.two-col {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
}

.side-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.side-col .block {
  margin-bottom: 0;
}

/* Recommendations */
.rec-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.rec-card {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: var(--r-md);
  background: var(--bg-soft);
  cursor: pointer;
  transition: all 0.15s;
}

.rec-card:hover {
  background: white;
  box-shadow: var(--sh-1);
}

.rec-card__thumb {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
  flex-shrink: 0;
}

.rec-card__body h4 {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 2px;
}

/* Ranking list */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ranking-state {
  padding: 14px 0;
  color: var(--ink-soft);
  font-size: 13px;
}

.ranking-list li {
  list-style: none;
}

.ranking-list__button {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border: 0;
  border-radius: 10px;
  background: white;
  color: inherit;
  font: inherit;
  text-align: left;
  transition: background 0.15s;
  cursor: pointer;
}

.ranking-list__button:hover {
  background: var(--teal-soft);
}

.rank-no {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--muted);
  width: 18px;
  text-align: center;
}

.rank-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.rank-info strong {
  font-size: 14px;
  font-weight: 600;
}

.trend {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 6px;
  border-radius: 4px;
}

.trend--up { background: #E1F5EA; color: #1A7A4A; }
.trend--down { background: #FBEAE2; color: #B12C3A; }
.trend--same { background: var(--bg-2); color: var(--muted); }
.trend--new { background: var(--teal-soft); color: var(--teal-3); }

/* Live pill */
.live-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-2);
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--success);
  animation: pulse 1.6s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Activity */
.activity {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity li {
  display: flex;
  gap: 12px;
  padding: 10px;
  background: white;
  border-radius: 10px;
}

.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--info);
  margin-top: 6px;
  flex-shrink: 0;
}

.activity-dot--success { background: var(--success); }
.activity-dot--coral { background: var(--coral); }
.activity-dot--PLAN_CREATED { background: var(--info); }
.activity-dot--PLACE_ADDED { background: var(--success); }
.activity-dot--PLACE_SCHEDULED { background: var(--coral); }
.activity-dot--ROUTE_OPTIMIZED { background: var(--teal); }

.activity-state {
  padding: 12px 0;
  color: var(--ink-soft);
  font-size: 13px;
}

.activity p {
  font-size: 14px;
  color: var(--ink);
  margin-bottom: 2px;
}

@media (max-width: 900px) {
  .welcome {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
  }

  .plan-grid,
  .two-col,
  .rec-grid {
    grid-template-columns: 1fr;
  }

  .plan-grid {
    display: flex;
  }

  .plan-card {
    flex-basis: min(88%, 420px);
  }

  .plan-empty {
    grid-template-columns: 1fr;
  }
}
</style>

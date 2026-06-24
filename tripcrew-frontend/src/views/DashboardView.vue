<template>
  <div class="page dashboard-page">
    <AppHeader />

    <main class="container dashboard">
      <section class="dashboard-hero">
        <div class="dashboard-hero__copy">
          <p class="dashboard-hero__eyebrow">YOUR TRAVEL DESK</p>
          <h1>안녕하세요, {{ displayName }} 님.<br /><span>다음 여행을 시작해 볼까요?</span></h1>
          <p>{{ welcomeMessage }}</p>
          <div class="dashboard-hero__actions">
            <BaseButton variant="primary" size="lg" @click="$router.push('/plans')">+ 새 계획 만들기</BaseButton>
            <button type="button" class="text-action" @click="$router.push('/wishlist')">찜한 여행지 보기 <span>→</span></button>
          </div>
        </div>
        <div class="dashboard-hero__stats" aria-label="여행 현황">
          <div class="stat-card stat-card--plans">
            <span>진행 중인 계획</span>
            <strong>{{ dashboardPlans.length }}</strong>
            <small>개 여행을 준비 중이에요</small>
          </div>
          <div class="stat-card stat-card--guide">
            <span class="stat-card__spark">✦</span>
            <strong>여행은<br />가볍게 시작해요</strong>
            <small>아래 도우미가 취향을 정리해드려요.</small>
          </div>
        </div>
      </section>

      <section class="trip-starter">
        <div class="trip-starter__head">
          <div>
            <p class="section-kicker">QUICK START</p>
            <h2>어떤 여행을 떠나고 싶으세요?</h2>
          </div>
          <p>테마를 고르면 TripBot에 딱 맞는 질문을 준비해둘게요.</p>
        </div>

        <div class="theme-grid">
          <button
            v-for="theme in travelThemes"
            :key="theme.key"
            type="button"
            :class="['theme-card', { 'theme-card--selected': selectedTheme.key === theme.key }]"
            @click="selectedThemeKey = theme.key"
          >
            <span class="theme-card__icon">{{ theme.icon }}</span>
            <span class="theme-card__body">
              <strong>{{ theme.title }}</strong>
              <small>{{ theme.description }}</small>
            </span>
            <span class="theme-card__check">{{ selectedTheme.key === theme.key ? '✓' : '' }}</span>
          </button>
        </div>

        <div class="trip-starter__bottom">
          <p><span>TIP</span> {{ selectedTheme.prompt }}</p>
          <BaseButton variant="primary" @click="startTravelWithTheme">TripBot에게 추천받기 →</BaseButton>
        </div>
      </section>

      <section class="block plans-block">
        <div class="block__head">
          <div>
            <p class="section-kicker">MY PLANS</p>
            <h2 class="t-h2">이어가는 여행 계획</h2>
          </div>
          <button type="button" class="view-all" @click="$router.push('/plans')">전체 보기 →</button>
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

      <div class="two-col">
        <section class="block explore-block">
          <p class="section-kicker">EXPLORE</p>
          <h2 class="t-h2">여행 준비, 이렇게 이어가 보세요</h2>
          <div class="explore-steps">
            <button type="button" class="explore-step" @click="$router.push('/attractions')">
              <span>01</span><strong>관광지 둘러보기</strong><small>가보고 싶은 장소를 찾아보세요.</small><b>→</b>
            </button>
            <button type="button" class="explore-step" @click="$router.push('/wishlist')">
              <span>02</span><strong>마음에 드는 곳 저장</strong><small>찜 목록에 모아 비교해 보세요.</small><b>→</b>
            </button>
            <button type="button" class="explore-step" @click="$router.push('/plans')">
              <span>03</span><strong>나만의 일정 완성</strong><small>동행자와 함께 계획을 다듬어요.</small><b>→</b>
            </button>
          </div>
        </section>

        <aside class="side-col">
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

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppFooter from '@/components/common/AppFooter.vue'
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

const travelThemes = [
  { key: 'rest', icon: '☁️', title: '느긋한 휴식', description: '카페와 산책, 여유로운 하루', prompt: '주말에 가볍게 쉬고 싶어요. 카페와 산책을 중심으로 여유로운 여행 코스를 추천해주세요.' },
  { key: 'food', icon: '🍜', title: '맛있는 미식 여행', description: '지역의 맛을 따라 떠나는 코스', prompt: '지역 맛집과 시장을 중심으로 떠나는 미식 여행 코스를 추천해주세요.' },
  { key: 'nature', icon: '🌿', title: '자연 속으로', description: '바다와 숲, 풍경을 만나는 시간', prompt: '자연 풍경과 산책을 즐길 수 있는 여행 코스를 추천해주세요.' },
  { key: 'together', icon: '👋', title: '함께하는 여행', description: '친구·가족과 좋은 추억 만들기', prompt: '친구 또는 가족과 함께 즐기기 좋은 여행 코스를 추천해주세요.' },
]
const selectedThemeKey = ref('rest')

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
const selectedTheme = computed(() =>
  travelThemes.find((theme) => theme.key === selectedThemeKey.value) || travelThemes[0],
)

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

function startTravelWithTheme() {
  router.push({ path: '/chat', query: { prompt: selectedTheme.value.prompt } })
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
.page-soft {
  background: linear-gradient(120deg, #e8f3ef 0%, #f5f7f5 46%, #faeee8 100%);
}

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
  background: var(--surface);
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
  background: var(--surface);
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
  background: var(--surface);
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
  background: var(--surface);
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
  background: var(--surface);
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
.trend--new { background: var(--teal-soft); color: var(--teal-ink); }

/* Live pill */
.live-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: var(--surface);
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
  background: var(--surface);
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

/* Dashboard refresh */
.dashboard-page {
  background:
    radial-gradient(circle at 87% 8%, rgba(216, 90, 48, 0.12), transparent 24%),
    radial-gradient(circle at 12% 15%, rgba(15, 110, 86, 0.12), transparent 29%),
    var(--bg-soft);
}

.dashboard {
  padding-top: 32px;
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.74fr);
  gap: 24px;
  margin-bottom: 24px;
}

.dashboard-hero__copy {
  padding: 38px 40px;
  border-radius: var(--r-xl);
  background: var(--teal-3);
  box-shadow: var(--sh-2);
  color: white;
}

.dashboard-hero__eyebrow,
.section-kicker {
  margin-bottom: 9px;
  color: var(--coral);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
}

.dashboard-hero h1 {
  font-size: clamp(28px, 3vw, 40px);
  font-weight: 800;
  line-height: 1.22;
  letter-spacing: -1.3px;
}

.dashboard-hero h1 span { color: #b8e2d3; }

.dashboard-hero__copy > p:not(.dashboard-hero__eyebrow) {
  margin-top: 15px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 15px;
}

.dashboard-hero__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 28px;
}

.dashboard-hero__actions .btn--primary { background: var(--coral); }
.dashboard-hero__actions .btn--primary:hover { background: var(--coral-2); }

.text-action {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 700;
}
.text-action:hover { color: white; }
.text-action span { margin-left: 4px; }

.dashboard-hero__stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.stat-card {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  padding: 26px;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  background: var(--surface);
  box-shadow: var(--sh-1);
}

.stat-card > span:not(.stat-card__spark) {
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 700;
}
.stat-card > strong { margin-top: auto; color: var(--teal-ink); font-size: 46px; line-height: 1; letter-spacing: -2px; }
.stat-card > small { margin-top: 8px; color: var(--ink-soft); font-size: 12px; line-height: 1.4; }
.stat-card--guide { background: var(--coral-tint); border-color: var(--coral-soft); }
.stat-card--guide > strong { margin-top: 18px; color: var(--coral-ink); font-size: 20px; line-height: 1.35; letter-spacing: -0.4px; }
.stat-card--guide > small { margin-top: auto; }
.stat-card__spark { color: var(--coral); font-size: 25px; }

.trip-starter {
  margin-bottom: 24px;
  padding: 30px;
  border-radius: var(--r-xl);
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--sh-1);
}

.trip-starter__head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 24px;
  margin-bottom: 22px;
}
.trip-starter__head h2 { font-size: 24px; letter-spacing: -0.6px; }
.trip-starter__head > p { color: var(--ink-soft); font-size: 14px; line-height: 1.5; white-space: nowrap; }

.theme-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.theme-card {
  position: relative;
  min-height: 132px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  background: var(--bg-soft);
  text-align: left;
  transition: transform 0.15s, border-color 0.15s, background 0.15s;
}
.theme-card:hover { transform: translateY(-2px); border-color: var(--teal); background: var(--teal-tint); }
.theme-card--selected { border-color: var(--teal); background: var(--teal-soft); box-shadow: inset 0 0 0 1px var(--teal); }
.theme-card__icon { font-size: 24px; }
.theme-card__body { margin-top: auto; }
.theme-card__body strong, .theme-card__body small { display: block; }
.theme-card__body strong { color: var(--ink); font-size: 15px; }
.theme-card__body small { margin-top: 4px; color: var(--ink-soft); font-size: 12px; line-height: 1.35; }
.theme-card__check { position: absolute; top: 14px; right: 14px; color: var(--teal-ink); font-weight: 800; }

.trip-starter__bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 16px 18px;
  border-radius: var(--r-md);
  background: var(--bg-soft);
}
.trip-starter__bottom p { color: var(--ink-3); font-size: 13px; }
.trip-starter__bottom p span { margin-right: 8px; color: var(--coral); font-family: var(--font-mono); font-size: 11px; font-weight: 700; }

.plans-block .block__head { margin-bottom: 22px; }
.plans-block .section-kicker, .explore-block .section-kicker { margin-bottom: 5px; }
.view-all { color: var(--teal-ink); font-size: 14px; font-weight: 700; }
.view-all:hover { color: var(--teal); }

.explore-block { padding: 30px; }
.explore-block > .t-h2 { margin-bottom: 18px; }
.explore-steps { display: grid; gap: 10px; }
.explore-step {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  column-gap: 10px;
  align-items: center;
  padding: 14px;
  border-radius: var(--r-md);
  background: var(--bg-soft);
  text-align: left;
}
.explore-step:hover { background: var(--teal-tint); }
.explore-step > span { grid-row: span 2; color: var(--coral); font-family: var(--font-mono); font-size: 12px; font-weight: 700; }
.explore-step strong { color: var(--ink); font-size: 14px; }
.explore-step small { color: var(--ink-soft); font-size: 12px; }
.explore-step b { grid-row: span 2; color: var(--teal); font-size: 18px; }

@media (max-width: 900px) {
  .dashboard-hero, .two-col { grid-template-columns: 1fr; }
  .theme-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .dashboard { padding: 20px var(--space-4) 56px; }
  .dashboard-hero__copy, .trip-starter, .block { padding: 24px; }
  .dashboard-hero__stats { grid-template-columns: 1fr 1fr; }
  .stat-card { min-height: 155px; padding: 18px; }
  .stat-card > strong { font-size: 36px; }
  .trip-starter__head, .trip-starter__bottom { align-items: flex-start; flex-direction: column; }
  .theme-grid { grid-template-columns: 1fr 1fr; gap: 8px; }
  .theme-card { min-height: 120px; padding: 14px; }
  .trip-starter__bottom .btn { width: 100%; }
}
</style>

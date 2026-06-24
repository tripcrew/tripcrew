<template>
  <div class="page page-soft page-ambient">
    <AppHeader />

    <main class="container my-plans-layout">
      <header class="page-header">
        <h1 class="t-h1">내 여행 계획</h1>
        <BaseButton variant="primary" size="lg" :disabled="creating" @click="createPlan">
          {{ creating ? '생성 중…' : '＋ 새 계획 만들기' }}
        </BaseButton>
      </header>

      <!-- 크루에서 내보내져 돌아온 경우 안내 -->
      <div v-if="leftNotice" class="left-notice">
        <span>{{ leftNotice }}</span>
        <button class="left-notice__close" aria-label="닫기" @click="leftNotice = ''">✕</button>
      </div>

      <!-- 받은 초대(수락 대기) -->
      <section v-if="invites.length > 0" class="section-block invite-block">
        <h2 class="t-h2 section-title">받은 초대 <span class="muted">{{ invites.length }}개</span></h2>
        <div class="invite-list">
          <article v-for="invite in invites" :key="invite.planId" class="invite-card">
            <div class="invite-info">
              <h3>{{ invite.planTitle }}</h3>
              <p class="invite-meta">
                <strong>{{ invite.inviterNickname }}</strong>님이 초대 · {{ roleText(invite.role) }}
              </p>
            </div>
            <div class="invite-actions">
              <button
                class="mini-btn"
                :disabled="invite.busy"
                @click="rejectInvite(invite)"
              >거절</button>
              <button
                class="mini-btn mini-btn--primary"
                :disabled="invite.busy"
                @click="acceptInvite(invite)"
              >수락</button>
            </div>
          </article>
        </div>
      </section>

      <!-- 로딩 -->
      <p v-if="loading" class="state-msg">불러오는 중…</p>

      <!-- 에러 -->
      <p v-else-if="error" class="state-msg state-msg--error">{{ error }}</p>

      <!-- 빈 상태 -->
      <div v-else-if="plans.length === 0" class="empty">
        <p class="t-h2">아직 여행 계획이 없어요</p>
        <p class="t-caption muted">＋ 새 계획 만들기로 첫 여행을 시작해 보세요.</p>
      </div>

      <!-- 목록 -->
      <section v-else class="section-block">
        <header class="plans-overview">
          <div>
            <p class="plans-overview__eyebrow">MY TRIPS</p>
            <h2 class="t-h2">여행 계획 <span class="muted">{{ plans.length }}개</span></h2>
          </div>
          <p class="t-caption">여행 날짜를 기준으로 정리했어요.</p>
        </header>

        <section v-for="group in planGroups" :key="group.key" :class="['plan-status-group', `plan-status-group--${group.key}`]">
          <header class="plan-status-group__head">
            <div>
              <span :class="['status-dot', `status-dot--${group.key}`]"></span>
              <h3>{{ group.title }}</h3>
              <span class="plan-status-group__count">{{ group.plans.length }}</span>
            </div>
            <p>{{ group.description }}</p>
          </header>

          <div v-if="group.plans.length > 0" class="plans-grid">
            <article
              v-for="p in group.plans"
              :key="p.id"
              class="plan-card"
              @click="$router.push(`/plans/${p.id}/edit`)"
            >
              <div class="plan-card__top">
                <div class="chip-group">
                  <span :class="['status-chip', `status--${p.status.key}`]">{{ p.status.label }}</span>
                  <span v-if="p.myRole && p.myRole !== 'OWNER'" class="status-chip status--shared">
                    {{ roleText(p.myRole) }} · 공유받음
                  </span>
                </div>
                <span class="t-mono muted">v.{{ p.version }}</span>
              </div>
              <span class="updated t-caption">수정 {{ formatRelative(p.updatedAt) }}</span>
              <h3>{{ p.title }}</h3>
              <p class="meta">{{ formatDates(p.startDate, p.endDate) }}</p>

              <div class="card-footer">
                <span class="t-caption muted">조회 {{ p.viewCount }}</span>
              </div>
            </article>
          </div>
          <p v-else class="plan-status-empty">{{ group.emptyMessage }}</p>
        </section>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { tripPlanApi } from '@/api/tripPlans'

const router = useRouter()
const route = useRoute()

// 공동 계획에서 내보내져 이 화면으로 돌아온 경우 안내(쿼리는 즉시 정리해 새로고침 시 사라지게)
const leftNotice = ref('')

const plans = ref([])
const invites = ref([])
const loading = ref(true)
const error = ref('')
const creating = ref(false)
const today = startOfDay(new Date())

const planGroups = computed(() => {
  const grouped = {
    active: [],
    ready: [],
    done: [],
  }

  plans.value.forEach((plan) => {
    const status = getStatus(plan)
    grouped[status.key].push({ ...plan, status })
  })

  grouped.active.sort((left, right) => getSortTime(left.startDate, left.updatedAt) - getSortTime(right.startDate, right.updatedAt))
  grouped.ready.sort((left, right) => getSortTime(left.startDate, left.updatedAt) - getSortTime(right.startDate, right.updatedAt))
  grouped.done.sort((left, right) => getSortTime(right.endDate, right.updatedAt) - getSortTime(left.endDate, left.updatedAt))

  return [
    { key: 'active', title: '진행 중', description: '지금 여행 중인 계획이에요.', plans: grouped.active, emptyMessage: '진행 중인 여행 계획이 없어요.' },
    { key: 'ready', title: '예정', description: '곧 떠날 여행과 날짜를 정할 계획이에요.', plans: grouped.ready, emptyMessage: '예정된 여행 계획이 없어요.' },
    { key: 'done', title: '완료', description: '다녀온 여행 계획을 다시 확인할 수 있어요.', plans: grouped.done, emptyMessage: '완료된 여행 계획이 없어요.' },
  ]
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    // 받은 초대와 내 계획을 함께 불러온다(초대는 비핵심이라 실패해도 목록은 보여줌)
    const [planList, inviteList] = await Promise.all([
      tripPlanApi.list(),
      tripPlanApi.listInvites().catch(() => []),
    ])
    plans.value = planList
    invites.value = inviteList.map((i) => ({ ...i, busy: false }))
  } catch (e) {
    error.value = '목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function acceptInvite(invite) {
  if (invite.busy) return
  invite.busy = true
  try {
    await tripPlanApi.acceptInvite(invite.planId)
    invites.value = invites.value.filter((i) => i.planId !== invite.planId)
    await load() // 수락한 계획을 '내 계획'에 반영
  } catch (e) {
    invite.busy = false
    error.value = '초대를 수락하지 못했습니다.'
  }
}

async function rejectInvite(invite) {
  if (invite.busy) return
  invite.busy = true
  try {
    await tripPlanApi.rejectInvite(invite.planId)
    invites.value = invites.value.filter((i) => i.planId !== invite.planId)
  } catch (e) {
    invite.busy = false
    error.value = '초대를 거절하지 못했습니다.'
  }
}

async function createPlan() {
  if (creating.value) return
  creating.value = true
  try {
    const created = await tripPlanApi.create({ title: '새 여행 계획' })
    router.push(`/plans/${created.id}/edit`)
  } catch (e) {
    error.value = '계획을 만들지 못했습니다.'
  } finally {
    creating.value = false
  }
}

function roleText(role) {
  if (role === 'EDITOR') return '편집자'
  if (role === 'VIEWER') return '뷰어'
  return role
}

function formatDates(start, end) {
  if (!start && !end) return '날짜 미정'
  if (start && end) return `${start} — ${end}`
  return start || end
}

function getStatus(plan) {
  const start = parseDate(plan.startDate)
  const end = parseDate(plan.endDate)
  if (!start || !end || today < start) return { key: 'ready', label: '예정' }
  if (today > end) return { key: 'done', label: '완료' }
  return { key: 'active', label: '진행 중' }
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

function getSortTime(dateValue, updatedAt) {
  const date = parseDate(dateValue)
  if (date) return date.getTime()
  return updatedAt ? new Date(updatedAt).getTime() : Number.MAX_SAFE_INTEGER
}

function formatRelative(iso) {
  if (!iso) return ''
  const then = new Date(iso).getTime()
  const diffMin = Math.round((Date.now() - then) / 60000)
  if (diffMin < 1) return '방금 전'
  if (diffMin < 60) return `${diffMin}분 전`
  const diffHr = Math.round(diffMin / 60)
  if (diffHr < 24) return `${diffHr}시간 전`
  return `${Math.round(diffHr / 24)}일 전`
}

onMounted(() => {
  if (route.query.left === 'removed') {
    leftNotice.value = '여행 크루에서 내보내져 해당 계획에 더 이상 접근할 수 없어요.'
    router.replace({ query: {} }) // 안내만 한 번 보여주고 쿼리 정리
  }
  load()
})
</script>

<style scoped>
.my-plans-layout {
  padding: 40px var(--space-6) 80px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.state-msg {
  padding: 40px 0;
  text-align: center;
  color: var(--ink-soft);
}

.state-msg--error {
  color: var(--coral);
}

.empty {
  text-align: center;
  padding: 80px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-block {
  margin-bottom: 40px;
}

/* 크루 내보내짐 안내 배너 */
.left-notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px 16px;
  border-radius: var(--r-md, 12px);
  background: var(--coral-tint, #fff1ec);
  color: var(--coral, #e06a4f);
  font-size: 14px;
  font-weight: 600;
}

.left-notice__close {
  flex-shrink: 0;
  color: inherit;
  opacity: 0.7;
  font-size: 13px;
}

.left-notice__close:hover { opacity: 1; }

/* 받은 초대 */
.invite-block {
  background: var(--teal-soft, #e6f6f4);
  border-radius: var(--r-lg);
  padding: 20px;
}

.invite-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.invite-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-md, 12px);
  padding: 14px 16px;
}

.invite-info { min-width: 0; }

.invite-info h3 {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.2px;
  margin-bottom: 2px;
}

.invite-meta {
  font-size: 13px;
  color: var(--ink-soft);
}

.invite-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.mini-btn {
  padding: 7px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid var(--line);
  background: var(--surface);
  color: var(--ink-3);
  transition: all 0.15s;
}

.mini-btn:hover:not(:disabled) { background: var(--bg-2); }

.mini-btn--primary {
  background: var(--teal, #2bb5a6);
  border-color: var(--teal, #2bb5a6);
  color: white;
}

.mini-btn--primary:hover:not(:disabled) { background: var(--teal-2, #239b8e); }

.mini-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.section-title {
  margin-bottom: 16px;
}

.muted { color: var(--ink-soft); font-weight: 500; }

.plans-overview {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 28px;
}

.plans-overview__eyebrow {
  margin-bottom: 5px;
  color: var(--coral);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
}

.plan-status-group + .plan-status-group {
  margin-top: 32px;
}

.plan-status-group__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.plan-status-group__head > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.plan-status-group__head h3 {
  color: var(--ink);
  font-size: 16px;
  font-weight: 800;
}

.plan-status-group__head > p {
  color: var(--ink-soft);
  font-size: 13px;
}

.plan-status-group__count {
  min-width: 22px;
  padding: 2px 7px;
  border-radius: 999px;
  background: var(--bg-2);
  color: var(--ink-soft);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.status-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.status-dot--active { background: var(--teal); box-shadow: 0 0 0 4px var(--teal-soft); }
.status-dot--ready { background: var(--coral); box-shadow: 0 0 0 4px var(--coral-tint); }
.status-dot--done { background: var(--success); box-shadow: 0 0 0 4px #E1F5EA; }

.plan-status-empty {
  padding: 18px;
  border: 1px dashed var(--line-2);
  border-radius: var(--r-md);
  background: var(--glass);
  color: var(--ink-soft);
  font-size: 13px;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.plan-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  position: relative;
}

.plan-card:hover {
  border-color: var(--teal);
  box-shadow: var(--sh-2);
  transform: translateY(-2px);
}

.plan-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-chip {
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}

.status--active { background: var(--teal); color: white; }
.status--ready { background: var(--coral-tint); color: var(--coral-ink); }
.status--done { background: #E1F5EA; color: #1A7A4A; }
.status--shared { background: var(--coral-tint, #fff1ec); color: var(--coral, #e06a4f); }

.chip-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.updated {
  display: block;
  margin-bottom: 8px;
  font-size: 11px;
}

.plan-card h3 {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: -0.3px;
  margin-bottom: 6px;
}

.meta {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 20px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

@media (max-width: 900px) {
  .plans-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .my-plans-layout { padding: 28px var(--space-4) 56px; }
  .page-header, .plans-overview, .plan-status-group__head { align-items: flex-start; flex-direction: column; }
  .plans-grid { grid-template-columns: 1fr; }
  .plans-overview { margin-bottom: 24px; }
}
</style>

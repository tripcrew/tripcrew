<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container my-plans-layout">
      <header class="page-header">
        <h1 class="t-h1">내 여행 계획</h1>
        <BaseButton variant="primary" size="lg" :disabled="creating" @click="createPlan">
          {{ creating ? '생성 중…' : '＋ 새 계획 만들기' }}
        </BaseButton>
      </header>

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
        <h2 class="t-h2 section-title">전체 <span class="muted">{{ plans.length }}개</span></h2>
        <div class="plans-grid">
          <article
            v-for="p in plans"
            :key="p.id"
            class="plan-card"
            @click="$router.push(`/plans/${p.id}/edit`)"
          >
            <div class="plan-card__top">
              <div class="chip-group">
                <span class="status-chip status--draft">계획</span>
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
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { tripPlanApi } from '@/api/tripPlans'

const router = useRouter()

const plans = ref([])
const invites = ref([])
const loading = ref(true)
const error = ref('')
const creating = ref(false)

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

onMounted(load)
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
  background: white;
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
  background: white;
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

.plans-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.plan-card {
  background: white;
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

.status--draft { background: var(--bg-2); color: var(--ink-3); }
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
</style>

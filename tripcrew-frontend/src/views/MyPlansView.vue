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
              <span class="status-chip status--draft">계획</span>
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
const loading = ref(true)
const error = ref('')
const creating = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  try {
    plans.value = await tripPlanApi.list()
  } catch (e) {
    error.value = '목록을 불러오지 못했습니다.'
  } finally {
    loading.value = false
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

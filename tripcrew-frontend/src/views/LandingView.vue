<template>
  <div class="page">
    <AppHeader :logged-in="false" />

    <!-- Hero -->
    <section class="hero">
      <div class="container hero__inner">
        <div class="hero__content">
          <div class="hero__eyebrow">공공데이터 · AI · 동선 최적화</div>
          <h1 class="hero__title">
            한 줄로 시작하는<br />
            <span class="accent">나만의 여행 계획</span>
          </h1>
          <p class="hero__lead">
            한국관광공사 데이터를 기반으로 한 신뢰도 높은 추천과,
            <br />AI 챗봇으로 5초 만에 만드는 코스.
          </p>
          <div class="hero__cta">
            <BaseButton variant="primary" size="lg" @click="$router.push('/chat')">
              챗봇으로 시작하기 →
            </BaseButton>
            <BaseButton variant="secondary" size="lg" @click="$router.push('/attractions')">
              여행지 둘러보기
            </BaseButton>
          </div>
        </div>

        <div class="hero__preview">
          <div class="chatbot-preview">
            <div class="chatbot-preview__bar">
              <span class="dot dot--r"></span>
              <span class="dot dot--y"></span>
              <span class="dot dot--g"></span>
              <span class="t-mono ml">PREVIEW · TRIPBOT</span>
            </div>
            <div class="chatbot-preview__body">
              <div class="bubble bubble--user">"여수 2박3일 바다 위주"</div>
              <div class="bubble bubble--bot">
                <strong>5초 안에 코스 5개</strong>를 추천해드릴게요 ✨
                <div class="preview-cards">
                  <div class="preview-card">★ 4.7 · 8h · 바다와 동백</div>
                  <div class="preview-card preview-card--accent">★ 4.8 · 7h · 우천 대비 코스</div>
                  <div class="preview-card">★ 4.5 · 6h · 여유 산책</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Ranking -->
    <section class="section section--ranking">
      <div class="container">
        <div class="ranking-panel">
          <div class="section__head">
            <div>
              <p class="section__eyebrow">TRENDING NOW</p>
              <h2 class="t-h1">지금 인기 있는 여행지</h2>
              <p class="t-caption">최근 한 시간 동안 가장 많이 둘러본 여행지예요.</p>
            </div>
            <div class="ranking-head-actions">
              <div class="live-pill">
                <span class="live-dot"></span>
                실시간 집계
              </div>
              <button type="button" class="ranking-more" @click="$router.push('/attractions')">전체 여행지 보기 →</button>
            </div>
          </div>

          <p v-if="rankingLoading" class="t-caption">실시간 랭킹을 불러오는 중입니다.</p>
          <p v-else-if="rankingError" class="t-caption">{{ rankingError }}</p>
          <p v-else-if="popular.length === 0" class="t-caption">아직 최근 1시간 내 랭킹 데이터가 없습니다.</p>

          <div v-else class="ranking-grid">
            <button
              v-for="item in popular"
              :key="item.id"
              type="button"
              class="ranking-card"
              @click="goToAttraction(item.id)"
            >
              <span class="ranking-card__rank">{{ String(item.rank).padStart(2, '0') }}</span>
              <span class="ranking-card__thumb">
                <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" />
                <span v-else class="thumb-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <circle cx="9" cy="9" r="2"/>
                    <path d="m21 15-5-5L5 21"/>
                  </svg>
                </span>
              </span>
              <span class="ranking-card__body">
                <span class="ranking-card__title" role="heading" aria-level="3">{{ item.title }}</span>
                <span class="ranking-card__meta">{{ item.region }}</span>
              </span>
              <span :class="['ranking-card__trend', `trend--${item.trend}`]">
                <span v-if="item.trend === 'up'">▲ {{ item.delta }}</span>
                <span v-else-if="item.trend === 'down'">▼ {{ item.delta }}</span>
                <span v-else-if="item.trend === 'new'">NEW</span>
                <span v-else>─</span>
              </span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Co-edit CTA -->
    <section class="section section--coral">
      <div class="container">
        <div class="co-edit-banner">
          <div>
            <div class="t-mono accent">REAL-TIME COLLABORATION</div>
            <h2 class="t-h1">동행자와 함께 계획하면<br />더 빠릅니다</h2>
            <p class="t-body" style="margin-top: 12px; color: var(--ink-3);">
              회원가입 후 친구를 초대하고 실시간으로 함께 일정을 편집하세요.<br />
              최대 10명까지 동시 편집 · WebSocket 기반 즉시 동기화.
            </p>
            <div style="margin-top: 24px;">
              <BaseButton variant="primary" size="lg" @click="$router.push({ path: '/auth', query: { mode: 'signup' } })">
                3초 만에 회원가입 →
              </BaseButton>
            </div>
          </div>

          <div class="co-edit-preview">
            <div class="presence">
              <div class="avatar avatar--lg" style="background: var(--teal);">민</div>
              <div class="avatar avatar--lg" style="background: var(--coral);">지</div>
              <div class="avatar avatar--lg" style="background: var(--violet);">현</div>
              <div class="avatar avatar--lg avatar--more">+2</div>
            </div>
            <div class="presence-label">5명이 함께 편집 중 · v.42</div>
          </div>
        </div>
      </div>
    </section>

    <AppFooter />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { rankingApi } from '@/api/rankings'

const router = useRouter()
const popular = ref([])
const rankingLoading = ref(true)
const rankingError = ref('')
let rankingTimer = null

async function loadRanking() {
  try {
    popular.value = await rankingApi.getAttractions()
    rankingError.value = ''
  } catch {
    popular.value = []
    rankingError.value = ''
  } finally {
    rankingLoading.value = false
  }
}

function goToAttraction(id) {
  router.push(`/attractions/${id}`)
}

onMounted(() => {
  loadRanking()
  rankingTimer = window.setInterval(loadRanking, 30_000)
})

onBeforeUnmount(() => {
  if (rankingTimer) window.clearInterval(rankingTimer)
})
</script>

<style scoped>
/* Hero */
.hero {
  padding: 80px 0 64px;
  background:
    radial-gradient(circle at 90% 10%, var(--teal-tint) 0%, transparent 50%),
    radial-gradient(circle at 10% 90%, var(--coral-tint) 0%, transparent 40%),
    var(--bg);
}

.hero__inner {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 64px;
  align-items: center;
}

.hero__eyebrow {
  display: inline-block;
  padding: 6px 14px;
  background: var(--teal-soft);
  color: var(--teal-3);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.2px;
  margin-bottom: 24px;
}

.hero__title {
  font-size: 56px;
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -1.8px;
  color: var(--ink);
  margin-bottom: 24px;
}

.hero__title .accent {
  color: var(--teal);
  position: relative;
  display: inline-block;
}

.hero__title .accent::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 0;
  right: 0;
  height: 12px;
  background: var(--coral);
  opacity: 0.18;
  z-index: -1;
}

.hero__lead {
  font-size: 18px;
  line-height: 1.6;
  color: var(--ink-3);
  margin-bottom: 32px;
}

.hero__cta {
  display: flex;
  gap: 12px;
}

.hero__preview {
  display: flex;
  justify-content: center;
}

.chatbot-preview {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: var(--r-xl);
  border: 1px solid var(--line);
  box-shadow: var(--sh-3);
  overflow: hidden;
}

.chatbot-preview__bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 18px;
  background: var(--bg-soft);
  border-bottom: 1px solid var(--line);
}

.dot {
  width: 12px; height: 12px; border-radius: 50%;
}
.dot--r { background: #FF5F57; }
.dot--y { background: #FEBC2E; }
.dot--g { background: #28C840; }

.ml {
  margin-left: 12px;
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 1px;
}

.chatbot-preview__body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bubble {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 15px;
  line-height: 1.5;
  max-width: 85%;
}

.bubble--user {
  align-self: flex-end;
  background: var(--teal);
  color: white;
  font-weight: 500;
}

.bubble--bot {
  align-self: flex-start;
  background: var(--bg-2);
  color: var(--ink);
}

.preview-cards {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
}

.preview-card {
  padding: 8px 12px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  color: var(--ink-3);
}

.preview-card--accent {
  border-color: var(--coral);
  background: var(--coral-tint);
  color: var(--coral-2);
  font-weight: 600;
}

/* Sections */
.section {
  padding: 80px 0;
}

.section--coral {
  background: linear-gradient(135deg, var(--coral-tint) 0%, var(--teal-tint) 100%);
}

.section--ranking {
  padding-top: 52px;
  padding-bottom: 56px;
  background: linear-gradient(180deg, var(--bg) 0%, #f6fbf8 100%);
}

.ranking-panel {
  padding: 30px;
  border: 1px solid var(--line);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--sh-1);
}

.section__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

.section__eyebrow {
  margin-bottom: 8px;
  color: var(--coral);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1.1px;
}

.ranking-head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ranking-more {
  padding: 7px 12px;
  color: var(--teal-3);
  font-size: 13px;
  font-weight: 700;
}

.ranking-more:hover { color: var(--teal); }

.live-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: var(--ink-2);
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  animation: pulse 1.6s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.8); }
}

/* Ranking */
.ranking-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.ranking-card {
  width: 100%;
  text-align: left;
  font: inherit;
  background: white;
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 14px;
  position: relative;
  transition: all 0.2s;
  cursor: pointer;
}

.ranking-card:hover {
  border-color: var(--teal);
  box-shadow: var(--sh-2);
  transform: translateY(-2px);
}

.ranking-card__rank {
  display: block;
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--muted);
  margin-bottom: 10px;
}

.ranking-card__thumb {
  display: block;
  width: 100%;
  aspect-ratio: 4/3;
  background: var(--bg-2);
  border-radius: 10px;
  margin-bottom: 12px;
  overflow: hidden;
  position: relative;
}

.ranking-card__thumb img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--muted);
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
}

.ranking-card__title {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 4px;
}

.ranking-card__meta {
  display: block;
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 8px;
}

.tag-row {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.ranking-card__trend {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 6px;
}

.trend--up { background: #E1F5EA; color: #1A7A4A; }
.trend--down { background: #FBEAE2; color: #B12C3A; }
.trend--same { background: var(--bg-2); color: var(--ink-soft); }
.trend--new { background: var(--teal-soft); color: var(--teal-3); }

@media (max-width: 1080px) {
  .ranking-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 680px) {
  .section--ranking { padding: 40px 0 44px; }
  .ranking-panel { padding: 20px 16px; border-radius: var(--r-lg); }
  .section__head { align-items: flex-start; flex-direction: column; gap: 14px; margin-bottom: 22px; }
  .ranking-head-actions { width: 100%; justify-content: space-between; }
  .ranking-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
  .ranking-card { padding: 10px; }
  .ranking-card__trend { top: 8px; right: 8px; }
}

/* Co-edit */
.co-edit-banner {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 64px;
  align-items: center;
}

.t-mono.accent {
  color: var(--coral);
  font-weight: 600;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.co-edit-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.presence {
  display: flex;
}

.presence .avatar {
  margin-left: -12px;
  border: 3px solid white;
}

.presence .avatar:first-child { margin-left: 0; }

.avatar--lg {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 22px;
}

.avatar--more {
  background: var(--ink-2) !important;
  font-size: 16px !important;
}

.presence-label {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--ink-3);
  background: white;
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
}
</style>

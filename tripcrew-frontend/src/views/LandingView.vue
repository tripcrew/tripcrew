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
              직접 계획 짜기
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
    <section class="section">
      <div class="container">
        <div class="section__head">
          <div>
            <h2 class="t-h1">지금 인기 있는 여행지</h2>
            <p class="t-caption">실시간 랭킹 · 최근 1시간 기준 · Redis Sorted Set</p>
          </div>
          <div class="live-pill">
            <span class="live-dot"></span>
            LIVE
          </div>
        </div>

        <p v-if="rankingLoading" class="t-caption">실시간 랭킹을 불러오는 중입니다.</p>
        <p v-else-if="rankingError" class="t-caption">{{ rankingError }}</p>
        <p v-else-if="popular.length === 0" class="t-caption">아직 최근 1시간 내 랭킹 데이터가 없습니다.</p>

        <div v-else class="ranking-grid">
          <article v-for="item in popular" :key="item.id" class="ranking-card">
            <div class="ranking-card__rank">{{ String(item.rank).padStart(2, '0') }}</div>
            <div class="ranking-card__thumb">
              <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" />
              <div v-else class="thumb-placeholder">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <circle cx="9" cy="9" r="2"/>
                  <path d="m21 15-5-5L5 21"/>
                </svg>
              </div>
            </div>
            <div class="ranking-card__body">
              <h3 class="ranking-card__title">{{ item.title }}</h3>
              <p class="ranking-card__meta">{{ item.region }}</p>
              <div class="tag-row">
                <span class="chip chip--teal">최근 1시간 {{ item.score }}점</span>
              </div>
            </div>
            <div :class="['ranking-card__trend', `trend--${item.trend}`]">
              <span v-if="item.trend === 'up'">▲ {{ item.delta }}</span>
              <span v-else-if="item.trend === 'down'">▼ {{ item.delta }}</span>
              <span v-else-if="item.trend === 'new'">NEW</span>
              <span v-else>─</span>
            </div>
          </article>
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
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { rankingApi } from '@/api/rankings'

const popular = ref([])
const rankingLoading = ref(true)
const rankingError = ref('')
let rankingTimer = null

async function loadRanking() {
  try {
    popular.value = await rankingApi.getAttractions()
    rankingError.value = ''
  } catch (error) {
    rankingError.value = error?.response?.data?.message || '실시간 랭킹을 불러오지 못했습니다.'
  } finally {
    rankingLoading.value = false
  }
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

.section__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 32px;
}

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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.ranking-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
  position: relative;
  transition: all 0.2s;
}

.ranking-card:hover {
  border-color: var(--teal);
  box-shadow: var(--sh-2);
  transform: translateY(-2px);
}

.ranking-card__rank {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--muted);
  margin-bottom: 12px;
}

.ranking-card__thumb {
  width: 100%;
  aspect-ratio: 16/10;
  background: var(--bg-2);
  border-radius: 8px;
  margin-bottom: 14px;
  overflow: hidden;
  position: relative;
}

.ranking-card__thumb img {
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
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 4px;
}

.ranking-card__meta {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 10px;
}

.tag-row {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.ranking-card__trend {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 6px;
}

.trend--up { background: #E1F5EA; color: #1A7A4A; }
.trend--down { background: #FBEAE2; color: #B12C3A; }
.trend--same { background: var(--bg-2); color: var(--ink-soft); }
.trend--new { background: var(--teal-soft); color: var(--teal-3); }

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

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
            취향과 일정에 맞춘 여행지 추천부터,
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
              <transition name="msg">
                <div v-if="showUser" class="bubble bubble--user">"여수 2박3일 바다 위주"</div>
              </transition>
              <transition name="msg" mode="out-in">
                <div v-if="showThinking" key="thinking" class="bubble bubble--bot bubble--typing">
                  <span class="typing-dots"><i></i><i></i><i></i></span>
                  <span class="typing-text">열심히 계획하고 있어요</span>
                </div>
                <div v-else-if="showAnswer" key="answer" class="bubble bubble--bot">
                  <strong>5초 안에 코스 5개</strong>를 추천해드릴게요 ✨
                  <div class="preview-cards">
                    <div class="preview-card">★ 4.7 · 8h · 바다와 동백</div>
                    <div class="preview-card preview-card--accent">★ 4.8 · 7h · 우천 대비 코스</div>
                    <div class="preview-card">★ 4.5 · 6h · 여유 산책</div>
                  </div>
                </div>
              </transition>
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
              최대 10명까지 동시 편집.
            </p>
            <div style="margin-top: 24px;">
              <BaseButton variant="primary" size="lg" @click="$router.push({ path: '/auth', query: { mode: 'signup' } })">
                3초 만에 회원가입 →
              </BaseButton>
            </div>
          </div>

          <div class="co-edit-preview" ref="coEditPreview" :class="{ 'is-in': presenceIn }">
            <div class="presence">
              <div class="avatar avatar--lg" style="background: var(--teal);">민</div>
              <div class="avatar avatar--lg" style="background: var(--coral);">지</div>
              <div class="avatar avatar--lg" style="background: var(--violet);">현</div>
              <div class="avatar avatar--lg avatar--more">+2</div>
            </div>
            <div class="presence-label"><span class="live-now"></span>5명이 함께 편집 중 · v.42</div>
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

// 협업 섹션 presence 아바타 등장 — 화면에 들어왔을 때 1회만(스크롤마다 X)
const coEditPreview = ref(null)
const presenceIn = ref(false)
let presenceObserver = null

// Hero 챗봇 대화 연출: 입력 → 생각 중(타이핑) → 추천 답변 순서로 시간차 재생
const showUser = ref(false)
const showThinking = ref(false)
const showAnswer = ref(false)
let heroTimers = []

function prefersReduced() {
  return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function playHeroSequence() {
  if (prefersReduced()) {
    // 모션 최소 선호 시 타이핑 단계 없이 최종 대화 상태를 즉시 표시
    showUser.value = true
    showAnswer.value = true
    return
  }
  heroTimers.push(window.setTimeout(() => { showUser.value = true }, 350))
  heroTimers.push(window.setTimeout(() => { showThinking.value = true }, 950))
  heroTimers.push(window.setTimeout(() => {
    showThinking.value = false
    showAnswer.value = true
  }, 2350))
}

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
  playHeroSequence()

  // reduced-motion 이거나 IO 미지원이면 바로 노출, 아니면 뷰포트 진입 시 1회 재생
  if (prefersReduced() || !('IntersectionObserver' in window)) {
    presenceIn.value = true
  } else if (coEditPreview.value) {
    presenceObserver = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        presenceIn.value = true
        presenceObserver.disconnect()
        presenceObserver = null
      }
    }, { threshold: 0.4 })
    presenceObserver.observe(coEditPreview.value)
  }
})

onBeforeUnmount(() => {
  if (rankingTimer) window.clearInterval(rankingTimer)
  if (presenceObserver) presenceObserver.disconnect()
  heroTimers.forEach((t) => window.clearTimeout(t))
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
  color: var(--teal-ink);
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
  background: var(--surface);
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
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  color: var(--ink-3);
}

.preview-card--accent {
  border-color: var(--coral);
  background: var(--coral-tint);
  color: var(--coral-ink);
  font-weight: 600;
}

/* Hero 챗봇 preview — 버블/카드가 차례로 fade-up (최초 마운트 1회).
   reduced-motion 이면 media 블록이 적용 안 돼 즉시 최종 상태로 보인다. */
@keyframes rise {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
/* 추천 코스(주황) 카드가 등장 후 아주 은은하게 위아래로 떠다니며 시선을 끈다 */
@keyframes float-accent {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}
/* '생각 중' 타이핑 점 — 차례로 통통 */
@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.45; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* Hero 왼쪽 카피/CTA — 마운트 시 위에서부터 차례로 가볍게 fade-up (오른쪽 챗봇 연출과 균형) */
@media (prefers-reduced-motion: no-preference) {
  .hero__eyebrow { animation: rise 0.5s ease-out both; }
  .hero__title   { animation: rise 0.5s ease-out 0.08s both; }
  .hero__lead    { animation: rise 0.5s ease-out 0.16s both; }
  .hero__cta     { animation: rise 0.5s ease-out 0.24s both; }
}

/* 메시지 등장/교체(입력→타이핑→답변)는 Vue <transition name="msg"> 로 부드럽게.
   reduced-motion 이면 global.css 의 전역 transition:none 으로 즉시 처리된다. */
.msg-enter-active { transition: opacity 0.35s ease, transform 0.35s ease; }
.msg-leave-active { transition: opacity 0.2s ease; }
.msg-enter-from { opacity: 0; transform: translateY(8px); }
.msg-leave-to { opacity: 0; }

.bubble--typing {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-soft);
}
.typing-dots { display: inline-flex; align-items: center; gap: 4px; }
.typing-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--muted);
}
.typing-text { font-size: 14px; }

@media (prefers-reduced-motion: no-preference) {
  /* 답변이 뜨면 카드 3개가 차례로 fade-up (마운트 기준 stagger) */
  .chatbot-preview .preview-card { animation: rise 0.5s ease-out both; }
  .chatbot-preview .preview-card:nth-child(1) { animation-delay: 0.05s; }
  .chatbot-preview .preview-card:nth-child(3) { animation-delay: 0.29s; }
  /* 카드 2 = 추천(주황) 카드: 등장(delay 0.17s) 뒤 가벼운 float 시작 */
  .chatbot-preview .preview-card--accent {
    animation:
      rise 0.5s ease-out 0.17s both,
      float-accent 2.8s ease-in-out 1.1s infinite;
  }
  .typing-dots i { animation: typing-bounce 1.2s ease-in-out infinite; }
  .typing-dots i:nth-child(2) { animation-delay: 0.15s; }
  .typing-dots i:nth-child(3) { animation-delay: 0.30s; }
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
  background: var(--glass);
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
  color: var(--teal-ink);
  font-size: 13px;
  font-weight: 700;
}

.ranking-more:hover { color: var(--teal); }

.live-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: var(--surface);
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
  background: var(--surface);
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
.trend--new { background: var(--teal-soft); color: var(--teal-ink); }

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
  background: var(--surface-inverse) !important;
  font-size: 16px !important;
}

.presence-label {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--ink-3);
  background: var(--surface);
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
}

/* 동시편집을 암시하는 초록 presence 점 — 랭킹 '실시간 집계' 점과 같은 pulse(숨쉬기) 효과로 통일 */
.live-now {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 7px;
  vertical-align: middle;
  border-radius: 50%;
  background: var(--success);
  animation: pulse 1.6s ease-in-out infinite;
}
@media (prefers-reduced-motion: reduce) {
  .live-dot, .live-now { animation: none; }
}

/* 협업 아바타 — 섹션이 뷰포트에 들어오면(is-in) 차례로 pop-in.
   reduced-motion 이면 이 블록 자체가 비활성 → 처음부터 보임(JS는 is-in 만 토글). */
@keyframes pop-in {
  from { opacity: 0; transform: scale(0.9); }
  to   { opacity: 1; transform: scale(1); }
}
@media (prefers-reduced-motion: no-preference) {
  .co-edit-preview .presence .avatar { opacity: 0; }
  .co-edit-preview.is-in .presence .avatar { animation: pop-in 0.55s ease-out forwards; }
  .co-edit-preview.is-in .presence .avatar:nth-child(1) { animation-delay: 0.00s; }
  .co-edit-preview.is-in .presence .avatar:nth-child(2) { animation-delay: 0.13s; }
  .co-edit-preview.is-in .presence .avatar:nth-child(3) { animation-delay: 0.26s; }
  .co-edit-preview.is-in .presence .avatar:nth-child(4) { animation-delay: 0.39s; }
}
</style>

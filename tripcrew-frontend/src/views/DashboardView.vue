<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container dashboard">
      <!-- Welcome -->
      <section class="welcome">
        <div>
          <h1 class="welcome__title">안녕하세요, 민지 님 👋</h1>
          <p class="welcome__sub">
            다음 여행까지 <strong>D-12</strong> · 여수 2박3일 계획이 진행 중입니다.
          </p>
        </div>
        <BaseButton variant="primary" size="lg">+ 새 계획 만들기</BaseButton>
      </section>

      <!-- Active plans -->
      <section class="block">
        <div class="block__head">
          <h2 class="t-h2">진행 중인 여행 계획</h2>
          <span class="t-mono muted">{{ plans.length }}개</span>
        </div>

        <div class="plan-grid">
          <article v-for="p in plans" :key="p.id" class="plan-card" @click="$router.push(`/plans/${p.id}/edit`)">
            <div class="plan-card__top">
              <span :class="['status-chip', `status--${p.statusKey}`]">{{ p.status }}</span>
              <span v-if="p.dday" class="dday">D-{{ p.dday }}</span>
            </div>
            <h3 class="plan-card__title">{{ p.title }}</h3>
            <p class="plan-card__meta">{{ p.dates }} · {{ p.members }}</p>

            <div class="plan-card__members">
              <div v-for="m in p.avatars" :key="m.letter" class="avatar avatar--sm" :style="{ background: m.color }">{{ m.letter }}</div>
              <div v-if="p.extra" class="avatar avatar--sm avatar--more">+{{ p.extra }}</div>
            </div>

            <div class="progress">
              <div class="progress__bar">
                <div class="progress__fill" :style="{ width: p.progress + '%' }"></div>
              </div>
              <span class="progress__label">{{ p.progress }}%</span>
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

            <ol class="ranking-list">
              <li v-for="(item, i) in ranking" :key="item.id">
                <span class="rank-no">{{ i + 1 }}</span>
                <div class="rank-info">
                  <strong>{{ item.name }}</strong>
                  <span class="t-caption">{{ item.region }}</span>
                </div>
                <span :class="['trend', `trend--${item.trend}`]">
                  <template v-if="item.trend === 'up'">▲ {{ item.delta }}</template>
                  <template v-else-if="item.trend === 'down'">▼ {{ item.delta }}</template>
                  <template v-else>─</template>
                </span>
              </li>
            </ol>
          </section>

          <!-- Activity -->
          <section class="block block--soft">
            <div class="block__head">
              <h2 class="t-h2">최근 활동</h2>
            </div>
            <ul class="activity">
              <li>
                <div class="activity-dot"></div>
                <div>
                  <p>지원 님이 여수 계획에 <strong>'케이블카'</strong> 추가</p>
                  <span class="t-caption">5분 전</span>
                </div>
              </li>
              <li>
                <div class="activity-dot activity-dot--success"></div>
                <div>
                  <p>동선 최적화 완료 · <strong>이동시간 -32%</strong></p>
                  <span class="t-caption">12분 전</span>
                </div>
              </li>
              <li>
                <div class="activity-dot activity-dot--coral"></div>
                <div>
                  <p>강릉 후기 <strong>★ 4점</strong> 작성</p>
                  <span class="t-caption">어제</span>
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
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const plans = [
  {
    id: 42, title: '여수 2박3일 바다 위주', dates: '2026.06.04 — 06.06',
    members: '5명 공동 편집', statusKey: 'active', status: '진행 중', dday: 12,
    progress: 65,
    avatars: [{ letter: '민', color: 'var(--teal)' }, { letter: '지', color: 'var(--coral)' }, { letter: '현', color: 'var(--violet)' }],
    extra: 2
  },
  {
    id: 41, title: '강릉 1박2일 카페 투어', dates: '미정 · 혼자',
    members: '혼자', statusKey: 'draft', status: '초안', dday: null,
    progress: 22,
    avatars: [{ letter: '민', color: 'var(--teal)' }],
    extra: 0
  }
]

const recommendations = [
  { id: 1, name: '통영 동피랑', rating: 4.6, tag: '자연' },
  { id: 2, name: '속초 영금정', rating: 4.5, tag: '자연' },
  { id: 3, name: '남해 독일마을', rating: 4.4, tag: '문화' },
  { id: 4, name: '가평 자라섬', rating: 4.3, tag: '자연' }
]

const ranking = [
  { id: 1, name: '오동도 동백숲', region: '전남 여수', trend: 'up', delta: 2 },
  { id: 2, name: '강릉 안목해변', region: '강원 강릉', trend: 'same' },
  { id: 3, name: '제주 성산일출봉', region: '제주 서귀포', trend: 'down', delta: 1 },
  { id: 4, name: '부산 감천문화마을', region: '부산 사하', trend: 'up', delta: 1 },
  { id: 5, name: '통영 동피랑', region: '경남 통영', trend: 'same' }
]
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

/* Plan cards */
.plan-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.plan-card {
  background: linear-gradient(135deg, var(--teal-tint) 0%, white 60%);
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
}

.plan-card__meta {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 16px;
}

.plan-card__members {
  display: flex;
  margin-bottom: 16px;
}

.plan-card__members .avatar {
  margin-left: -8px;
  border: 2px solid white;
}

.plan-card__members .avatar:first-child { margin-left: 0; }

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 12px;
  flex-shrink: 0;
}

.avatar--sm { width: 28px; height: 28px; font-size: 12px; }
.avatar--more { background: var(--ink-2) !important; }

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
  background: var(--teal);
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

.ranking-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 10px;
  background: white;
  transition: background 0.15s;
  cursor: pointer;
}

.ranking-list li:hover {
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

.activity p {
  font-size: 14px;
  color: var(--ink);
  margin-bottom: 2px;
}
</style>

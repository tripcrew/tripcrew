<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container my-plans-layout">
      <header class="page-header">
        <h1 class="t-h1">내 여행 계획</h1>
        <BaseButton variant="primary" size="lg">＋ 새 계획 만들기</BaseButton>
      </header>

      <!-- Tabs -->
      <div class="tabs-row">
        <div class="tabs">
          <button :class="['tab', { active: tab === 'all' }]" @click="tab = 'all'">
            전체 <span class="t-mono">12</span>
          </button>
          <button :class="['tab', { active: tab === 'active' }]" @click="tab = 'active'">
            진행 중 <span class="t-mono">2</span>
          </button>
          <button :class="['tab', { active: tab === 'draft' }]" @click="tab = 'draft'">
            초안 <span class="t-mono">1</span>
          </button>
          <button :class="['tab', { active: tab === 'done' }]" @click="tab = 'done'">
            완료 <span class="t-mono">9</span>
          </button>
        </div>

        <div class="tabs-controls">
          <div class="search-bar">
            <span>🔍</span>
            <input type="text" placeholder="계획 검색" />
          </div>
          <select class="sort-select">
            <option>최근 수정순</option>
            <option>이름순</option>
            <option>D-day 임박순</option>
          </select>
        </div>
      </div>

      <!-- Active -->
      <section class="section-block">
        <h2 class="t-h2 section-title">진행 중 <span class="muted">2개</span></h2>
        <div class="plans-grid">
          <article v-for="p in activePlans" :key="p.id" class="plan-card" @click="$router.push(`/plans/${p.id}/edit`)">
            <div class="plan-card__top">
              <span :class="['status-chip', `status--${p.statusKey}`]">{{ p.status }}</span>
              <span v-if="p.dday" class="dday">D-{{ p.dday }}</span>
            </div>
            <span class="updated t-caption">수정 {{ p.updated }}</span>
            <h3>{{ p.title }}</h3>
            <p class="meta">{{ p.dates }}</p>

            <div class="card-footer">
              <div class="members">
                <div v-for="m in p.members" :key="m.letter" class="avatar avatar--sm" :style="{ background: m.color }">{{ m.letter }}</div>
                <div v-if="p.extra" class="avatar avatar--sm avatar--more">+{{ p.extra }}</div>
              </div>
              <span class="t-caption">{{ p.label }}</span>
            </div>
          </article>
        </div>
      </section>

      <!-- Done -->
      <section class="section-block">
        <div class="block-head">
          <h2 class="t-h2 section-title">완료 <span class="muted">9개</span></h2>
          <button class="link-teal">캘린더 보기 →</button>
        </div>

        <div class="plans-grid">
          <article v-for="p in donePlans" :key="p.id" class="plan-card plan-card--done">
            <div class="plan-card__top">
              <span class="status-chip status--done">완료</span>
              <span v-if="p.rating" class="rating">★ {{ p.rating }}</span>
            </div>
            <h3>{{ p.title }}</h3>
            <p class="meta">{{ p.dates }} · {{ p.label }}</p>

            <div class="card-footer">
              <a v-if="p.rating" href="#" class="link-teal">후기</a>
              <a v-else href="#" class="link-muted">후기 미작성</a>
            </div>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const tab = ref('all')

const activePlans = [
  {
    id: 42, title: '여수 2박3일 바다 위주', dates: '2026.06.04 — 06.06',
    statusKey: 'active', status: '진행 중', dday: 12, updated: '12분 전', label: '5명 공동 편집',
    members: [{ letter: '민', color: 'var(--teal)' }, { letter: '지', color: 'var(--coral)' }, { letter: '현', color: 'var(--violet)' }],
    extra: 2
  },
  {
    id: 41, title: '강릉 1박2일 카페 투어', dates: '날짜 미정 · 혼자',
    statusKey: 'draft', status: '초안', dday: null, updated: '2주 전', label: '혼자',
    members: [{ letter: '민', color: 'var(--teal)' }],
    extra: 0
  }
]

const donePlans = [
  { id: 35, title: '제주 3박4일 가족여행', dates: '2026.04.10 — 04.13', label: '4명', rating: 4.5 },
  { id: 34, title: '강릉 당일치기', dates: '2026.03.22', label: '2명', rating: null },
  { id: 33, title: '부산 야경 데이트', dates: '2026.02.14', label: '2명', rating: 5.0 }
]
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

/* Tabs */
.tabs-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.tabs {
  display: flex;
  background: white;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 4px;
  gap: 2px;
}

.tab {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-soft);
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.15s;
}

.tab .t-mono {
  font-size: 11px;
  background: var(--bg-2);
  padding: 2px 7px;
  border-radius: 999px;
  color: var(--ink-3);
}

.tab.active {
  background: var(--teal);
  color: white;
}

.tab.active .t-mono {
  background: rgba(255,255,255,0.2);
  color: white;
}

.tabs-controls {
  display: flex;
  gap: 8px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 0 14px;
  width: 240px;
}

.search-bar input {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  padding: 10px 0;
  font-size: 14px;
}

.sort-select {
  background: white;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
}

/* Sections */
.section-block {
  margin-bottom: 40px;
}

.block-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  margin-bottom: 16px;
}

.muted { color: var(--ink-soft); font-weight: 500; }

.link-teal {
  color: var(--teal);
  font-weight: 600;
  font-size: 14px;
}

.link-muted {
  color: var(--ink-soft);
  font-size: 13px;
}

/* Plan cards */
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

.plan-card--done {
  background: var(--bg-soft);
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
.status--draft { background: var(--bg-2); color: var(--ink-3); }
.status--done { background: var(--success); color: white; }

.dday {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 800;
  color: var(--coral);
}

.rating {
  color: var(--warning);
  font-weight: 700;
  font-size: 13px;
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

.members {
  display: flex;
}

.members .avatar {
  margin-left: -6px;
  border: 2px solid white;
}

.members .avatar:first-child { margin-left: 0; }

.avatar {
  width: 26px; height: 26px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 11px;
  flex-shrink: 0;
}

.avatar--more {
  background: var(--ink-2) !important;
}
</style>

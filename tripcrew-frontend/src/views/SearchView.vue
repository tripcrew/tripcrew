<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container search-layout">
      <!-- Filter sidebar -->
      <aside class="filter-panel">
        <div class="filter-head">
          <h2 class="t-h2">필터</h2>
          <button class="link-muted">초기화</button>
        </div>

        <div class="filter-group">
          <h4>시·도</h4>
          <select class="filter-select">
            <option>전라남도</option>
            <option>강원도</option>
            <option>제주특별자치도</option>
          </select>
        </div>

        <div class="filter-group">
          <h4>시·군·구</h4>
          <select class="filter-select">
            <option>여수시</option>
            <option>순천시</option>
            <option>광양시</option>
          </select>
        </div>

        <div class="filter-group">
          <h4>콘텐츠 타입</h4>
          <ul class="check-list">
            <li><label><input type="checkbox" checked /> 관광지 <span class="count">(124)</span></label></li>
            <li><label><input type="checkbox" checked /> 문화시설 <span class="count">(38)</span></label></li>
            <li><label><input type="checkbox" /> 음식점 <span class="count">(212)</span></label></li>
            <li><label><input type="checkbox" /> 숙박 <span class="count">(89)</span></label></li>
            <li><label><input type="checkbox" /> 쇼핑 <span class="count">(45)</span></label></li>
          </ul>
        </div>

        <div class="filter-group">
          <h4>평점</h4>
          <div class="chip-row">
            <button class="filter-chip active">전체</button>
            <button class="filter-chip">★ 4+</button>
            <button class="filter-chip">★ 4.5+</button>
          </div>
        </div>
      </aside>

      <!-- Results -->
      <section class="results">
        <div class="results-head">
          <div class="search-bar">
            <span class="search-icon">🔍</span>
            <input type="text" placeholder="관광지 이름 또는 키워드" />
          </div>
          <div class="view-toggle">
            <button class="active">목록</button>
            <button>지도</button>
          </div>
        </div>

        <div class="results-meta">
          <p>
            <strong>전남 여수시</strong> · 관광지 + 문화시설 = <strong>162개</strong>
            <span class="t-mono muted ml">TourAPI 조회 중...</span>
          </p>
          <p class="t-mono muted">3 / 6 cached (HIT) · 3 fetching from TourAPI... p50 5ms · p99 720ms</p>
        </div>

        <div class="cards-grid">
          <article v-for="a in attractions" :key="a.id" class="att-card" :class="{ 'is-skeleton': a.skeleton }" @click="!a.skeleton && $router.push(`/attractions/${a.id}`)">
            <div class="att-card__thumb">
              <div v-if="!a.skeleton" class="thumb-grad"></div>
              <div v-else class="skeleton skeleton--thumb"></div>
              <span v-if="a.cached" class="cache-badge">● CACHED</span>
            </div>
            <div class="att-card__body">
              <template v-if="!a.skeleton">
                <div class="rating-row">
                  <span class="rating">★ {{ a.rating }}</span>
                  <span class="t-caption">({{ a.reviewCount }})</span>
                </div>
                <h3>{{ a.name }}</h3>
                <p class="t-caption">{{ a.address }} · {{ a.note }}</p>
                <div class="tag-row">
                  <span v-for="t in a.tags" :key="t" class="chip chip--teal">{{ t }}</span>
                </div>
              </template>
              <template v-else>
                <div class="skeleton skeleton--line" style="width: 30%"></div>
                <div class="skeleton skeleton--line" style="width: 80%; height: 18px;"></div>
                <div class="skeleton skeleton--line" style="width: 60%"></div>
              </template>
            </div>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import AppHeader from '@/components/common/AppHeader.vue'

const attractions = [
  { id: 126508, name: '오동도 동백숲', address: '전남 여수시 수정동', note: '무료', rating: 4.7, reviewCount: 382, tags: ['자연', '동백'], cached: true },
  { id: 126509, name: '여수 해상케이블카', address: '자산공원 ↔ 돌산공원', note: '전망', rating: 4.6, reviewCount: 218, tags: ['자연', '전망'], cached: true },
  { id: 126510, name: '하멜등대', address: '여수 종포동', note: '무료', rating: 4.5, reviewCount: 95, tags: ['자연', '야경'], cached: true },
  { id: 126511, name: '돌산공원', address: '여수 돌산읍', note: '일몰', rating: 4.4, reviewCount: 156, tags: ['자연', '일몰'], cached: false, skeleton: true },
  { id: 126512, name: '여수 박물관', address: '여수 신월동', note: '실내', rating: 4.2, reviewCount: 67, tags: ['문화'], cached: false, skeleton: true },
  { id: 126513, name: '거북선대교', address: '여수 ↔ 돌산', note: '야경', rating: 4.6, reviewCount: 203, tags: ['문화', '야경'], cached: false, skeleton: true }
]
</script>

<style scoped>
.search-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  padding: 40px var(--space-6) 80px;
}

/* Filter */
.filter-panel {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 24px;
  align-self: start;
  position: sticky;
  top: 88px;
}

.filter-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.link-muted {
  font-size: 13px;
  color: var(--ink-soft);
  text-decoration: underline;
}

.filter-group {
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.filter-group:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.filter-group h4 {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-2);
  margin-bottom: 10px;
}

.filter-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line-2);
  border-radius: 8px;
  font-size: 14px;
  background: white;
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.check-list label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--ink-3);
  cursor: pointer;
}

.check-list input { accent-color: var(--teal); }

.count {
  color: var(--muted);
  font-size: 12px;
  margin-left: auto;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.filter-chip {
  padding: 6px 12px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-3);
  transition: all 0.15s;
}

.filter-chip.active {
  background: var(--teal);
  border-color: var(--teal);
  color: white;
}

/* Results */
.results-head {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 0 16px;
}

.search-bar input {
  flex: 1;
  padding: 12px 0;
  border: none;
  outline: none;
  background: none;
  font-size: 15px;
}

.view-toggle {
  display: flex;
  background: white;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 4px;
  gap: 2px;
}

.view-toggle button {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-soft);
}

.view-toggle .active {
  background: var(--teal);
  color: white;
}

.results-meta {
  background: var(--bg-2);
  padding: 14px 18px;
  border-radius: var(--r-md);
  margin-bottom: 20px;
}

.results-meta p {
  font-size: 14px;
  color: var(--ink-3);
}

.results-meta strong { color: var(--ink); }

.ml { margin-left: 12px; }

/* Cards */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.att-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}

.att-card:not(.is-skeleton):hover {
  transform: translateY(-2px);
  box-shadow: var(--sh-2);
  border-color: var(--teal);
}

.att-card__thumb {
  width: 100%;
  aspect-ratio: 16/10;
  position: relative;
  background: var(--bg-2);
}

.thumb-grad {
  width: 100%; height: 100%;
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
}

.cache-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: var(--success);
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 4px;
}

.att-card__body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating {
  color: var(--warning);
  font-weight: 700;
  font-size: 13px;
}

.att-card h3 {
  font-size: 16px;
  font-weight: 700;
}

.tag-row {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

/* Skeleton */
.skeleton {
  background: linear-gradient(90deg, var(--bg-2) 25%, var(--line) 50%, var(--bg-2) 75%);
  background-size: 200% 100%;
  animation: skeleton 1.5s infinite;
  border-radius: 4px;
}

.skeleton--thumb {
  width: 100%; height: 100%;
}

.skeleton--line {
  height: 13px;
  border-radius: 4px;
}

@keyframes skeleton {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.muted { color: var(--muted); }
</style>

<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container search-layout">
      <aside class="filter-panel">
        <div class="filter-head">
          <h2 class="t-h2">필터</h2>
          <button class="link-muted" type="button" @click="resetFilters">초기화</button>
        </div>

        <div class="filter-group">
          <h4>시·도</h4>
          <select v-model.number="filters.sidoCode" class="filter-select">
            <option :value="null">전체</option>
            <option v-for="sido in sidos" :key="sido.code" :value="sido.code">
              {{ sido.name }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <h4>시·군·구</h4>
          <select v-model.number="filters.gugunCode" class="filter-select" :disabled="guguns.length === 0">
            <option :value="null">전체</option>
            <option v-for="gugun in guguns" :key="gugun.code" :value="gugun.code">
              {{ gugun.name }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <h4>콘텐츠 타입</h4>
          <ul class="check-list">
            <li v-for="type in contentTypes" :key="type.id">
              <label>
                <input v-model="filters.contentTypeIds" type="checkbox" :value="type.id" />
                {{ type.name }}
              </label>
            </li>
          </ul>
        </div>

        <div class="filter-group">
          <h4>평점</h4>
          <div class="chip-row">
            <button class="filter-chip active" type="button">전체</button>
            <button class="filter-chip" type="button" disabled>★ 4+</button>
            <button class="filter-chip" type="button" disabled>★ 4.5+</button>
          </div>
        </div>
      </aside>

      <section class="results">
        <div class="results-head">
          <div class="search-bar">
            <span class="search-icon">⌕</span>
            <input v-model.trim="keyword" type="text" placeholder="관광지 이름 또는 키워드" />
          </div>
          <div class="view-toggle">
            <button class="active" type="button">목록</button>
            <button type="button" disabled>지도</button>
          </div>
        </div>

        <div class="results-meta">
          <p>
            <strong>{{ selectedRegionLabel }}</strong>
            <span v-if="selectedTypeLabel"> · {{ selectedTypeLabel }}</span>
            = <strong>{{ pageData.totalCount.toLocaleString() }}개</strong>
            <span v-if="isLoading" class="t-mono muted ml">조회 중...</span>
          </p>
          <p class="t-mono muted">
            page {{ pageData.page }} / {{ Math.max(pageData.totalPages, 1) }} · size {{ pageData.size }}
          </p>
        </div>

        <div v-if="errorMessage" class="empty-state">
          {{ errorMessage }}
        </div>

        <div v-else-if="isLoading" class="cards-grid">
          <article v-for="n in filters.size" :key="n" class="att-card is-skeleton">
            <div class="att-card__thumb">
              <div class="skeleton skeleton--thumb"></div>
            </div>
            <div class="att-card__body">
              <div class="skeleton skeleton--line" style="width: 30%"></div>
              <div class="skeleton skeleton--line" style="width: 80%; height: 18px;"></div>
              <div class="skeleton skeleton--line" style="width: 60%"></div>
            </div>
          </article>
        </div>

        <div v-else-if="attractions.length === 0" class="empty-state">
          조건에 맞는 관광지가 없습니다.
        </div>

        <div v-else class="cards-grid">
          <article
            v-for="a in attractions"
            :key="a.no"
            class="att-card"
            @click="$router.push(`/attractions/${a.no}`)"
          >
            <div class="att-card__thumb">
              <img v-if="a.imageUrl" :src="a.imageUrl" :alt="a.title" />
              <div v-else class="thumb-grad"></div>
            </div>
            <div class="att-card__body">
              <div class="rating-row">
                <span class="rating">TripCrew</span>
                <span class="t-caption">{{ a.sido }} {{ a.gugun }}</span>
              </div>
              <h3>{{ a.title }}</h3>
              <p class="t-caption">{{ a.address || '주소 정보 없음' }}</p>
              <div class="tag-row">
                <span v-if="a.contentType" class="chip chip--teal">{{ a.contentType }}</span>
                <span v-if="a.sido" class="chip">{{ a.sido }}</span>
              </div>
            </div>
          </article>
        </div>

        <nav v-if="pageData.totalPages > 1" class="pagination" aria-label="관광지 페이지">
          <button type="button" :disabled="pageData.page <= 1" @click="changePage(pageData.page - 1)">
            이전
          </button>
          <button
            v-for="pageNumber in visiblePages"
            :key="pageNumber"
            type="button"
            :class="{ active: pageNumber === pageData.page }"
            @click="changePage(pageNumber)"
          >
            {{ pageNumber }}
          </button>
          <button type="button" :disabled="pageData.page >= pageData.totalPages" @click="changePage(pageData.page + 1)">
            다음
          </button>
        </nav>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'

import { attractionApi } from '@/api/attractions'
import AppHeader from '@/components/common/AppHeader.vue'

const DEFAULT_PAGE_SIZE = 6

const keyword = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const attractions = ref([])
const pageData = reactive({
  page: 1,
  size: DEFAULT_PAGE_SIZE,
  totalCount: 0,
  totalPages: 0,
})
const filters = reactive({
  sidoCode: null,
  gugunCode: null,
  contentTypeIds: [],
  page: 1,
  size: DEFAULT_PAGE_SIZE,
})

const sidos = [
  { code: 1, name: '서울' },
  { code: 6, name: '부산' },
  { code: 31, name: '경기도' },
  { code: 32, name: '강원특별자치도' },
  { code: 38, name: '전라남도' },
  { code: 39, name: '제주도' },
]

const gugunsBySido = {
  1: [
    { code: 1, name: '강남구' },
    { code: 23, name: '종로구' },
  ],
  6: [
    { code: 16, name: '해운대구' },
    { code: 15, name: '중구' },
  ],
  31: [
    { code: 2, name: '고양시' },
    { code: 14, name: '시흥시' },
  ],
  32: [
    { code: 1, name: '강릉시' },
    { code: 6, name: '양구군' },
  ],
  38: [
    { code: 11, name: '순천시' },
    { code: 13, name: '여수시' },
  ],
  39: [
    { code: 3, name: '서귀포시' },
    { code: 4, name: '제주시' },
  ],
}

const contentTypes = [
  { id: 12, name: '관광지' },
  { id: 14, name: '문화시설' },
  { id: 15, name: '축제공연행사' },
  { id: 25, name: '여행코스' },
  { id: 28, name: '레포츠' },
  { id: 32, name: '숙박' },
  { id: 38, name: '쇼핑' },
  { id: 39, name: '음식점' },
]

let debounceTimer = null

const guguns = computed(() => gugunsBySido[filters.sidoCode] || [])
const selectedSido = computed(() => sidos.find((sido) => sido.code === filters.sidoCode))
const selectedGugun = computed(() => guguns.value.find((gugun) => gugun.code === filters.gugunCode))
const selectedRegionLabel = computed(() => {
  if (selectedSido.value && selectedGugun.value) return `${selectedSido.value.name} ${selectedGugun.value.name}`
  if (selectedSido.value) return selectedSido.value.name
  return '전국'
})
const selectedTypeLabel = computed(() => {
  if (filters.contentTypeIds.length === 0) return '전체 콘텐츠'
  return contentTypes
    .filter((type) => filters.contentTypeIds.includes(type.id))
    .map((type) => type.name)
    .join(' + ')
})
const visiblePages = computed(() => {
  const total = pageData.totalPages
  const current = pageData.page
  const start = Math.max(1, current - 2)
  const end = Math.min(total, start + 4)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})

function buildParams() {
  return {
    keyword: keyword.value || null,
    sidoCode: filters.sidoCode,
    gugunCode: filters.gugunCode,
    contentTypeIds: filters.contentTypeIds,
    page: filters.page,
    size: filters.size,
  }
}

async function loadAttractions() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const data = await attractionApi.search(buildParams())
    attractions.value = data.items || []
    pageData.page = data.page || filters.page
    pageData.size = data.size || filters.size
    pageData.totalCount = data.totalCount || 0
    pageData.totalPages = data.totalPages || 0
  } catch (error) {
    attractions.value = []
    pageData.totalCount = 0
    pageData.totalPages = 0
    errorMessage.value = error?.response?.data?.message || '관광지 목록을 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

function scheduleSearch() {
  window.clearTimeout(debounceTimer)
  debounceTimer = window.setTimeout(() => {
    filters.page = 1
    loadAttractions()
  }, 300)
}

function changePage(page) {
  if (page < 1 || page > pageData.totalPages || page === filters.page) return
  filters.page = page
  loadAttractions()
}

function resetFilters() {
  keyword.value = ''
  filters.sidoCode = null
  filters.gugunCode = null
  filters.contentTypeIds = []
  filters.page = 1
  loadAttractions()
}

watch(keyword, scheduleSearch)

watch(
  () => filters.sidoCode,
  () => {
    filters.gugunCode = null
    filters.page = 1
    loadAttractions()
  },
)

watch(
  () => [filters.gugunCode, [...filters.contentTypeIds]],
  () => {
    filters.page = 1
    loadAttractions()
  },
  { deep: true },
)

onMounted(loadAttractions)

onBeforeUnmount(() => {
  window.clearTimeout(debounceTimer)
})
</script>

<style scoped>
.search-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  padding: 40px var(--space-6) 80px;
}

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

.filter-chip:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

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

.view-toggle button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
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

.att-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-grad {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
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
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 260px;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  color: var(--ink-soft);
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 24px;
}

.pagination button {
  min-width: 38px;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: white;
  color: var(--ink-3);
  font-weight: 700;
}

.pagination button.active {
  background: var(--teal);
  border-color: var(--teal);
  color: white;
}

.pagination button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.skeleton {
  background: linear-gradient(90deg, var(--bg-2) 25%, var(--line) 50%, var(--bg-2) 75%);
  background-size: 200% 100%;
  animation: skeleton 1.5s infinite;
  border-radius: 4px;
}

.skeleton--thumb {
  width: 100%;
  height: 100%;
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

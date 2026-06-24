<template>
  <div class="page page-soft page-ambient">
    <AppHeader />

    <main class="container search-layout">
      <aside class="filter-panel">
        <div class="filter-head">
          <h2 class="t-h2">필터</h2>
          <button class="link-muted" type="button" @click="resetFilters">초기화</button>
        </div>

        <div class="filter-group">
          <h4>시·도</h4>
          <select v-model.number="filters.sidoCode" class="filter-select" :disabled="sidosLoading">
            <option :value="null">전체</option>
            <option v-for="sido in sidos" :key="sido.code" :value="sido.code">
              {{ sido.name }}
            </option>
          </select>
        </div>

        <div class="filter-group">
          <h4>시·군·구</h4>
          <select v-model.number="filters.gugunCode" class="filter-select" :disabled="gugunsLoading || guguns.length === 0">
            <option :value="null">전체</option>
            <option v-for="gugun in guguns" :key="gugun.code" :value="gugun.code">
              {{ gugun.name }}
            </option>
          </select>
          <p v-if="regionError" class="filter-error">{{ regionError }}</p>
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
            <button :class="['filter-chip', { active: filters.minRating === null }]" type="button" @click="setMinRating(null)">전체</button>
            <button :class="['filter-chip', { active: filters.minRating === 3.5 }]" type="button" @click="setMinRating(3.5)">★ 3.5+</button>
          </div>
        </div>
      </aside>

      <section class="results">
        <div class="results-head">
          <div class="search-bar">
            <span class="search-icon">⌕</span>
            <input v-model.trim="keyword" type="text" placeholder="관광지 이름 또는 키워드" />
          </div>
        </div>

        <div class="results-meta">
          <p>
            <strong>{{ selectedRegionLabel }}</strong>
            <span v-if="selectedTypeLabel"> · {{ selectedTypeLabel }}</span>
            <span v-if="filters.minRating !== null"> · ★ {{ filters.minRating }} 이상</span>
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
            @click="goToDetail(a.no)"
          >
            <div class="att-card__thumb">
              <img v-if="a.imageUrl" :src="a.imageUrl" :alt="cleanDisplayName(a.title)" />
              <div v-else class="thumb-grad"></div>
            </div>
            <div class="att-card__body">
              <div class="card-region">
                <span class="t-caption">{{ a.sido }} {{ a.gugun }}</span>
              </div>
              <div class="card-title-row">
                <h3>{{ cleanDisplayName(a.title) }}</h3>
                <button
                  class="card-like"
                  :class="{ liked: likeState(a).liked }"
                  type="button"
                  :disabled="likeBusyNo === a.no"
                  :aria-label="likeState(a).liked ? '찜 해제' : '찜하기'"
                  :title="likeState(a).liked ? '찜 해제' : '가보고 싶어요(찜)'"
                  @click.stop="toggleCardLike(a)"
                >
                  <span
                    class="card-like__heart"
                    :class="{ 'card-like__heart--pop': likeBounceNo === a.no }"
                    @animationend="likeBounceNo = null"
                  >{{ likeState(a).liked ? '♥' : '♡' }}</span>
                  <span class="card-like__count">{{ formatLikeCount(likeState(a).likeCount) }}</span>
                </button>
              </div>
              <p class="t-caption">{{ a.address || '주소 정보 없음' }}</p>
              <div class="card-footer">
                <div class="tag-row">
                  <span v-if="a.contentType" class="chip chip--teal">{{ a.contentType }}</span>
                  <span v-if="a.sido" class="chip">{{ a.sido }}</span>
                </div>
                <span class="review-stat" :class="{ 'review-stat--empty': !a.reviewCount }">
                  <template v-if="a.reviewCount">★ {{ Number(a.reviewAverage).toFixed(1) }}</template>
                  <template v-else>☆ 0.0</template>
                </span>
              </div>
            </div>
          </article>
        </div>

        <nav v-if="pageData.totalPages > 1" class="pagination" aria-label="관광지 페이지">
          <button type="button" :disabled="pageData.page <= 1" @click="goToPreviousPageGroup">
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
          <button type="button" :disabled="pageData.page >= pageData.totalPages" @click="goToNextPageGroup">
            다음
          </button>
        </nav>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { attractionApi } from '@/api/attractions'
import { attractionLikeApi } from '@/api/attractionLikes'
import { regionApi } from '@/api/regions'
import AppHeader from '@/components/common/AppHeader.vue'
import { useAuthStore } from '@/stores/auth'

const DEFAULT_PAGE_SIZE = 6
const PAGE_GROUP_SIZE = 5
const MIN_KEYWORD_LENGTH = 2

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const attractions = ref([])
const sidos = ref([])
const guguns = ref([])
const sidosLoading = ref(false)
const gugunsLoading = ref(false)
const regionError = ref('')
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
  minRating: null,
  page: 1,
  size: DEFAULT_PAGE_SIZE,
})

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
let suppressNextRouteLoad = false
let isApplyingRouteQuery = false
let gugunRequest = 0

const selectedSido = computed(() => sidos.value.find((sido) => sido.code === filters.sidoCode))
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
  const start = Math.floor((current - 1) / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE + 1
  const end = Math.min(total, start + PAGE_GROUP_SIZE - 1)
  return Array.from({ length: end - start + 1 }, (_, index) => start + index)
})

function buildParams() {
  const normalizedKeyword = keyword.value.trim()

  return {
    keyword: normalizedKeyword.length >= MIN_KEYWORD_LENGTH ? normalizedKeyword : null,
    sidoCode: filters.sidoCode,
    gugunCode: filters.gugunCode,
    contentTypeIds: filters.contentTypeIds,
    minRating: filters.minRating,
    page: filters.page,
    size: filters.size,
  }
}

function toPositiveNumber(value, fallback) {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback
}

function toNullableNumber(value) {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

function toNumberArray(value) {
  const values = Array.isArray(value) ? value : [value]
  return values.map((item) => Number(item)).filter((item) => Number.isInteger(item) && item > 0)
}

function toNullableRating(value) {
  const parsed = Number(value)
  return parsed === 3.5 ? parsed : null
}

function applyQueryToState(query) {
  keyword.value = typeof query.keyword === 'string' ? query.keyword : ''
  filters.sidoCode = toNullableNumber(query.sidoCode)
  filters.gugunCode = toNullableNumber(query.gugunCode)
  filters.contentTypeIds = toNumberArray(query.contentTypeIds)
  filters.minRating = toNullableRating(query.minRating)
  filters.page = toPositiveNumber(query.page, 1)
  filters.size = toPositiveNumber(query.size, DEFAULT_PAGE_SIZE)
}

async function restoreQueryAndLoad() {
  isApplyingRouteQuery = true
  await loadSidos()
  applyQueryToState(route.query)
  await loadGuguns(filters.sidoCode)
  await loadAttractions()
  await nextTick()
  isApplyingRouteQuery = false
}

async function loadSidos() {
  if (sidos.value.length > 0) return
  sidosLoading.value = true
  regionError.value = ''
  try {
    sidos.value = await regionApi.listSidos()
  } catch (error) {
    regionError.value = error?.response?.data?.message || '지역 목록을 불러오지 못했습니다.'
  } finally {
    sidosLoading.value = false
  }
}

async function loadGuguns(sidoCode) {
  const request = ++gugunRequest
  guguns.value = []
  if (!sidoCode) {
    gugunsLoading.value = false
    return
  }

  gugunsLoading.value = true
  regionError.value = ''
  try {
    const data = await regionApi.listGuguns(sidoCode)
    if (request === gugunRequest) guguns.value = data
  } catch (error) {
    if (request === gugunRequest) {
      regionError.value = error?.response?.data?.message || '시·군·구 목록을 불러오지 못했습니다.'
    }
  } finally {
    if (request === gugunRequest) gugunsLoading.value = false
  }
}

function buildRouteQuery() {
  const nextQuery = {}
  const normalizedKeyword = keyword.value.trim()

  if (normalizedKeyword) nextQuery.keyword = normalizedKeyword
  if (filters.sidoCode) nextQuery.sidoCode = String(filters.sidoCode)
  if (filters.gugunCode) nextQuery.gugunCode = String(filters.gugunCode)
  if (filters.contentTypeIds.length) {
    nextQuery.contentTypeIds = filters.contentTypeIds.map((typeId) => String(typeId))
  }
  if (filters.minRating !== null) nextQuery.minRating = String(filters.minRating)
  if (filters.page > 1) nextQuery.page = String(filters.page)
  if (filters.size !== DEFAULT_PAGE_SIZE) nextQuery.size = String(filters.size)

  return nextQuery
}

function queriesEqual(left, right) {
  return JSON.stringify(left) === JSON.stringify(right)
}

async function syncRouteQuery(replace = true) {
  const query = buildRouteQuery()
  if (queriesEqual(route.query, query)) return

  suppressNextRouteLoad = true
  await router[replace ? 'replace' : 'push']({ name: 'search', query })
}

async function loadAttractions() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const data = await attractionApi.search(buildParams())
    attractions.value = data.items || []
    loadLikeCounts(attractions.value)
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
  if (isApplyingRouteQuery) return

  window.clearTimeout(debounceTimer)
  debounceTimer = window.setTimeout(async () => {
    filters.page = 1
    await syncRouteQuery()
    loadAttractions()
  }, 300)
}

async function changePage(page) {
  if (page < 1 || page > pageData.totalPages || page === filters.page) return
  filters.page = page
  await syncRouteQuery(false)
  loadAttractions()
}

function goToPreviousPageGroup() {
  const currentGroupStart = Math.floor((pageData.page - 1) / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE + 1
  changePage(Math.max(1, currentGroupStart - PAGE_GROUP_SIZE))
}

function goToNextPageGroup() {
  const currentGroupStart = Math.floor((pageData.page - 1) / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE + 1
  changePage(Math.min(pageData.totalPages, currentGroupStart + PAGE_GROUP_SIZE))
}

async function resetFilters() {
  keyword.value = ''
  filters.sidoCode = null
  filters.gugunCode = null
  filters.contentTypeIds = []
  filters.minRating = null
  filters.page = 1
  await syncRouteQuery()
  loadAttractions()
}

function goToDetail(no) {
  router.push(`/attractions/${no}`)
}

function setMinRating(rating) {
  filters.minRating = rating
}

// 카드 찜(좋아요) — 배치 조회로 채우고, 하트 클릭 시 토글
const likeMap = ref({})
const likeBusyNo = ref(null)
const likeBounceNo = ref(null) // 방금 토글한 카드 — 하트 바운스용(animationend에 초기화)

function likeState(a) {
  return likeMap.value[a.no] || { likeCount: 0, liked: false }
}
function formatLikeCount(n) {
  return n > 999 ? '999+' : String(n || 0)
}

async function loadLikeCounts(items) {
  const nos = items.map((a) => a.no)
  if (nos.length === 0) return
  try {
    const rows = await attractionLikeApi.counts(nos)
    const map = {}
    rows.forEach((r) => {
      map[r.no] = { likeCount: r.likeCount, liked: r.liked }
    })
    likeMap.value = map
  } catch {
    likeMap.value = {}
  }
}

async function toggleCardLike(a) {
  if (!auth.isAuthenticated) {
    router.push({ path: '/auth', query: { mode: 'login', redirect: route.fullPath } })
    return
  }
  if (likeBusyNo.value) return
  likeBusyNo.value = a.no
  const cur = likeState(a)
  try {
    const res = cur.liked ? await attractionLikeApi.unlike(a.no) : await attractionLikeApi.like(a.no)
    likeMap.value = { ...likeMap.value, [a.no]: { likeCount: res.likeCount, liked: res.liked } }
    likeBounceNo.value = a.no
  } catch {
    // 실패 시 현재 상태 유지
  } finally {
    likeBusyNo.value = null
  }
}

function cleanDisplayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}

watch(keyword, scheduleSearch)

watch(
  () => filters.sidoCode,
  async () => {
    if (isApplyingRouteQuery) return

    filters.gugunCode = null
    filters.page = 1
    await loadGuguns(filters.sidoCode)
    await syncRouteQuery()
    loadAttractions()
  },
)

watch(
  () => [filters.gugunCode, [...filters.contentTypeIds], filters.minRating],
  async () => {
    if (isApplyingRouteQuery) return

    filters.page = 1
    await syncRouteQuery()
    loadAttractions()
  },
  { deep: true },
)

watch(
  () => route.query,
  () => {
    if (suppressNextRouteLoad) {
      suppressNextRouteLoad = false
      return
    }

    restoreQueryAndLoad()
  },
)

onMounted(restoreQueryAndLoad)

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
  background: var(--surface);
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
  background: var(--surface);
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
  background: var(--surface);
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
  perspective: 1200px;
}

.att-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
  cursor: pointer;
  transform-style: preserve-3d;
  transition: transform 0.35s cubic-bezier(0.2, 0.8, 0.2, 1), box-shadow 0.35s ease, border-color 0.35s ease;
}

.att-card__thumb {
  width: 100%;
  aspect-ratio: 16/10;
  position: relative;
  overflow: hidden;
  background: var(--bg-2);
}

.att-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.55s cubic-bezier(0.2, 0.8, 0.2, 1), filter 0.35s ease;
}

.att-card__thumb::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 45%, rgba(10, 38, 35, 0.2));
  opacity: 0;
  transition: opacity 0.35s ease;
  pointer-events: none;
}

@media (hover: hover) {
  .att-card:not(.is-skeleton):hover {
    transform: translateY(-7px) rotateX(2deg) rotateY(-1.5deg);
    box-shadow: 0 18px 34px rgba(24, 78, 72, 0.18);
    border-color: rgba(32, 139, 133, 0.65);
  }

  .att-card:not(.is-skeleton):hover .att-card__thumb img {
    transform: scale(1.07);
    filter: saturate(1.08);
  }

  .att-card:not(.is-skeleton):hover .att-card__thumb::after {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .att-card,
  .att-card__thumb img,
  .att-card__thumb::after {
    transition: none;
  }
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

.card-region {
  display: flex;
  align-items: center;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: auto;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.card-title-row h3 {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 카드 찜 버튼: 제목 라인 우측, 하트 + 개수 */
.card-like {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 9px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--bg-soft);
  color: var(--ink-soft);
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

.card-like:hover:not(:disabled) {
  border-color: var(--danger);
  color: var(--danger);
}

.card-like:disabled {
  opacity: 0.6;
  cursor: default;
}

.card-like__heart {
  font-size: 14px;
  color: var(--ink-soft);
  transition: color 0.15s;
}

/* 찜 토글 시 하트가 통통 튀는 피드백 (reduced-motion이면 비활성) */
@keyframes like-pop {
  0% { transform: scale(1); }
  35% { transform: scale(1.32); }
  62% { transform: scale(0.9); }
  100% { transform: scale(1); }
}
@media (prefers-reduced-motion: no-preference) {
  .card-like__heart--pop { animation: like-pop 0.4s ease-out; }
}

.card-like.liked {
  border-color: var(--danger);
  background: color-mix(in srgb, var(--danger) 8%, white);
  color: var(--danger);
}

.card-like.liked .card-like__heart {
  color: var(--danger);
}

.card-like__count {
  font-family: var(--font-mono);
}

.review-stat {
  font-size: 12px;
  font-weight: 700;
}

.review-stat {
  color: var(--warning);
}

.review-stat--empty { color: var(--muted); }

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
  background: var(--surface);
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
  background: var(--surface);
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

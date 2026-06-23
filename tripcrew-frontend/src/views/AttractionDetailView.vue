<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container detail-layout">
      <nav class="breadcrumb">
        <router-link to="/attractions" class="bc-link">관광지</router-link>
        › {{ attraction?.sido || '지역' }} › {{ attraction?.gugun || '전체' }} ›
        <strong>{{ cleanDisplayName(attraction?.title) || '상세' }}</strong>
      </nav>

      <div v-if="isLoading" class="detail-state">관광지 정보를 불러오는 중입니다.</div>
      <div v-else-if="errorMessage" class="detail-state">{{ errorMessage }}</div>

      <template v-else-if="attraction">
        <section class="gallery" :class="{ 'gallery--single': sideImages.length === 0 }">
          <div class="gallery__main">
            <img v-if="primaryImage" :src="primaryImage" :alt="cleanDisplayName(attraction.title)" />
            <div v-else class="gallery__placeholder">
              <span>TripCrew</span>
            </div>
          </div>
          <div v-if="sideImages.length" class="gallery__side">
            <div v-for="(image, index) in sideImages" :key="image" class="gallery__thumb">
              <img :src="image" :alt="`${cleanDisplayName(attraction.title)} 보조 이미지 ${index + 1}`" />
            </div>
          </div>
        </section>

        <div class="detail-grid">
          <article class="detail-main">
            <header class="detail-header">
              <div class="detail-title">
                <h1 class="t-h1">{{ cleanDisplayName(attraction.title) }}</h1>
                <p class="t-body" style="color: var(--ink-3); margin-top: 4px;">
                  {{ attraction.sido }} {{ attraction.gugun }} · {{ attraction.contentType || '관광지' }}
                </p>
              </div>
              <div class="header-actions">
                <button class="icon-action" type="button" aria-label="찜" title="찜">♡</button>
                <button class="icon-action" type="button" aria-label="공유" title="공유" @click="copyShareUrl">↗</button>
              </div>
            </header>

            <div class="rating-block">
              <div class="rating-num">
                <span class="big-rating">TripCrew</span>
                <span class="t-caption">공공 관광 데이터</span>
              </div>
              <div class="tag-row">
                <span v-if="attraction.contentType" class="chip chip--teal">{{ attraction.contentType }}</span>
                <span v-if="attraction.sido" class="chip">{{ attraction.sido }}</span>
                <span v-if="attraction.gugun" class="chip">{{ attraction.gugun }}</span>
              </div>
            </div>

            <p class="description">
              {{ attraction.overview || '상세 설명이 아직 제공되지 않았습니다.' }}
            </p>

            <dl class="detail-info">
              <div>
                <dt>주소</dt>
                <dd>{{ fullAddress || '주소 정보 없음' }}</dd>
              </div>
              <div>
                <dt>전화</dt>
                <dd>{{ attraction.tel || '전화번호 정보 없음' }}</dd>
              </div>
              <div v-if="attraction.homepage">
                <dt>홈페이지</dt>
                <dd v-html="attraction.homepage"></dd>
              </div>
            </dl>

            <div class="cta-row">
              <BaseButton variant="primary" size="lg" :disabled="planPanelLoading" @click="openPlanPanel">
                {{ planPanelLoading ? '계획 불러오는 중' : '+ 내 여행 계획에 추가' }}
              </BaseButton>
              <BaseButton variant="secondary" size="lg">지도에서 보기</BaseButton>
            </div>

            <section v-if="showPlanPanel" class="add-plan-panel">
              <div v-if="planOptions.length === 0" class="add-plan-empty">
                <strong>추가할 여행 계획이 없습니다.</strong>
                <p class="t-caption">먼저 여행 계획을 만든 뒤 관광지를 담아 주세요.</p>
                <BaseButton variant="secondary" size="sm" @click="$router.push('/plans')">계획 만들기</BaseButton>
              </div>
              <template v-else>
                <div class="field">
                  <label>여행 계획</label>
                  <select v-model="selectedPlanId">
                    <option v-for="plan in planOptions" :key="plan.id" :value="plan.id">
                      {{ plan.title || '제목 없음' }} · {{ formatDates(plan.startDate, plan.endDate) }}
                    </option>
                  </select>
                </div>
                <div class="field field--compact">
                  <label>Day</label>
                  <select v-model.number="visitDay">
                    <option v-for="day in selectedPlanDayOptions" :key="day" :value="day">
                      Day {{ day }}
                    </option>
                  </select>
                </div>
                <BaseButton variant="secondary" :disabled="addingToPlan" @click="addToPlan">
                  {{ addingToPlan ? '추가 중…' : '추가' }}
                </BaseButton>
              </template>
              <p v-if="planMessage" class="plan-message">{{ planMessage }}</p>
              <p v-if="planError" class="plan-error">{{ planError }}</p>
            </section>

            <section class="reviews-preview">
              <header class="block-head">
                <h2 class="t-h2">최근 후기</h2>
                <div class="reviews-head-right">
                  <span v-if="reviewSummary.count > 0" class="review-avg">
                    <span class="stars">{{ avgStars }}</span>
                    <strong>{{ reviewSummary.average.toFixed(1) }}</strong>
                    <span class="t-caption">· {{ reviewSummary.count }}개</span>
                  </span>
                  <router-link :to="`/attractions/${attraction.no}/reviews`" class="link-teal">모두 보기 →</router-link>
                </div>
              </header>

              <p v-if="reviewsLoading" class="t-caption review-empty">후기를 불러오는 중…</p>
              <p v-else-if="recentReviews.length === 0" class="t-caption review-empty">
                아직 후기가 없어요. 첫 후기를 남겨보세요!
              </p>

              <ul v-else class="review-list">
                <li v-for="r in recentReviews" :key="r.id" class="review-item">
                  <div class="avatar avatar--sm" :style="{ background: avatarColor(r.userId) }">
                    {{ avatarLetter(r.authorNickname) }}
                  </div>
                  <div class="review-body">
                    <div class="review-line">
                      <strong>{{ r.authorNickname }}</strong>
                      <span class="stars">{{ starText(r.rating) }}</span>
                      <span class="t-caption">· {{ formatDate(r.createdAt) }}</span>
                    </div>
                    <p v-if="r.content">{{ r.content }}</p>
                    <ReviewImages :urls="r.imageUrls || []" size="sm" :max="4" />
                  </div>
                </li>
              </ul>
            </section>
          </article>

          <aside class="detail-side">
            <section class="info-card">
              <header class="info-head">
                <h3>위치 정보</h3>
                <span class="t-mono">Tour data</span>
              </header>
              <div class="location-box">
                <strong>{{ fullAddress || '주소 정보 없음' }}</strong>
                <p class="t-caption">{{ attraction.sido }} {{ attraction.gugun }}</p>
              </div>
            </section>

            <section class="info-card">
              <header class="info-head">
                <h3>기본 정보</h3>
                <span class="t-mono">Tour data</span>
              </header>
              <ul class="ev-list">
                <li>
                  <div>
                    <strong>콘텐츠 타입</strong>
                    <p class="t-caption">{{ attraction.contentType || attraction.contentTypeId }}</p>
                  </div>
                </li>
                <li>
                  <div>
                    <strong>지역</strong>
                    <p class="t-caption">{{ attraction.sido }} {{ attraction.gugun }}</p>
                  </div>
                </li>
              </ul>
            </section>
          </aside>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { attractionApi } from '@/api/attractions'
import { reviewApi } from '@/api/reviews'
import { tripPlanApi } from '@/api/tripPlans'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ReviewImages from '@/components/review/ReviewImages.vue'

const route = useRoute()
const attraction = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')

// 최근 후기 미리보기(최신 3개) + 평점 요약
const recentReviews = ref([])
const reviewSummary = ref({ average: 0, count: 0 })
const reviewsLoading = ref(false)
const showPlanPanel = ref(false)
const planPanelLoading = ref(false)
const addingToPlan = ref(false)
const planOptions = ref([])
const selectedPlanId = ref(null)
const visitDay = ref(1)
const planMessage = ref('')
const planError = ref('')

const avgStars = computed(() => starText(Math.round(reviewSummary.value.average)))

const fullAddress = computed(() =>
  [attraction.value?.addr1, attraction.value?.addr2].filter(Boolean).join(' '),
)

const PALETTE = ['var(--violet)', 'var(--coral)', 'var(--info)', 'var(--teal)', 'var(--warning)']
function avatarColor(userId) {
  return PALETTE[(userId || 0) % PALETTE.length]
}
function avatarLetter(nickname) {
  return nickname ? nickname.charAt(0) : '?'
}
function starText(n) {
  const filled = Math.max(0, Math.min(5, n))
  return '★'.repeat(filled) + '☆'.repeat(5 - filled)
}
function formatDate(iso) {
  return iso ? iso.slice(0, 10) : ''
}

const galleryImages = computed(() =>
  [attraction.value?.firstImage1, attraction.value?.firstImage2]
    .map((image) => image?.trim())
    .filter(Boolean),
)

const primaryImage = computed(() => galleryImages.value[0] || '')
const sideImages = computed(() => galleryImages.value.slice(1))
const selectedPlan = computed(() =>
  planOptions.value.find((plan) => plan.id === selectedPlanId.value),
)
const selectedPlanDayOptions = computed(() => {
  const count = getPlanDayCount(selectedPlan.value)
  return Array.from({ length: count }, (_, index) => index + 1)
})

async function loadAttraction() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    attraction.value = await attractionApi.get(route.params.id)
  } catch (error) {
    attraction.value = null
    errorMessage.value = error?.response?.data?.message || '관광지 정보를 불러오지 못했습니다.'
  } finally {
    isLoading.value = false
  }
}

async function loadReviews() {
  reviewsLoading.value = true
  try {
    const res = await reviewApi.listByTarget('ATTRACTION', route.params.id, { page: 0, size: 3, sort: 'LATEST' })
    recentReviews.value = res.content || []
    if (res.summary) reviewSummary.value = res.summary
  } catch {
    recentReviews.value = []
    reviewSummary.value = { average: 0, count: 0 }
  } finally {
    reviewsLoading.value = false
  }
}

async function copyShareUrl() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    window.alert('링크를 복사했습니다.')
  } catch {
    window.alert('링크 복사에 실패했습니다.')
  }
}

async function openPlanPanel() {
  showPlanPanel.value = true
  planMessage.value = ''
  planError.value = ''
  if (planOptions.value.length > 0) return

  planPanelLoading.value = true
  try {
    planOptions.value = await tripPlanApi.list()
    selectedPlanId.value = planOptions.value[0]?.id ?? null
    visitDay.value = selectedPlanDayOptions.value[0] ?? 1
  } catch (error) {
    planError.value = error?.response?.data?.message || '여행 계획 목록을 불러오지 못했습니다.'
  } finally {
    planPanelLoading.value = false
  }
}

async function addToPlan() {
  if (!selectedPlanId.value || !attraction.value || addingToPlan.value) return
  addingToPlan.value = true
  planMessage.value = ''
  planError.value = ''

  try {
    const allowedDays = selectedPlanDayOptions.value
    if (!allowedDays.includes(visitDay.value)) {
      visitDay.value = allowedDays[0] ?? 1
    }
    await tripPlanApi.addPlace(selectedPlanId.value, {
      attractionId: attraction.value.no,
      visitDay: visitDay.value || null,
      memo: null,
    })
    planMessage.value = '여행 계획에 추가했습니다.'
  } catch (error) {
    planError.value = error?.response?.data?.message || '여행 계획에 추가하지 못했습니다.'
  } finally {
    addingToPlan.value = false
  }
}

function formatDates(start, end) {
  if (!start && !end) return '날짜 미정'
  if (start && end) return `${start} ~ ${end}`
  return start || end
}

function cleanDisplayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}

function getPlanDayCount(plan) {
  if (!plan?.startDate || !plan?.endDate) return 1
  const start = new Date(plan.startDate)
  const end = new Date(plan.endDate)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end < start) return 1
  return Math.floor((end - start) / 86400000) + 1
}

watch(selectedPlanId, () => {
  if (!selectedPlanDayOptions.value.includes(visitDay.value)) {
    visitDay.value = selectedPlanDayOptions.value[0] ?? 1
  }
})

function loadAll() {
  loadAttraction()
  loadReviews()
}

watch(() => route.params.id, loadAll)
onMounted(loadAll)
</script>

<style scoped>
.detail-layout {
  padding: 32px var(--space-6) 80px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 20px;
}

.breadcrumb strong {
  color: var(--ink);
}

.bc-link {
  color: var(--ink-soft);
  transition: color 0.15s;
}

.bc-link:hover {
  color: var(--teal);
  text-decoration: underline;
}

.detail-state {
  display: grid;
  place-items: center;
  min-height: 320px;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  color: var(--ink-soft);
  font-weight: 700;
}

.gallery {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(220px, 1fr);
  gap: 12px;
  margin-bottom: 32px;
  align-items: stretch;
}

.gallery--single {
  grid-template-columns: 1fr;
}

.gallery__main,
.gallery__thumb {
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
  position: relative;
  overflow: hidden;
  min-width: 0;
  min-height: 0;
}

.gallery__main {
  border-radius: var(--r-lg);
  height: clamp(280px, 31vw, 420px);
}

.gallery__main img,
.gallery__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.gallery__side {
  display: grid;
  grid-auto-rows: minmax(0, 1fr);
  gap: 12px;
  height: clamp(280px, 31vw, 420px);
}

.gallery__thumb {
  border-radius: var(--r-md);
}

.gallery__placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: rgba(15, 23, 42, 0.6);
  color: white;
  font-weight: 700;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 32px;
  align-items: start;
}

.detail-main {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 32px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.detail-title {
  min-width: 0;
  flex: 1;
}

.detail-title h1 {
  overflow-wrap: anywhere;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.icon-action {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 10px;
  color: var(--ink-2);
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  transition: background 0.15s, border-color 0.15s, transform 0.15s;
}

.icon-action:hover {
  background: var(--bg-2);
  border-color: var(--line-2);
  transform: translateY(-1px);
}

.rating-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line);
}

.rating-num {
  display: flex;
  align-items: center;
  gap: 8px;
}

.big-rating {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 800;
  color: var(--warning);
}

.tag-row { display: flex; flex-wrap: wrap; gap: 6px; }

.description {
  font-size: 15px;
  line-height: 1.7;
  color: var(--ink-2);
  margin-bottom: 28px;
}

.detail-info {
  display: grid;
  gap: 10px;
  margin-bottom: 28px;
}

.detail-info div {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 12px;
  padding: 12px 14px;
  background: var(--bg-soft);
  border-radius: 10px;
}

.detail-info dt {
  color: var(--ink-soft);
  font-size: 13px;
}

.detail-info dd {
  color: var(--ink-2);
  font-size: 14px;
}

.link-teal {
  color: var(--teal);
  font-weight: 600;
}

.cta-row {
  display: flex;
  gap: 10px;
  margin-bottom: 32px;
}

.add-plan-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 96px auto;
  gap: 10px;
  align-items: end;
  padding: 16px;
  margin-bottom: 32px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 12px;
}

.add-plan-empty {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.add-plan-empty strong {
  font-size: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  color: var(--ink-3);
  font-size: 13px;
  font-weight: 700;
}

.field select,
.field input {
  width: 100%;
  height: 40px;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 0 12px;
  background: white;
  font: inherit;
}

.field--compact {
  min-width: 84px;
}

.plan-message,
.plan-error {
  grid-column: 1 / -1;
  font-size: 13px;
  font-weight: 700;
}

.plan-message { color: var(--success); }
.plan-error { color: var(--coral); }

.reviews-preview {
  border-top: 1px solid var(--line);
  padding-top: 24px;
}

.block-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.review-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: var(--bg-soft);
  border-radius: 10px;
}

.review-item strong {
  font-size: 14px;
  font-weight: 700;
}

.review-item p {
  font-size: 14px;
  color: var(--ink-2);
  margin-top: 4px;
  white-space: pre-wrap;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-body { flex: 1; min-width: 0; }

.review-line {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.review-line .stars { color: var(--warning); font-size: 13px; }

.reviews-head-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.review-avg {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.review-avg .stars { color: var(--warning); font-size: 13px; }
.review-avg strong { font-weight: 800; color: var(--ink); }

.review-empty {
  padding: 16px 0;
  color: var(--ink-soft);
}

.avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 12px;
  flex-shrink: 0;
}

.detail-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
}

.info-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.info-head h3 {
  font-size: 14px;
  font-weight: 700;
}

.info-head .t-mono {
  font-size: 11px;
  color: var(--muted);
}

.location-box {
  padding: 16px;
  border-radius: 10px;
  background: var(--bg-soft);
}

.location-box strong {
  display: block;
  margin-bottom: 6px;
  font-family: var(--font-mono);
  font-size: 13px;
}

.ev-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ev-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: var(--bg-soft);
  border-radius: 8px;
}

.ev-list strong {
  font-size: 14px;
  font-weight: 700;
  display: block;
}

.ev-list p {
  font-size: 12px;
}

@media (max-width: 900px) {
  .gallery {
    grid-template-columns: 1fr;
  }

  .gallery__main,
  .gallery__side {
    height: auto;
  }

  .gallery__main {
    aspect-ratio: 16 / 10;
  }

  .gallery__side {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .gallery__thumb {
    aspect-ratio: 4 / 3;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-header {
    align-items: flex-start;
  }

  .add-plan-panel {
    grid-template-columns: 1fr;
  }
}
</style>

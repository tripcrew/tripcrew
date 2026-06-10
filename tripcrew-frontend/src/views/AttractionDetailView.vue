<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container detail-layout">
      <nav class="breadcrumb">
        관광지 › {{ attraction?.sido || '지역' }} › {{ attraction?.gugun || '전체' }} ›
        <strong>{{ attraction?.title || '상세' }}</strong>
      </nav>

      <div v-if="isLoading" class="detail-state">관광지 정보를 불러오는 중입니다.</div>
      <div v-else-if="errorMessage" class="detail-state">{{ errorMessage }}</div>

      <template v-else-if="attraction">
        <section class="gallery">
          <div class="gallery__main">
            <img v-if="attraction.firstImage1" :src="attraction.firstImage1" :alt="attraction.title" />
          </div>
          <div class="gallery__side">
            <div class="gallery__thumb">
              <img v-if="attraction.firstImage2" :src="attraction.firstImage2" :alt="`${attraction.title} 보조 이미지`" />
            </div>
            <div class="gallery__thumb"></div>
            <div class="gallery__thumb gallery__thumb--more">
              <span>TripCrew</span>
            </div>
          </div>
        </section>

        <div class="detail-grid">
          <article class="detail-main">
            <header class="detail-header">
              <div>
                <h1 class="t-h1">{{ attraction.title }}</h1>
                <p class="t-body" style="color: var(--ink-3); margin-top: 4px;">
                  {{ attraction.sido }} {{ attraction.gugun }} · {{ attraction.contentType || '관광지' }}
                </p>
              </div>
              <div class="header-actions">
                <button class="icon-action" type="button">♡ 찜</button>
                <button class="icon-action" type="button" @click="copyShareUrl">↗ 공유</button>
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
              <BaseButton variant="primary" size="lg">+ 내 여행 계획에 추가</BaseButton>
              <BaseButton variant="secondary" size="lg">지도에서 보기</BaseButton>
            </div>

            <section class="reviews-preview">
              <header class="block-head">
                <h2 class="t-h2">최근 후기</h2>
                <router-link :to="`/attractions/${attraction.no}/reviews`" class="link-teal">모두 보기 →</router-link>
              </header>
              <div class="review-item">
                <div class="avatar avatar--sm" style="background: var(--violet);">T</div>
                <div>
                  <strong>TripCrew</strong> <span class="t-caption">후기 기능 준비 중</span>
                  <p>관광지 후기 데이터가 연결되면 이 영역에 최근 후기가 표시됩니다.</p>
                </div>
              </div>
            </section>
          </article>

          <aside class="detail-side">
            <section class="info-card">
              <header class="info-head">
                <h3>위치 정보</h3>
                <span class="t-mono">Tour data</span>
              </header>
              <div class="location-box">
                <strong>{{ attraction.latitude }}, {{ attraction.longitude }}</strong>
                <p class="t-caption">{{ fullAddress || '주소 정보 없음' }}</p>
              </div>
            </section>

            <section class="info-card">
              <header class="info-head">
                <h3>기본 정보</h3>
                <span class="t-mono">contentId {{ attraction.contentId }}</span>
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
                    <strong>지역 코드</strong>
                    <p class="t-caption">{{ attraction.areaCode }} / {{ attraction.siGunGuCode }}</p>
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
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const route = useRoute()
const attraction = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')

const fullAddress = computed(() =>
  [attraction.value?.addr1, attraction.value?.addr2].filter(Boolean).join(' '),
)

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

async function copyShareUrl() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    window.alert('링크를 복사했습니다.')
  } catch {
    window.alert('링크 복사에 실패했습니다.')
  }
}

watch(() => route.params.id, loadAttraction)
onMounted(loadAttraction)
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
  grid-template-columns: 2fr 1fr;
  gap: 12px;
  margin-bottom: 32px;
  height: 380px;
}

.gallery__main,
.gallery__thumb {
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
  position: relative;
  overflow: hidden;
}

.gallery__main {
  border-radius: var(--r-lg);
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
  grid-template-rows: 1fr 1fr;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.gallery__thumb {
  border-radius: var(--r-md);
}

.gallery__thumb--more {
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
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.icon-action {
  padding: 8px 14px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.icon-action:hover { background: var(--bg-2); }

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
</style>

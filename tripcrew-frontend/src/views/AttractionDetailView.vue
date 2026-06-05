<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container detail-layout">
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        관광지 › 전남 › 여수시 › <strong>오동도 동백숲</strong>
      </nav>

      <!-- Gallery -->
      <section class="gallery">
        <div class="gallery__main"></div>
        <div class="gallery__side">
          <div class="gallery__thumb"></div>
          <div class="gallery__thumb"></div>
          <div class="gallery__thumb gallery__thumb--more">
            <span>+ 12장</span>
          </div>
        </div>
      </section>

      <div class="detail-grid">
        <!-- Main content -->
        <article class="detail-main">
          <header class="detail-header">
            <div>
              <h1 class="t-h1">오동도 동백숲</h1>
              <p class="t-body" style="color: var(--ink-3); margin-top: 4px;">
                전라남도 여수시 수정동 · 자연관광지
              </p>
            </div>
            <div class="header-actions">
              <button class="icon-action">♡ 찜</button>
              <button class="icon-action">↗ 공유</button>
            </div>
          </header>

          <div class="rating-block">
            <div class="rating-num">
              <span class="big-rating">★ 4.7</span>
              <span class="t-caption">후기 382개</span>
            </div>
            <div class="tag-row">
              <span class="chip chip--teal">자연관광지</span>
              <span class="chip">동백</span>
              <span class="chip">야경</span>
            </div>
          </div>

          <p class="description">
            여수 앞바다에 위치한 작은 섬으로, 약 3,000여 그루의 동백나무가 군락을 이루고 있어
            11월부터 4월까지 붉은 동백꽃이 만개합니다. 하멜등대와 자산공원으로 이어지는
            산책로가 인기...
            <a href="#" class="link-teal">더보기 →</a>
          </p>

          <div class="cta-row">
            <BaseButton variant="primary" size="lg">+ 내 여행 계획에 추가</BaseButton>
            <BaseButton variant="secondary" size="lg">지도에서 보기</BaseButton>
          </div>

          <!-- Reviews preview -->
          <section class="reviews-preview">
            <header class="block-head">
              <h2 class="t-h2">최근 후기</h2>
              <router-link to="/attractions/126508/reviews" class="link-teal">모두 보기 →</router-link>
            </header>
            <div class="review-item">
              <div class="avatar avatar--sm" style="background: var(--violet);">현</div>
              <div>
                <strong>현우</strong> <span class="t-caption">★★★★★ 5.0 · 3일 전</span>
                <p>동백꽃 시즌에 정말 예뻤어요. 산책로도 잘 정비되어 있고, 야간 조명도 인상적.</p>
              </div>
            </div>
          </section>
        </article>

        <!-- Sidebar -->
        <aside class="detail-side">
          <!-- Weather -->
          <section class="info-card">
            <header class="info-head">
              <h3>오늘 날씨</h3>
              <span class="t-mono">OpenWeatherMap</span>
            </header>
            <div class="weather-main">
              <div class="weather-temp">14°</div>
              <div>
                <p><strong>흐림</strong></p>
                <p class="t-caption">체감 12° · 강수 30%</p>
              </div>
            </div>
            <div class="sun-info">
              <span>☀ 일출 <strong>06:42</strong></span>
              <span>🌙 일몰 <strong>18:23</strong></span>
            </div>
            <div class="weather-week">
              <div class="day"><span>내일</span><span>☀</span><strong>18°</strong></div>
              <div class="day"><span>목</span><span>☀</span><strong>19°</strong></div>
              <div class="day"><span>금</span><span>☁</span><strong>15°</strong></div>
              <div class="day"><span>토</span><span>🌧</span><strong>12°</strong></div>
              <div class="day"><span>일</span><span>☁</span><strong>14°</strong></div>
            </div>
          </section>

          <!-- EV chargers -->
          <section class="info-card">
            <header class="info-head">
              <h3>주변 전기차 충전소</h3>
              <span class="t-mono">반경 2km · 8개</span>
            </header>
            <ul class="ev-list">
              <li>
                <div>
                  <strong>오동도 공영주차장</strong>
                  <p class="t-caption">급속 100kW · 2기 사용 가능</p>
                </div>
                <span class="chip chip--success">● 운영중</span>
              </li>
              <li>
                <div>
                  <strong>자산공원 주차장</strong>
                  <p class="t-caption">완속 7kW · 4기 / 1기 사용중</p>
                </div>
                <span class="chip chip--warning">● 일부</span>
              </li>
              <li>
                <div>
                  <strong>여수해양공원</strong>
                  <p class="t-caption">급속 50kW · 점검중</p>
                </div>
                <span class="chip chip--danger">● 점검</span>
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

/* Gallery */
.gallery {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
  margin-bottom: 32px;
  height: 380px;
}

.gallery__main {
  background: linear-gradient(135deg, var(--teal-soft), var(--coral-tint));
  border-radius: var(--r-lg);
}

.gallery__side {
  display: grid;
  grid-template-rows: 1fr 1fr;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.gallery__thumb {
  background: linear-gradient(135deg, var(--teal-tint), var(--bg-2));
  border-radius: var(--r-md);
  position: relative;
  overflow: hidden;
}

.gallery__thumb--more {
  display: grid;
  place-items: center;
  background: rgba(15, 23, 42, 0.6);
  color: white;
  font-weight: 700;
  cursor: pointer;
}

/* Detail grid */
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
  font-size: 24px;
  font-weight: 800;
  color: var(--warning);
}

.tag-row { display: flex; gap: 6px; }

.description {
  font-size: 15px;
  line-height: 1.7;
  color: var(--ink-2);
  margin-bottom: 28px;
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

/* Reviews preview */
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

/* Sidebar */
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

/* Weather */
.weather-main {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
}

.weather-temp {
  font-size: 42px;
  font-weight: 800;
  color: var(--teal-3);
  letter-spacing: -2px;
}

.sun-info {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: var(--bg-soft);
  border-radius: 10px;
  font-size: 13px;
  color: var(--ink-3);
  margin-bottom: 14px;
}

.weather-week {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
}

.day {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  background: var(--bg-soft);
  border-radius: 8px;
  font-size: 12px;
}

.day strong { font-size: 14px; }

/* EV list */
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

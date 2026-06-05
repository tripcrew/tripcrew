<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container reviews-layout">
      <nav class="breadcrumb">
        관광지 › 전남 › 여수시 › <strong>오동도 동백숲</strong> › 후기
      </nav>

      <div class="reviews-grid">
        <!-- Left: Write form -->
        <section class="write-form">
          <header class="form-head">
            <h2 class="t-h2">후기 작성</h2>
            <p class="t-caption">오동도 동백숲</p>
          </header>

          <div class="form-block">
            <label class="form-label">전체 만족도</label>
            <div class="star-input">
              <span v-for="n in 5" :key="n" :class="['star-btn', { active: n <= rating }]" @click="rating = n">★</span>
              <span class="rating-num">{{ rating.toFixed(1) }}</span>
            </div>
          </div>

          <div class="form-block">
            <label class="form-label">사진 첨부 <span class="t-caption">(최대 10장)</span></label>
            <div class="upload-area">
              <div class="upload-icon">📷</div>
              <p>여기로 드래그 또는 <a href="#" class="link-teal">파일 선택</a></p>
              <p class="t-mono muted">Presigned URL · 서버 거치지 않고 S3로 직접 업로드</p>
            </div>
          </div>

          <div class="form-block">
            <label class="form-label">후기 내용</label>
            <textarea
              v-model="content"
              class="review-textarea"
              placeholder="이 곳에서의 경험을 자유롭게 작성해주세요."
              rows="6"
            />
            <div class="textarea-foot">
              <span class="t-caption">최소 20자 이상 권장</span>
              <span class="t-mono">{{ content.length }} / 1000</span>
            </div>
          </div>

          <div class="form-actions">
            <BaseButton variant="secondary">임시 저장</BaseButton>
            <BaseButton variant="primary" full>후기 등록</BaseButton>
          </div>

          <p class="api-note t-mono">POST /api/reviews · /api/reviews/upload-url</p>
        </section>

        <!-- Right: Reviews list -->
        <section class="reviews-list">
          <header class="list-head">
            <div class="rating-summary">
              <div class="rating-big">
                <strong>4.7</strong>
                <div class="stars">★★★★★</div>
                <span class="t-caption">382 후기</span>
              </div>

              <div class="rating-bars">
                <div v-for="(b, i) in bars" :key="i" class="bar-row">
                  <span class="bar-label">{{ 5 - i }}★</span>
                  <div class="bar">
                    <div class="bar-fill" :style="{ width: b.pct + '%' }"></div>
                  </div>
                  <span class="bar-count">{{ b.count }}</span>
                </div>
              </div>
            </div>

            <div class="list-controls">
              <select class="sort-select">
                <option>도움순</option>
                <option>최신순</option>
                <option>평점 높은순</option>
                <option>평점 낮은순</option>
              </select>
            </div>
          </header>

          <ul class="reviews">
            <li v-for="r in reviews" :key="r.id" class="review-item">
              <header class="review-head">
                <div class="avatar" :style="{ background: r.color }">{{ r.letter }}</div>
                <div class="reviewer-info">
                  <strong>{{ r.author }}</strong>
                  <span class="t-caption">{{ r.level }}</span>
                </div>
                <div class="review-meta">
                  <span class="stars">{{ '★'.repeat(r.rating) + '☆'.repeat(5 - r.rating) }}</span>
                  <span class="rating-text">{{ r.rating.toFixed(1) }}</span>
                  <span class="t-caption">· {{ r.date }}</span>
                </div>
              </header>
              <p class="review-content">{{ r.content }}</p>
              <footer class="review-foot">
                <button class="helpful-btn">
                  👍 도움됐어요 <span>{{ r.helpful }}</span>
                </button>
                <button class="report-btn">신고</button>
              </footer>
            </li>
          </ul>

          <button class="load-more">더 보기</button>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const rating = ref(4)
const content = ref('동백꽃이 만개한 시기에 방문했어요. 산책로가 잘 정비되어 있어서 부모님 모시고 다녀오기에 정말 좋았습니다. 다만 주말이라 그런지 사람이 너무 많아서 케이블카는 거의 한 시간 기다렸어요.')

const bars = [
  { count: 275, pct: 72 },
  { count: 82, pct: 21 },
  { count: 19, pct: 5 },
  { count: 4, pct: 1 },
  { count: 2, pct: 1 }
]

const reviews = [
  {
    id: 1, author: '현우', letter: '현', color: 'var(--violet)', level: 'Lv.3 트레블러',
    rating: 5, date: '3일 전', helpful: 24,
    content: '동백꽃 시즌에 정말 예뻤어요. 산책로도 잘 정비되어 있고, 야간에 조명 켜지는 것도 인상적이었습니다. 케이블카까지 같이 보면 시간이 빠르게 가요.'
  },
  {
    id: 2, author: '지원', letter: '지', color: 'var(--coral)', level: 'Lv.5 어드벤처러',
    rating: 4, date: '1주 전', helpful: 18,
    content: '풍경은 좋은데 케이블카 대기가 1시간 넘어요. 평일에 가시는 걸 추천합니다. 동백숲 산책로는 1시간 정도면 충분합니다.'
  },
  {
    id: 3, author: '하늘', letter: '하', color: 'var(--info)', level: 'Lv.2 익스플로러',
    rating: 5, date: '2주 전', helpful: 12,
    content: '여수 여행 중 가장 좋았던 곳이에요. 아침 일찍 가면 사람도 많지 않고 사진 찍기 정말 좋습니다.'
  }
]
</script>

<style scoped>
.reviews-layout {
  padding: 32px var(--space-6) 80px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 24px;
}

.breadcrumb strong { color: var(--ink); }

.reviews-grid {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 24px;
  align-items: start;
}

/* Write form */
.write-form {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 28px;
  position: sticky;
  top: 88px;
}

.form-head {
  padding-bottom: 20px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 20px;
}

.form-head .t-caption {
  margin-top: 4px;
  color: var(--teal);
  font-weight: 600;
}

.form-block { margin-bottom: 24px; }

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-2);
  margin-bottom: 10px;
}

/* Star input */
.star-input {
  display: flex;
  align-items: center;
  gap: 6px;
}

.star-btn {
  font-size: 28px;
  color: var(--line-2);
  cursor: pointer;
  transition: transform 0.1s, color 0.15s;
  user-select: none;
}

.star-btn:hover { transform: scale(1.1); }
.star-btn.active { color: var(--warning); }

.rating-num {
  margin-left: 8px;
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--warning);
}

/* Upload area */
.upload-area {
  padding: 32px 24px;
  background: var(--bg-soft);
  border: 1.5px dashed var(--line-2);
  border-radius: 12px;
  text-align: center;
  transition: all 0.15s;
}

.upload-area:hover {
  border-color: var(--teal);
  background: var(--teal-tint);
}

.upload-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.upload-area p {
  font-size: 14px;
  color: var(--ink-3);
  margin-bottom: 4px;
}

.upload-area .muted {
  font-size: 11px;
  color: var(--muted);
  margin-top: 8px;
}

.link-teal { color: var(--teal); font-weight: 700; }

/* Textarea */
.review-textarea {
  width: 100%;
  padding: 14px;
  background: var(--bg-soft);
  border: 1px solid var(--line-2);
  border-radius: 10px;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  transition: all 0.15s;
}

.review-textarea:focus {
  outline: none;
  background: white;
  border-color: var(--teal);
  box-shadow: 0 0 0 2px var(--teal-soft);
}

.textarea-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: var(--ink-soft);
}

.textarea-foot .t-mono { color: var(--muted); }

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.api-note {
  margin-top: 16px;
  font-size: 11px;
  color: var(--muted);
  padding: 8px 12px;
  background: var(--bg-2);
  border-radius: 6px;
}

/* Reviews list */
.reviews-list {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 28px;
}

.list-head {
  padding-bottom: 24px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 24px;
}

.rating-summary {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  margin-bottom: 20px;
}

.rating-big {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px;
  background: var(--teal-tint);
  border-radius: 12px;
}

.rating-big strong {
  font-size: 48px;
  font-weight: 800;
  color: var(--teal-3);
  letter-spacing: -1.5px;
  line-height: 1;
}

.stars {
  color: var(--warning);
  font-size: 16px;
  letter-spacing: 1px;
}

.rating-bars {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bar-row {
  display: grid;
  grid-template-columns: 30px 1fr 40px;
  gap: 10px;
  align-items: center;
}

.bar-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--warning);
}

.bar {
  height: 8px;
  background: var(--bg-2);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: var(--warning);
  border-radius: 4px;
}

.bar-count {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--ink-soft);
  text-align: right;
}

.list-controls {
  display: flex;
  justify-content: flex-end;
}

.sort-select {
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
}

/* Review items */
.reviews {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 20px;
  background: var(--bg-soft);
  border-radius: 12px;
  transition: background 0.15s;
}

.review-item:hover { background: var(--teal-tint); }

.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.reviewer-info {
  flex: 1;
  min-width: 0;
}

.reviewer-info strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
}

.reviewer-info .t-caption {
  font-size: 11px;
  color: var(--coral);
  font-weight: 600;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--ink-soft);
}

.review-meta .stars { font-size: 13px; }

.rating-text {
  font-family: var(--font-mono);
  font-weight: 700;
  color: var(--warning);
}

.review-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--ink-2);
  margin-bottom: 12px;
}

.review-foot {
  display: flex;
  gap: 8px;
}

.helpful-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-3);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.15s;
}

.helpful-btn:hover {
  border-color: var(--teal);
  color: var(--teal);
}

.helpful-btn span {
  font-family: var(--font-mono);
  color: var(--muted);
}

.report-btn {
  padding: 6px 12px;
  font-size: 12px;
  color: var(--ink-soft);
}

.report-btn:hover { color: var(--danger); }

.load-more {
  width: 100%;
  margin-top: 24px;
  padding: 14px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-3);
}

.load-more:hover {
  background: white;
  border-color: var(--teal);
  color: var(--teal);
}

.muted { color: var(--muted); }
</style>

<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container reviews-layout">
      <nav class="breadcrumb">
        관광지 › <strong>{{ attractionTitle }}</strong> › 후기
      </nav>

      <div class="reviews-grid">
        <!-- Left: Write form -->
        <section class="write-form">
          <header class="form-head">
            <h2 class="t-h2">후기 작성</h2>
            <p class="t-caption">{{ attractionTitle }}</p>
          </header>

          <div class="form-block">
            <label class="form-label">전체 만족도</label>
            <div class="star-input">
              <span v-for="n in 5" :key="n" :class="['star-btn', { active: n <= rating }]" @click="rating = n">★</span>
              <span class="rating-num">{{ rating.toFixed(1) }}</span>
            </div>
          </div>

          <div class="form-block">
            <label class="form-label">후기 내용</label>
            <textarea
              v-model="content"
              class="review-textarea"
              placeholder="이 곳에서의 경험을 자유롭게 작성해주세요."
              rows="6"
              maxlength="1000"
            />
            <div class="textarea-foot">
              <span class="t-caption">최소 20자 이상 권장</span>
              <span class="t-mono">{{ content.length }} / 1000</span>
            </div>
          </div>

          <p v-if="formError" class="form-error">{{ formError }}</p>

          <div class="form-actions">
            <BaseButton variant="primary" full :disabled="submitting" @click="submitReview">
              {{ submitting ? '등록 중…' : '후기 등록' }}
            </BaseButton>
          </div>

          <p v-if="!isAuthenticated" class="api-note t-mono">로그인 후 후기를 작성할 수 있어요.</p>
        </section>

        <!-- Right: Reviews list -->
        <section class="reviews-list">
          <header class="list-head">
            <div class="rating-summary">
              <div class="rating-big">
                <strong>{{ averageRating.toFixed(1) }}</strong>
                <div class="stars">{{ averageStars }}</div>
                <span class="t-caption">{{ reviewCount }} 후기</span>
              </div>

              <div class="rating-bars">
                <div v-for="b in bars" :key="b.star" class="bar-row">
                  <span class="bar-label">{{ b.star }}★</span>
                  <div class="bar">
                    <div class="bar-fill" :style="{ width: b.pct + '%' }"></div>
                  </div>
                  <span class="bar-count">{{ b.count }}</span>
                </div>
              </div>
            </div>
          </header>

          <p v-if="loading" class="empty-note">불러오는 중…</p>
          <p v-else-if="listError" class="empty-note">{{ listError }}</p>
          <p v-else-if="reviewCount === 0" class="empty-note">아직 후기가 없어요. 첫 후기를 남겨보세요!</p>

          <ul v-else class="reviews">
            <li v-for="r in reviews" :key="r.id" class="review-item">
              <header class="review-head">
                <div class="avatar" :style="{ background: avatarColor(r.userId) }">{{ avatarLetter(r.authorNickname) }}</div>
                <div class="reviewer-info">
                  <strong>{{ r.authorNickname }}</strong>
                </div>
                <div class="review-meta">
                  <span class="stars">{{ starText(r.rating) }}</span>
                  <span class="rating-text">{{ r.rating.toFixed(1) }}</span>
                  <span class="t-caption">· {{ formatDate(r.createdAt) }}</span>
                </div>
              </header>
              <p class="review-content">{{ r.content }}</p>
              <footer class="review-foot">
                <button
                  v-if="r.userId !== currentUserId"
                  class="report-btn"
                  :disabled="reportedIds.includes(r.id)"
                  @click="openReport(r)"
                >
                  {{ reportedIds.includes(r.id) ? '신고됨' : '신고' }}
                </button>
                <span v-else class="own-tag">내 후기</span>
              </footer>
            </li>
          </ul>
        </section>
      </div>
    </main>

    <!-- 신고 모달 -->
    <div v-if="reportModal.open" class="modal-backdrop" @click.self="closeReport">
      <div class="modal">
        <h3 class="t-h3">후기 신고</h3>
        <p class="modal-sub t-caption">신고 사유를 선택해주세요. 관리자가 검토 후 조치합니다.</p>

        <div class="reason-list">
          <label v-for="opt in REPORT_REASONS" :key="opt.code" class="reason-item">
            <input type="radio" :value="opt.code" v-model="reportModal.reason" />
            <span>{{ opt.label }}</span>
          </label>
        </div>

        <textarea
          v-model="reportModal.detail"
          class="review-textarea"
          placeholder="상세 사유(선택)"
          rows="3"
          maxlength="500"
        />

        <p v-if="reportModal.error" class="form-error">{{ reportModal.error }}</p>

        <div class="modal-actions">
          <BaseButton variant="secondary" @click="closeReport">취소</BaseButton>
          <BaseButton variant="primary" :disabled="reportModal.submitting" @click="submitReport">
            {{ reportModal.submitting ? '신고 중…' : '신고하기' }}
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { reviewApi } from '@/api/reviews'
import { reportApi } from '@/api/reports'
import { attractionApi } from '@/api/attractions'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const TARGET_TYPE = 'ATTRACTION'
const targetId = computed(() => Number(route.params.id))

const attraction = ref(null)
const reviews = ref([])
const reportedIds = ref([])
const loading = ref(false)
const listError = ref('')

// 작성 폼
const rating = ref(5)
const content = ref('')
const submitting = ref(false)
const formError = ref('')

const isAuthenticated = computed(() => auth.isAuthenticated)
const currentUserId = computed(() => (auth.user ? auth.user.id : null))
const attractionTitle = computed(() => (attraction.value ? attraction.value.title : '관광지'))

const reviewCount = computed(() => reviews.value.length)
const averageRating = computed(() => {
  if (!reviews.value.length) return 0
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return sum / reviews.value.length
})
const averageStars = computed(() => starText(Math.round(averageRating.value)))
// 5★ ~ 1★ 분포
const bars = computed(() =>
  [5, 4, 3, 2, 1].map((star) => {
    const count = reviews.value.filter((r) => r.rating === star).length
    const pct = reviews.value.length ? Math.round((count / reviews.value.length) * 100) : 0
    return { star, count, pct }
  }),
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

async function loadAll() {
  loading.value = true
  listError.value = ''
  try {
    const [list, detail] = await Promise.all([
      reviewApi.listByTarget(TARGET_TYPE, targetId.value),
      attractionApi.get(targetId.value).catch(() => null),
    ])
    reviews.value = list
    attraction.value = detail
  } catch (e) {
    listError.value = '후기를 불러오지 못했어요.'
  } finally {
    loading.value = false
  }
}

function requireLogin() {
  if (!isAuthenticated.value) {
    router.push({ path: '/auth', query: { mode: 'login', redirect: route.fullPath } })
    return false
  }
  return true
}

async function submitReview() {
  if (!requireLogin()) return
  if (content.value.trim().length === 0) {
    formError.value = '후기 내용을 입력해주세요.'
    return
  }
  submitting.value = true
  formError.value = ''
  try {
    await reviewApi.create({
      targetType: TARGET_TYPE,
      targetId: targetId.value,
      rating: rating.value,
      content: content.value.trim(),
    })
    content.value = ''
    rating.value = 5
    await loadAll()
  } catch (e) {
    formError.value = e?.response?.data?.message || '후기 등록에 실패했어요.'
  } finally {
    submitting.value = false
  }
}

// 신고 모달
const REPORT_REASONS = [
  { code: 'SPAM', label: '스팸/도배' },
  { code: 'ABUSE', label: '욕설/비방' },
  { code: 'ADVERTISING', label: '광고/홍보' },
  { code: 'INAPPROPRIATE', label: '부적절한 내용' },
  { code: 'OTHER', label: '기타' },
]
const reportModal = ref({ open: false, reviewId: null, reason: 'SPAM', detail: '', submitting: false, error: '' })

function openReport(review) {
  if (!requireLogin()) return
  reportModal.value = { open: true, reviewId: review.id, reason: 'SPAM', detail: '', submitting: false, error: '' }
}
function closeReport() {
  reportModal.value.open = false
}
async function submitReport() {
  reportModal.value.submitting = true
  reportModal.value.error = ''
  try {
    await reportApi.create({
      targetType: 'REVIEW',
      targetId: reportModal.value.reviewId,
      reason: reportModal.value.reason,
      detail: reportModal.value.detail.trim() || null,
    })
    reportedIds.value = [...reportedIds.value, reportModal.value.reviewId]
    reportModal.value.open = false
  } catch (e) {
    reportModal.value.error = e?.response?.data?.message || '신고에 실패했어요.'
  } finally {
    reportModal.value.submitting = false
  }
}

onMounted(loadAll)
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

.form-error {
  margin-top: 12px;
  font-size: 13px;
  color: var(--danger);
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

.empty-note {
  padding: 32px 0;
  text-align: center;
  font-size: 14px;
  color: var(--ink-soft);
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
  white-space: pre-wrap;
}

.review-foot {
  display: flex;
  gap: 8px;
  align-items: center;
}

.report-btn {
  padding: 6px 12px;
  font-size: 12px;
  color: var(--ink-soft);
}

.report-btn:hover:not(:disabled) { color: var(--danger); }
.report-btn:disabled { color: var(--muted); cursor: default; }

.own-tag {
  font-size: 12px;
  color: var(--muted);
}

/* 신고 모달 */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: grid;
  place-items: center;
  z-index: 100;
  padding: 16px;
}

.modal {
  background: white;
  border-radius: var(--r-xl);
  padding: 28px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.2);
}

.modal-sub {
  margin: 8px 0 20px;
  color: var(--ink-soft);
}

.reason-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.reason-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
}

.reason-item:hover { border-color: var(--teal); }

.modal-actions {
  display: flex;
  gap: 8px;
  margin-top: 20px;
}

.modal-actions :deep(button) { flex: 1; }
</style>

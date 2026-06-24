<template>
  <div class="page page-soft page-ambient">
    <AppHeader />

    <main class="container wishlist-layout">
      <header class="wishlist-head">
        <div>
          <h1 class="t-h1">가보고 싶어요</h1>
          <p class="t-caption">찜한 관광지를 모아두고, 바로 여행 계획에 담아보세요.</p>
        </div>
        <span v-if="!loading && items.length" class="count-badge">{{ items.length }}곳</span>
      </header>

      <p v-if="loading" class="state-note">불러오는 중…</p>
      <p v-else-if="loadError" class="state-note">{{ loadError }}</p>

      <div v-else-if="items.length === 0" class="empty-box">
        <div class="empty-heart">♡</div>
        <p class="empty-title">아직 찜한 곳이 없어요</p>
        <p class="t-caption">관광지 상세에서 하트를 눌러 가보고 싶은 곳을 저장해보세요.</p>
        <BaseButton variant="primary" @click="goExplore">관광지 둘러보기</BaseButton>
      </div>

      <div v-else class="cards-grid">
        <article v-for="item in items" :key="item.no" class="wish-card">
          <div class="wish-card__thumb" @click="goToDetail(item.no)">
            <img v-if="item.imageUrl" :src="item.imageUrl" :alt="cleanDisplayName(item.title)" />
            <div v-else class="thumb-grad"></div>
            <button
              class="card-heart"
              type="button"
              :disabled="busyNo === item.no"
              aria-label="찜 해제"
              title="찜 해제"
              @click.stop="removeLike(item)"
            >♥</button>
          </div>

          <div class="wish-card__body">
            <div class="meta-row">
              <span class="stars">{{ starText(item.avgRating) }}</span>
              <span v-if="item.reviewCount > 0" class="rating-text">
                {{ item.avgRating.toFixed(1) }}
                <span class="t-caption">({{ item.reviewCount }})</span>
              </span>
              <span v-else class="t-caption">평점 없음</span>
            </div>

            <h3 class="wish-title" @click="goToDetail(item.no)">{{ cleanDisplayName(item.title) }}</h3>
            <p class="t-caption addr">{{ item.address || `${item.sido || ''} ${item.gugun || ''}` }}</p>

            <div class="tag-row">
              <span v-if="item.contentType" class="chip chip--teal">{{ item.contentType }}</span>
              <span class="chip chip--like">♥ {{ formatCount(item.likeCount) }}</span>
            </div>

            <div class="card-actions">
              <BaseButton variant="secondary" @click="goToDetail(item.no)">상세</BaseButton>
              <BaseButton variant="primary" @click="openAddToPlan(item)">계획에 담기</BaseButton>
            </div>
          </div>
        </article>
      </div>
    </main>

    <!-- 여행 계획에 담기 모달 -->
    <div v-if="planModal.open" class="modal-backdrop" @click.self="closePlanModal">
      <div class="modal">
        <h3 class="t-h3">여행 계획에 담기</h3>
        <p class="modal-sub t-caption">{{ cleanDisplayName(planModal.title) }}</p>

        <p v-if="planLoading" class="state-note">계획을 불러오는 중…</p>

        <template v-else-if="planOptions.length === 0">
          <p class="state-note">아직 만든 여행 계획이 없어요. 먼저 계획을 만들어주세요.</p>
          <div class="modal-actions">
            <BaseButton variant="secondary" @click="closePlanModal">닫기</BaseButton>
            <BaseButton variant="primary" @click="goToPlans">계획 만들러 가기</BaseButton>
          </div>
        </template>

        <template v-else>
          <div class="form-block">
            <label class="form-label">계획 선택</label>
            <select v-model="selectedPlanId" class="select-input">
              <option v-for="p in planOptions" :key="p.id" :value="p.id">{{ p.title }}</option>
            </select>
          </div>

          <div class="form-block">
            <label class="form-label">며칠째 일정</label>
            <select v-model.number="visitDay" class="select-input">
              <option v-for="d in dayOptions" :key="d" :value="d">{{ d }}일차</option>
            </select>
          </div>

          <p v-if="planModal.error" class="form-error">{{ planModal.error }}</p>
          <p v-if="planModal.message" class="form-ok">{{ planModal.message }}</p>

          <div class="modal-actions">
            <BaseButton variant="secondary" @click="closePlanModal">닫기</BaseButton>
            <BaseButton variant="primary" :disabled="planModal.busy" @click="confirmAddToPlan">
              {{ planModal.busy ? '담는 중…' : '담기' }}
            </BaseButton>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { attractionLikeApi } from '@/api/attractionLikes'
import { tripPlanApi } from '@/api/tripPlans'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const items = ref([])
const loading = ref(false)
const loadError = ref('')
const busyNo = ref(null)

// 계획에 담기 모달
const planModal = ref({ open: false, no: null, title: '', busy: false, error: '', message: '' })
const planOptions = ref([])
const planLoading = ref(false)
const planLoaded = ref(false)
const selectedPlanId = ref(null)
const visitDay = ref(1)

const selectedPlan = computed(() => planOptions.value.find((p) => p.id === selectedPlanId.value))
const dayOptions = computed(() => {
  const count = planDayCount(selectedPlan.value)
  return Array.from({ length: count }, (_, i) => i + 1)
})

function formatCount(n) {
  return n > 999 ? '999+' : String(n || 0)
}
function starText(avg) {
  const n = Math.round(avg || 0)
  const filled = Math.max(0, Math.min(5, n))
  return '★'.repeat(filled) + '☆'.repeat(5 - filled)
}
function cleanDisplayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}
function planDayCount(plan) {
  if (!plan || !plan.startDate || !plan.endDate) return 1
  const start = new Date(plan.startDate)
  const end = new Date(plan.endDate)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end < start) return 1
  return Math.floor((end - start) / 86400000) + 1
}

function goExplore() {
  router.push('/attractions')
}
function goToDetail(no) {
  router.push(`/attractions/${no}`)
}
function goToPlans() {
  router.push('/plans')
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    items.value = await attractionLikeApi.mine()
  } catch (e) {
    loadError.value = '찜 목록을 불러오지 못했어요.'
  } finally {
    loading.value = false
  }
}

async function removeLike(item) {
  if (busyNo.value) return
  busyNo.value = item.no
  try {
    await attractionLikeApi.unlike(item.no)
    items.value = items.value.filter((x) => x.no !== item.no)
  } catch (e) {
    loadError.value = '찜 해제에 실패했어요.'
  } finally {
    busyNo.value = null
  }
}

async function openAddToPlan(item) {
  planModal.value = { open: true, no: item.no, title: item.title, busy: false, error: '', message: '' }
  if (planLoaded.value) return
  planLoading.value = true
  try {
    planOptions.value = await tripPlanApi.list()
    planLoaded.value = true
    selectedPlanId.value = planOptions.value.length ? planOptions.value[0].id : null
    visitDay.value = dayOptions.value[0] || 1
  } catch (e) {
    planModal.value.error = '여행 계획 목록을 불러오지 못했어요.'
  } finally {
    planLoading.value = false
  }
}
function closePlanModal() {
  planModal.value.open = false
}
async function confirmAddToPlan() {
  if (!selectedPlanId.value || planModal.value.busy) return
  planModal.value.busy = true
  planModal.value.error = ''
  planModal.value.message = ''
  try {
    const days = dayOptions.value
    if (!days.includes(visitDay.value)) visitDay.value = days[0] || 1
    await tripPlanApi.addPlace(selectedPlanId.value, {
      attractionId: planModal.value.no,
      visitDay: visitDay.value || null,
      memo: null,
    })
    planModal.value.message = '여행 계획에 담았어요.'
  } catch (e) {
    planModal.value.error = e?.response?.data?.message || '계획에 담지 못했어요.'
  } finally {
    planModal.value.busy = false
  }
}

onMounted(() => {
  if (!auth.isAuthenticated) {
    router.replace({ path: '/auth', query: { mode: 'login', redirect: '/wishlist' } })
    return
  }
  load()
})
</script>

<style scoped>
.wishlist-layout {
  padding: 32px var(--space-6) 80px;
}

.wishlist-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.wishlist-head .t-caption {
  margin-top: 6px;
}

.count-badge {
  flex-shrink: 0;
  padding: 6px 14px;
  background: var(--teal-tint);
  color: var(--teal-ink);
  border-radius: 999px;
  font-weight: 700;
  font-size: 14px;
}

.state-note {
  padding: 24px 0;
  text-align: center;
  color: var(--ink-soft);
  font-size: 14px;
}

/* 빈 상태 */
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 64px 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  text-align: center;
}

.empty-heart {
  font-size: 48px;
  color: var(--line-2);
}

.empty-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--ink);
}

.empty-box :deep(button) {
  margin-top: 12px;
}

/* 카드 그리드 */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.wish-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.15s, transform 0.15s;
}

.wish-card:hover {
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.wish-card__thumb {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: var(--bg-2);
  cursor: pointer;
}

.wish-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

.thumb-grad {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--teal-tint), var(--bg-2));
}

.card-heart {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  font-size: 18px;
  color: var(--danger);
  background: var(--glass);
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.12s, background 0.12s;
}

.card-heart:hover:not(:disabled) {
  transform: scale(1.1);
  background: var(--surface);
}

.card-heart:disabled {
  opacity: 0.6;
  cursor: default;
}

.wish-card__body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-row .stars {
  color: var(--warning);
  font-size: 13px;
  letter-spacing: 1px;
}

.rating-text {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 13px;
  color: var(--ink-2);
}

.wish-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  cursor: pointer;
  line-height: 1.3;
}

.wish-title:hover {
  color: var(--teal);
}

.addr {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 2px;
}

.chip {
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  background: var(--bg-2);
  color: var(--ink-soft);
}

.chip--teal {
  background: var(--teal-tint);
  color: var(--teal-ink);
}

.chip--like {
  background: color-mix(in srgb, var(--danger) 10%, white);
  color: var(--danger);
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
  padding-top: 10px;
}

.card-actions :deep(button) {
  flex: 1;
  padding: 8px 10px;
  font-size: 13px;
}

/* 모달 */
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
  background: var(--surface);
  border-radius: var(--r-xl);
  padding: 28px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.2);
}

.modal-sub {
  margin: 6px 0 18px;
  color: var(--teal);
  font-weight: 600;
}

.form-block {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-2);
  margin-bottom: 8px;
}

.select-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line-2);
  border-radius: 10px;
  font-size: 14px;
  background: var(--bg-soft);
}

.form-error {
  font-size: 13px;
  color: var(--danger);
  margin-bottom: 8px;
}

.form-ok {
  font-size: 13px;
  color: var(--teal-ink);
  margin-bottom: 8px;
}

.modal-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.modal-actions :deep(button) {
  flex: 1;
}
</style>

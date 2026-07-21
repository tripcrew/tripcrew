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

      <div v-else v-stagger class="cards-grid">
        <article v-for="(item, i) in items" :key="item.no" class="wish-card" :style="{ '--i': i }">
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

    <!-- 여행 계획에 담기 모달 (공용 컴포넌트) -->
    <AddToPlanModal :attraction="planTarget" @close="planTarget = null" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import AddToPlanModal from '@/components/common/AddToPlanModal.vue'
import { attractionLikeApi } from '@/api/attractionLikes'
import { useAuthStore } from '@/stores/auth'
import { vStagger } from '@/composables/useReveal'

const router = useRouter()
const auth = useAuthStore()

const items = ref([])
const loading = ref(false)
const loadError = ref('')
const busyNo = ref(null)

// 계획에 담기 모달 대상 { no, title } — null 이면 닫힘
const planTarget = ref(null)

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

function goExplore() {
  router.push('/attractions')
}
function goToDetail(no) {
  router.push(`/attractions/${no}`)
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

function openAddToPlan(item) {
  planTarget.value = { no: item.no, title: item.title }
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

/* 찜 카드가 보이면 순차로 fade-up (v-stagger, 최초 1회). reduced-motion이면 즉시 노출 */
@media (prefers-reduced-motion: no-preference) {
  .cards-grid > .wish-card { opacity: 0; }
  .cards-grid.stagger-in > .wish-card {
    animation: wish-rise 0.5s ease-out forwards;
    animation-delay: calc(min(var(--i, 0), 12) * 0.05s);
  }
}
@keyframes wish-rise {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: none; }
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

/* ── 반응형: 카드 그리드 모바일 대응 (모달은 공용 AddToPlanModal 컴포넌트가 담당) ── */
@media (max-width: 900px) {
  .wishlist-layout { padding-left: var(--space-4); padding-right: var(--space-4); }
}

@media (max-width: 640px) {
  .wishlist-layout { padding: 24px var(--space-4) 56px; }
  /* 제목 + 개수 뱃지가 붙지 않게 간격 확보 */
  .wishlist-head { gap: 12px; }
  /* 좁은 폭에선 1열(auto-fill이 처리하지만 명시적으로 고정) */
  .cards-grid { grid-template-columns: 1fr; gap: 16px; }
  .wish-card__thumb { height: 160px; }
  /* 상세/담기 버튼 터치 타깃 확보 */
  .card-actions :deep(button) { padding: 11px 10px; }
}
</style>

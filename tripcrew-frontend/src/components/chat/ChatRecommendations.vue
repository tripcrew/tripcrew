<template>
  <div class="recos">
    <p class="recos__label">
      <span class="recos__spark">✦</span> 이 여행에 어울리는 관광지
    </p>
    <div class="recos__strip">
      <article v-for="a in attractions" :key="a.no" class="reco-card">
        <div class="reco-card__thumb" @click="goToDetail(a.no)">
          <img v-if="a.imageUrl" :src="a.imageUrl" :alt="displayName(a.title)" loading="lazy" />
          <div v-else class="thumb-grad"></div>
          <button
            class="card-like"
            type="button"
            :class="{ 'is-liked': likeState(a).liked }"
            :disabled="likeBusyNo === a.no"
            :aria-label="likeState(a).liked ? '찜 해제' : '찜하기'"
            @click.stop="toggleLike(a)"
          >
            <span class="card-like__heart" :class="{ 'card-like__heart--pop': likeBounceNo === a.no }" @animationend="likeBounceNo = null">
              {{ likeState(a).liked ? '♥' : '♡' }}
            </span>
          </button>
        </div>

        <div class="reco-card__body">
          <h4 class="reco-title" @click="goToDetail(a.no)">{{ displayName(a.title) }}</h4>
          <p class="reco-addr">{{ a.address || `${a.sido || ''} ${a.gugun || ''}`.trim() }}</p>
          <div class="reco-meta">
            <span v-if="a.reviewCount" class="reco-rating">★ {{ Number(a.reviewAverage).toFixed(1) }}</span>
            <span v-else class="reco-rating reco-rating--none">평점 없음</span>
            <span class="reco-like-count">♥ {{ formatCount(likeState(a).likeCount) }}</span>
          </div>
          <BaseButton variant="primary" size="sm" full class="reco-add-btn" @click="onAdd(a)">계획에 담기</BaseButton>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import BaseButton from '@/components/common/BaseButton.vue'
import { attractionLikeApi } from '@/api/attractionLikes'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  // 해석 완료된 관광지 카드 목록 [{ no, title, imageUrl, address, sido, gugun, reviewAverage, reviewCount }]
  attractions: { type: Array, required: true },
})
const emit = defineEmits(['add'])

const router = useRouter()
const auth = useAuthStore()

const likeMap = ref({}) // no -> { liked, likeCount }
const likeBusyNo = ref(null)
const likeBounceNo = ref(null)

function likeState(a) {
  return likeMap.value[a.no] || { likeCount: 0, liked: false }
}
function formatCount(n) {
  return n > 999 ? '999+' : String(n || 0)
}
function displayName(value) {
  return String(value || '')
    .trim()
    .replace(/(?:\s+\(?#?\d{5,}\)?)+\s*$/g, '')
    .replace(/^\s*(?:\(?#?\d{5,}\)?\s+)+/g, '')
}
function goToDetail(no) {
  router.push(`/attractions/${no}`)
}

async function loadLikeCounts() {
  const nos = props.attractions.map((a) => a.no)
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

function requireLogin() {
  router.push({ path: '/auth', query: { mode: 'login', redirect: '/chat' } })
}

async function toggleLike(a) {
  if (!auth.isAuthenticated) {
    requireLogin()
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

function onAdd(a) {
  if (!auth.isAuthenticated) {
    requireLogin()
    return
  }
  emit('add', a)
}

onMounted(loadLikeCounts)
</script>

<style scoped>
.recos {
  margin-top: 12px;
}

.recos__label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-2);
  margin-bottom: 10px;
}

.recos__spark {
  color: var(--teal);
}

.recos__strip {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 6px;
  scroll-snap-type: x mandatory;
}

.reco-card {
  flex: 0 0 180px;
  scroll-snap-align: start;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg, 14px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.15s, transform 0.15s;
}

.reco-card:hover {
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.reco-card__thumb {
  position: relative;
  height: 110px;
  background: var(--bg-2);
  cursor: pointer;
}

.reco-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-grad {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--teal-tint), var(--bg-2));
}

.card-like {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  font-size: 16px;
  color: var(--danger);
  background: var(--glass);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.12s, background 0.12s;
}

.card-like:hover:not(:disabled) {
  transform: scale(1.1);
  background: var(--surface);
}

.card-like:disabled {
  opacity: 0.6;
  cursor: default;
}

.card-like__heart {
  display: inline-block;
  line-height: 1;
}

@media (prefers-reduced-motion: no-preference) {
  .card-like__heart--pop {
    animation: like-pop 0.4s ease-out;
  }
}

@keyframes like-pop {
  0% { transform: scale(1); }
  40% { transform: scale(1.4); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

.reco-card__body {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
}

.reco-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.3;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reco-title:hover {
  color: var(--teal);
}

.reco-addr {
  font-size: 12px;
  color: var(--ink-soft);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reco-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-family: var(--font-mono);
}

.reco-rating {
  color: var(--warning);
  font-weight: 700;
}

.reco-rating--none {
  color: var(--muted);
  font-weight: 500;
}

.reco-like-count {
  color: var(--danger);
}

.reco-add-btn {
  margin-top: auto;
}

@media (max-width: 640px) {
  .reco-card {
    flex-basis: 160px;
  }
  .reco-card__thumb {
    height: 100px;
  }
}
</style>

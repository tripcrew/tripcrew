<template>
  <div v-if="urls.length" :class="['review-images', `is-${size}`]">
    <button
      v-for="(url, i) in visibleUrls"
      :key="url"
      type="button"
      class="thumb"
      @click="open(i)"
    >
      <img :src="toAssetUrl(url)" :alt="`후기 이미지 ${i + 1}`" loading="lazy" />
      <span v-if="i === visibleUrls.length - 1 && hiddenCount > 0" class="more-overlay">
        +{{ hiddenCount }}
      </span>
    </button>
  </div>

  <!-- 라이트박스 -->
  <Teleport to="body">
    <div v-if="lightboxOpen" class="lightbox" @click.self="close">
      <button class="lb-close" type="button" aria-label="닫기" @click="close">×</button>
      <button v-if="urls.length > 1" class="lb-nav lb-prev" type="button" aria-label="이전" @click.stop="prev">‹</button>
      <img class="lb-img" :src="toAssetUrl(urls[activeIndex])" :alt="`후기 이미지 ${activeIndex + 1}`" />
      <button v-if="urls.length > 1" class="lb-nav lb-next" type="button" aria-label="다음" @click.stop="next">›</button>
      <span v-if="urls.length > 1" class="lb-count">{{ activeIndex + 1 }} / {{ urls.length }}</span>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { toAssetUrl } from '@/api/http'

const props = defineProps({
  urls: { type: Array, default: () => [] },
  // 'sm' = 상세 미리보기, 'md' = 후기 목록
  size: { type: String, default: 'md' },
  // 표시할 최대 썸네일 수(초과분은 마지막 썸네일에 +N 오버레이). 0 = 제한 없음
  max: { type: Number, default: 0 },
})

const lightboxOpen = ref(false)
const activeIndex = ref(0)

const visibleUrls = computed(() => (props.max > 0 ? props.urls.slice(0, props.max) : props.urls))
const hiddenCount = computed(() => Math.max(0, props.urls.length - visibleUrls.value.length))

function open(i) {
  activeIndex.value = i
  lightboxOpen.value = true
}
function close() {
  lightboxOpen.value = false
}
function prev() {
  activeIndex.value = (activeIndex.value - 1 + props.urls.length) % props.urls.length
}
function next() {
  activeIndex.value = (activeIndex.value + 1) % props.urls.length
}

function onKey(e) {
  if (!lightboxOpen.value) return
  if (e.key === 'Escape') close()
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
}

watch(lightboxOpen, (isOpen) => {
  if (isOpen) window.addEventListener('keydown', onKey)
  else window.removeEventListener('keydown', onKey)
})
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<style scoped>
.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.thumb {
  position: relative;
  padding: 0;
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: var(--bg-2);
  transition: transform 0.12s, box-shadow 0.12s;
}

.thumb:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.thumb img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 후기 목록(기본) */
.is-md .thumb {
  width: 104px;
  height: 104px;
}

/* 상세 미리보기(작게) */
.is-sm .thumb {
  width: 64px;
  height: 64px;
  border-radius: 8px;
}

.more-overlay {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  font-weight: 700;
  font-size: 16px;
}

/* 라이트박스 */
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.82);
  padding: 24px;
}

.lb-img {
  width: auto;
  height: auto;
  max-width: 96vw;
  max-height: 94vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.lb-close {
  position: absolute;
  top: 20px;
  right: 24px;
  width: 44px;
  height: 44px;
  font-size: 30px;
  line-height: 1;
  color: white;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  cursor: pointer;
}

.lb-close:hover {
  background: rgba(255, 255, 255, 0.24);
}

.lb-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  font-size: 36px;
  line-height: 1;
  color: white;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  cursor: pointer;
}

.lb-nav:hover {
  background: rgba(255, 255, 255, 0.24);
}

.lb-prev {
  left: 24px;
}

.lb-next {
  right: 24px;
}

.lb-count {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-family: var(--font-mono);
  font-size: 13px;
  background: rgba(0, 0, 0, 0.5);
  padding: 4px 12px;
  border-radius: 999px;
}
</style>

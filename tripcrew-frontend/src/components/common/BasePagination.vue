<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="페이지 이동">
    <button type="button" :disabled="current <= 1" aria-label="첫 페이지" @click="go(1)">«</button>
    <button type="button" :disabled="current <= 1" aria-label="이전 페이지" @click="go(current - 1)">‹</button>
    <button
      v-for="p in pages"
      :key="p"
      type="button"
      :class="{ active: p === current }"
      :aria-current="p === current ? 'page' : undefined"
      @click="go(p)"
    >{{ p }}</button>
    <button type="button" :disabled="current >= totalPages" aria-label="다음 페이지" @click="go(current + 1)">›</button>
    <button type="button" :disabled="current >= totalPages" aria-label="마지막 페이지" @click="go(totalPages)">»</button>
  </nav>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 공용 페이지네이션(클라이언트 사이드). 네이버식 « ‹ 1 2 3 4 5 › » 구성.
 * modelValue = 현재 페이지(1-base). total / pageSize 로 총 페이지를 계산하고
 * 5개씩 묶어 현재 그룹만 노출한다. 1페이지뿐이면 렌더링하지 않는다.
 */
const props = defineProps({
  modelValue: { type: Number, default: 1 },
  total: { type: Number, required: true },
  pageSize: { type: Number, default: 15 },
  groupSize: { type: Number, default: 5 },
})
const emit = defineEmits(['update:modelValue'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const current = computed(() => Math.min(Math.max(1, props.modelValue), totalPages.value))

const pages = computed(() => {
  const start = Math.floor((current.value - 1) / props.groupSize) * props.groupSize + 1
  const end = Math.min(totalPages.value, start + props.groupSize - 1)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

function go(p) {
  const next = Math.min(Math.max(1, p), totalPages.value)
  if (next !== current.value) emit('update:modelValue', next)
}
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 24px;
}

.pagination button {
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: white;
  color: var(--ink-3);
  font-weight: 700;
  font-size: 13px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

.pagination button:hover:not(:disabled):not(.active) { background: var(--bg-soft); }

.pagination button.active {
  background: var(--teal);
  border-color: var(--teal);
  color: white;
}

.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
</style>

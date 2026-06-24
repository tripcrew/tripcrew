<template>
  <span>{{ text }}</span>
</template>

<script setup>
/**
 * 숫자 카운트업. 값이 도착(또는 변경)되면 현재 표시값에서 목표값까지 easeOut 으로 굴린다.
 * - value 가 null/undefined 면 '—'(아직 안 온 값). 데이터가 오면 0→실제값으로 1회 카운트업.
 * - prefers-reduced-motion 이면 애니메이션 없이 즉시 최종값.
 */
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  value: { type: Number, default: null },
  duration: { type: Number, default: 900 },
})

const displayed = ref(0)
let raf = null
let startTs = 0
let from = 0
let to = 0

const text = computed(() =>
  props.value === null || props.value === undefined
    ? '—'
    : Math.round(displayed.value).toLocaleString(),
)

function prefersReduced() {
  return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function cancel() {
  if (raf) cancelAnimationFrame(raf)
  raf = null
}

function step(ts) {
  if (!startTs) startTs = ts
  const p = Math.min(1, (ts - startTs) / props.duration)
  const eased = 1 - Math.pow(1 - p, 3) // easeOutCubic
  displayed.value = from + (to - from) * eased
  if (p < 1) raf = requestAnimationFrame(step)
  else raf = null
}

function animateTo(target) {
  if (typeof target !== 'number') return
  cancel()
  if (prefersReduced()) {
    displayed.value = target
    return
  }
  from = displayed.value
  to = target
  startTs = 0
  raf = requestAnimationFrame(step)
}

onMounted(() => animateTo(props.value))
watch(() => props.value, (v) => animateTo(v))
onBeforeUnmount(cancel)
</script>

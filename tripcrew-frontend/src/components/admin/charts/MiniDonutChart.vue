<template>
  <div class="donut-wrap">
    <svg :viewBox="`0 0 ${SIZE} ${SIZE}`" class="donut-svg" role="img" :aria-label="ariaLabel">
      <g :transform="`rotate(-90 ${C} ${C})`">
        <circle :cx="C" :cy="C" :r="R" class="track" :stroke-width="STROKE" fill="none" />
        <circle
          v-for="(seg, si) in segments"
          :key="seg.label"
          :cx="C"
          :cy="C"
          :r="R"
          fill="none"
          class="donut-seg"
          :stroke-width="STROKE"
          :stroke-dasharray="`${seg.dash} ${circumference - seg.dash}`"
          :stroke-dashoffset="-seg.offset"
          :style="{ stroke: seg.color, animationDelay: si * 90 + 'ms' }"
        >
          <title>{{ seg.label }} · {{ seg.value }} ({{ seg.percent }}%)</title>
        </circle>
      </g>
      <text :x="C" :y="C - 2" class="donut-total" text-anchor="middle">{{ total }}</text>
      <text :x="C" :y="C + 14" class="donut-cap" text-anchor="middle">합계</text>
    </svg>
    <ul class="donut-legend">
      <li v-for="seg in segments" :key="seg.label" class="legend-row">
        <span class="legend-dot" :style="{ background: seg.color }"></span>
        <span class="legend-label">{{ seg.label }}</span>
        <span class="legend-val">{{ seg.value }} · {{ seg.percent }}%</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** [{ label, value, color }] */
  data: { type: Array, default: () => [] },
  ariaLabel: { type: String, default: '분포 도넛 차트' },
})

const SIZE = 120
const C = 60
const R = 45
const STROKE = 18
const circumference = 2 * Math.PI * R

const total = computed(() => props.data.reduce((s, d) => s + (d.value || 0), 0))

const segments = computed(() => {
  const t = total.value
  let acc = 0
  return props.data.map((d) => {
    const value = d.value || 0
    const frac = t > 0 ? value / t : 0
    const dash = frac * circumference
    const seg = {
      label: d.label,
      value,
      color: d.color,
      dash,
      offset: acc,
      percent: t > 0 ? Math.round(frac * 100) : 0,
    }
    acc += dash
    return seg
  })
})
</script>

<style scoped>
.donut-wrap {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}
.donut-svg { width: 172px; height: 172px; flex: 0 0 auto; }
.track { stroke: var(--bg-2); }

/* 각 조각이 자기 시작점에서 제 길이(인라인 dasharray)까지 자라난다.
   from 만 정의 → 100% 는 인라인 stroke-dasharray(underlying value)가 적용됨. */
.donut-seg { animation: donut-seg 0.85s ease-out both; }
@keyframes donut-seg {
  from { stroke-dasharray: 0 282.743; }
}

@media (prefers-reduced-motion: reduce) {
  .donut-seg { animation: none; }
}
.donut-total {
  font-family: var(--font-mono);
  font-size: 24px;
  font-weight: 800;
  fill: var(--ink);
}
.donut-cap { font-size: 9px; fill: var(--muted); }
.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1 1 140px;
  min-width: 140px;
}
.legend-row {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 13px;
}
.legend-dot { width: 11px; height: 11px; border-radius: 3px; flex: 0 0 auto; }
.legend-label { font-weight: 700; color: var(--ink-2); }
.legend-val { margin-left: auto; color: var(--ink-soft); font-family: var(--font-mono); }
</style>

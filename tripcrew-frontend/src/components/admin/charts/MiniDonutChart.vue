<template>
  <div class="donut-wrap">
    <svg :viewBox="`0 0 ${SIZE} ${SIZE}`" class="donut-svg" role="img" :aria-label="ariaLabel">
      <g :transform="`rotate(-90 ${C} ${C})`">
        <circle :cx="C" :cy="C" :r="R" class="track" :stroke-width="STROKE" fill="none" />
        <circle
          v-for="seg in segments"
          :key="seg.label"
          :cx="C"
          :cy="C"
          :r="R"
          fill="none"
          :stroke-width="STROKE"
          :stroke-dasharray="`${seg.dash} ${circumference - seg.dash}`"
          :stroke-dashoffset="-seg.offset"
          :style="{ stroke: seg.color }"
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
const R = 44
const STROKE = 16
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
  gap: 18px;
  flex-wrap: wrap;
}
.donut-svg { width: 132px; height: 132px; flex: 0 0 auto; }
.track { stroke: var(--bg-2); }
.donut-total {
  font-family: var(--font-mono);
  font-size: 22px;
  font-weight: 800;
  fill: var(--ink);
}
.donut-cap { font-size: 9px; fill: var(--muted); }
.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1 1 120px;
  min-width: 120px;
}
.legend-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.legend-dot { width: 10px; height: 10px; border-radius: 3px; flex: 0 0 auto; }
.legend-label { font-weight: 700; color: var(--ink-2); }
.legend-val { margin-left: auto; color: var(--ink-soft); font-family: var(--font-mono); }
</style>

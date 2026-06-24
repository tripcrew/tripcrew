<template>
  <div ref="wrap" class="chart-wrap">
    <svg :width="width" :height="H" class="chart-svg" role="img" :aria-label="ariaLabel">
      <line :x1="plotL" :y1="plotTop" :x2="plotR" :y2="plotTop" class="grid" />
      <line :x1="plotL" :y1="plotBottom" :x2="plotR" :y2="plotBottom" class="axis" />
      <g v-for="bar in bars" :key="bar.key">
        <rect
          :x="bar.x"
          :y="bar.y"
          :width="bar.w"
          :height="bar.h"
          rx="2"
          class="bar"
          :class="{ 'bar--active': hoverIndex === bar.key }"
          @mouseenter="hoverIndex = bar.key"
          @mouseleave="hoverIndex = -1"
        />
        <text v-if="bar.showValue" :x="bar.cx" :y="bar.y - 5" class="val" text-anchor="middle">{{ bar.value }}</text>
        <text v-if="bar.showTick" :x="bar.cx" :y="H - 6" class="tick" text-anchor="middle">{{ bar.tick }}</text>
      </g>
    </svg>
    <div v-if="tip" class="chart-tip" :style="tipStyle">
      <strong>{{ tip.title }}</strong>
      <span>{{ tip.value }}{{ unit }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

import { useChartWidth } from './useChartWidth'

const props = defineProps({
  /** [{ label: 'YYYY-MM-DD', value: number }] */
  data: { type: Array, default: () => [] },
  unit: { type: String, default: '명' },
  ariaLabel: { type: String, default: '막대 차트' },
})

const wrap = ref(null)
const { width } = useChartWidth(wrap)
const hoverIndex = ref(-1)

const H = 220
const padTop = 26
const padBottom = 22
const padL = 14
const padR = 14
const plotTop = padTop
const plotBottom = H - padBottom
const plotL = padL
const plotR = computed(() => Math.max(plotL + 10, width.value - padR))
const plotH = plotBottom - plotTop

const maxValue = computed(() => {
  const max = props.data.reduce((m, d) => (d.value > m ? d.value : m), 0)
  return max <= 0 ? 1 : max
})

const bars = computed(() => {
  const n = props.data.length || 1
  const plotW = plotR.value - plotL
  const step = plotW / n
  const barW = Math.max(2, Math.min(40, step * 0.62))
  // 눈금(날짜) 솎기: 그 달의 실제 일수(28~31)가 아니라 항상 최대 31일을 기준으로 간격을 정한다.
  // (n 기준이면 30일 달은 다 나오고 31일 달만 한 칸씩 건너뛰는 등 달마다 밀도가 달라짐)
  const REF_DAYS = 31
  const MIN_LABEL_GAP = 26 // 라벨 사이 최소 px
  const tickEvery = Math.max(1, Math.ceil((REF_DAYS * MIN_LABEL_GAP) / plotW))
  return props.data.map((d, i) => {
    const h = (d.value / maxValue.value) * plotH
    const cx = plotL + i * step + step / 2
    return {
      key: i,
      label: d.label,
      value: d.value,
      x: cx - barW / 2,
      w: barW,
      h,
      y: plotBottom - h,
      cx,
      tick: shortDay(d.label),
      showTick: i % tickEvery === 0 || i === n - 1,
      showValue: d.value > 0 && barW >= 13,
    }
  })
})

const tip = computed(() => {
  if (hoverIndex.value < 0) return null
  const bar = bars.value[hoverIndex.value]
  if (!bar) return null
  return { title: fullDate(bar.label), value: bar.value, x: bar.cx, y: bar.y }
})

const tipStyle = computed(() => {
  const t = tip.value
  if (!t) return {}
  return { left: `${t.x}px`, top: `${Math.max(4, t.y - 8)}px` }
})

// 'YYYY-MM-DD' → 일(앞 0 제거)
function shortDay(label) {
  const parts = String(label).split('-')
  return parts.length === 3 ? String(Number(parts[2])) : String(label).slice(-2)
}
// 'YYYY-MM-DD' → 'M월 D일'
function fullDate(label) {
  const parts = String(label).split('-')
  return parts.length === 3 ? `${Number(parts[1])}월 ${Number(parts[2])}일` : label
}
</script>

<style scoped>
.chart-wrap { width: 100%; min-width: 0; position: relative; }
.chart-svg { display: block; max-width: 100%; }
.bar { fill: var(--teal); transition: fill 0.12s; }
.bar--active { fill: var(--teal-3); }
.grid { stroke: var(--line); stroke-width: 1; stroke-dasharray: 3 3; }
.axis { stroke: var(--line); stroke-width: 1; }
.tick { fill: var(--muted); font-size: 10px; font-family: var(--font-mono); }
.val { fill: var(--ink-soft); font-size: 10px; font-weight: 700; font-family: var(--font-mono); }

.chart-tip {
  position: absolute;
  transform: translate(-50%, -100%);
  background: var(--ink);
  color: white;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 1px;
  white-space: nowrap;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
  z-index: 5;
}
.chart-tip strong { font-weight: 700; }
.chart-tip span { color: rgba(255, 255, 255, 0.82); }
</style>

<template>
  <div ref="wrap" class="chart-wrap">
    <svg :width="width" :height="H" class="chart-svg" role="img" :aria-label="ariaLabel" @mouseleave="hoverIndex = -1">
      <line :x1="plotL" :y1="plotTop" :x2="plotR" :y2="plotTop" class="grid" />
      <line :x1="plotL" :y1="plotBottom" :x2="plotR" :y2="plotBottom" class="axis" />

      <!-- hover 세로 가이드 -->
      <line v-if="guideX !== null" :x1="guideX" :y1="plotTop" :x2="guideX" :y2="plotBottom" class="guide" />

      <g v-for="line in lines" :key="line.name">
        <polyline :points="line.points" class="series-line" :style="{ stroke: line.color }" />
        <circle
          v-for="(pt, i) in line.dots"
          :key="i"
          :cx="pt.x"
          :cy="pt.y"
          :r="hoverIndex === i ? 4 : 2.4"
          :style="{ fill: line.color }"
        />
      </g>

      <text v-for="tk in ticks" :key="tk.x" :x="tk.x" :y="H - 6" class="tick" text-anchor="middle">{{ tk.text }}</text>

      <!-- 마우스 추적용 투명 오버레이 -->
      <rect
        :x="plotL"
        :y="plotTop"
        :width="Math.max(0, plotR - plotL)"
        :height="plotBottom - plotTop"
        fill="transparent"
        @mousemove="onMove"
      />
    </svg>
    <div v-if="tip" class="chart-tip" :style="tipStyle">
      <strong>{{ tip.title }}</strong>
      <span v-for="row in tip.rows" :key="row.name" class="tip-row">
        <span class="tip-dot" :style="{ background: row.color }"></span>{{ row.name }} {{ row.value }}{{ unit }}
      </span>
    </div>
    <div class="legend">
      <span v-for="s in series" :key="s.name" class="legend-item">
        <span class="legend-dot" :style="{ background: s.color }"></span>{{ s.name }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

import { useChartWidth } from './useChartWidth'

const props = defineProps({
  /** [{ name, color, data: [{ label, value }] }] — 모든 series 의 data 길이는 동일 */
  series: { type: Array, default: () => [] },
  unit: { type: String, default: '건' },
  ariaLabel: { type: String, default: '추이 차트' },
})

const wrap = ref(null)
const { width } = useChartWidth(wrap)
const hoverIndex = ref(-1)

const H = 220
const padTop = 18
const padBottom = 22
const padL = 14
const padR = 14
const plotTop = padTop
const plotBottom = H - padBottom
const plotL = padL
const plotR = computed(() => Math.max(plotL + 10, width.value - padR))
const plotH = plotBottom - plotTop

const pointCount = computed(() => {
  const first = props.series[0]
  return first && first.data ? first.data.length : 0
})

const maxValue = computed(() => {
  let max = 0
  props.series.forEach((s) => {
    ;(s.data || []).forEach((d) => {
      if (d.value > max) max = d.value
    })
  })
  return max <= 0 ? 1 : max
})

function xAt(i, n) {
  if (n <= 1) return plotL
  return plotL + (i / (n - 1)) * (plotR.value - plotL)
}
function yAt(value) {
  return plotBottom - (value / maxValue.value) * plotH
}

const lines = computed(() =>
  props.series.map((s) => {
    const data = s.data || []
    const n = data.length
    const dots = data.map((d, i) => ({ x: xAt(i, n), y: yAt(d.value), value: d.value, label: d.label }))
    return { name: s.name, color: s.color, dots, points: dots.map((p) => `${p.x},${p.y}`).join(' ') }
  }),
)

const ticks = computed(() => {
  const n = pointCount.value
  if (!n) return []
  const data = props.series[0].data
  const plotW = plotR.value - plotL
  const maxLabels = Math.max(6, Math.floor(plotW / 34))
  const tickEvery = Math.ceil(n / maxLabels)
  const out = []
  for (let i = 0; i < n; i++) {
    if (i % tickEvery === 0 || i === n - 1) out.push({ x: xAt(i, n), text: shortDay(data[i].label) })
  }
  return out
})

function onMove(e) {
  const n = pointCount.value
  if (!n) return
  const rect = e.currentTarget.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const ratio = (mx - plotL) / Math.max(1, plotR.value - plotL)
  let idx = Math.round(ratio * (n - 1))
  if (idx < 0) idx = 0
  if (idx > n - 1) idx = n - 1
  hoverIndex.value = idx
}

const guideX = computed(() => (hoverIndex.value < 0 ? null : xAt(hoverIndex.value, pointCount.value)))

const tip = computed(() => {
  const i = hoverIndex.value
  if (i < 0 || !props.series.length) return null
  const first = props.series[0].data[i]
  if (!first) return null
  let topY = plotBottom
  const rows = props.series.map((s) => {
    const pt = s.data[i]
    const y = yAt(pt ? pt.value : 0)
    if (y < topY) topY = y
    return { name: s.name, color: s.color, value: pt ? pt.value : 0 }
  })
  return { title: fullDate(first.label), rows, x: xAt(i, pointCount.value), y: topY }
})

const tipStyle = computed(() => {
  const t = tip.value
  if (!t) return {}
  return { left: `${t.x}px`, top: `${Math.max(4, t.y - 8)}px` }
})

function shortDay(label) {
  const parts = String(label).split('-')
  return parts.length === 3 ? String(Number(parts[2])) : String(label).slice(-2)
}
function fullDate(label) {
  const parts = String(label).split('-')
  return parts.length === 3 ? `${Number(parts[1])}월 ${Number(parts[2])}일` : label
}
</script>

<style scoped>
.chart-wrap { width: 100%; position: relative; }
.chart-svg { display: block; }
.series-line { fill: none; stroke-width: 2; stroke-linejoin: round; stroke-linecap: round; }
.grid { stroke: var(--line); stroke-width: 1; stroke-dasharray: 3 3; }
.axis { stroke: var(--line); stroke-width: 1; }
.guide { stroke: var(--muted); stroke-width: 1; stroke-dasharray: 2 2; }
.tick { fill: var(--muted); font-size: 10px; font-family: var(--font-mono); }

.chart-tip {
  position: absolute;
  transform: translate(-50%, -100%);
  background: var(--ink);
  color: white;
  padding: 7px 10px;
  border-radius: 8px;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 3px;
  white-space: nowrap;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
  z-index: 5;
}
.chart-tip strong { font-weight: 700; }
.tip-row { display: inline-flex; align-items: center; gap: 6px; color: rgba(255, 255, 255, 0.9); }
.tip-dot { width: 8px; height: 8px; border-radius: 50%; }

.legend {
  display: flex;
  gap: 18px;
  justify-content: center;
  margin-top: 10px;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--ink-soft);
  font-weight: 600;
}
.legend-dot { width: 9px; height: 9px; border-radius: 50%; }
</style>

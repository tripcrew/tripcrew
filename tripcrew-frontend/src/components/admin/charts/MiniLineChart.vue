<template>
  <div class="chart-wrap">
    <svg :viewBox="`0 0 ${W} ${H}`" class="chart-svg" role="img" :aria-label="ariaLabel">
      <line :x1="padL" :y1="topY" :x2="W - padR" :y2="topY" class="grid" />
      <line :x1="padL" :y1="baseY" :x2="W - padR" :y2="baseY" class="axis" />
      <g v-for="line in lines" :key="line.name">
        <polyline :points="line.points" class="series-line" :style="{ stroke: line.color }" />
        <circle v-for="(pt, i) in line.dots" :key="i" :cx="pt.x" :cy="pt.y" r="2.4" :style="{ fill: line.color }">
          <title>{{ line.name }} · {{ pt.label }} · {{ pt.value }}</title>
        </circle>
      </g>
      <text v-for="tk in ticks" :key="tk.x" :x="tk.x" :y="H - 4" class="tick" text-anchor="middle">{{ tk.text }}</text>
    </svg>
    <div class="legend">
      <span v-for="s in series" :key="s.name" class="legend-item">
        <span class="legend-dot" :style="{ background: s.color }"></span>{{ s.name }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** [{ name, color, data: [{ label, value }] }] — 모든 series 의 data 길이는 동일하다고 가정 */
  series: { type: Array, default: () => [] },
  ariaLabel: { type: String, default: '추이 차트' },
})

const W = 320
const H = 150
const padL = 8
const padR = 8
const topY = 12
const baseY = 124

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
  if (n <= 1) return padL
  const plotW = W - padL - padR
  return padL + (i / (n - 1)) * plotW
}
function yAt(value) {
  const plotH = baseY - topY
  return baseY - (value / maxValue.value) * plotH
}

const lines = computed(() =>
  props.series.map((s) => {
    const data = s.data || []
    const n = data.length
    const dots = data.map((d, i) => ({ x: xAt(i, n), y: yAt(d.value), value: d.value, label: d.label }))
    return {
      name: s.name,
      color: s.color,
      dots,
      points: dots.map((p) => `${p.x},${p.y}`).join(' '),
    }
  }),
)

const ticks = computed(() => {
  const n = pointCount.value
  if (!n) return []
  const data = props.series[0].data
  const tickEvery = Math.ceil(n / 7)
  const out = []
  for (let i = 0; i < n; i++) {
    if (i % tickEvery === 0 || i === n - 1) {
      out.push({ x: xAt(i, n), text: String(data[i].label).slice(-2) })
    }
  }
  return out
})
</script>

<style scoped>
.chart-wrap { width: 100%; }
.chart-svg { width: 100%; height: auto; display: block; }
.series-line { fill: none; stroke-width: 2; stroke-linejoin: round; stroke-linecap: round; }
.grid { stroke: var(--line); stroke-width: 1; stroke-dasharray: 3 3; }
.axis { stroke: var(--line); stroke-width: 1; }
.tick { fill: var(--muted); font-size: 8px; font-family: var(--font-mono); }
.legend {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 8px;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--ink-soft);
  font-weight: 600;
}
.legend-dot { width: 9px; height: 9px; border-radius: 50%; }
</style>

<template>
  <div class="chart-wrap">
    <svg :viewBox="`0 0 ${W} ${H}`" class="chart-svg" role="img" :aria-label="ariaLabel">
      <!-- 가로 기준선(0/최댓값) -->
      <line :x1="padL" :y1="topY" :x2="W - padR" :y2="topY" class="grid" />
      <line :x1="padL" :y1="baseY" :x2="W - padR" :y2="baseY" class="axis" />
      <g v-for="bar in bars" :key="bar.key">
        <rect :x="bar.x" :y="bar.y" :width="bar.w" :height="bar.h" rx="2" class="bar">
          <title>{{ bar.label }} · {{ bar.value }}</title>
        </rect>
        <text v-if="bar.showTick" :x="bar.cx" :y="H - 4" class="tick" text-anchor="middle">{{ bar.tick }}</text>
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** [{ label: 'YYYY-MM-DD', value: number }] */
  data: { type: Array, default: () => [] },
  ariaLabel: { type: String, default: '막대 차트' },
})

// viewBox 좌표계(반응형: width:100% 로 스케일)
const W = 320
const H = 150
const padL = 8
const padR = 8
const topY = 12
const baseY = 124

const maxValue = computed(() => {
  const max = props.data.reduce((m, d) => (d.value > m ? d.value : m), 0)
  return max <= 0 ? 1 : max
})

const bars = computed(() => {
  const n = props.data.length || 1
  const plotW = W - padL - padR
  const step = plotW / n
  const barW = Math.max(2, step * 0.6)
  const plotH = baseY - topY
  // 라벨이 겹치지 않도록 약 7개만 눈금 표시
  const tickEvery = Math.ceil(n / 7)
  return props.data.map((d, i) => {
    const h = (d.value / maxValue.value) * plotH
    const x = padL + i * step + (step - barW) / 2
    return {
      key: i,
      label: d.label,
      value: d.value,
      x,
      w: barW,
      h,
      y: baseY - h,
      cx: padL + i * step + step / 2,
      tick: String(d.label).slice(-2), // YYYY-MM-DD → DD
      showTick: i % tickEvery === 0 || i === n - 1,
    }
  })
})
</script>

<style scoped>
.chart-wrap { width: 100%; }
.chart-svg { width: 100%; height: auto; display: block; }
.bar { fill: var(--teal); transition: fill 0.15s; }
.bar:hover { fill: var(--teal-3); }
.grid { stroke: var(--line); stroke-width: 1; stroke-dasharray: 3 3; }
.axis { stroke: var(--line); stroke-width: 1; }
.tick { fill: var(--muted); font-size: 8px; font-family: var(--font-mono); }
</style>

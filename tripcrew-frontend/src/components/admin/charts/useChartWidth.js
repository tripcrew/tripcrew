import { onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * 컨테이너 폭을 픽셀로 추적하는 컴포저블. 차트를 viewBox 스케일이 아니라
 * 실제 픽셀 좌표로 그리기 위함 — 폭은 부모를 꽉 채우되 높이는 고정되어
 * 와이드 모니터에서도 세로로 늘어지지 않고, 픽셀 좌표라 hover 툴팁도 정확하다.
 *
 * @param {import('vue').Ref<HTMLElement|null>} elRef 측정할 요소 ref
 * @returns {{ width: import('vue').Ref<number> }}
 */
export function useChartWidth(elRef) {
  const width = ref(600)
  let ro = null

  function measure() {
    if (elRef.value) {
      const w = elRef.value.clientWidth
      if (w > 0) width.value = w
    }
  }

  onMounted(() => {
    measure()
    if (typeof ResizeObserver !== 'undefined') {
      ro = new ResizeObserver(measure)
      if (elRef.value) ro.observe(elRef.value)
    } else {
      window.addEventListener('resize', measure)
    }
  })

  onBeforeUnmount(() => {
    if (ro) ro.disconnect()
    else window.removeEventListener('resize', measure)
  })

  return { width }
}

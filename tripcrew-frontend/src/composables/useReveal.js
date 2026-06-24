/**
 * v-reveal — 요소가 뷰포트에 들어오면 1회 fade-up 시키는 디렉티브.
 *   <section v-reveal> ...        // 기본
 *   <section v-reveal="120"> ...  // 120ms 지연(같은 화면에 여러 섹션 동시 노출 시 stagger)
 *
 * 동작: 마운트 시 .reveal-init(숨김) 부여 → IntersectionObserver로 진입하면 .reveal-in 부여(노출 transition).
 * - prefers-reduced-motion 이거나 IO 미지원이면 숨김 단계 없이 즉시 .reveal-in (최종 상태).
 * - 실제 fade-up CSS(.reveal-init / .reveal-in)는 이 디렉티브를 쓰는 컴포넌트에서 정의한다
 *   (no-preference 가드 안에 둘 것 — 끄면 즉시 최종).
 */
function prefersReduced() {
  return typeof window !== 'undefined'
    && window.matchMedia
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

export const vReveal = {
  mounted(el, binding) {
    if (prefersReduced() || typeof window === 'undefined' || !('IntersectionObserver' in window)) {
      el.classList.add('reveal-in')
      return
    }
    el.classList.add('reveal-init')
    if (typeof binding.value === 'number') el.style.transitionDelay = `${binding.value}ms`

    const observer = new IntersectionObserver((entries, obs) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('reveal-in')
          obs.unobserve(entry.target)
        }
      })
    }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' })

    observer.observe(el)
    el._revealObserver = observer
  },
  unmounted(el) {
    if (el._revealObserver) {
      el._revealObserver.disconnect()
      el._revealObserver = null
    }
  },
}

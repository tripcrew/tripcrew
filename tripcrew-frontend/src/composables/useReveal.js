/**
 * 스크롤 진입 시 1회 재생되는 등장 모션 디렉티브 모음.
 * 실제 CSS(.reveal-init/.reveal-in, .stagger-in 자식 규칙)는 쓰는 컴포넌트에서
 * no-preference 가드 안에 정의한다(끄면 즉시 최종 상태).
 *
 *   v-reveal           // 요소 자체가 fade-up (.reveal-init → .reveal-in)
 *   v-reveal="120"     // 120ms 지연
 *   v-stagger          // 컨테이너가 보이면 .stagger-in 부여 → 자식들을 CSS로 순차 등장
 */
function prefersReduced() {
  return typeof window !== 'undefined'
    && window.matchMedia
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function observeOnce(el, onIntersect) {
  const observer = new IntersectionObserver((entries, obs) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        onIntersect(entry.target)
        obs.unobserve(entry.target)
      }
    })
  }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' })
  observer.observe(el)
  return observer
}

function makeDirective(triggerClass, initClass) {
  return {
    mounted(el, binding) {
      if (prefersReduced() || typeof window === 'undefined' || !('IntersectionObserver' in window)) {
        el.classList.add(triggerClass)
        return
      }
      if (initClass) el.classList.add(initClass)
      if (typeof binding.value === 'number') el.style.transitionDelay = `${binding.value}ms`
      el._revealObserver = observeOnce(el, (t) => t.classList.add(triggerClass))
    },
    unmounted(el) {
      if (el._revealObserver) {
        el._revealObserver.disconnect()
        el._revealObserver = null
      }
    },
  }
}

// 요소 자체 fade-up
export const vReveal = makeDirective('reveal-in', 'reveal-init')
// 컨테이너가 보이면 자식들을 순차 등장(컨테이너 자체는 숨기지 않음)
export const vStagger = makeDirective('stagger-in', null)

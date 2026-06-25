import { computed, ref } from 'vue'

/**
 * 라이트 ↔ 다크 테마 토글.
 *
 * - 진실의 원천은 `<html data-theme="...">` 어트리뷰트. 최초 적용은 index.html 의
 *   인라인 스크립트가 페인트 전에 끝내므로(FOUC 방지), 여기서는 그 값을 읽어 와 동기화한다.
 * - 선택은 localStorage('tripcrew-theme')에 영속화. 저장값이 없으면 **다크가 기본**(시스템 설정 무시).
 *   index.html 의 pre-paint 스크립트와 동일 규칙(`saved !== 'light'` → 다크).
 * - 모듈 스코프 ref 라 어느 컴포넌트에서 import 해도 같은 상태를 공유(헤더·관리자 톱바 동기).
 */
const STORAGE_KEY = 'tripcrew-theme'

function resolveInitial() {
  const saved = localStorage.getItem(STORAGE_KEY)
  // 명시적 'light' 저장만 라이트, 그 외(저장 없음·'dark')는 다크 기본
  return saved === 'light' ? 'light' : 'dark'
}

const theme = ref(
  document.documentElement.getAttribute('data-theme') || resolveInitial()
)

function apply(next) {
  theme.value = next
  document.documentElement.setAttribute('data-theme', next)
  try {
    localStorage.setItem(STORAGE_KEY, next)
  } catch {
    /* 사파리 프라이빗 모드 등 storage 차단 시 무시(테마는 세션 내에서 동작) */
  }
}

export function useTheme() {
  // computed off the shared `theme` ref → 헤더·관리자 톱바 토글이 서로 즉시 동기화
  const isDark = computed(() => theme.value === 'dark')

  function toggle() {
    apply(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, isDark, toggle }
}

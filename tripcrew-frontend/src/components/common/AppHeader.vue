<template>
  <header class="app-header">
    <div class="app-header__inner">
      <router-link :to="logoTo" class="app-header__logo">
        TripCrew<span class="dot">.</span>
      </router-link>

      <nav class="app-header__nav" v-if="!minimal">
        <router-link to="/home" class="nav-link">홈</router-link>
        <router-link to="/attractions" class="nav-link">관광지</router-link>
        <router-link to="/chat" class="nav-link">챗봇</router-link>
        <router-link to="/plans" class="nav-link">내 계획</router-link>
        <router-link to="/wishlist" class="nav-link">찜</router-link>
        <router-link to="/notices" class="nav-link">공지</router-link>
      </nav>

      <div class="app-header__actions">
        <button
          class="icon-btn theme-toggle"
          type="button"
          :aria-label="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'"
          :title="isDark ? '라이트 모드' : '다크 모드'"
          @click="toggleTheme"
        >
          <svg v-if="isDark" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="12" cy="12" r="4.2"/>
            <path d="M12 2.5v2M12 19.5v2M4.6 4.6l1.4 1.4M18 18l1.4 1.4M2.5 12h2M19.5 12h2M4.6 19.4 6 18M18 6l1.4-1.4"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M21 12.5A8.5 8.5 0 1 1 11.5 3a6.5 6.5 0 0 0 9.5 9.5z"/>
          </svg>
        </button>

        <template v-if="isLoggedIn">
          <!-- 내부 클릭은 바깥-클릭 핸들러로 전파시키지 않는다. 닫기는 모두 명시적으로 처리
               (더보기/삭제 시 클릭한 엘리먼트가 DOM 에서 사라져 contains 판정이 빗나가는 버그 방지). -->
          <div class="notif" ref="notifRef" @click.stop>
            <button class="icon-btn notif__bell" aria-label="알림" @click="toggleNotif">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 16v-5a6 6 0 1 0-12 0v5l-2 2h16l-2-2zM10 21a2 2 0 0 0 4 0"/>
              </svg>
              <span v-if="unreadCount > 0" class="notif__badge">{{ badgeText }}</span>
            </button>

            <div v-if="notifOpen" class="notif__panel">
              <div class="notif__head">
                <span class="notif__title">알림</span>
                <div v-if="notifications.length > 0" class="notif__actions">
                  <button
                    v-if="unreadCount > 0"
                    class="notif__readall"
                    @click="handleReadAll"
                  >모두 읽음</button>
                  <button
                    class="notif__deleteall"
                    :disabled="notifDeleting"
                    @click="handleDeleteAll"
                  >{{ notifDeleting ? '삭제 중…' : '전체 삭제' }}</button>
                </div>
              </div>

              <div class="notif__list">
                <p v-if="notifLoading" class="notif__empty">불러오는 중…</p>
                <p v-else-if="notifications.length === 0" class="notif__empty">새로운 알림이 없어요</p>
                <div
                  v-for="n in visibleNotifications"
                  :key="n.id"
                  class="notif__item"
                  :class="{ 'notif__item--unread': !n.read }"
                >
                  <button class="notif__main" @click="handleNotifClick(n)">
                    <span v-if="!n.read" class="notif__dot" aria-hidden="true"></span>
                    <span class="notif__body">
                      <span class="notif__msg">{{ n.message }}</span>
                      <span class="notif__meta">
                        <span
                          v-if="chipFor(n)"
                          class="notif__chip"
                          :class="chipFor(n).cls"
                        >{{ chipFor(n).label }}</span>
                        <span class="notif__time">{{ formatRelative(n.createdAt) }}</span>
                      </span>
                    </span>
                  </button>
                  <button class="notif__del" aria-label="알림 삭제" title="삭제" @click.stop="handleDelete(n)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                      <line x1="6" y1="6" x2="18" y2="18"/><line x1="18" y1="6" x2="6" y2="18"/>
                    </svg>
                  </button>
                </div>

                <button
                  v-if="!notifLoading && notifications.length > visibleCount"
                  class="notif__more"
                  @click="showMore"
                >더보기 ({{ notifications.length - visibleCount }})</button>
              </div>
            </div>
          </div>
          <router-link to="/profile" class="avatar avatar--sm" :aria-label="`${displayName} 프로필`">
            {{ avatarText }}
          </router-link>
          <button class="logout-btn" aria-label="로그아웃" title="로그아웃" @click="handleLogout">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            로그아웃
          </button>
        </template>
        <template v-else>
          <router-link :to="{ path: '/auth', query: { mode: 'login' } }" class="nav-link">로그인</router-link>
          <router-link :to="{ path: '/auth', query: { mode: 'signup' } }" class="btn btn--primary btn--sm">회원가입</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { notificationApi, notificationRoute } from '@/api/notifications'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

const { isDark, toggle: toggleTheme } = useTheme()

const props = defineProps({
  loggedIn: { type: Boolean, default: null },
  minimal: { type: Boolean, default: false }
})

const router = useRouter()
const authStore = useAuthStore()
const isLoggedIn = computed(() => props.loggedIn ?? authStore.isAuthenticated)

async function handleLogout() {
  if (!window.confirm('로그아웃하시겠어요?')) return
  await authStore.logout()
  router.replace('/')
}

/* ── 알림 ─────────────────────────────────────────────
   벨 뱃지(미읽음 수)는 진입 시 + 60초 폴링으로 갱신,
   드롭다운을 열 때 최근 목록을 불러온다. 클릭 시 읽음 처리 + ref 경로로 이동. */
const UNREAD_POLL_MS = 60000
const NOTIF_PAGE = 10 // 한 번에 보여줄 개수(그 이상은 '더보기')

const notifRef = ref(null)
const notifOpen = ref(false)
const notifLoading = ref(false)
const notifDeleting = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
const visibleCount = ref(NOTIF_PAGE)
const badgeText = computed(() => (unreadCount.value > 9 ? '9+' : String(unreadCount.value)))
const visibleNotifications = computed(() => notifications.value.slice(0, visibleCount.value))

let pollTimer = null

async function refreshUnread() {
  if (!isLoggedIn.value) return
  try {
    unreadCount.value = await notificationApi.unreadCount()
  } catch {
    /* 네트워크/인증 오류는 조용히 무시(뱃지 비핵심) */
  }
}

async function toggleNotif() {
  notifOpen.value = !notifOpen.value
  if (!notifOpen.value) return
  visibleCount.value = NOTIF_PAGE // 열 때마다 처음 N개부터
  notifLoading.value = true
  try {
    notifications.value = await notificationApi.list()
  } catch {
    notifications.value = []
  } finally {
    notifLoading.value = false
  }
}

function showMore() {
  visibleCount.value += NOTIF_PAGE
}

async function handleDelete(n) {
  const wasUnread = !n.read
  try {
    await notificationApi.remove(n.id)
    notifications.value = notifications.value.filter((x) => x.id !== n.id)
    if (wasUnread) unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    /* 무시 */
  }
}

async function handleDeleteAll() {
  if (notifDeleting.value || notifications.value.length === 0) return
  notifDeleting.value = true
  try {
    await notificationApi.removeAll()
    notifications.value = []
    unreadCount.value = 0
  } catch {
    /* 삭제 실패 시 현재 목록을 유지한다. */
  } finally {
    notifDeleting.value = false
  }
}

async function handleReadAll() {
  try {
    await notificationApi.markAllRead()
    notifications.value = notifications.value.map((n) => ({ ...n, read: true }))
    unreadCount.value = 0
  } catch {
    /* 무시 */
  }
}

async function handleNotifClick(n) {
  if (!n.read) {
    try {
      await notificationApi.markRead(n.id)
      n.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {
      /* 읽음 실패해도 이동은 진행 */
    }
  }
  const to = notificationRoute(n)
  notifOpen.value = false
  if (to) router.push(to)
}

/** 신고 결과 알림에 붙는 상태 칩(처리완료/기각). 그 외 type 은 칩 없음(null). */
function chipFor(n) {
  if (n.type === 'REPORT_RESOLVED') return { label: '처리완료', cls: 'notif__chip--resolved' }
  if (n.type === 'REPORT_DISMISSED') return { label: '기각', cls: 'notif__chip--dismissed' }
  if (n.type === 'INQUIRY_ANSWERED') return { label: '답변완료', cls: 'notif__chip--resolved' }
  if (n.type === 'SANCTION_APPLIED') return { label: '제재 안내', cls: 'notif__chip--dismissed' }
  if (n.type === 'SANCTION_REVIEW_REQUIRED') return { label: '검토 요청', cls: 'notif__chip--dismissed' }
  return null
}

/** 상대 시각(방금/n분 전/n시간 전/n일 전), 그 이상은 날짜. */
function formatRelative(iso) {
  if (!iso) return ''
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return ''
  const diff = Date.now() - then
  const min = Math.floor(diff / 60000)
  if (min < 1) return '방금'
  if (min < 60) return `${min}분 전`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}시간 전`
  const day = Math.floor(hour / 24)
  if (day < 7) return `${day}일 전`
  return new Date(iso).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' })
}

function onDocClick(e) {
  if (notifOpen.value && notifRef.value && !notifRef.value.contains(e.target)) {
    notifOpen.value = false
  }
}

function startPolling() {
  stopPolling()
  refreshUnread()
  pollTimer = window.setInterval(refreshUnread, UNREAD_POLL_MS)
}

function stopPolling() {
  if (pollTimer) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
}

// 로그인 상태가 바뀌면 폴링 시작/정지 (로그아웃 시 뱃지 초기화)
watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    startPolling()
  } else {
    stopPolling()
    notifOpen.value = false
    notifications.value = []
    unreadCount.value = 0
  }
})

onMounted(() => {
  document.addEventListener('click', onDocClick)
  if (isLoggedIn.value) startPolling()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
  stopPolling()
})
const logoTo = computed(() => isLoggedIn.value ? '/home' : '/')
const displayName = computed(() => authStore.user?.nickname || authStore.user?.email || '사용자')
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || 'U')
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--header-bg);
  backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid var(--line);
  height: var(--header-height);
}

.app-header__inner {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--space-6);
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

.app-header__logo {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.6px;
  color: var(--teal-ink);
}

.app-header__logo .dot {
  color: var(--coral);
}

.app-header__nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex: 1;
}

.nav-link {
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-3);
  transition: all 0.15s;
}

.nav-link:hover {
  background: var(--bg-2);
  color: var(--ink);
}

.nav-link.router-link-active {
  background: var(--teal-soft);
  color: var(--teal-ink);
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.icon-btn {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: var(--ink-3);
  transition: background 0.15s;
}

.icon-btn:hover {
  background: var(--bg-2);
  color: var(--ink);
}

/* ── 알림 벨 + 드롭다운 ── */
.notif {
  position: relative;
}

.notif__bell {
  position: relative;
}

.notif__badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--coral);
  color: white;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  box-shadow: 0 0 0 2px var(--header-bg);
}

.notif__panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 320px;
  max-height: 420px;
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  z-index: 200;
}

.notif__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--line);
}

.notif__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--ink);
}

.notif__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notif__readall {
  font-size: 12px;
  font-weight: 600;
  color: var(--teal-ink);
}

.notif__deleteall {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-3);
}

.notif__deleteall:hover:not(:disabled) {
  color: var(--coral);
  text-decoration: underline;
}

.notif__deleteall:disabled {
  cursor: wait;
  opacity: 0.6;
}

.notif__readall:hover {
  text-decoration: underline;
}

.notif__list {
  overflow-y: auto;
}

.notif__empty {
  padding: 28px 16px;
  text-align: center;
  font-size: 13px;
  color: var(--ink-3);
}

.notif__item {
  display: flex;
  align-items: stretch;
  width: 100%;
  border-bottom: 1px solid var(--bg-2);
  transition: background 0.15s;
}

.notif__item:last-child {
  border-bottom: none;
}

.notif__item:hover {
  background: var(--bg-2);
}

.notif__item--unread {
  background: var(--teal-soft);
}

.notif__item--unread:hover {
  background: var(--teal-soft);
}

/* 클릭 영역(읽음+이동). 행의 대부분을 차지 */
.notif__main {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
  padding: 12px 8px 12px 16px;
  text-align: left;
}

/* 삭제(x). 평소 흐릿, hover 시 또렷. */
.notif__del {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 34px;
  color: var(--ink-3);
  opacity: 0.4;
  transition: opacity 0.15s, color 0.15s;
}

.notif__item:hover .notif__del {
  opacity: 1;
}

.notif__del:hover {
  color: var(--coral);
}

.notif__more {
  width: 100%;
  padding: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--teal-ink);
  border-top: 1px solid var(--line);
  transition: background 0.15s;
}

.notif__more:hover {
  background: var(--bg-2);
}

.notif__dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  margin-top: 5px;
  border-radius: 50%;
  background: var(--coral);
}

.notif__body {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.notif__msg {
  font-size: 13px;
  line-height: 1.4;
  color: var(--ink);
}

.notif__meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notif__chip {
  display: inline-flex;
  align-items: center;
  padding: 1px 7px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  line-height: 1.6;
}

.notif__chip--resolved {
  background: var(--teal-3);
  color: white;
}

.notif__chip--dismissed {
  background: var(--surface);
  color: var(--ink-3);
  border: 1px solid var(--line);
}

.notif__time {
  font-size: 11px;
  color: var(--ink-3);
}

/* 로그아웃: 아이콘 + 텍스트. hover 시 '나가기'를 암시하는 coral 틴트 */
.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-3);
  white-space: nowrap;
  transition: all 0.15s;
}

.logout-btn:hover {
  background: var(--coral-tint, #fff1ec);
  color: var(--coral, #e06a4f);
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--teal);
  color: white;
  font-weight: 700;
  font-size: 14px;
  transition: background 0.15s, transform 0.15s;
}

.avatar:hover {
  background: var(--teal-2);
  transform: translateY(-1px);
}

.avatar--sm { width: 32px; height: 32px; font-size: 13px; }

.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.15s;
  white-space: nowrap;
}

.btn--primary {
  background: var(--coral);
  color: white;
}

.btn--primary:hover {
  background: var(--coral-2);
}

.btn--sm {
  padding: 7px 14px;
  font-size: 13px;
}
</style>

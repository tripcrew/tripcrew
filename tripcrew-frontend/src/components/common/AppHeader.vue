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
        <template v-if="isLoggedIn">
          <button class="icon-btn" aria-label="알림">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 16v-5a6 6 0 1 0-12 0v5l-2 2h16l-2-2zM10 21a2 2 0 0 0 4 0"/>
            </svg>
          </button>
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

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
const logoTo = computed(() => isLoggedIn.value ? '/home' : '/')
const displayName = computed(() => authStore.user?.nickname || authStore.user?.email || '사용자')
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || 'U')
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
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
  color: var(--teal-3);
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
  color: var(--teal-3);
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

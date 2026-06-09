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
        <a href="#" class="nav-link">커뮤니티</a>
      </nav>

      <div class="app-header__actions">
        <template v-if="loggedIn">
          <button class="icon-btn" aria-label="알림">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 16v-5a6 6 0 1 0-12 0v5l-2 2h16l-2-2zM10 21a2 2 0 0 0 4 0"/>
            </svg>
          </button>
          <div class="avatar avatar--sm">민</div>
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

const props = defineProps({
  loggedIn: { type: Boolean, default: true },
  minimal: { type: Boolean, default: false }
})

const logoTo = computed(() => props.loggedIn ? '/home' : '/')
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

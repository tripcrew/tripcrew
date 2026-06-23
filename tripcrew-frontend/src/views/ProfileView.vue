<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container profile-page">
      <section class="profile-panel">
        <div class="profile-head">
          <div class="avatar avatar--lg">{{ avatarText }}</div>
          <div>
            <p class="t-caption">마이페이지</p>
            <h1 class="profile-title">{{ displayName }} 님</h1>
            <p class="profile-email">{{ userEmail }}</p>
          </div>
        </div>

        <dl class="profile-info">
          <div>
            <dt>닉네임</dt>
            <dd>{{ displayName }}</dd>
          </div>
          <div>
            <dt>이메일</dt>
            <dd>{{ userEmail }}</dd>
          </div>
          <div>
            <dt>권한</dt>
            <dd>{{ userRole }}</dd>
          </div>
        </dl>

        <div class="profile-actions">
          <BaseButton variant="secondary" @click="goHome">홈으로</BaseButton>
          <BaseButton variant="danger" @click="handleLogout">로그아웃</BaseButton>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const displayName = computed(() => authStore.user?.nickname || '여행자')
const userEmail = computed(() => authStore.user?.email || '이메일 정보 없음')
const userRole = computed(() => authStore.user?.role || 'USER')
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || 'U')

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace({ path: '/auth', query: { mode: 'login' } })
  }
})

function goHome() {
  router.push('/home')
}

async function handleLogout() {
  const confirmed = window.confirm('로그아웃하시겠어요?')
  if (!confirmed) return

  await authStore.logout()
  router.replace('/')
}
</script>

<style scoped>
.profile-page {
  padding: 48px var(--space-6) 80px;
}

.profile-panel {
  max-width: 720px;
  margin: 0 auto;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 32px;
  box-shadow: var(--sh-1);
}

.profile-head {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding-bottom: 28px;
  border-bottom: 1px solid var(--line);
}

.profile-title {
  margin: 2px 0 4px;
  font-size: 28px;
  font-weight: 800;
}

.profile-email {
  color: var(--ink-soft);
}

.avatar {
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--teal);
  color: white;
  font-weight: 800;
}

.avatar--lg {
  width: 72px;
  height: 72px;
  font-size: 28px;
  flex: 0 0 auto;
}

.profile-info {
  display: grid;
  gap: 12px;
  margin: 28px 0;
}

.profile-info div {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: var(--space-4);
  padding: 14px 16px;
  background: var(--bg-soft);
  border-radius: var(--r-md);
}

.profile-info dt {
  color: var(--ink-soft);
  font-size: 14px;
}

.profile-info dd {
  color: var(--ink);
  font-weight: 700;
}

.profile-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

@media (max-width: 640px) {
  .profile-panel {
    padding: 24px;
  }

  .profile-head {
    align-items: flex-start;
  }

  .profile-info div {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  .profile-actions {
    flex-direction: column;
  }
}
</style>

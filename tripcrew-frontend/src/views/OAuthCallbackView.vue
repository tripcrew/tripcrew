<template>
  <div class="oauth-callback">
    <div class="oauth-card">
      <template v-if="errorMessage">
        <div class="oauth-icon oauth-icon--error">!</div>
        <h1 class="oauth-title">로그인하지 못했어요</h1>
        <p class="oauth-desc">{{ errorMessage }}</p>
        <BaseButton variant="primary" size="lg" @click="goAuth">로그인으로 돌아가기</BaseButton>
      </template>
      <template v-else>
        <span class="oauth-spinner" aria-hidden="true"></span>
        <h1 class="oauth-title">로그인 중…</h1>
        <p class="oauth-desc">잠시만 기다려 주세요.</p>
      </template>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import BaseButton from '@/components/common/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const errorMessage = ref('')

// 제공자/백엔드가 돌려준 에러코드 → 사용자용 안내 문구.
const ERROR_MESSAGES = {
  email_required: '이메일 제공에 동의해야 가입할 수 있어요. 다시 시도해 주세요.',
  email_not_verified: '이메일이 검증되지 않아 기존 계정과 자동 연동할 수 없어요.',
  account_unavailable: '이용할 수 없는 계정입니다. 관리자에게 문의해 주세요.',
}

function messageFor(code) {
  return ERROR_MESSAGES[code] || '소셜 로그인에 실패했어요. 다시 시도해 주세요.'
}

function redirectTarget() {
  const role = authStore.user?.role
  if (role === 'ADMIN' || role === 'SUPER_ADMIN') return '/admin'
  return '/home'
}

function goAuth() {
  router.replace({ path: '/auth', query: { mode: 'login' } })
}

onMounted(async () => {
  const error = typeof route.query.error === 'string' ? route.query.error : ''
  const code = typeof route.query.code === 'string' ? route.query.code : ''

  if (error) {
    errorMessage.value = messageFor(error)
    return
  }
  if (!code) {
    errorMessage.value = messageFor()
    return
  }
  try {
    await authStore.loginWithOAuthCode(code)
    router.replace(redirectTarget())
  } catch {
    errorMessage.value = messageFor()
  }
})
</script>

<style scoped>
.oauth-callback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 0% 0%, var(--teal-tint) 0%, transparent 40%),
    radial-gradient(circle at 100% 100%, var(--coral-tint) 0%, transparent 40%),
    var(--bg);
}

.oauth-card {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 28px;
  text-align: center;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.oauth-title {
  margin: 4px 0 0;
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--ink);
}

.oauth-desc {
  margin: 0 0 8px;
  font-size: 0.9rem;
  color: var(--muted);
}

.oauth-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--teal-tint);
  border-top-color: var(--teal);
  border-radius: 50%;
  animation: oauth-spin 0.8s linear infinite;
}

.oauth-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  font-size: 1.4rem;
  font-weight: 800;
}

.oauth-icon--error {
  background: var(--danger-tint, var(--coral-tint));
  color: var(--danger);
}

@keyframes oauth-spin {
  to { transform: rotate(360deg); }
}

@media (prefers-reduced-motion: reduce) {
  .oauth-spinner { animation: none; }
}

/* 반응형: 폰에서 카드 여백 축소(내용은 이미 중앙 정렬·max-width 360) */
@media (max-width: 640px) {
  .oauth-callback { padding: 20px; }
  .oauth-card { padding: 32px 22px; }
}
</style>

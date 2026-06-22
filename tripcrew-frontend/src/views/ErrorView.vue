<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container error-layout">
      <section class="error-focus">
        <article class="error-card" :class="def.cardClass">
          <span class="error-tag t-mono">{{ def.tag }}</span>
          <div class="error-icon">{{ def.icon }}</div>
          <h2 class="t-h2">{{ def.title }}</h2>
          <p class="error-desc">
            <span v-for="(line, i) in def.desc" :key="i" class="error-desc-line">{{ line }}</span>
          </p>
          <div class="error-actions">
            <BaseButton
              v-for="action in def.actions"
              :key="action.label"
              :variant="action.variant"
              @click="$router.push(action.to)"
            >{{ action.label }}</BaseButton>
          </div>
          <p v-if="def.meta" class="error-meta t-mono">{{ def.meta }}</p>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const route = useRoute()

/**
 * 에러 코드별 화면 정의. 라우트 /errors/:type 의 type 으로 고른다.
 * 미지정/미정의 type 은 404 로 폴백.
 */
const ERRORS = {
  '403': {
    cardClass: 'error-card--breaker',
    tag: 'HTTP 403 · FORBIDDEN',
    icon: '🔒',
    title: '접근 권한이 없어요',
    desc: ['이 페이지는 관리자 전용입니다.', '현재 계정 권한으로는 접근할 수 없어요.'],
    actions: [
      { label: '홈으로', variant: 'primary', to: '/home' },
      { label: '내 여행 계획', variant: 'secondary', to: '/plans' },
    ],
    meta: 'error_code: FORBIDDEN',
  },
  '404': {
    cardClass: 'error-card--404',
    tag: 'HTTP 404',
    icon: '🧭',
    title: '길을 잃으셨나요?',
    desc: ['찾으시는 페이지가 존재하지 않거나 삭제되었어요.', '주소를 다시 확인하거나 홈으로 돌아가보세요.'],
    actions: [
      { label: '홈으로', variant: 'primary', to: '/' },
      { label: '관광지 둘러보기', variant: 'secondary', to: '/attractions' },
    ],
    meta: 'error_code: NOT_FOUND',
  },
  cb: {
    cardClass: 'error-card--breaker',
    tag: 'CIRCUIT BREAKER · OPEN',
    icon: '⚡',
    title: '관광지 정보를 불러오지 못했어요',
    desc: ['한국관광공사 시스템에 일시적인 문제가 있어요.', '잠시 후 자동으로 다시 시도합니다.'],
    actions: [{ label: '홈으로', variant: 'primary', to: '/home' }],
    meta: 'error_code: CB_OPEN',
  },
  offline: {
    cardClass: 'error-card--offline',
    tag: 'OFFLINE',
    icon: '📡',
    title: '인터넷 연결을 확인해주세요',
    desc: ['네트워크 신호가 약하거나 끊겼어요.', '연결이 복구되면 다시 시도해주세요.'],
    actions: [{ label: '홈으로', variant: 'primary', to: '/home' }],
    meta: '',
  },
}

const def = computed(() => ERRORS[route.params.type] || ERRORS['404'])
</script>

<style scoped>
.error-layout {
  padding: 40px var(--space-6) 80px;
}

.error-focus {
  max-width: 520px;
  margin: 40px auto;
}

.error-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 36px;
  text-align: center;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  min-height: 380px;
}

/* Variants */
.error-card--breaker {
  background: linear-gradient(135deg, #FFF5F5 0%, white 60%);
  border-color: #FBEAE2;
}

.error-card--offline {
  background: linear-gradient(135deg, #E8F1F7 0%, white 60%);
  border-color: #DBEAF2;
}

.error-card--404 {
  background: linear-gradient(135deg, #F4F0FF 0%, white 60%);
  border-color: #EEEAFB;
}

.error-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  padding: 4px 10px;
  background: rgba(15, 23, 42, 0.06);
  color: var(--ink-3);
  border-radius: 4px;
}

.error-card--breaker .error-tag { background: var(--coral); color: white; }
.error-card--offline .error-tag { background: var(--info); color: white; }
.error-card--404 .error-tag { background: var(--violet); color: white; }

.error-icon {
  font-size: 64px;
  line-height: 1;
  margin-top: 28px;
  margin-bottom: 4px;
}

.error-card h2 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.4px;
  color: var(--ink);
}

.error-desc {
  font-size: 14px;
  color: var(--ink-3);
  line-height: 1.6;
  max-width: 360px;
}

.error-desc-line {
  display: block;
}

.error-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.error-meta {
  margin-top: auto;
  padding-top: 14px;
  font-size: 11px;
  color: var(--muted);
}
</style>

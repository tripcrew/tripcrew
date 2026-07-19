<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container detail-layout">
      <nav class="breadcrumb">
        <RouterLink to="/notices" class="crumb-link">공지사항</RouterLink> › <strong>상세</strong>
      </nav>

      <section v-if="error" class="state-panel">
        <strong>공지를 불러오지 못했습니다</strong>
        <p>{{ error }}</p>
        <BaseButton variant="secondary" @click="load">다시 시도</BaseButton>
      </section>

      <p v-else-if="loading" class="list-empty">불러오는 중…</p>

      <article v-else-if="notice" class="notice-article">
        <header class="article-head">
          <span v-if="notice.pinned" class="pin-badge">📌 고정 공지</span>
          <h1 class="t-h1">{{ notice.title }}</h1>
          <div class="article-meta t-mono">
            {{ authorName }} · {{ formatDate(notice.createdAt) }} · 조회 {{ notice.viewCount }}
          </div>
        </header>

        <div class="article-body">{{ notice.content }}</div>

        <footer class="article-foot">
          <RouterLink to="/notices"><BaseButton variant="secondary">목록으로</BaseButton></RouterLink>
        </footer>
      </article>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'

import { noticeApi } from '@/api/notices'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const route = useRoute()
const notice = ref(null)
const loading = ref(false)
const error = ref('')

const authorName = computed(() => (notice.value && notice.value.authorNickname) || '관리자')

function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10).replaceAll('-', '.')
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    notice.value = await noticeApi.detail(route.params.id)
  } catch (e) {
    if (e.response?.status === 404) error.value = '존재하지 않는 공지입니다.'
    else error.value = e.response?.data?.message || e.message || '알 수 없는 오류'
  } finally {
    loading.value = false
  }
}

// /notices/:id 간 이동 시 재조회
watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.detail-layout { padding-top: 32px; padding-bottom: 64px; max-width: 820px; }

.breadcrumb {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 16px;
}
.breadcrumb strong { color: var(--ink); }
.crumb-link { color: var(--teal-ink); text-decoration: none; }
.crumb-link:hover { text-decoration: underline; }

.notice-article {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 32px;
}

.article-head { border-bottom: 1px solid var(--line); padding-bottom: 20px; }
.pin-badge {
  display: inline-block;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--teal-ink);
}
.article-meta { margin-top: 12px; font-size: 12px; color: var(--muted); }

.article-body {
  margin-top: 24px;
  font-size: 15px;
  line-height: 1.8;
  color: var(--ink-2);
  white-space: pre-wrap;
  word-break: break-word;
}

.article-foot { margin-top: 32px; }

.list-empty { padding: 48px 20px; text-align: center; color: var(--muted); }

.state-panel {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}
.state-panel strong { font-size: 16px; color: var(--ink); }
.state-panel p { font-size: 13px; color: var(--ink-soft); }

/* ── 반응형: 공지 상세 모바일 대응 ── */
@media (max-width: 640px) {
  .detail-layout { padding-bottom: 48px; }
  /* 본문 카드 패딩 축소 */
  .notice-article { padding: 20px 18px; }
  .article-head { padding-bottom: 16px; }
  /* 긴 제목 줄바꿈 보장 */
  .article-head .t-h1 { overflow-wrap: anywhere; }
  .article-body { margin-top: 20px; font-size: 14px; line-height: 1.75; }
  .article-foot { margin-top: 24px; }
}
</style>

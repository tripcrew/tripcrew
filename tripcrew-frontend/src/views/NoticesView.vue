<template>
  <div class="page page-soft page-ambient">
    <AppHeader />

    <main class="container notices-layout">
      <nav class="breadcrumb">
        TripCrew › <strong>공지사항</strong>
      </nav>

      <header class="page-head">
        <h1 class="t-h1">공지사항</h1>
        <p class="t-caption">서비스 점검 · 신규 기능 · 이용 정책 안내</p>
      </header>

      <section v-if="error" class="state-panel">
        <strong>목록을 불러오지 못했습니다</strong>
        <p>{{ error }}</p>
        <BaseButton variant="secondary" @click="load">다시 시도</BaseButton>
      </section>

      <section v-else class="notice-list">
        <p v-if="loading" class="list-empty">불러오는 중…</p>
        <p v-else-if="notices.length === 0" class="list-empty">등록된 공지가 없습니다.</p>

        <RouterLink
          v-for="n in notices"
          :key="n.id"
          :to="`/notices/${n.id}`"
          class="notice-row"
          :class="{ pinned: n.pinned }"
        >
          <span v-if="n.pinned" class="pin-badge">📌 고정</span>
          <span class="notice-title">{{ n.title }}</span>
          <span class="notice-meta t-mono">
            {{ formatDate(n.createdAt) }} · 조회 {{ n.viewCount }}
          </span>
        </RouterLink>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { noticeApi } from '@/api/notices'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const notices = ref([])
const loading = ref(false)
const error = ref('')

function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10).replaceAll('-', '.')
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    notices.value = await noticeApi.list()
  } catch (e) {
    error.value = e.response?.data?.message || e.message || '알 수 없는 오류'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.notices-layout { padding-top: 32px; padding-bottom: 64px; }

.breadcrumb {
  font-size: 13px;
  color: var(--ink-soft);
  margin-bottom: 16px;
}
.breadcrumb strong { color: var(--ink); }

.page-head { margin-bottom: 24px; }
.page-head .t-caption { margin-top: 6px; color: var(--ink-soft); }

.notice-list {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  overflow: hidden;
}

.notice-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
  text-decoration: none;
  color: var(--ink-2);
  transition: background 0.15s;
}
.notice-row:last-child { border-bottom: none; }
.notice-row:hover { background: var(--bg-soft); }
.notice-row.pinned { background: var(--teal-soft); }
.notice-row.pinned:hover { background: var(--teal-soft); filter: brightness(0.98); }

.pin-badge {
  font-size: 12px;
  font-weight: 700;
  color: var(--teal-ink);
  white-space: nowrap;
}

.notice-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-meta {
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
}

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

/* ── 반응형: 공지 목록 행 모바일 대응 ── */
@media (max-width: 640px) {
  .notices-layout { padding-bottom: 48px; }
  .page-head { margin-bottom: 20px; }
  /* 제목 | 날짜·조회수가 한 줄에 꽉 차면 날짜가 아래로 자연스럽게 감싸지도록 */
  .notice-row { flex-wrap: wrap; row-gap: 4px; padding: 14px 16px; }
  .notice-title { white-space: normal; }
  .notice-meta { font-size: 11px; }
}
</style>

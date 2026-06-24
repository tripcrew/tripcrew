<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="support-page">
      <div class="support-page__inner">
        <router-link to="/" class="back-link">← TripCrew 홈으로</router-link>

        <header class="support-head">
          <p class="support-head__eyebrow">TRIPCREW · SUPPORT</p>
          <h1>1:1 문의</h1>
          <p class="support-head__lead">궁금한 점이나 불편한 점을 남겨 주세요. 관리자가 확인 후 답변해 드립니다. 답변이 등록되면 알림으로 알려드려요.</p>
          <p class="support-head__hint">일반적인 질문은 <router-link to="/faq">자주 묻는 질문</router-link>과 <router-link to="/notices">공지사항</router-link>에서 먼저 확인하실 수 있어요.</p>
        </header>

        <!-- 토스트(인라인): 작성 성공/오류를 부드럽게 알림 -->
        <p v-if="flash" :class="['support-flash', flash.type === 'error' ? 'support-flash--error' : 'support-flash--ok']">
          {{ flash.text }}
        </p>

        <!-- 비로그인: 로그인 유도 -->
        <section v-if="!isLoggedIn" class="support-card support-card--login">
          <div class="support-card__icon" aria-hidden="true">✎</div>
          <h2>문의하려면 로그인이 필요해요</h2>
          <p class="support-card__desc">로그인하면 문의를 남기고 답변을 확인할 수 있습니다.</p>
          <router-link :to="{ path: '/auth', query: { mode: 'login', redirect: '/support' } }" class="support-button support-button--primary">로그인하기</router-link>
        </section>

        <template v-else>
          <!-- 문의 작성 폼 -->
          <section class="support-card">
            <h2 class="support-card__title">문의 작성</h2>
            <form class="inquiry-form" @submit.prevent="submit">
              <label class="field">
                <span class="field__label">제목</span>
                <input
                  v-model.trim="form.title"
                  type="text"
                  class="field__input"
                  maxlength="150"
                  placeholder="문의 제목을 입력하세요"
                  :disabled="submitting"
                />
              </label>
              <label class="field">
                <span class="field__label">내용</span>
                <textarea
                  v-model.trim="form.content"
                  class="field__input field__textarea"
                  maxlength="5000"
                  rows="6"
                  placeholder="문의 내용을 자세히 적어 주세요"
                  :disabled="submitting"
                ></textarea>
                <span class="field__count">{{ form.content.length }} / 5000</span>
              </label>
              <div class="inquiry-form__actions">
                <BaseButton type="submit" variant="primary" :disabled="!canSubmit">
                  {{ submitting ? '등록 중…' : '문의 등록' }}
                </BaseButton>
              </div>
            </form>
          </section>

          <!-- 내 문의 내역 -->
          <section class="support-list" aria-label="내 문의 내역">
            <div class="support-list__head">
              <h2 class="support-card__title">내 문의 내역</h2>
              <span class="support-list__count">{{ inquiries.length }}건</span>
            </div>

            <p v-if="loading" class="support-empty">불러오는 중…</p>
            <p v-else-if="inquiries.length === 0" class="support-empty">아직 등록한 문의가 없어요.</p>

            <ul v-else class="inquiry-items">
              <li v-for="iq in inquiries" :key="iq.id" class="inquiry-item">
                <button class="inquiry-item__head" @click="toggle(iq.id)">
                  <span class="inquiry-item__title">{{ iq.title }}</span>
                  <span class="inquiry-item__meta">
                    <span :class="['status-chip', iq.status === 'ANSWERED' ? 'status-chip--answered' : 'status-chip--open']">
                      {{ iq.status === 'ANSWERED' ? '답변완료' : '답변 대기' }}
                    </span>
                    <span class="inquiry-item__date">{{ formatDate(iq.createdAt) }}</span>
                    <svg class="inquiry-item__caret" :class="{ open: expandedId === iq.id }" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="6 9 12 15 18 9"/></svg>
                  </span>
                </button>

                <div v-if="expandedId === iq.id" class="inquiry-item__body">
                  <div class="inquiry-block">
                    <span class="inquiry-block__label">문의 내용</span>
                    <p class="inquiry-block__text">{{ iq.content }}</p>
                  </div>
                  <div v-if="iq.answer" class="inquiry-block inquiry-block--answer">
                    <span class="inquiry-block__label">관리자 답변 <em v-if="iq.answeredAt">· {{ formatDate(iq.answeredAt) }}</em></span>
                    <p class="inquiry-block__text">{{ iq.answer }}</p>
                  </div>
                  <p v-else class="inquiry-pending">아직 답변이 등록되지 않았어요. 답변이 등록되면 알림으로 알려드릴게요.</p>
                </div>
              </li>
            </ul>
          </section>
        </template>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import AppFooter from '@/components/common/AppFooter.vue'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { inquiryApi } from '@/api/inquiries'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const isLoggedIn = computed(() => auth.isAuthenticated)

const form = ref({ title: '', content: '' })
const submitting = ref(false)
const inquiries = ref([])
const loading = ref(false)
const expandedId = ref(null)
const flash = ref(null)

const canSubmit = computed(
  () => !submitting.value && form.value.title.length > 0 && form.value.content.length > 0,
)

let flashTimer = null
function showFlash(type, text) {
  flash.value = { type, text }
  if (flashTimer) window.clearTimeout(flashTimer)
  flashTimer = window.setTimeout(() => { flash.value = null }, 4000)
}

function formatDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10).replaceAll('-', '.')
}

function toggle(id) {
  expandedId.value = expandedId.value === id ? null : id
}

async function loadMine() {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    inquiries.value = await inquiryApi.myList()
  } catch {
    inquiries.value = []
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await inquiryApi.create({ title: form.value.title, content: form.value.content })
    form.value = { title: '', content: '' }
    showFlash('ok', '문의가 등록되었어요. 답변이 등록되면 알림으로 알려드릴게요.')
    await loadMine()
  } catch (e) {
    const msg = (e.response && e.response.data && e.response.data.message) || '문의 등록에 실패했어요. 잠시 후 다시 시도해 주세요.'
    showFlash('error', msg)
  } finally {
    submitting.value = false
  }
}

onMounted(loadMine)
</script>

<style scoped>
.support-page { padding: 56px var(--space-6) 0; }
.support-page__inner { max-width: 760px; margin: 0 auto; }

.back-link {
  display: inline-flex;
  margin-bottom: 24px;
  color: var(--teal-3);
  font-size: 14px;
  font-weight: 700;
}

.support-head { margin-bottom: 20px; }
.support-head__eyebrow {
  color: var(--teal);
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.8px;
}
.support-head h1 { margin-top: 8px; color: var(--ink); font-size: 30px; letter-spacing: -0.8px; }
.support-head__lead { margin-top: 12px; color: var(--ink-3); font-size: 16px; line-height: 1.6; }
.support-head__hint { margin-top: 8px; color: var(--ink-soft); font-size: 13px; }
.support-head__hint a { color: var(--teal-3); font-weight: 700; }

/* 인라인 토스트 */
.support-flash {
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: var(--r-md);
  font-size: 14px;
  font-weight: 600;
}
.support-flash--ok { background: #E1F5EA; color: #1A7A4A; }
.support-flash--error { background: #FFE5E8; color: #B12C3A; }

.support-card {
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  background: var(--bg);
  padding: 28px;
  margin-bottom: 20px;
}
.support-card__title { color: var(--ink); font-size: 18px; margin-bottom: 18px; }

.support-card--login { text-align: center; padding: 48px 28px; }
.support-card__icon {
  display: grid;
  width: 52px;
  height: 52px;
  margin: 0 auto 18px;
  place-items: center;
  border-radius: 50%;
  background: var(--teal-soft);
  color: var(--teal);
  font-size: 24px;
}
.support-card--login h2 { color: var(--ink); font-size: 20px; }
.support-card__desc { margin-top: 10px; color: var(--ink-soft); font-size: 14px; }
.support-card--login .support-button { margin-top: 22px; }

/* 폼 */
.inquiry-form { display: grid; gap: 18px; }
.field { display: grid; gap: 7px; position: relative; }
.field__label { font-size: 13px; font-weight: 700; color: var(--ink-2); }
.field__input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--line-2);
  border-radius: var(--r-md);
  font-size: 14px;
  color: var(--ink);
  background: var(--bg);
  transition: border-color 0.15s;
}
.field__input:focus { outline: none; border-color: var(--teal); }
.field__textarea { resize: vertical; line-height: 1.6; }
.field__count { align-self: flex-end; font-size: 12px; color: var(--muted); }
.inquiry-form__actions { display: flex; justify-content: flex-end; }

/* 내 문의 내역 */
.support-list {
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  background: var(--bg);
  padding: 28px;
}
.support-list__head { display: flex; align-items: baseline; gap: 10px; margin-bottom: 16px; }
.support-list__count { font-size: 13px; color: var(--ink-soft); font-weight: 600; }
.support-empty { padding: 28px 0; text-align: center; color: var(--ink-soft); font-size: 14px; }

.inquiry-items { display: grid; gap: 10px; }
.inquiry-item {
  border: 1px solid var(--line);
  border-radius: var(--r-md);
  overflow: hidden;
}
.inquiry-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
}
.inquiry-item__head:hover { background: var(--bg-2); }
.inquiry-item__title { font-size: 14px; font-weight: 700; color: var(--ink); min-width: 0; word-break: break-word; }
.inquiry-item__meta { display: inline-flex; align-items: center; gap: 10px; flex-shrink: 0; }
.inquiry-item__date { font-size: 12px; color: var(--ink-soft); font-family: var(--font-mono); }
.inquiry-item__caret { color: var(--muted); transition: transform 0.15s; }
.inquiry-item__caret.open { transform: rotate(180deg); }

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}
.status-chip--open { background: var(--bg-2); color: var(--ink-3); border: 1px solid var(--line-2); }
.status-chip--answered { background: var(--teal-3); color: white; }

.inquiry-item__body { padding: 4px 16px 18px; border-top: 1px solid var(--bg-2); }
.inquiry-block { margin-top: 14px; }
.inquiry-block__label { display: block; font-size: 12px; font-weight: 700; color: var(--ink-soft); margin-bottom: 6px; }
.inquiry-block__label em { font-style: normal; font-weight: 600; color: var(--muted); }
.inquiry-block__text { font-size: 14px; line-height: 1.65; color: var(--ink-2); white-space: pre-wrap; word-break: break-word; }
.inquiry-block--answer {
  padding: 14px 16px;
  background: var(--teal-soft);
  border-radius: var(--r-md);
}
.inquiry-block--answer .inquiry-block__label { color: var(--teal-3); }
.inquiry-pending { margin-top: 14px; font-size: 13px; color: var(--ink-soft); }

.support-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 18px;
  border-radius: var(--r-md);
  font-size: 14px;
  font-weight: 700;
}
.support-button--primary { background: var(--teal); color: white; }
.support-button--primary:hover { background: var(--teal-2); }

@media (max-width: 640px) {
  .support-page { padding: 32px var(--space-4) 0; }
  .support-card, .support-list { padding: 22px 18px; }
  .support-head h1 { font-size: 25px; }
  .inquiry-item__title { font-size: 13px; }
}
</style>

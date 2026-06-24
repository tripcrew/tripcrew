<template>
  <div class="page chatbot-page">
    <AppHeader />

    <main class="container chat-layout">
      <!-- Sidebar -->
      <aside class="chat-sidebar">
        <button class="new-chat-btn" type="button" @click="resetChat">
          <span>＋</span> 새 대화
        </button>
        <div class="recent-label">SUGGESTIONS</div>
        <ul class="recent-list">
          <li
            v-for="prompt in suggestedPrompts"
            :key="prompt"
            :class="{ active: input === prompt }"
            @click="usePrompt(prompt)"
          >
            {{ prompt }}
          </li>
        </ul>
      </aside>

      <!-- Chat area -->
      <section class="chat-main">
        <header class="chat-header">
          <div class="bot-info">
            <div class="bot-avatar">🤖</div>
            <div>
              <strong>TripBot</strong>
              <span class="t-mono">
                <span class="dot-pulse" :class="{ 'dot-pulse--idle': !loading }"></span>
                Gemini · {{ loading ? '응답 중...' : '대기 중' }}
              </span>
            </div>
          </div>
        </header>

        <div ref="chatBodyRef" class="chat-body">
          <div v-for="message in messages" :key="message.id" class="msg" :class="messageClass(message.role)">
            <div v-if="message.role === 'ASSISTANT'" class="msg__avatar">🤖</div>
            <div class="msg__content">
              <div v-if="message.intro" class="msg__bubble msg__bubble--intro">
                <strong>{{ message.title }}</strong>
                <p>{{ message.content }}</p>
                <ul>
                  <li>여행 기간과 지역</li>
                  <li>동행자와 이동수단</li>
                  <li>원하는 분위기나 꼭 하고 싶은 것</li>
                </ul>
                <span class="intro-example">예: “강릉 1박 2일, 친구와 기차 여행, 카페와 바다 위주”</span>
              </div>
              <div v-else class="msg__bubble">
                <div class="markdown-message" v-html="renderMessage(message.content)"></div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="msg msg--bot">
            <div class="msg__avatar">🤖</div>
            <div class="msg__content">
              <div class="msg__bubble typing-bubble">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>

          <div v-if="errorMessage" class="chat-error">
            {{ errorMessage }}
          </div>
        </div>

        <!-- Input -->
        <footer class="chat-input">
          <form class="input-wrap" @submit.prevent="sendMessage">
            <input
              v-model="input"
              type="text"
              placeholder="예: 2박3일, 부모님 모시고, 휠체어 접근 가능한 곳..."
              :disabled="loading"
            />
            <button class="send-btn" type="submit" :disabled="!canSend">
              {{ loading ? '응답 중' : '전송' }}
            </button>
          </form>
        </footer>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { chatApi } from '@/api/chat'
import AppHeader from '@/components/common/AppHeader.vue'

const input = ref('')
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const chatBodyRef = ref(null)
const suggestedPrompts = [
  '여수 2박3일 바다 위주, 대중교통으로 이동',
  '강릉 카페 투어 1박2일',
  '제주 가족여행 3박4일, 부모님과 함께',
  '부산 야경 데이트 코스 추천',
  '비 오는 날 갈만한 서울 실내 코스',
  '휠체어 접근 가능한 여행지 추천',
]
const messages = ref([createWelcomeMessage()])

const canSend = computed(() => input.value.trim().length > 0 && !loading.value)

watch(
  () => route.query.prompt,
  (prompt) => {
    input.value = typeof prompt === 'string' ? prompt : ''
  },
  { immediate: true },
)

function messageClass(role) {
  return role === 'USER' ? 'msg--user' : 'msg--bot'
}

function usePrompt(prompt) {
  input.value = prompt
}

function resetChat() {
  input.value = ''
  errorMessage.value = ''
  messages.value = [createWelcomeMessage()]
}

function createWelcomeMessage() {
  return {
    id: crypto.randomUUID(),
    role: 'ASSISTANT',
    intro: true,
    title: '안녕하세요! 여행 계획을 함께 만들어볼까요?',
    content: '아는 정보만 편하게 알려주시면, 취향에 맞는 관광지와 동선을 추천해드릴게요.',
  }
}

function renderMessage(content) {
  const lines = escapeHtml(content).split('\n')
  const html = []
  let listType = null

  const closeList = () => {
    if (listType) html.push(`</${listType}>`)
    listType = null
  }

  lines.forEach((line) => {
    const heading = line.match(/^#{1,3}\s+(.+)$/)
    const unordered = line.match(/^\s*[-*]\s+(.+)$/)
    const ordered = line.match(/^\s*\d+\.\s+(.+)$/)

    if (heading) {
      closeList()
      html.push(`<h3>${formatInlineMarkdown(heading[1])}</h3>`)
    } else if (unordered || ordered) {
      const nextListType = unordered ? 'ul' : 'ol'
      if (listType !== nextListType) {
        closeList()
        listType = nextListType
        html.push(`<${listType}>`)
      }
      html.push(`<li>${formatInlineMarkdown((unordered || ordered)[1])}</li>`)
    } else if (line.trim()) {
      closeList()
      html.push(`<p>${formatInlineMarkdown(line)}</p>`)
    } else {
      closeList()
    }
  })
  closeList()
  return html.join('')
}

function escapeHtml(value) {
  return String(value || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

function formatInlineMarkdown(value) {
  return value.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
}

async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({
    id: crypto.randomUUID(),
    role: 'USER',
    content: text,
  })
  input.value = ''
  errorMessage.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const response = await chatApi.send({ message: text })
    messages.value.push({
      id: crypto.randomUUID(),
      role: 'ASSISTANT',
      content: response.answer || '답변을 가져오지 못했어요.',
    })
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || '챗봇 응답 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chatbot-page {
  height: 100vh;
  background: var(--bg-soft);
  display: flex;
  flex-direction: column;
}

.chat-layout {
  display: grid;
  grid-template-columns: minmax(240px, 280px) minmax(0, 1fr);
  gap: 24px;
  padding: 24px var(--space-6);
  flex: 1;
  width: 100%;
  max-width: 1440px;
  min-height: 0;
  box-sizing: border-box;
}

/* Sidebar */
.chat-sidebar {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 20px;
  overflow-y: auto;
}

.new-chat-btn {
  width: 100%;
  padding: 12px;
  background: var(--teal-3);
  color: white;
  border-radius: 10px;
  font-weight: 600;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.recent-label {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  color: var(--muted);
  letter-spacing: 1px;
  margin-bottom: 10px;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recent-list li {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--ink-3);
  cursor: pointer;
  transition: background 0.15s;
}

.recent-list li:hover { background: var(--bg-soft); }
.recent-list li.active {
  background: var(--teal-soft);
  color: var(--teal-ink);
  font-weight: 600;
}

.msg__bubble--intro {
  min-width: min(100%, 390px);
}

.msg__bubble--intro strong {
  display: block;
  margin-bottom: 8px;
  font-size: 15px;
}

.msg__bubble--intro p {
  line-height: 1.55;
}

.msg__bubble--intro ul {
  margin: 12px 0;
  padding-left: 18px;
  color: var(--ink-3);
  font-size: 13px;
  line-height: 1.7;
}

.intro-example {
  display: block;
  padding: 9px 10px;
  border-radius: 8px;
  background: var(--glass);
  color: var(--teal-ink);
  font-size: 12px;
  line-height: 1.45;
}

.markdown-message :deep(h3) {
  margin: 0 0 10px;
  color: var(--ink);
  font-size: 16px;
  font-weight: 800;
}

.markdown-message :deep(p) {
  margin: 0 0 10px;
  line-height: 1.65;
}

.markdown-message :deep(ul),
.markdown-message :deep(ol) {
  margin: 0 0 12px;
  padding-left: 20px;
  line-height: 1.7;
}

.markdown-message :deep(strong) {
  color: var(--ink);
  font-weight: 800;
}

/* Main */
.chat-main {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.chat-header {
  padding: 20px 28px;
  border-bottom: 1px solid var(--line);
  background: var(--bg-soft);
}

.bot-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  width: 40px; height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--teal), var(--teal-3));
  display: grid;
  place-items: center;
  font-size: 20px;
}

.bot-info strong {
  display: block;
  font-size: 16px;
  font-weight: 700;
}

.bot-info .t-mono {
  font-size: 12px;
  color: var(--ink-soft);
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot-pulse {
  width: 8px; height: 8px;
  background: var(--success);
  border-radius: 50%;
  animation: pulse 1.4s ease-in-out infinite;
}

.dot-pulse--idle {
  animation: none;
  opacity: 0.65;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.msg {
  display: flex;
  gap: 14px;
}

.msg--user {
  justify-content: flex-end;
}

.msg--user .msg__content {
  display: flex;
  justify-content: flex-end;
}

.msg--user .msg__bubble {
  background: var(--teal);
  color: white;
  border-radius: 16px 16px 4px 16px;
  max-width: 70%;
}

.msg__avatar {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: var(--teal-soft);
  display: grid;
  place-items: center;
  font-size: 18px;
  flex-shrink: 0;
}

.msg__content {
  flex: 1;
  min-width: 0;
  max-width: 760px;
}

.msg__bubble {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: keep-all;
  overflow-wrap: anywhere;
}

.msg--bot .msg__bubble {
  display: inline-block;
  max-width: 100%;
  background: var(--bg-soft);
  color: var(--ink);
  border-radius: 4px 16px 16px 16px;
}

.typing-bubble {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.typing-bubble span {
  width: 7px;
  height: 7px;
  background: var(--muted);
  border-radius: 50%;
  animation: typing 1s ease-in-out infinite;
}

.typing-bubble span:nth-child(2) {
  animation-delay: 0.12s;
}

.typing-bubble span:nth-child(3) {
  animation-delay: 0.24s;
}

@keyframes typing {
  0%, 100% { transform: translateY(0); opacity: 0.45; }
  50% { transform: translateY(-3px); opacity: 1; }
}

.chat-error {
  align-self: center;
  max-width: 620px;
  padding: 10px 14px;
  background: var(--coral-tint);
  border: 1px solid var(--coral);
  border-radius: 8px;
  color: var(--ink);
  font-size: 14px;
}

/* Input */
.chat-input {
  padding: 20px 28px;
  border-top: 1px solid var(--line);
  background: var(--bg-soft);
}

.input-wrap {
  display: flex;
  gap: 8px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 4px 4px 4px 18px;
}

.input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  padding: 12px 0;
  font-size: 15px;
}

.send-btn {
  padding: 10px 18px;
  background: var(--coral);
  color: white;
  border-radius: 8px;
  font-weight: 600;
}

.send-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.api-note {
  margin-top: 10px;
  font-size: 11px;
  color: var(--muted);
}

@media (max-width: 860px) {
  .chat-layout {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .chat-sidebar {
    display: none;
  }

  .msg--user .msg__bubble {
    max-width: 86%;
  }
}
</style>

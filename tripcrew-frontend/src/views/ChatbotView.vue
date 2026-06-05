<template>
  <div class="page chatbot-page">
    <AppHeader />

    <main class="container chat-layout">
      <!-- Sidebar -->
      <aside class="chat-sidebar">
        <button class="new-chat-btn">
          <span>＋</span> 새 대화
        </button>
        <div class="recent-label">RECENT</div>
        <ul class="recent-list">
          <li class="active">여수 2박3일 바다 위주</li>
          <li>강릉 카페 투어 1박</li>
          <li>제주 가족여행 3박4일</li>
          <li>부산 야경 데이트</li>
        </ul>
      </aside>

      <!-- Chat area -->
      <section class="chat-main">
        <header class="chat-header">
          <div class="bot-info">
            <div class="bot-avatar">🤖</div>
            <div>
              <strong>TripBot</strong>
              <span class="t-mono"><span class="dot-pulse"></span> OpenAI · 응답 중...</span>
            </div>
          </div>
        </header>

        <div class="chat-body">
          <!-- User message -->
          <div class="msg msg--user">
            <div class="msg__bubble">
              여수 2박3일, 바다 위주에 카페 1~2개. 차 없이 대중교통으로 다닐 거예요.
            </div>
          </div>

          <!-- Bot message -->
          <div class="msg msg--bot">
            <div class="msg__avatar">🤖</div>
            <div class="msg__content">
              <div class="msg__bubble">
                <p>
                  <strong>대중교통 위주의 여수 바다 코스 5개</strong>를 준비했어요.
                  <span class="weather-note">☔ 우천 예보(둘째날 오후)</span>가 있어 실내 코스를 한 곳 섞었어요.
                </p>
              </div>

              <div class="course-cards">
                <article class="course-card">
                  <header>
                    <span class="course-label">코스 A</span>
                    <span class="course-meta">★ 4.7 · 8시간</span>
                  </header>
                  <h4>바다와 동백 명소 코스</h4>
                  <p class="t-caption">오동도 → 하멜등대 → 케이블카 → 돌산공원</p>
                </article>

                <article class="course-card course-card--featured">
                  <header>
                    <span class="course-label">코스 B</span>
                    <span class="recommend-badge">추천</span>
                    <span class="course-meta">★ 4.8 · 7시간</span>
                  </header>
                  <h4>우천 대비 실내+바다 믹스</h4>
                  <p class="t-caption">박물관 → 동백숲 → 카페거리 → 야경 케이블카</p>
                </article>

                <article class="course-card">
                  <header>
                    <span class="course-label">코스 C</span>
                    <span class="course-meta">★ 4.5 · 6시간</span>
                  </header>
                  <h4>여유로운 바다 산책 코스</h4>
                </article>

                <article class="course-card">
                  <header>
                    <span class="course-label">코스 D</span>
                    <span class="course-meta">★ 4.4 · 9시간</span>
                  </header>
                  <h4>맛집 중심 + 바다</h4>
                </article>
              </div>

              <div class="stream-status">
                <span class="t-mono">stream · token 247 / ~400</span>
                <div class="stream-bar">
                  <div class="stream-fill" style="width: 62%"></div>
                </div>
              </div>

              <div class="msg__actions">
                <BaseButton variant="primary" @click="$router.push('/plans/42/edit')">
                  코스 B로 계획 시작하기 →
                </BaseButton>
                <BaseButton variant="secondary">다른 추천 받기</BaseButton>
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <footer class="chat-input">
          <div class="input-wrap">
            <input type="text" placeholder="예: 2박3일, 부모님 모시고, 휠체어 접근 가능한 곳..." />
            <button class="send-btn">↑ 전송</button>
          </div>
          <p class="api-note t-mono">POST /api/chat/messages (text/event-stream)</p>
        </footer>
      </section>
    </main>
  </div>
</template>

<script setup>
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
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
  grid-template-columns: 260px 1fr;
  gap: 24px;
  padding: 24px var(--space-6);
  flex: 1;
  min-height: 0;
}

/* Sidebar */
.chat-sidebar {
  background: white;
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
  color: var(--teal-3);
  font-weight: 600;
}

/* Main */
.chat-main {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  display: flex;
  flex-direction: column;
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
  max-width: 760px;
}

.msg__bubble {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.6;
}

.msg--bot .msg__bubble {
  background: var(--bg-soft);
  color: var(--ink);
  border-radius: 4px 16px 16px 16px;
}

.weather-note {
  display: inline-block;
  padding: 2px 8px;
  background: var(--info);
  color: white;
  border-radius: 999px;
  font-size: 12px;
  margin: 0 4px;
}

.course-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 16px;
}

.course-card {
  padding: 16px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.course-card:hover {
  border-color: var(--teal);
  box-shadow: var(--sh-1);
}

.course-card--featured {
  border-color: var(--coral);
  background: var(--coral-tint);
}

.course-card header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.course-label {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-3);
  background: var(--bg-2);
  padding: 2px 8px;
  border-radius: 4px;
}

.recommend-badge {
  font-size: 11px;
  font-weight: 700;
  background: var(--coral);
  color: white;
  padding: 2px 8px;
  border-radius: 999px;
}

.course-meta {
  margin-left: auto;
  font-size: 12px;
  color: var(--ink-soft);
}

.course-card h4 {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stream-status {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stream-status .t-mono {
  font-size: 11px;
  color: var(--muted);
}

.stream-bar {
  flex: 1;
  height: 2px;
  background: var(--line);
  border-radius: 1px;
  overflow: hidden;
}

.stream-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--teal), var(--coral));
  animation: stream 2s ease-in-out infinite;
}

@keyframes stream {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.msg__actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
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
  background: white;
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

.api-note {
  margin-top: 10px;
  font-size: 11px;
  color: var(--muted);
}
</style>

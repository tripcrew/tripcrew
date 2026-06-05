<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container coedit-layout">
      <header class="coedit-header">
        <div>
          <div class="status-row">
            <span class="ws-status">
              <span class="ws-dot"></span>
              실시간 연결됨
            </span>
            <span class="t-mono muted">v.42 → 충돌!</span>
          </div>
          <h1 class="t-h1">여수 2박3일 바다 위주</h1>
          <p class="t-caption">3명이 함께 편집 중</p>
        </div>

        <BaseButton variant="secondary">+ 동행 초대</BaseButton>
      </header>

      <div class="coedit-grid">
        <!-- Main timeline -->
        <section class="coedit-main">
          <div class="day-tabs">
            <button class="day-tab active">
              <strong>Day 1</strong>
              <span class="t-caption">6/4</span>
            </button>
            <button class="day-tab">
              <strong>Day 2</strong>
              <span class="t-caption">6/5</span>
            </button>
            <button class="day-tab">
              <strong>Day 3</strong>
              <span class="t-caption">6/6</span>
            </button>
          </div>

          <div class="timeline">
            <div class="timeline-row">
              <div class="time-col"><strong>09:00</strong></div>
              <div class="item-card">
                <button class="drag-handle">⋮⋮</button>
                <div class="item-body"><h3>오동도 동백숲</h3></div>
              </div>
            </div>

            <div class="timeline-row">
              <div class="time-col"><strong>11:00</strong></div>
              <div class="item-card item-card--editing-by-other">
                <div class="lock-icon">🔒</div>
                <div class="item-body">
                  <h3>하멜등대</h3>
                  <p class="t-caption">
                    <span class="editor-tag" style="background: var(--coral);">지</span>
                    지원님이 편집 중...
                  </p>
                </div>
              </div>
            </div>

            <div class="timeline-row">
              <div class="time-col"><strong>13:00</strong></div>
              <div class="item-card item-card--me-editing">
                <button class="drag-handle">📝</button>
                <div class="item-body">
                  <h3>여수 해상케이블카</h3>
                  <p class="t-caption">← 내가 편집 중</p>
                </div>
              </div>
            </div>

            <div class="timeline-row">
              <div class="time-col"><strong>15:00</strong></div>
              <div class="item-card">
                <button class="drag-handle">⋮⋮</button>
                <div class="item-body"><h3>돌산공원 + 거북선대교</h3></div>
              </div>
            </div>

            <div class="timeline-row newly-added">
              <div class="time-col"><strong>19:00</strong></div>
              <div class="item-card item-card--just-added">
                <span class="editor-tag" style="background: var(--violet);">현</span>
                <div class="item-body">
                  <h3>밤바다 일루미네이션</h3>
                  <p class="t-caption">방금 현우님이 추가했어요</p>
                </div>
                <span class="new-badge">＋ NEW</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Sidebar -->
        <aside class="coedit-side">
          <!-- Active users -->
          <section class="side-card">
            <header class="side-head">
              <h3>실시간 참여자</h3>
              <span class="t-mono">{{ users.length }}명 활성</span>
            </header>
            <ul class="user-list">
              <li v-for="u in users" :key="u.name">
                <div class="avatar" :style="{ background: u.color }">{{ u.letter }}</div>
                <div class="user-info">
                  <strong>{{ u.name }}</strong>
                  <span class="t-caption">{{ u.status }}</span>
                </div>
                <span class="user-dot" :class="`user-dot--${u.dotColor}`"></span>
              </li>
            </ul>
          </section>

          <!-- History -->
          <section class="side-card">
            <header class="side-head">
              <h3>변경 이력</h3>
            </header>
            <ul class="history-list">
              <li>
                <span class="t-mono version">v.42</span>
                <p>현우 가 '밤바다 일루미네이션' 추가</p>
              </li>
              <li>
                <span class="t-mono version">v.41</span>
                <p>나 가 Day 2 순서 변경</p>
              </li>
              <li>
                <span class="t-mono version">v.40</span>
                <p>동선 최적화 적용</p>
              </li>
              <li>
                <span class="t-mono version">v.39</span>
                <p>지원 이 케이블카 시간 조정</p>
              </li>
            </ul>
          </section>
        </aside>
      </div>
    </main>

    <!-- Conflict modal -->
    <div class="modal-overlay">
      <div class="modal modal--conflict">
        <header class="modal__head">
          <span class="modal-icon">⚠️</span>
          <h2 class="t-h2">다른 사용자가 먼저 수정했어요</h2>
          <p class="t-caption">
            지원님이 같은 항목을 먼저 저장했습니다 (v.42 → v.43).<br />
            두 변경을 어떻게 합칠지 선택해주세요.
          </p>
        </header>

        <div class="diff-grid">
          <div class="diff-col">
            <header class="diff-head diff-head--server">
              서버 · v.43 (지원)
            </header>
            <div class="diff-body">
              <p><strong>하멜등대</strong></p>
              <p class="t-caption">시간: 10:30 ~ 11:30</p>
              <p class="t-caption">메모: "야경 추천"</p>
            </div>
          </div>

          <div class="diff-col">
            <header class="diff-head diff-head--mine">
              내 변경 · 미저장
            </header>
            <div class="diff-body">
              <p><strong>하멜등대</strong></p>
              <p class="t-caption">시간: 11:00 ~ 12:30</p>
              <p class="t-caption">메모: "꼭 들르기"</p>
            </div>
          </div>
        </div>

        <footer class="modal__foot modal__foot--cols">
          <BaseButton variant="secondary">서버 변경 사용</BaseButton>
          <BaseButton variant="secondary">내 변경 사용</BaseButton>
          <BaseButton variant="primary">머지 (수동)</BaseButton>
        </footer>

        <p class="modal-note t-mono">
          HTTP 409 Conflict · 낙관적 락 / version 컬럼 불일치
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const users = [
  { name: '민지 (나)', letter: '민', color: 'var(--teal)', status: '활성', dotColor: 'success' },
  { name: '지원', letter: '지', color: 'var(--coral)', status: '하멜등대 편집 중', dotColor: 'warning' },
  { name: '현우', letter: '현', color: 'var(--violet)', status: '방금 활동', dotColor: 'success' }
]
</script>

<style scoped>
.coedit-layout {
  padding: 32px var(--space-6) 80px;
}

.coedit-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.ws-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: var(--success);
  color: white;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.ws-dot {
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  animation: pulse 1.6s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.muted { color: var(--ink-soft); }

/* Grid */
.coedit-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 24px;
  align-items: start;
}

.coedit-main {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 24px;
}

.day-tabs {
  display: flex;
  gap: 6px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 20px;
}

.day-tab {
  padding: 10px 18px;
  background: var(--bg-soft);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.day-tab strong { font-size: 14px; }

.day-tab.active {
  background: var(--teal);
  color: white;
}

.day-tab.active .t-caption { color: rgba(255,255,255,0.8); }

/* Timeline */
.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-row {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 16px;
  align-items: center;
}

.time-col strong {
  font-family: var(--font-mono);
  font-size: 14px;
  font-weight: 700;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 12px;
  position: relative;
}

.item-card--editing-by-other {
  background: var(--coral-tint);
  border-color: var(--coral);
}

.item-card--me-editing {
  background: var(--teal-tint);
  border-color: var(--teal);
  border-style: dashed;
}

.item-card--just-added {
  background: #F4F0FF;
  border-color: var(--violet);
  animation: highlight 1.5s ease-out;
}

@keyframes highlight {
  0% { background: var(--violet); }
  100% { background: #F4F0FF; }
}

.drag-handle, .lock-icon {
  color: var(--muted);
  font-size: 14px;
}

.item-body { flex: 1; }
.item-body h3 { font-size: 15px; font-weight: 700; }

.editor-tag {
  width: 20px; height: 20px;
  border-radius: 50%;
  color: white;
  font-weight: 700;
  font-size: 11px;
  display: inline-grid;
  place-items: center;
  vertical-align: middle;
  margin-right: 6px;
}

.new-badge {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 800;
  background: var(--violet);
  color: white;
  padding: 3px 8px;
  border-radius: 4px;
  letter-spacing: 1px;
}

/* Sidebar */
.coedit-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 88px;
}

.side-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  padding: 20px;
}

.side-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.side-head h3 {
  font-size: 14px;
  font-weight: 700;
}

.side-head .t-mono {
  font-size: 11px;
  color: var(--ink-soft);
}

/* User list */
.user-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: var(--bg-soft);
  border-radius: 10px;
}

.avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}

.user-info { flex: 1; }
.user-info strong {
  display: block;
  font-size: 13px;
  font-weight: 700;
}

.user-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
}

.user-dot--success { background: var(--success); }
.user-dot--warning { background: var(--warning); }

/* History */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-list li {
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
}

.version {
  background: var(--bg-2);
  color: var(--ink-3);
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  height: fit-content;
  font-weight: 700;
}

.history-list p {
  font-size: 13px;
  color: var(--ink-2);
}

/* Conflict Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: grid;
  place-items: center;
  z-index: 1000;
}

.modal--conflict {
  background: white;
  border-radius: var(--r-xl);
  padding: 32px;
  width: 90%;
  max-width: 640px;
  box-shadow: var(--sh-modal);
}

.modal__head {
  text-align: center;
  margin-bottom: 24px;
}

.modal-icon {
  display: inline-block;
  font-size: 36px;
  margin-bottom: 8px;
}

.modal__head .t-caption {
  font-size: 14px;
  margin-top: 8px;
  color: var(--ink-soft);
}

.diff-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 24px;
}

.diff-col {
  border: 1px solid var(--line);
  border-radius: 10px;
  overflow: hidden;
}

.diff-head {
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 700;
  color: white;
}

.diff-head--server { background: var(--info); }
.diff-head--mine { background: var(--coral); }

.diff-body {
  padding: 16px;
  background: var(--bg-soft);
}

.diff-body p { font-size: 14px; margin-bottom: 4px; }

.modal__foot--cols {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.modal-note {
  text-align: center;
  margin-top: 16px;
  font-size: 11px;
  color: var(--danger);
  background: #FFF5F5;
  padding: 8px;
  border-radius: 6px;
}
</style>

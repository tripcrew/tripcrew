<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container plan-edit-layout">
      <header class="plan-header">
        <div>
          <div class="status-row">
            <span class="status-chip status--active">진행 중</span>
            <span class="t-mono muted">v.42</span>
          </div>
          <h1 class="t-h1">여수 2박3일 바다 위주</h1>
          <p class="t-caption">2026.06.04 — 06.06 · <span class="autosave">● 자동 저장됨 (5초 전)</span></p>
        </div>

        <div class="header-right">
          <div class="presence">
            <div class="avatar" style="background: var(--teal);">민</div>
            <div class="avatar" style="background: var(--coral);">지</div>
            <div class="avatar" style="background: var(--violet);">현</div>
          </div>
          <BaseButton variant="secondary">미리보기</BaseButton>
          <BaseButton variant="primary" @click="showOptimizeModal = true">동선 최적화</BaseButton>
        </div>
      </header>

      <div class="plan-grid">
        <!-- Days tabs + Timeline -->
        <section class="plan-main">
          <div class="day-tabs">
            <button v-for="d in days" :key="d.id" :class="['day-tab', { active: d.active }]">
              <strong>{{ d.label }}</strong>
              <span class="t-caption">{{ d.date }}</span>
            </button>
            <button class="day-tab day-tab--add">+ 일자 추가</button>
          </div>

          <div class="timeline">
            <div v-for="(item, i) in items" :key="item.id" class="timeline-row">
              <div class="time-col">
                <strong>{{ item.start }}</strong>
                <span class="t-caption">{{ item.end }}</span>
              </div>

              <div class="dot-col">
                <div class="time-dot">{{ i + 1 }}</div>
                <div v-if="i < items.length - 1" class="time-line"></div>
              </div>

              <div class="item-col">
                <div class="item-card">
                  <button class="drag-handle">⋮⋮</button>
                  <div class="item-body">
                    <h3>{{ item.name }}</h3>
                    <p class="t-caption">{{ item.region }} · {{ item.note }}</p>
                  </div>
                </div>
                <div v-if="item.transit" class="transit-row">
                  <span class="transit-icon">{{ item.transit.icon }}</span>
                  <span>{{ item.transit.mode }} {{ item.transit.duration }} · {{ item.transit.distance }}</span>
                </div>
              </div>
            </div>

            <button class="add-item-btn">+ 일정 추가</button>
          </div>
        </section>

        <!-- Map -->
        <aside class="plan-map">
          <div class="map-canvas">
            <div class="map-grad"></div>
            <div class="route-pin pin-1">1</div>
            <div class="route-pin pin-2">2</div>
            <div class="route-pin pin-3">3</div>
            <div class="route-pin pin-4">4</div>
            <svg class="route-svg" viewBox="0 0 300 400">
              <path d="M 80 80 Q 150 100 180 160 T 220 280 L 100 340" stroke="var(--coral)" stroke-width="3" stroke-dasharray="6 4" fill="none" />
            </svg>
          </div>
          <div class="map-info">
            <h4>Day 1 동선</h4>
            <p class="t-mono">총 <strong>4.2km</strong> · <strong>47분</strong></p>
          </div>
          <div class="map-controls">
            <button>+</button>
            <button>−</button>
          </div>
        </aside>
      </div>
    </main>

    <!-- Optimize Modal -->
    <transition name="modal-fade">
      <div v-if="showOptimizeModal" class="modal-overlay" @click.self="showOptimizeModal = false">
        <div class="modal">
          <header class="modal__head">
            <h2 class="t-h2">동선을 최적화하고 있어요</h2>
            <p class="t-caption">Day 1의 4개 장소를 가장 짧은 이동시간으로 재배열 중입니다. 다른 탭에서 계속 작업하셔도 됩니다.</p>
          </header>

          <div class="modal__progress">
            <div class="progress-info">
              <span class="t-mono">거리 행렬 계산 → 2-opt 후처리</span>
              <span class="t-mono"><strong>1.4s</strong> / ~2.0s</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill"></div>
            </div>
          </div>

          <ul class="steps">
            <li class="done">✓ 장소별 좌표 조회</li>
            <li class="done">✓ Kakao Mobility 거리행렬 호출</li>
            <li class="active">● 2-opt 알고리즘 실행 중...</li>
            <li class="pending">○ 결과 WebSocket 푸시</li>
          </ul>

          <footer class="modal__foot">
            <BaseButton variant="ghost" @click="showOptimizeModal = false">백그라운드로 보내기</BaseButton>
          </footer>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const showOptimizeModal = ref(false)

const days = [
  { id: 1, label: 'Day 1', date: '6/4', active: true },
  { id: 2, label: 'Day 2', date: '6/5', active: false },
  { id: 3, label: 'Day 3', date: '6/6', active: false }
]

const items = [
  { id: 1, start: '09:00', end: '~10:30', name: '오동도 동백숲', region: '전남 여수', note: '도보 5분', transit: { icon: '🚶', mode: '도보', duration: '8분', distance: '480m' } },
  { id: 2, start: '11:00', end: '~12:00', name: '하멜등대', region: '전남 여수', note: '야경 명소', transit: { icon: '🚌', mode: '버스', duration: '18분', distance: '4.2km' } },
  { id: 3, start: '13:00', end: '~14:30', name: '여수 해상케이블카', region: '자산공원 → 돌산공원', note: '', transit: { icon: '🚶', mode: '도보', duration: '12분', distance: '850m' } },
  { id: 4, start: '15:00', end: '~17:00', name: '돌산공원 + 거북선대교', region: '전남 여수', note: '일몰 명소' }
]
</script>

<style scoped>
.plan-edit-layout {
  padding: 32px var(--space-6) 80px;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.status-chip {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}

.status--active { background: var(--teal); color: white; }

.autosave {
  color: var(--success);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.presence {
  display: flex;
}

.presence .avatar {
  margin-left: -8px;
  border: 2px solid white;
}

.presence .avatar:first-child { margin-left: 0; }

.avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: white;
  font-weight: 700;
  font-size: 13px;
}

.muted { color: var(--ink-soft); }

/* Plan grid */
.plan-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
  align-items: start;
}

.plan-main {
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
  transition: all 0.15s;
}

.day-tab strong {
  font-size: 14px;
  font-weight: 700;
}

.day-tab.active {
  background: var(--teal);
  color: white;
}

.day-tab.active .t-caption { color: rgba(255,255,255,0.8); }

.day-tab--add {
  background: transparent;
  border: 1px dashed var(--line-2);
  color: var(--ink-soft);
  font-size: 13px;
  align-items: center;
  justify-content: center;
  flex-direction: row;
}

/* Timeline */
.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-row {
  display: grid;
  grid-template-columns: 80px 32px 1fr;
  gap: 12px;
}

.time-col {
  text-align: right;
  padding-top: 14px;
  display: flex;
  flex-direction: column;
}

.time-col strong {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 700;
}

.time-col .t-caption {
  font-size: 11px;
}

.dot-col {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.time-dot {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: var(--teal);
  color: white;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 12px;
  margin-top: 12px;
}

.time-line {
  flex: 1;
  width: 2px;
  background: var(--line-2);
  margin: 4px 0;
  min-height: 30px;
}

.item-col {
  display: flex;
  flex-direction: column;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 12px;
  transition: all 0.15s;
}

.item-card:hover {
  border-color: var(--teal);
  background: white;
  box-shadow: var(--sh-1);
}

.drag-handle {
  color: var(--muted);
  cursor: grab;
  font-size: 14px;
  letter-spacing: -2px;
}

.item-body {
  flex: 1;
}

.item-body h3 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 2px;
}

.transit-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  font-size: 12px;
  color: var(--ink-soft);
}

.transit-icon { font-size: 14px; }

.add-item-btn {
  margin-top: 12px;
  padding: 14px;
  background: transparent;
  border: 1.5px dashed var(--line-2);
  border-radius: 12px;
  font-size: 13px;
  color: var(--ink-soft);
  font-weight: 600;
}

.add-item-btn:hover {
  border-color: var(--teal);
  color: var(--teal);
}

/* Map */
.plan-map {
  position: sticky;
  top: 88px;
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  overflow: hidden;
  height: 600px;
  display: flex;
  flex-direction: column;
}

.map-canvas {
  flex: 1;
  position: relative;
  background: var(--bg-2);
  overflow: hidden;
}

.map-grad {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 30% 40%, var(--teal-soft) 0%, transparent 50%),
    radial-gradient(circle at 70% 70%, var(--coral-tint) 0%, transparent 50%),
    repeating-linear-gradient(0deg, transparent 0, transparent 28px, var(--line) 28px, var(--line) 29px),
    repeating-linear-gradient(90deg, transparent 0, transparent 28px, var(--line) 28px, var(--line) 29px);
}

.route-pin {
  position: absolute;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--coral);
  color: white;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(216, 90, 48, 0.4);
  z-index: 2;
}

.pin-1 { top: 18%; left: 24%; }
.pin-2 { top: 38%; left: 56%; }
.pin-3 { top: 68%; left: 68%; }
.pin-4 { top: 82%; left: 30%; }

.route-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.map-info {
  padding: 14px 18px;
  background: white;
  border-top: 1px solid var(--line);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-info h4 { font-size: 14px; font-weight: 700; }

.map-controls {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  flex-direction: column;
  background: white;
  border: 1px solid var(--line);
  border-radius: 8px;
  overflow: hidden;
  z-index: 3;
}

.map-controls button {
  width: 32px;
  height: 32px;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink-3);
}

.map-controls button:hover { background: var(--bg-2); }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: grid;
  place-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: var(--r-xl);
  padding: 32px;
  width: 90%;
  max-width: 480px;
  box-shadow: var(--sh-modal);
}

.modal__head {
  text-align: center;
  margin-bottom: 24px;
}

.modal__head .t-caption {
  font-size: 14px;
  margin-top: 8px;
  color: var(--ink-soft);
}

.modal__progress {
  margin-bottom: 24px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--ink-3);
}

.progress-info strong { color: var(--coral); font-weight: 700; }

.progress-bar {
  height: 6px;
  background: var(--bg-2);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  width: 70%;
  background: linear-gradient(90deg, var(--teal), var(--coral));
  border-radius: 3px;
  animation: progress 2s ease-in-out infinite;
}

@keyframes progress {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.steps {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-soft);
  border-radius: 10px;
}

.steps li {
  font-size: 14px;
  font-family: var(--font-mono);
}

.steps .done { color: var(--success); }
.steps .active { color: var(--coral); font-weight: 700; }
.steps .pending { color: var(--muted); }

.modal__foot {
  text-align: center;
}

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.2s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>

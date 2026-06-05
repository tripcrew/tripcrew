<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container error-layout">
      <header class="error-header">
        <h1 class="t-h1">에러 · 빈 상태 화면</h1>
        <p class="t-caption">SC-12 · 4-up · 모든 에러 상태를 친근한 카피로</p>
      </header>

      <div class="error-grid">
        <!-- 1. Circuit Breaker OPEN -->
        <article class="error-card error-card--breaker">
          <span class="error-tag t-mono">CIRCUIT BREAKER · OPEN</span>
          <div class="error-icon">⚡</div>
          <h2 class="t-h2">관광지 정보를 불러오지 못했어요</h2>
          <p class="error-desc">
            한국관광공사 시스템에 일시적인 문제가 있어요.<br />
            <strong>2분 후 자동으로 다시 시도</strong>합니다.
          </p>
          <div class="error-actions">
            <BaseButton variant="primary">지금 다시 시도</BaseButton>
            <BaseButton variant="secondary">캐시된 결과 보기</BaseButton>
          </div>
          <p class="error-meta t-mono">error_code: CB_OPEN · request_id: a3f1...c92</p>
        </article>

        <!-- 2. Empty state -->
        <article class="error-card error-card--empty">
          <span class="error-tag t-mono">EMPTY STATE</span>
          <div class="error-icon">＋</div>
          <h2 class="t-h2">아직 여행 계획이 없어요</h2>
          <p class="error-desc">
            첫 여행 계획을 시작해보세요.<br />
            챗봇에게 한 줄로 요청하면 <strong>5초 만에 코스</strong>를 추천해드려요.
          </p>
          <div class="error-actions">
            <BaseButton variant="primary" @click="$router.push('/chat')">챗봇으로 시작</BaseButton>
            <BaseButton variant="secondary" @click="$router.push('/plans/new/edit')">직접 만들기</BaseButton>
          </div>
          <div class="suggestion-chips">
            <span class="suggestion-chip">여수 2박3일</span>
            <span class="suggestion-chip">제주 가족여행</span>
            <span class="suggestion-chip">강릉 카페투어</span>
            <span class="suggestion-chip">부산 야경</span>
          </div>
        </article>

        <!-- 3. Offline -->
        <article class="error-card error-card--offline">
          <span class="error-tag t-mono">OFFLINE</span>
          <div class="error-icon">📡</div>
          <h2 class="t-h2">인터넷 연결을 확인해주세요</h2>
          <p class="error-desc">
            네트워크 신호가 약하거나 끊겼어요.<br />
            연결이 복구되면 <strong>자동으로 새로고침</strong> 됩니다.
          </p>
          <div class="error-actions">
            <BaseButton variant="primary">다시 연결</BaseButton>
            <BaseButton variant="secondary">오프라인 모드</BaseButton>
          </div>
          <p class="error-meta t-mono">retry in 3s · backoff: 2.0x</p>
        </article>

        <!-- 4. 404 -->
        <article class="error-card error-card--404">
          <span class="error-tag t-mono">HTTP 404</span>
          <div class="error-icon error-icon--big">4<span class="zero">0</span>4</div>
          <h2 class="t-h2">길을 잃으셨나요?</h2>
          <p class="error-desc">
            찾으시는 페이지가 존재하지 않거나, 삭제되었어요.<br />
            주소를 다시 확인하거나 홈으로 돌아가보세요.
          </p>
          <div class="error-actions">
            <BaseButton variant="primary" @click="$router.push('/')">홈으로</BaseButton>
            <BaseButton variant="secondary" @click="$router.push('/attractions')">관광지 둘러보기</BaseButton>
          </div>
          <p class="error-meta t-mono">path: /plans/99999 · 1.4s · request_id: f2c0...8d1</p>
        </article>
      </div>

      <section class="error-philosophy">
        <h3 class="t-h2">설계 원칙 · 친근한 카피 + 다음 행동 제안</h3>
        <p class="t-body">
          모든 에러 화면은 <strong>이유 · 사용자 액션 · 시스템 상태</strong>를 분리해 표시합니다.
          비난조 대신 <strong>다음 행동을 제안하는 카피</strong>를 사용하고,
          <code class="t-mono">request_id</code>를 노출해 CS 문의 추적이 쉽도록 합니다.
        </p>

        <div class="code-mapping">
          <h4>Error Code Mapping</h4>
          <table class="error-table">
            <tr>
              <td class="t-mono code-cb">CB</td>
              <td><code>CB_OPEN</code></td>
              <td>TourAPI fallback</td>
            </tr>
            <tr>
              <td class="t-mono code-404">404</td>
              <td><code>PLAN_NOT_FOUND · USER_NOT_FOUND</code></td>
              <td>리소스 없음</td>
            </tr>
            <tr>
              <td class="t-mono code-204">204</td>
              <td><code>Empty body</code></td>
              <td>빈 상태 화면 트리거</td>
            </tr>
          </table>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
</script>

<style scoped>
.error-layout {
  padding: 40px var(--space-6) 80px;
}

.error-header {
  margin-bottom: 32px;
}

.error-header .t-caption {
  margin-top: 6px;
  color: var(--ink-soft);
}

/* 4-up grid */
.error-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 40px;
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

.error-card--empty {
  background: linear-gradient(135deg, var(--teal-tint) 0%, white 60%);
  border-color: var(--teal-soft);
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
.error-card--empty .error-tag { background: var(--teal); color: white; }
.error-card--offline .error-tag { background: var(--info); color: white; }
.error-card--404 .error-tag { background: var(--violet); color: white; }

.error-icon {
  font-size: 64px;
  line-height: 1;
  margin-top: 28px;
  margin-bottom: 4px;
}

.error-icon--big {
  font-family: var(--font-mono);
  font-size: 88px;
  font-weight: 800;
  color: var(--violet);
  letter-spacing: -4px;
  line-height: 0.9;
}

.error-icon--big .zero {
  display: inline-block;
  animation: spin 4s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

.error-desc strong { color: var(--ink); }

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

/* Suggestion chips */
.suggestion-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  margin-top: auto;
  padding-top: 16px;
}

.suggestion-chip {
  padding: 5px 12px;
  background: white;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ink-3);
  cursor: pointer;
  transition: all 0.15s;
}

.suggestion-chip:hover {
  background: var(--teal);
  border-color: var(--teal);
  color: white;
}

/* Philosophy */
.error-philosophy {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 32px;
}

.error-philosophy h3 {
  margin-bottom: 12px;
}

.error-philosophy .t-body {
  color: var(--ink-3);
  line-height: 1.7;
  margin-bottom: 24px;
}

.error-philosophy strong { color: var(--ink); }

.error-philosophy code {
  background: var(--bg-2);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: var(--coral);
}

.code-mapping h4 {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--ink-2);
}

.error-table {
  background: var(--bg-soft);
  border-radius: 10px;
  overflow: hidden;
}

.error-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--line);
  font-size: 13px;
  color: var(--ink-3);
}

.error-table tr:last-child td {
  border-bottom: none;
}

.error-table td:first-child {
  width: 60px;
  text-align: center;
  font-weight: 800;
  color: white;
}

.code-cb { background: var(--coral); }
.code-404 { background: var(--violet); }
.code-204 { background: var(--info); }

.error-table code {
  background: transparent;
  color: var(--ink);
  padding: 0;
  font-size: 13px;
}
</style>

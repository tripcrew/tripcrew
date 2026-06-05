<template>
  <div class="page auth-page">
    <header class="auth-header">
      <router-link to="/" class="logo">TripCrew<span class="dot">.</span></router-link>
    </header>

    <div class="auth-grid">
      <!-- Left: Marketing -->
      <section class="auth-left">
        <h1 class="auth-left__title">
          <span class="brand">TripCrew<span style="color: var(--coral)">.</span></span><br />
          함께 만드는 여행 계획에<br />
          <span class="accent">참여하세요</span>
        </h1>
        <p class="auth-left__sub">
          3초 만에 가입하고 챗봇 추천 · 동선 최적화 · 동행자 협업까지<br />
          무료로 이용해보세요.
        </p>

        <ul class="auth-features">
          <li>
            <div class="feature-icon">✓</div>
            <div>
              <strong>한국관광공사 데이터 기반</strong>
              <span>검증된 추천</span>
            </div>
          </li>
          <li>
            <div class="feature-icon">✓</div>
            <div>
              <strong>최대 10명까지 동시 편집</strong>
              <span>WebSocket 실시간 동기화</span>
            </div>
          </li>
          <li>
            <div class="feature-icon">✓</div>
            <div>
              <strong>개인정보는 암호화되어</strong>
              <span>안전하게 저장</span>
            </div>
          </li>
        </ul>
      </section>

      <!-- Right: Form -->
      <section class="auth-right">
        <div class="auth-card">
          <div class="auth-tabs">
            <button
              :class="['auth-tab', { active: mode === 'login' }]"
              @click="mode = 'login'"
            >
              로그인
            </button>
            <button
              :class="['auth-tab', { active: mode === 'signup' }]"
              @click="mode = 'signup'"
            >
              회원가입
            </button>
          </div>

          <div v-if="mode === 'signup'" class="auth-form">
            <h2 class="auth-form__title">계정 만들기</h2>
            <p class="auth-form__lead">이메일과 비밀번호를 입력해주세요.</p>

            <div class="field">
              <label>이메일 <span class="req">*</span></label>
              <input type="email" placeholder="trip@crew.kr" />
            </div>

            <div class="field">
              <label>비밀번호 <span class="req">*</span></label>
              <input type="password" value="password123" />
              <div class="pwd-meter">
                <div class="meter-bar">
                  <div class="meter-fill" style="width: 60%; background: var(--warning);"></div>
                </div>
                <span class="meter-label">보통</span>
              </div>
              <p class="field-help">영문 + 숫자 8자 이상 ✓ · 특수문자 권장</p>
            </div>

            <div class="field">
              <label>비밀번호 확인 <span class="req">*</span></label>
              <input type="password" value="password" class="error-input" />
              <p class="field-error">! 비밀번호가 일치하지 않습니다</p>
            </div>

            <label class="check-row">
              <input type="checkbox" />
              <span>
                <strong>서비스 이용약관</strong> 및
                <strong>개인정보 처리방침</strong>에 동의합니다 <span class="req">*</span>
              </span>
            </label>

            <BaseButton variant="primary" size="lg" full @click="$router.push('/home')">
              계정 만들기
            </BaseButton>

            <p class="switch-mode">
              이미 계정이 있으신가요?
              <a href="#" @click.prevent="mode = 'login'">로그인 →</a>
            </p>
          </div>

          <div v-else class="auth-form">
            <h2 class="auth-form__title">다시 만나서 반갑습니다 👋</h2>
            <p class="auth-form__lead">계정 정보를 입력해주세요.</p>

            <div class="field">
              <label>이메일</label>
              <input type="email" placeholder="trip@crew.kr" />
            </div>

            <div class="field">
              <label>비밀번호</label>
              <input type="password" value="••••••••" />
              <p class="field-error">! 비밀번호가 올바르지 않습니다 (5회 중 3회 시도)</p>
            </div>

            <div class="login-options">
              <label class="check-row">
                <input type="checkbox" />
                <span>로그인 상태 유지</span>
              </label>
              <a href="#" class="link-muted">비밀번호 찾기</a>
            </div>

            <BaseButton variant="primary" size="lg" full @click="$router.push('/home')">
              로그인
            </BaseButton>

            <div class="divider">또는</div>

            <button class="oauth-btn">
              <span class="oauth-icon">💬</span>
              카카오로 시작
            </button>
          </div>
        </div>

        <div class="api-note">
          <code class="t-mono">POST /api/auth/signup · /api/auth/login</code>
          <code class="t-mono">POST /api/auth/refresh → 401 → re-login</code>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import BaseButton from '@/components/common/BaseButton.vue'

const mode = ref('signup')
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 0% 0%, var(--teal-tint) 0%, transparent 40%),
    radial-gradient(circle at 100% 100%, var(--coral-tint) 0%, transparent 40%),
    var(--bg);
}

.auth-header {
  padding: 24px 32px;
}

.logo {
  font-size: 22px;
  font-weight: 800;
  color: var(--teal-3);
  letter-spacing: -0.6px;
}
.logo .dot { color: var(--coral); }

.auth-grid {
  max-width: 1280px;
  margin: 0 auto;
  padding: 40px var(--space-6) 80px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

/* Left */
.auth-left__title {
  font-size: 44px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -1.2px;
  color: var(--ink);
  margin-bottom: 20px;
}

.auth-left__title .brand {
  color: var(--teal-3);
}

.auth-left__title .accent {
  color: var(--teal);
}

.auth-left__sub {
  font-size: 17px;
  line-height: 1.6;
  color: var(--ink-3);
  margin-bottom: 40px;
}

.auth-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.auth-features li {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.feature-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--teal);
  color: white;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.auth-features strong {
  display: block;
  font-weight: 700;
  color: var(--ink);
}

.auth-features span {
  font-size: 14px;
  color: var(--ink-soft);
}

/* Right card */
.auth-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.auth-card {
  background: white;
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  box-shadow: var(--sh-2);
  overflow: hidden;
}

.auth-tabs {
  display: flex;
  background: var(--bg-soft);
  padding: 6px;
  margin: 6px;
  border-radius: 10px;
  gap: 4px;
}

.auth-tab {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  color: var(--ink-soft);
  transition: all 0.15s;
}

.auth-tab.active {
  background: white;
  color: var(--ink);
  box-shadow: var(--sh-1);
}

.auth-form {
  padding: 24px 32px 32px;
}

.auth-form__title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
}

.auth-form__lead {
  font-size: 14px;
  color: var(--ink-soft);
  margin-bottom: 24px;
}

.field {
  margin-bottom: 18px;
}

.field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-2);
  margin-bottom: 6px;
}

.req { color: var(--coral); }

.field input {
  width: 100%;
  padding: 12px 14px;
  background: var(--bg-soft);
  border: 1px solid var(--line-2);
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.15s;
}

.field input:focus {
  outline: none;
  background: white;
  border-color: var(--teal);
  box-shadow: 0 0 0 2px var(--teal-soft);
}

.field input.error-input {
  border-color: var(--danger);
  background: #FFF5F5;
}

.field-help {
  font-size: 12px;
  color: var(--ink-soft);
  margin-top: 6px;
}

.field-error {
  font-size: 12px;
  color: var(--danger);
  margin-top: 6px;
  font-weight: 500;
}

.pwd-meter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.meter-bar {
  flex: 1;
  height: 4px;
  background: var(--bg-2);
  border-radius: 2px;
  overflow: hidden;
}

.meter-fill {
  height: 100%;
  transition: width 0.2s;
}

.meter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--warning);
}

.check-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
  font-size: 13px;
  color: var(--ink-3);
  margin-bottom: 20px;
}

.check-row input[type="checkbox"] {
  margin-top: 2px;
  accent-color: var(--teal);
}

.check-row strong {
  color: var(--ink);
  text-decoration: underline;
}

.switch-mode {
  text-align: center;
  margin-top: 18px;
  font-size: 13px;
  color: var(--ink-soft);
}

.switch-mode a {
  color: var(--teal);
  font-weight: 600;
  margin-left: 4px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.link-muted {
  font-size: 13px;
  color: var(--ink-soft);
  text-decoration: underline;
}

.divider {
  text-align: center;
  margin: 20px 0;
  font-size: 12px;
  color: var(--muted);
  position: relative;
}

.divider::before, .divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background: var(--line);
}
.divider::before { left: 0; }
.divider::after { right: 0; }

.oauth-btn {
  width: 100%;
  padding: 12px;
  background: #FEE500;
  color: #181818;
  border-radius: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.api-note {
  padding: 14px 18px;
  background: var(--bg-2);
  border-radius: var(--r-md);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.api-note code {
  display: block;
  color: var(--ink-3);
}
</style>

<template>
  <div class="page auth-page">
    <header class="auth-header">
      <router-link :to="authStore.isAuthenticated ? '/home' : '/'" class="logo">TripCrew<span class="dot">.</span></router-link>
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
              <strong>실제 이동 시간 기반</strong>
              <span>네이버 Directions 동선 최적화</span>
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
              @click="setMode('login')"
            >
              로그인
            </button>
            <button
              :class="['auth-tab', { active: mode === 'signup' }]"
              @click="setMode('signup')"
            >
              회원가입
            </button>
          </div>

          <form v-if="mode === 'signup'" class="auth-form" @submit.prevent="handleSignup">
            <h2 class="auth-form__title">계정 만들기</h2>
            <p class="auth-form__lead">이메일, 닉네임, 비밀번호를 입력해주세요.</p>

            <div class="field">
              <label>이메일 <span class="req">*</span></label>
              <input v-model.trim="signupForm.email" type="email" placeholder="trip@crew.kr" required />
            </div>

            <div class="field">
              <label>닉네임 <span class="req">*</span></label>
              <input
                v-model.trim="signupForm.nickname"
                type="text"
                placeholder="여행메이트"
                maxlength="50"
                required
              />
            </div>

            <div class="field">
              <label>비밀번호 <span class="req">*</span></label>
              <input v-model="signupForm.password" type="password" minlength="8" maxlength="64" required />
              <div class="pwd-meter">
                <div class="meter-bar">
                  <div class="meter-fill" :style="passwordMeterStyle"></div>
                </div>
                <span class="meter-label" :class="`meter-label--${passwordStrength.key}`">{{ passwordStrength.label }}</span>
              </div>
              <p class="field-help">영문 + 숫자 8자 이상 ✓ · 특수문자 권장</p>
            </div>

            <div class="field">
              <label>비밀번호 확인 <span class="req">*</span></label>
              <input
                v-model="signupForm.passwordConfirm"
                type="password"
                :class="{ 'error-input': isPasswordMismatch }"
                required
              />
              <p v-if="isPasswordMismatch" class="field-error">! 비밀번호가 일치하지 않습니다</p>
            </div>

            <label class="check-row">
              <input v-model="signupForm.agreed" type="checkbox" required />
              <span>
                <strong>서비스 이용약관</strong> 및
                <strong>개인정보 처리방침</strong>에 동의합니다 <span class="req">*</span>
              </span>
            </label>

            <p v-if="signupError" class="form-error">{{ signupError }}</p>

            <BaseButton variant="primary" size="lg" type="submit" full :disabled="isSubmitting">
              {{ isSubmitting ? '처리 중...' : '계정 만들기' }}
            </BaseButton>

            <p class="switch-mode">
              이미 계정이 있으신가요?
              <a href="#" @click.prevent="setMode('login')">로그인 →</a>
            </p>
          </form>

          <form v-else class="auth-form" @submit.prevent="handleLogin">
            <h2 class="auth-form__title">다시 만나서 반갑습니다 👋</h2>
            <p class="auth-form__lead">계정 정보를 입력해주세요.</p>

            <div class="field">
              <label>이메일</label>
              <input v-model.trim="loginForm.email" type="email" placeholder="trip@crew.kr" required />
            </div>

            <div class="field">
              <label>비밀번호</label>
              <input v-model="loginForm.password" type="password" required />
            </div>

            <div class="login-options">
              <label class="check-row">
                <input type="checkbox" />
                <span>로그인 상태 유지</span>
              </label>
              <a href="#" class="link-muted">비밀번호 찾기</a>
            </div>

            <p v-if="loginError" class="form-error">{{ loginError }}</p>

            <BaseButton variant="primary" size="lg" type="submit" full :disabled="isSubmitting">
              {{ isSubmitting ? '처리 중...' : '로그인' }}
            </BaseButton>
          </form>
        </div>

        <aside v-if="mode === 'login'" class="auth-trip-card" aria-label="TripCrew 여행 계획 기능 소개">
          <div class="auth-trip-card__head">
            <span class="auth-trip-card__eyebrow">YOUR NEXT TRIP</span>
            <span class="auth-trip-card__badge">TripCrew</span>
          </div>
          <p class="auth-trip-card__title">한 번의 로그인으로<br />여행의 시작점을 만들어보세요.</p>
          <div class="auth-trip-card__route">
            <div class="auth-trip-card__stop">
              <span class="auth-trip-card__pin auth-trip-card__pin--start"></span>
              <span>취향에 맞는 관광지 찾기</span>
            </div>
            <div class="auth-trip-card__line"></div>
            <div class="auth-trip-card__stop">
              <span class="auth-trip-card__pin auth-trip-card__pin--mid"></span>
              <span>나만의 동선으로 계획하기</span>
            </div>
            <div class="auth-trip-card__line"></div>
            <div class="auth-trip-card__stop">
              <span class="auth-trip-card__pin auth-trip-card__pin--end"></span>
              <span>일행과 함께 완성하기</span>
            </div>
          </div>
        </aside>

      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import BaseButton from '@/components/common/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const mode = ref(getModeFromQuery(route.query.mode))

const signupForm = reactive({
  email: '',
  nickname: '',
  password: '',
  passwordConfirm: '',
  agreed: false,
})

const loginForm = reactive({
  email: '',
  password: '',
})

const isSubmitting = ref(false)
const signupError = ref('')
const loginError = ref('')

const isPasswordMismatch = computed(() =>
  signupForm.passwordConfirm.length > 0 && signupForm.password !== signupForm.passwordConfirm,
)

const passwordStrength = computed(() => {
  const password = signupForm.password
  if (password.length === 0) return { key: 'empty', label: '입력 전', width: '12%', color: 'var(--muted)' }
  if (password.length < 8) return { key: 'weak', label: '약함', width: '28%', color: 'var(--danger)' }

  const hasLetter = /[A-Za-z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasSpecial = /[^A-Za-z0-9]/.test(password)
  const score = [hasLetter, hasNumber, hasSpecial].filter(Boolean).length

  if (score >= 3) return { key: 'strong', label: '좋음', width: '100%', color: 'var(--teal)' }
  if (score >= 2) return { key: 'normal', label: '보통', width: '65%', color: 'var(--warning)' }
  return { key: 'weak', label: '약함', width: '40%', color: 'var(--danger)' }
})

const passwordMeterStyle = computed(() => ({
  width: passwordStrength.value.width,
  background: passwordStrength.value.color,
}))

function getErrorMessage(error, fallback) {
  const data = error?.response?.data
  if (data?.errors) {
    const firstFieldError = Object.values(data.errors)[0]
    if (firstFieldError) return firstFieldError
  }
  return data?.message || fallback
}

function getModeFromQuery(queryMode) {
  return queryMode === 'login' ? 'login' : 'signup'
}

function setMode(nextMode) {
  mode.value = nextMode
  router.replace({ path: '/auth', query: { mode: nextMode } })
}

watch(
  () => route.query.mode,
  (queryMode) => {
    mode.value = getModeFromQuery(queryMode)
  },
)

async function handleSignup() {
  signupError.value = ''
  if (isPasswordMismatch.value) return

  isSubmitting.value = true
  try {
    const credentials = {
      email: signupForm.email,
      password: signupForm.password,
    }
    await authStore.signup({
      ...credentials,
      nickname: signupForm.nickname,
    })
    await authStore.login(credentials)
    router.push(getRedirectTarget())
  } catch (error) {
    signupError.value = getErrorMessage(error, '회원가입에 실패했습니다. 입력값을 확인해주세요.')
  } finally {
    isSubmitting.value = false
  }
}

async function handleLogin() {
  loginError.value = ''
  isSubmitting.value = true
  try {
    await authStore.login(loginForm)
    router.push(getRedirectTarget())
  } catch (error) {
    loginError.value = getErrorMessage(error, '이메일 또는 비밀번호를 확인해주세요.')
  } finally {
    isSubmitting.value = false
  }
}

function getRedirectTarget() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return redirect
  }
  // 관리자/최고관리자는 사용자 대시보드 대신 관리자 대시보드로 바로 착지.
  const role = authStore.user?.role
  if (role === 'ADMIN' || role === 'SUPER_ADMIN') {
    return '/admin'
  }
  return '/home'
}
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
  color: var(--teal-ink);
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
  color: var(--teal-ink);
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
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  box-shadow: var(--sh-2);
  overflow: hidden;
}

.auth-trip-card {
  position: relative;
  overflow: hidden;
  padding: 20px 24px;
  border: 1px solid rgba(15, 118, 110, 0.2);
  border-radius: var(--r-xl);
  background:
    radial-gradient(circle at 90% 10%, rgba(255, 129, 102, 0.2), transparent 28%),
    linear-gradient(135deg, #EFFAF8, #F8FCFB);
}

.auth-trip-card::after {
  content: '';
  position: absolute;
  right: -26px;
  bottom: -36px;
  width: 120px;
  height: 120px;
  border: 1px solid rgba(15, 118, 110, 0.12);
  border-radius: 50%;
  box-shadow: 0 0 0 20px rgba(15, 118, 110, 0.05), 0 0 0 40px rgba(15, 118, 110, 0.03);
}

.auth-trip-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.auth-trip-card__eyebrow {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: var(--teal-ink);
}

.auth-trip-card__badge {
  padding: 4px 8px;
  border-radius: 999px;
  background: var(--glass);
  color: var(--ink-3);
  font-size: 11px;
  font-weight: 700;
}

.auth-trip-card__title {
  position: relative;
  z-index: 1;
  margin: 10px 0 16px;
  color: var(--ink);
  font-size: 17px;
  font-weight: 700;
  line-height: 1.45;
  letter-spacing: -0.3px;
}

.auth-trip-card__route {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 7px;
}

.auth-trip-card__stop {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  color: var(--ink-2);
  font-size: 11px;
  font-weight: 600;
  line-height: 1.35;
}

.auth-trip-card__pin {
  flex: 0 0 auto;
  width: 9px;
  height: 9px;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 0 0 1px currentColor;
}

.auth-trip-card__pin--start { color: var(--teal); background: var(--teal); }
.auth-trip-card__pin--mid { color: #D99525; background: #F1B247; }
.auth-trip-card__pin--end { color: var(--coral); background: var(--coral); }

.auth-trip-card__line {
  flex: 1 0 12px;
  height: 1px;
  min-width: 12px;
  background: rgba(15, 118, 110, 0.34);
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
  background: var(--surface);
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
  background: var(--surface);
  border-color: var(--teal);
  box-shadow: 0 0 0 2px var(--teal-soft);
}

.field input.error-input {
  border-color: var(--danger);
  background: rgba(220, 53, 69, 0.12);
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

.form-error {
  padding: 10px 12px;
  margin: -4px 0 14px;
  border: 1px solid rgba(224, 70, 85, 0.25);
  border-radius: 8px;
  background: rgba(220, 53, 69, 0.12);
  color: var(--danger);
  font-size: 13px;
  font-weight: 600;
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

.meter-label--empty {
  color: var(--muted);
}

.meter-label--weak {
  color: var(--danger);
}

.meter-label--normal {
  color: var(--warning);
}

.meter-label--strong {
  color: var(--teal);
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

@media (max-width: 860px) {
  .auth-header {
    padding: 20px 24px;
  }

  .auth-grid {
    grid-template-columns: 1fr;
    gap: 32px;
    padding: 24px var(--space-5) 56px;
  }

  .auth-left {
    max-width: 560px;
  }

  .auth-left__title {
    font-size: 36px;
  }
}

@media (max-width: 540px) {
  .auth-header {
    padding: 18px 20px;
  }

  .auth-grid {
    padding: 20px 16px 40px;
  }

  .auth-left__title {
    font-size: 31px;
  }

  .auth-left__sub br {
    display: none;
  }

  .auth-form {
    padding: 22px 20px 26px;
  }

  .auth-trip-card {
    padding: 18px;
  }

  .auth-trip-card__route {
    align-items: flex-start;
    flex-direction: column;
    gap: 5px;
  }

  .auth-trip-card__line {
    flex: 0 0 10px;
    width: 1px;
    min-width: 1px;
    margin-left: 4px;
  }
}
</style>

<template>
  <div class="page auth-page">
    <header class="auth-header">
      <router-link :to="authStore.isAuthenticated ? '/home' : '/'" class="logo">TripCrew<span class="dot">.</span></router-link>
      <button
        class="auth-theme-toggle"
        type="button"
        :aria-label="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'"
        :title="isDark ? '라이트 모드' : '다크 모드'"
        @click="toggleTheme"
      >
        <svg v-if="isDark" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="12" cy="12" r="4.2"/>
          <path d="M12 2.5v2M12 19.5v2M4.6 4.6l1.4 1.4M18 18l1.4 1.4M2.5 12h2M19.5 12h2M4.6 19.4 6 18M18 6l1.4-1.4"/>
        </svg>
        <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 12.5A8.5 8.5 0 1 1 11.5 3a6.5 6.5 0 0 0 9.5 9.5z"/>
        </svg>
      </button>
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
              <span>네이버 지도 기반 동선 최적화</span>
            </div>
          </li>
          <li>
            <div class="feature-icon">✓</div>
            <div>
              <strong>최대 10명까지 동시 편집</strong>
              <span>실시간 동기화</span>
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

          <!-- 채용담당자용 원클릭 데모 로그인 (로그인 탭에서만 노출) -->
          <div v-if="mode === 'login'" class="demo-hint">
            <p class="demo-hint__title">🎯 채용담당자님, 환영합니다 👋</p>
            <p class="demo-hint__desc">가입 없이 데모 계정으로 전체 기능을 둘러보세요.</p>
            <button
              type="button"
              class="demo-hint__btn"
              :disabled="isSubmitting"
              @click="handleDemoLogin"
            >
              {{ isSubmitting ? '로그인 중…' : '데모 계정으로 바로 로그인 →' }}
            </button>
            <p class="demo-hint__cred">
              <span>{{ DEMO_EMAIL }}</span>
              <span class="demo-hint__sep">·</span>
              <span>{{ DEMO_PASSWORD }}</span>
            </p>
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
            <h2 class="auth-form__title">만나서 반갑습니다.</h2>
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

          <!-- 소셜 로그인(로그인/회원가입 공통). 백엔드 OAuth2 로 top-level 이동. -->
          <div class="social-login">
            <div class="social-divider"><span>또는 소셜 계정으로</span></div>
            <button type="button" class="social-btn social-btn--kakao" @click="socialLogin('kakao')">
              <span class="social-btn__icon">K</span>
              카카오로 시작하기
            </button>
            <button type="button" class="social-btn social-btn--naver" @click="socialLogin('naver')">
              <span class="social-btn__icon">N</span>
              네이버로 시작하기
            </button>
          </div>
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
import { assetBaseURL } from '@/api/http'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

const { isDark, toggle: toggleTheme } = useTheme()

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

// 소셜 로그인: 백엔드 OAuth2 인가요청으로 top-level 이동(XHR 아님).
// 성공 시 백엔드가 /oauth/callback?code=... 로 되돌려보낸다.
function socialLogin(provider) {
  window.location.href = `${assetBaseURL}/oauth2/authorization/${provider}`
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

// 채용담당자용 체험 계정. 값을 채운 뒤 일반 로그인 흐름을 그대로 재사용한다.
const DEMO_EMAIL = 'demo@tripcrew.kr'
const DEMO_PASSWORD = 'tripcrew1234'

async function handleDemoLogin() {
  loginForm.email = DEMO_EMAIL
  loginForm.password = DEMO_PASSWORD
  await handleLogin()
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
}

.auth-theme-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  color: var(--ink-3);
  transition: background 0.15s, color 0.15s;
}

.auth-theme-toggle:hover {
  background: var(--bg-2);
  color: var(--ink);
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
    linear-gradient(135deg, var(--teal-tint), var(--surface));
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

/* 소셜 로그인 */
.social-login {
  margin-top: 18px;
}

.social-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 4px 0 14px;
  color: var(--muted);
  font-size: 0.8rem;
}

.social-divider::before,
.social-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 16px;
  margin-bottom: 10px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: filter 0.15s ease, transform 0.05s ease;
}

.social-btn:hover { filter: brightness(0.96); }
.social-btn:active { transform: translateY(1px); }

.social-btn__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 800;
}

/* 제공자 브랜드 컬러(식별성 위해 토큰 대신 브랜드 색 사용) */
.social-btn--kakao {
  background: #fee500;
  color: rgba(0, 0, 0, 0.85);
}
.social-btn--kakao .social-btn__icon {
  background: rgba(0, 0, 0, 0.85);
  color: #fee500;
}

.social-btn--naver {
  background: #03c75a;
  color: #fff;
}
.social-btn--naver .social-btn__icon {
  background: #fff;
  color: #03c75a;
}

/* 채용담당자용 원클릭 데모 로그인 카드 */
.demo-hint {
  margin-bottom: var(--space-5);
  padding: 16px;
  border: 1px solid var(--teal);
  border-radius: var(--radius-md);
  background: var(--teal-tint);
}
.demo-hint__title {
  margin: 0 0 4px;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--teal-ink);
}
.demo-hint__desc {
  margin: 0 0 12px;
  font-size: 0.82rem;
  line-height: 1.5;
  color: var(--ink-3);
}
.demo-hint__btn {
  width: 100%;
  padding: 11px 14px;
  border: none;
  border-radius: var(--radius-md);
  background: var(--teal);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: filter 0.15s ease, transform 0.05s ease;
}
.demo-hint__btn:hover:not(:disabled) { filter: brightness(1.06); }
.demo-hint__btn:active:not(:disabled) { transform: translateY(1px); }
.demo-hint__btn:disabled { opacity: 0.6; cursor: default; }
.demo-hint__cred {
  margin: 10px 0 0;
  text-align: center;
  font-size: 0.78rem;
  color: var(--ink-soft);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
.demo-hint__sep { margin: 0 6px; opacity: 0.5; }

@media (prefers-reduced-motion: reduce) {
  .social-btn,
  .demo-hint__btn { transition: none; }
}

/* 반응형: 폰에서는 좌측 소개 패널을 숨겨 폼이 화면을 꽉 채우도록 */
@media (max-width: 640px) {
  .auth-left { display: none; }
  .auth-grid { grid-template-columns: 1fr; }
  /* 터치 편의: 입력/탭 최소 높이 확보 */
  .field input,
  .auth-tab { min-height: 44px; }
  .demo-hint__btn,
  .social-btn { min-height: 44px; }
}
</style>

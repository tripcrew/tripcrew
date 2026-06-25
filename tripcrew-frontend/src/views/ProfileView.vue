<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="container profile-page">
      <section class="profile-panel">
        <div class="profile-head">
          <div class="avatar avatar--lg">{{ avatarText }}</div>
          <div>
            <p class="t-caption">마이페이지</p>
            <h1 class="profile-title">{{ displayName }} 님</h1>
            <p class="profile-email">{{ userEmail }}</p>
          </div>
        </div>

        <dl class="profile-info">
          <div>
            <dt>닉네임</dt>
            <dd>{{ displayName }}</dd>
          </div>
          <div>
            <dt>이메일</dt>
            <dd>{{ userEmail }}</dd>
          </div>
          <div>
            <dt>권한</dt>
            <dd>{{ userRole }}</dd>
          </div>
        </dl>

        <section v-if="isEditMode" class="edit-section">
          <div class="edit-section__head">
            <div>
              <h2>정보 수정</h2>
              <p v-if="isAdmin">정보를 변경하려면 현재 비밀번호를 입력해 주세요.</p>
              <p v-else>변경하거나 탈퇴하려면 현재 비밀번호를 입력해 주세요.</p>
            </div>
            <button type="button" class="edit-close" aria-label="정보 수정 닫기" @click="closeEditMode">×</button>
          </div>

          <form v-if="!isPasswordVerified" class="password-check" @submit.prevent="handlePasswordVerification">
            <label class="form-label" for="current-password">비밀번호</label>
            <div class="password-check__row">
              <input id="current-password" v-model="currentPassword" type="password" maxlength="64" autocomplete="current-password" required />
              <BaseButton variant="primary" type="submit" :disabled="isVerifyingPassword">
                {{ isVerifyingPassword ? '인증 중...' : '인증' }}
              </BaseButton>
            </div>
            <p v-if="passwordError" class="form-error">{{ passwordError }}</p>
          </form>

          <template v-else>
            <p class="verified-message">비밀번호 인증이 완료되었습니다.</p>
            <form class="nickname-form" @submit.prevent="handleNicknameUpdate">
              <label class="form-label" for="nickname">새 닉네임</label>
              <div class="nickname-form__row">
                <input id="nickname" v-model.trim="nickname" type="text" maxlength="50" required />
                <BaseButton variant="secondary" type="submit" :disabled="isUpdatingNickname">
                  {{ isUpdatingNickname ? '변경 중...' : '닉네임 변경' }}
                </BaseButton>
              </div>
              <p v-if="nicknameMessage" class="form-message">{{ nicknameMessage }}</p>
              <p v-if="nicknameError" class="form-error">{{ nicknameError }}</p>
            </form>

            <form class="nickname-form password-form" @submit.prevent="handlePasswordChange">
              <label class="form-label" for="new-password">새 비밀번호</label>
              <input id="new-password" v-model="newPassword" type="password" minlength="8" maxlength="64" autocomplete="new-password" placeholder="8자 이상" required />
              <label class="form-label" for="confirm-password">새 비밀번호 확인</label>
              <div class="nickname-form__row">
                <input id="confirm-password" v-model="confirmPassword" type="password" minlength="8" maxlength="64" autocomplete="new-password" required />
                <BaseButton variant="secondary" type="submit" :disabled="isUpdatingPassword">
                  {{ isUpdatingPassword ? '변경 중...' : '비밀번호 변경' }}
                </BaseButton>
              </div>
              <p v-if="passwordMessage" class="form-message">{{ passwordMessage }}</p>
              <p v-if="passwordChangeError" class="form-error">{{ passwordChangeError }}</p>
            </form>

            <template v-if="!isAdmin">
              <div class="withdraw-row">
                <div>
                  <h3>회원 탈퇴</h3>
                  <p>탈퇴 후에는 로그인하거나 계정을 복구할 수 없습니다.</p>
                </div>
                <BaseButton variant="danger" :disabled="isWithdrawing" @click="showWithdrawConfirm = true">
                  {{ isWithdrawing ? '처리 중...' : '탈퇴하기' }}
                </BaseButton>
              </div>
              <p v-if="withdrawError" class="form-error">{{ withdrawError }}</p>
            </template>
          </template>
        </section>

        <div class="profile-actions">
          <BaseButton variant="secondary" @click="goHome">{{ isAdmin ? '대시보드로' : '홈으로' }}</BaseButton>
          <BaseButton v-if="!isEditMode" class="profile-edit-button" variant="secondary" @click="openEditMode">정보 수정</BaseButton>
        </div>
      </section>
    </main>
    <transition name="overlay">
    <div v-if="showWithdrawConfirm" class="confirm-backdrop" @click.self="showWithdrawConfirm = false">
      <section class="confirm-dialog" role="dialog" aria-modal="true">
        <h2>정말 탈퇴하시겠습니까?</h2>
        <p>탈퇴 후에는 로그인하거나 계정을 복구할 수 없습니다.</p>
        <div class="confirm-dialog__actions">
          <BaseButton variant="secondary" @click="showWithdrawConfirm = false">취소</BaseButton>
          <BaseButton variant="danger" :disabled="isWithdrawing" @click="handleWithdraw">
            {{ isWithdrawing ? '처리 중...' : '탈퇴하기' }}
          </BaseButton>
        </div>
      </section>
    </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const displayName = computed(() => authStore.user?.nickname || '여행자')
const userEmail = computed(() => authStore.user?.email || '이메일 정보 없음')
const userRole = computed(() => authStore.user?.role || 'USER')
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || 'U')
const isEditMode = ref(false)
const isPasswordVerified = ref(false)
const nickname = ref(displayName.value)
const currentPassword = ref('')
const isVerifyingPassword = ref(false)
const passwordError = ref('')
const isUpdatingNickname = ref(false)
const nicknameMessage = ref('')
const nicknameError = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isUpdatingPassword = ref(false)
const passwordMessage = ref('')
const passwordChangeError = ref('')
const isAdmin = computed(() => ['ADMIN', 'SUPER_ADMIN'].includes(authStore.user?.role))
const isWithdrawing = ref(false)
const withdrawError = ref('')
const showWithdrawConfirm = ref(false)

watch(displayName, (value) => {
  nickname.value = value
})

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace({ path: '/auth', query: { mode: 'login' } })
  }
})

function goHome() {
  router.push(isAdmin.value ? '/admin' : '/home')
}

function openEditMode() {
  isEditMode.value = true
  isPasswordVerified.value = false
  currentPassword.value = ''
  passwordError.value = ''
  nicknameMessage.value = ''
  nicknameError.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  passwordMessage.value = ''
  passwordChangeError.value = ''
  withdrawError.value = ''
}

function closeEditMode() {
  isEditMode.value = false
  isPasswordVerified.value = false
  showWithdrawConfirm.value = false
  currentPassword.value = ''
  nickname.value = displayName.value
  nicknameMessage.value = ''
  nicknameError.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  passwordMessage.value = ''
  passwordChangeError.value = ''
  withdrawError.value = ''
}

async function handlePasswordVerification() {
  passwordError.value = ''
  isVerifyingPassword.value = true
  try {
    await authStore.verifyPassword(currentPassword.value)
    isPasswordVerified.value = true
  } catch (error) {
    passwordError.value = error?.response?.data?.message || '비밀번호를 확인하지 못했습니다.'
  } finally {
    isVerifyingPassword.value = false
  }
}

async function handleNicknameUpdate() {
  nicknameMessage.value = ''
  nicknameError.value = ''
  if (!nickname.value || !currentPassword.value) return

  isUpdatingNickname.value = true
  try {
    await authStore.updateNickname(nickname.value, currentPassword.value)
    nicknameMessage.value = '닉네임을 변경했어요.'
  } catch (error) {
    nicknameError.value = error?.response?.data?.message || '닉네임을 변경하지 못했습니다.'
  } finally {
    isUpdatingNickname.value = false
  }
}

async function handlePasswordChange() {
  passwordMessage.value = ''
  passwordChangeError.value = ''
  if (!newPassword.value || !confirmPassword.value) return
  if (newPassword.value.length < 8) {
    passwordChangeError.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  if (!/[A-Za-z]/.test(newPassword.value) || !/\d/.test(newPassword.value)) {
    passwordChangeError.value = '비밀번호는 영문과 숫자를 포함해야 합니다.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    passwordChangeError.value = '새 비밀번호가 일치하지 않습니다.'
    return
  }

  isUpdatingPassword.value = true
  try {
    await authStore.updatePassword(currentPassword.value, newPassword.value)
    passwordMessage.value = '비밀번호를 변경했어요.'
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (error) {
    passwordChangeError.value = fieldError(error, 'newPassword') || error?.response?.data?.message || '비밀번호를 변경하지 못했습니다.'
  } finally {
    isUpdatingPassword.value = false
  }
}

function fieldError(error, field) {
  return error?.response?.data?.errors?.[field] || ''
}

async function handleWithdraw() {
  withdrawError.value = ''

  isWithdrawing.value = true
  try {
    await authStore.withdraw(currentPassword.value)
    router.replace('/')
  } catch (error) {
    withdrawError.value = error?.response?.data?.message || '회원 탈퇴를 처리하지 못했습니다.'
  } finally {
    isWithdrawing.value = false
    showWithdrawConfirm.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 48px var(--space-6) 80px;
}

.profile-panel {
  max-width: 720px;
  margin: 0 auto;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  padding: 32px;
  box-shadow: var(--sh-1);
}

.profile-head {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding-bottom: 28px;
  border-bottom: 1px solid var(--line);
}

.profile-title {
  margin: 2px 0 4px;
  font-size: 28px;
  font-weight: 800;
}

.profile-email { color: var(--ink-soft); }

.avatar {
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--teal);
  color: white;
  font-weight: 800;
}

.avatar--lg {
  width: 72px;
  height: 72px;
  flex: 0 0 auto;
  font-size: 28px;
}

.profile-info {
  display: grid;
  gap: 12px;
  margin: 28px 0;
}

.profile-info div {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: var(--space-4);
  padding: 14px 16px;
  border-radius: var(--r-md);
  background: var(--bg-soft);
}

.profile-info dt {
  color: var(--ink-soft);
  font-size: 14px;
}

.profile-info dd {
  color: var(--ink);
  font-weight: 700;
}

.edit-section {
  margin-top: 28px;
  padding: 24px;
  border: 1px solid var(--line);
  border-radius: var(--r-lg);
  background: var(--bg-soft);
}

.edit-section__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.edit-section h2,
.withdraw-row h3 {
  font-size: 16px;
  font-weight: 800;
}

.edit-section__head p,
.withdraw-row p {
  margin-top: 5px;
  color: var(--ink-soft);
  font-size: 13px;
}

.edit-close {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: var(--ink-soft);
  font-size: 24px;
  line-height: 1;
}

.form-label {
  display: block;
  margin: 16px 0 7px;
  color: var(--ink-3);
  font-size: 13px;
  font-weight: 700;
}

.edit-section input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line-2);
  border-radius: 9px;
  background: var(--surface);
  font: inherit;
}

.password-check__row,
.nickname-form__row {
  display: flex;
  gap: 10px;
}

.password-check__row input,
.nickname-form__row input { min-width: 0; }

.password-form {
  margin-top: 4px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.verified-message {
  margin: 16px 0 -2px;
  color: var(--success);
  font-size: 13px;
  font-weight: 700;
}

.withdraw-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--line);
}

.withdraw-row h3 { color: var(--danger); }

.form-message,
.form-error {
  margin-top: 8px;
  font-size: 13px;
}

.form-message { color: var(--success); }
.form-error { color: var(--danger); }

.profile-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: 28px;
}

.profile-edit-button {
  border-color: var(--coral);
  background: var(--coral);
  color: white;
}

.profile-edit-button:hover:not(:disabled) {
  border-color: var(--coral-2);
  background: var(--coral-2);
}

.confirm-backdrop {
  position: fixed;
  z-index: 100;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(17, 31, 29, 0.4);
}

.confirm-dialog {
  width: min(100%, 400px);
  padding: 28px;
  border-radius: var(--r-xl);
  background: var(--surface);
  box-shadow: var(--sh-2);
}

.confirm-dialog h2 {
  font-size: 19px;
  font-weight: 800;
}

.confirm-dialog p {
  margin-top: 9px;
  color: var(--ink-soft);
  font-size: 14px;
  line-height: 1.55;
}

.confirm-dialog__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
}

@media (max-width: 640px) {
  .profile-panel { padding: 24px; }
  .profile-head { align-items: flex-start; }
  .profile-info div { grid-template-columns: 1fr; gap: 4px; }
  .password-check__row,
  .nickname-form__row,
  .withdraw-row,
  .profile-actions { flex-direction: column; }
  .withdraw-row { align-items: stretch; }
}
</style>

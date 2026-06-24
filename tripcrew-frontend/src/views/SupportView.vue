<template>
  <div class="page page-soft">
    <AppHeader />

    <main class="support-page">
      <div class="support-page__inner">
        <router-link to="/" class="back-link">← TripCrew 홈으로</router-link>

        <section class="support-card">
          <p class="support-card__eyebrow">TRIPCREW · SUPPORT</p>
          <div class="support-card__icon" aria-hidden="true">✦</div>
          <h1>1:1 문의 기능을 준비하고 있어요</h1>
          <p class="support-card__lead">문의 등록, 관리자 답변, 알림 기능은 더 나은 경험을 위해 준비 중입니다.</p>
          <p class="support-card__description">현재 궁금한 내용은 자주 묻는 질문과 공지사항에서 먼저 확인해 주세요. 서비스 관련 중요한 안내도 공지사항을 통해 전달됩니다.</p>

          <div class="support-card__actions">
            <router-link to="/faq" class="support-button support-button--primary">자주 묻는 질문 보기</router-link>
            <router-link to="/notices" class="support-button support-button--secondary">공지사항 보기</router-link>
          </div>
        </section>

        <section class="support-next" aria-label="문의 기능 안내">
          <h2>준비 중인 문의 기능</h2>
          <ul>
            <li>문의 내용 등록 및 진행 상태 확인</li>
            <li>관리자 답변 확인</li>
            <li>답변 등록 시 알림 제공</li>
          </ul>
        </section>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup>
import AppFooter from '@/components/common/AppFooter.vue'
import AppHeader from '@/components/common/AppHeader.vue'
</script>

<style scoped>
.support-page { padding: 56px var(--space-6) 0; }
.support-page__inner { max-width: 860px; margin: 0 auto; }

.back-link {
  display: inline-flex;
  margin-bottom: 24px;
  color: var(--teal-3);
  font-size: 14px;
  font-weight: 700;
}

.support-card, .support-next {
  border: 1px solid var(--line);
  border-radius: var(--r-xl);
  background: var(--bg);
}

.support-card { padding: 56px 48px; text-align: center; }
.support-card__eyebrow {
  color: var(--teal);
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.8px;
}
.support-card__icon {
  display: grid;
  width: 56px;
  height: 56px;
  margin: 24px auto;
  place-items: center;
  border-radius: 50%;
  background: var(--teal-soft);
  color: var(--teal);
  font-size: 28px;
}
.support-card h1 {
  color: var(--ink);
  font-size: 32px;
  letter-spacing: -0.8px;
}
.support-card__lead { margin-top: 14px; color: var(--ink-3); font-size: 17px; }
.support-card__description { max-width: 560px; margin: 12px auto 0; color: var(--ink-soft); font-size: 14px; line-height: 1.65; }
.support-card__actions { display: flex; justify-content: center; gap: 12px; margin-top: 32px; }
.support-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 18px;
  border: 1px solid transparent;
  border-radius: var(--r-md);
  font-size: 14px;
  font-weight: 700;
}
.support-button--primary { background: var(--teal); color: white; }
.support-button--primary:hover { background: var(--teal-2); }
.support-button--secondary { border-color: var(--line-2); color: var(--ink-3); }
.support-button--secondary:hover { background: var(--bg-2); color: var(--ink); }

.support-next { margin-top: 20px; padding: 32px 40px; }
.support-next h2 { color: var(--ink); font-size: 18px; }
.support-next ul { display: grid; gap: 10px; margin-top: 18px; }
.support-next li { position: relative; padding-left: 20px; color: var(--ink-3); font-size: 15px; }
.support-next li::before { content: '•'; position: absolute; left: 4px; color: var(--coral); }

@media (max-width: 640px) {
  .support-page { padding: 32px var(--space-4) 0; }
  .support-card { padding: 40px 24px; }
  .support-card h1 { font-size: 27px; }
  .support-card__actions { flex-direction: column; }
  .support-next { padding: 28px 24px; }
}
</style>

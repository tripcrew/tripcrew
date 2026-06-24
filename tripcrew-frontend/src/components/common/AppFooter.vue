<template>
  <footer class="app-footer">
    <div class="app-footer__inner">
      <div class="app-footer__brand">
        <div class="logo">TripCrew<span class="dot">.</span></div>
        <p class="t-caption">공공데이터 · AI · 동선 최적화로 만드는 여행 계획 플랫폼</p>
      </div>

      <div class="app-footer__links">
        <div class="link-group">
          <h4>서비스</h4>
          <a href="#">기능 소개</a>
          <a href="#">요금제</a>
          <a href="#">변경 사항</a>
        </div>
        <div class="link-group">
          <h4>지원</h4>
          <a href="#">고객지원</a>
          <a href="#">FAQ</a>
          <a href="#">문의하기</a>
        </div>
        <div class="link-group">
          <h4>법적 고지</h4>
          <router-link to="/terms">이용약관</router-link>
          <router-link to="/privacy">개인정보처리방침</router-link>
          <router-link to="/data-sources">공공데이터 출처</router-link>
        </div>
      </div>
    </div>

    <div class="app-footer__bottom">
      <span class="t-mono">© 2026 TripCrew</span>
      <span class="t-mono">v0.1.0 · Vue 3 + Spring Boot</span>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--teal-3);
  color: rgba(255, 255, 255, 0.86);
  padding: 64px 0 32px;
  margin-top: 80px;
}

.app-footer__inner {
  max-width: var(--container-max);
  margin: 0 auto;
  padding: 0 var(--space-6);
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 64px;
}

.app-footer__brand .logo {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.6px;
  color: white;
  margin-bottom: 12px;
}

.app-footer__brand .dot {
  color: var(--coral);
}

.app-footer__brand .t-caption {
  color: rgba(255, 255, 255, 0.6);
}

.app-footer__links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.link-group h4 {
  font-size: 13px;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

.link-group a {
  display: block;
  padding: 6px 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.15s;
}

.link-group a:hover {
  color: white;
}

.app-footer__bottom {
  max-width: var(--container-max);
  margin: 48px auto 0;
  padding: 24px var(--space-6) 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}
</style>

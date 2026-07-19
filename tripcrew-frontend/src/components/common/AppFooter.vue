<template>
  <footer class="app-footer">
    <div class="app-footer__inner">
      <div class="app-footer__brand">
        <div class="logo">TripCrew<span class="dot">.</span></div>
        <p class="t-caption">AI 추천 · 실제 이동 시간 기반 동선 최적화 · 공동 편집</p>
      </div>

      <div class="app-footer__links">
        <div class="link-group">
          <h4>서비스</h4>
          <ul class="feature-list">
            <li>
              <span class="feature-list__mark">✦</span>
              <span><strong>AI 맞춤 여행 추천</strong><small>한 줄의 취향으로 시작</small></span>
            </li>
            <li>
              <span class="feature-list__mark">✦</span>
              <span><strong>효율적인 동선 설계</strong><small>여행지 사이의 이동을 더 가볍게</small></span>
            </li>
            <li>
              <span class="feature-list__mark">✦</span>
              <span><strong>실시간 공동 편집</strong><small>동행자와 함께 완성하는 일정</small></span>
            </li>
          </ul>
        </div>
        <div class="link-group">
          <h4>고객지원</h4>
          <router-link to="/notices">공지사항</router-link>
          <router-link to="/faq">자주 묻는 질문</router-link>
          <router-link to="/support">1:1 문의</router-link>
        </div>
        <div class="link-group">
          <h4>법적 고지</h4>
          <router-link to="/terms">이용약관</router-link>
          <router-link to="/privacy">개인정보처리방침</router-link>
          <router-link to="/data-sources">관광지 데이터 안내</router-link>
        </div>
      </div>
    </div>

    <div class="app-footer__bottom">
      <span class="t-mono">© 2026 TripCrew</span>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: var(--teal-3);
  color: rgba(255, 255, 255, 0.86);
  padding: 64px 0 32px;
  /* Pin to viewport bottom on short pages (parent .page is a flex column);
     on tall pages this collapses to 0 and the footer follows content. */
  margin-top: auto;
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

.feature-list {
  display: grid;
  gap: 14px;
}

.feature-list li {
  display: flex;
  gap: 9px;
  color: rgba(255, 255, 255, 0.68);
}

.feature-list__mark {
  color: var(--coral);
  font-size: 12px;
  line-height: 1.5;
}

.feature-list strong,
.feature-list small {
  display: block;
}

.feature-list strong {
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
  font-weight: 700;
}

.feature-list small {
  margin-top: 2px;
  color: rgba(255, 255, 255, 0.48);
  font-size: 12px;
  line-height: 1.4;
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

@media (max-width: 760px) {
  .app-footer {
    padding-top: 48px;
  }

  .app-footer__inner {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .app-footer__links {
    gap: 20px;
  }

  .app-footer__bottom {
    margin-top: 40px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 640px) {
  .app-footer {
    padding-top: 40px;
  }
  .app-footer__inner {
    padding: 0 var(--space-4);
    gap: 32px;
  }
  /* '서비스' 소개 리스트(AI 추천·동선·공동편집)는 랜딩에도 있고 모바일에선 길기만 해 숨김
     → 브랜드 + 고객지원/법적고지 링크 + copyright 만 남겨 간결하게 */
  .app-footer__links {
    grid-template-columns: 1fr 1fr;
    gap: 24px 20px;
  }
  .app-footer__links .link-group:first-child {
    display: none;
  }
  .app-footer__bottom {
    padding: 24px var(--space-4) 0;
  }
}
</style>

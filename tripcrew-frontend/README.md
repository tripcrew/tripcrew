# TripCrew Frontend

> 함께 만드는 여행 계획 플랫폼
>
> **Vue 3 + Vite** 기반 프론트엔드 (정적 화면 구현체)

## 📋 개요

화면 설계서(`TripCrew_Screen_Spec.html`)에 정의된 **12개 데스크톱 화면**을 Vue 3로 구현한 정적 사이트입니다. 백엔드 API 연동 전 단계의 UI 골격이며, 디자인 시스템·라우팅·페이지 이동이 모두 작동합니다.

| ID | 화면 | 경로 | 비고 |
|---|---|---|---|
| SC-01 | 랜딩 페이지 | `/` | 비회원 진입점 |
| SC-02 | 회원가입/로그인 | `/auth` | 탭 전환 가능 |
| SC-03 | 메인 대시보드 | `/home` | 회원 진입점 |
| SC-04 | AI 챗봇 | `/chat` | 코스 추천 카드 |
| SC-05 | 관광지 검색 | `/attractions` | 필터 + 스켈레톤 |
| SC-06 | 관광지 상세 | `/attractions/:id` | 날씨, EV 충전소 |
| SC-07 | 여행 계획 편집 | `/plans/:id/edit` | 동선 최적화 모달 |
| SC-08 | 공동 편집 | `/plans/:id/co` | 충돌 모달 (409) |
| SC-09 | 내 여행 계획 | `/plans` | 진행/완료 분리 |
| SC-10 | 후기 작성/조회 | `/attractions/:id/reviews` | 별점, 사진 첨부 |
| SC-11 | 관리자 페이지 | `/admin/users` | 회원 관리 테이블 |
| SC-12 | 에러/빈 상태 | `/errors/:type?` | 4분할 카드 |

## 🚀 실행 방법

### 필수 환경
- **Node.js 18+** (Node 20 권장)
- npm 또는 pnpm

### 설치 & 실행

```bash
# 1. 의존성 설치
npm install

# 2. 개발 서버 시작 (자동으로 브라우저 열림 · 포트 5173)
npm run dev

# 3. 프로덕션 빌드
npm run build

# 4. 빌드 결과 미리보기
npm run preview
```

설치가 끝나면 자동으로 [http://localhost:5173](http://localhost:5173)이 열립니다.

### VSCode에서 작업하기

```bash
# 프로젝트 폴더에서 VSCode 열기
code .
```

추천 확장 프로그램:
- **Vue - Official** (Vue.volar) - Vue 3 공식 확장
- **ESLint**
- **Prettier**

## 📁 프로젝트 구조

```
tripcrew-frontend/
├── index.html              # Vite 진입 HTML (Pretendard, JetBrains Mono CDN)
├── vite.config.js          # @ → src 별칭, 포트 5173
├── package.json
│
└── src/
    ├── main.js             # Vue 앱 부트스트랩
    ├── App.vue             # 루트 컴포넌트 (RouterView)
    │
    ├── router/
    │   └── index.js        # 12개 라우트 + 404 폴백
    │
    ├── assets/
    │   └── styles/
    │       ├── reset.css   # CSS 리셋
    │       ├── tokens.css  # 디자인 토큰 (색상, 폰트, 그림자, radius)
    │       └── global.css  # 전역 유틸리티 (타이포, container, chip)
    │
    ├── components/
    │   └── common/
    │       ├── AppHeader.vue   # 공통 헤더 (로그인 상태 / 미니멀 모드)
    │       ├── AppFooter.vue   # 공통 푸터
    │       └── BaseButton.vue  # 4 variant × 3 size 버튼
    │
    └── views/              # 12개 화면 컴포넌트 (SC-01 ~ SC-12)
        ├── LandingView.vue
        ├── AuthView.vue
        ├── DashboardView.vue
        ├── ChatbotView.vue
        ├── SearchView.vue
        ├── AttractionDetailView.vue
        ├── PlanEditView.vue
        ├── CoEditView.vue
        ├── MyPlansView.vue
        ├── ReviewsView.vue
        ├── AdminView.vue
        └── ErrorView.vue
```

## 🎨 디자인 시스템

화면 설계서(SC-02 Design System)의 토큰을 그대로 적용했습니다.

### 색상
```
Primary Teal  · #0F6E56  ── 메인 브랜드
Teal-3        · #0A503E  ── 어두운 강조
Accent Coral  · #D85A30  ── CTA, 핵심 액션
```

### 타이포그래피
- **Pretendard Variable** (한글, 본문) — CDN 로드
- **JetBrains Mono** (코드, 데이터) — Google Fonts

### 모양
- **Radius**: 6 / 10 / 14 / 18px
- **Shadow**: 3단계 (sh-1, sh-2, sh-3) + 모달 전용

토큰은 `src/assets/styles/tokens.css`에 정의되어 있으며 CSS 변수로 모든 컴포넌트에서 사용됩니다.

## 🛠 기술 스택

| 영역 | 기술 |
|---|---|
| 프레임워크 | Vue 3.4 (Composition API · `<script setup>`) |
| 빌드 도구 | Vite 5 |
| 라우팅 | Vue Router 4 |
| 스타일 | Plain CSS · Scoped Styles · CSS Variables |
| 폰트 | Pretendard Variable, JetBrains Mono |

> 의도적으로 Tailwind / 컴포넌트 라이브러리를 사용하지 않았습니다. 디자인 토큰을 명확히 드러내고 백엔드 학습 부담을 줄이기 위함입니다.

## 🔄 백엔드 연동 가이드 (다음 단계)

현재는 정적 화면이지만 각 화면에 **백엔드 매핑 주석**(API 엔드포인트)을 표시해 두었습니다.

다음 단계에서 추가할 것:

1. **axios + 인터셉터 설정** (`src/api/`)
   - 토큰 자동 갱신 (401 → Refresh → Retry)
2. **Pinia 상태 관리** (`src/stores/`)
   - `auth`, `plans`, `attractions` 등
3. **WebSocket 클라이언트** (`src/services/ws.js`)
   - SC-07 동선 최적화 진행, SC-08 공동 편집
4. **폼 검증** (vee-validate 또는 자체 컴포저블)
5. **에러 바운더리** (`src/composables/useApi.js`)
   - Circuit Breaker 응답 → SC-12 라우팅

## 📌 알려진 제약

- **모바일 미지원** — 화면 설계서의 데스크톱 영역만 구현 (`viewport: width=1280`). 향후 반응형 또는 별도 모바일 라우트로 확장 가능.
- **이미지 없음** — 모든 썸네일은 CSS 그라데이션으로 자리만 잡혀 있음. 실제 이미지(공공데이터)는 API 연동 시 채워집니다.
- **인터랙션 일부만 작동** — 라우팅, 모드 전환(로그인↔회원가입), 별점 클릭 등 핵심 인터랙션만. 폼 제출, 드래그 앤 드롭은 백엔드와 함께 구현 예정.

## 📄 라이선스

학습용 프로젝트 — 자유롭게 참고/수정 가능합니다.

---

**TripCrew** · 2026

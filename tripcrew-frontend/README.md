# TripCrew Frontend

> 함께 만드는 여행 계획 플랫폼
>
> **Vue 3 + Vite** 기반 프론트엔드 (백엔드 REST · WebSocket API 연동 완료)

🔗 **라이브 — [tripcrew.duckdns.org](https://tripcrew.duckdns.org)**

## 📋 개요

화면 설계서(`TripCrew_Screen_Spec.html`)의 **12개 데스크톱 화면**에서 출발해, 백엔드 API와 완전히 연동된 **풀스택 서비스**로 완성한 SPA입니다. AWS EC2에 배포되어 실제로 운영 중입니다. 인증(JWT · 카카오/네이버 소셜 로그인)·여행 계획 CRUD·실시간 공동 편집(WebSocket)·챗봇·후기/평점·관리자 페이지 등 화면 대부분이 실제 API로 동작하며, 초기 12개 화면은 기능이 늘며 25+ 뷰로 확장되었습니다.

| ID | 화면 | 경로 | 비고 |
|---|---|---|---|
| SC-01 | 랜딩 페이지 | `/` | 비회원 진입점, 실시간 인기 랭킹 |
| SC-02 | 회원가입/로그인 | `/auth` | 이메일 + 카카오·네이버 소셜 로그인 |
| SC-03 | 메인 대시보드 | `/home` | 회원 진입점, 추천 + 활동 피드 |
| SC-04 | AI 챗봇 | `/chat` | Gemini 기반 코스 추천 |
| SC-05 | 관광지 검색 | `/attractions` | 필터 + 별점 + 찜 |
| SC-06 | 관광지 상세 | `/attractions/:id` | 지도(NAVER)·날씨·후기 |
| SC-07 | 여행 계획 편집 | `/plans/:id/edit` | 동선 최적화 + 실시간 공동 편집 |
| SC-08 | 공동 편집 | `/plans/:id/co` | → `/edit`로 리다이렉트(편집기 일원화) |
| SC-09 | 내 여행 계획 | `/plans` | 진행/완료 분리, 받은 초대 |
| SC-10 | 후기 작성/조회 | `/attractions/:id/reviews` | 별점, 이미지 업로드 |
| SC-11 | 관리자 페이지 | `/admin` | 대시보드·회원·신고·공지·문의 |
| SC-12 | 에러/빈 상태 | `/errors/:type?` | 403/404/CB/offline |

> 이후 추가된 화면: 찜 목록(`/wishlist`)·프로필(`/profile`)·공지(`/notices`)·1:1 문의(`/support`)·FAQ·약관/개인정보·OAuth 콜백(`/oauth/callback`) 등.

## 🚀 실행 방법

> 프론트는 백엔드 API(기본 `http://localhost:8080/api`)에 붙어 동작합니다. 전체 스택을 한 번에 띄우려면 저장소 루트의 **Docker Compose**(권장)를 사용하세요 — 루트 [`README.md`](../README.md) 참고.

### 필수 환경
- **Node.js 18+** (Node 20 권장)
- npm 또는 pnpm
- 로컬 실행 시 백엔드 서버 기동 필요 (`http://localhost:8080`)

### 설치 & 실행

```bash
# 1. 의존성 설치
npm install

# 2. 개발 서버 시작 (포트 5173 — 백엔드 CORS가 5173만 허용)
npm run dev

# 3. 프로덕션 빌드
npm run build

# 4. 빌드 결과 미리보기
npm run preview
```

개발 서버는 [http://localhost:5173](http://localhost:5173)에서 열립니다. ⚠️ 백엔드 CORS가 `5173`만 허용하므로 반드시 이 포트를 사용하세요.

### 환경 변수

`.env`(gitignore)로 API base URL·외부 SDK 키를 주입합니다.

```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_NAVER_MAP_CLIENT_ID=<NAVER Cloud Platform Maps Client ID>
```

> ⚠️ Vite 환경 변수는 **빌드 타임에 번들에 박힙니다.** 값을 바꾸면 재빌드해야 반영됩니다(운영은 web 컨테이너 재빌드).

## 📁 프로젝트 구조

```
tripcrew-frontend/
├── index.html              # Vite 진입 HTML (Pretendard, JetBrains Mono)
├── vite.config.js          # @ → src 별칭, 포트 5173
├── package.json
│
└── src/
    ├── main.js             # Vue 앱 부트스트랩
    ├── App.vue             # 루트 컴포넌트 (RouterView + 라우트 전환 fade)
    │
    ├── router/
    │   └── index.js        # 라우트 정의 + 권한 가드(meta.roles → 403)
    │
    ├── api/                # axios 기반 API 레이어 (도메인별 모듈)
    │   ├── http.js         #   공통 인스턴스 + JWT 인터셉터(401 → refresh → retry)
    │   ├── auth.js  attractions.js  tripPlans.js  reviews.js
    │   ├── notices.js  reports.js  admin.js  inquiries.js
    │   ├── notifications.js  attractionLikes.js  chat.js  ...
    │
    ├── stores/             # Pinia 상태
    │   ├── auth.js         #   로그인 상태 · 토큰 · 사용자
    │   └── adminMeta.js    #   관리자 배지(미처리 신고/문의)
    │
    ├── composables/
    │   ├── usePresence.js  #   STOMP 프레즌스 + 장소 실시간 동기화
    │   ├── useReveal.js    #   v-reveal / v-stagger 스크롤 등장 모션
    │   └── useTheme.js     #   라이트/다크 테마(첫 방문=라이트)
    │
    ├── assets/styles/
    │   ├── reset.css       # CSS 리셋
    │   ├── tokens.css      # 디자인 토큰 (색상·폰트·그림자·radius, 다크 변수)
    │   └── global.css      # 전역 유틸리티 + 모션(fade/overlay/pop)
    │
    ├── components/
    │   ├── common/         # AppHeader · AppFooter · BaseButton · BasePagination · AnimatedNumber ...
    │   ├── admin/          # AdminLayout · 인라인 SVG 차트(charts/)
    │   └── ...             # ReviewImages · ReviewImagePicker 등
    │
    └── views/              # 25+ 화면 컴포넌트
        ├── LandingView.vue  AuthView.vue  DashboardView.vue
        ├── ChatbotView.vue  SearchView.vue  AttractionDetailView.vue
        ├── PlanEditView.vue  MyPlansView.vue  ReviewsView.vue
        ├── WishlistView.vue  ProfileView.vue  OAuthCallbackView.vue
        ├── NoticesView.vue  SupportView.vue  FaqView.vue  LegalView.vue
        └── AdminDashboardView.vue  AdminView.vue  AdminReportsView.vue ...
```

## 🎨 디자인 시스템

화면 설계서(SC-02 Design System)의 토큰을 그대로 적용하고, 이후 다크 모드 변수를 추가했습니다.

### 색상
```
Primary Teal  · #0F6E56  ── 메인 브랜드
Teal-3        · #0A503E  ── 어두운 강조
Accent Coral  · #D85A30  ── CTA, 핵심 액션
```

라이트/다크 두 테마를 CSS 변수로 지원합니다(`prefers-color-scheme` 초기값 + localStorage, 첫 방문 기본값=라이트). 헤더 토글로 전환.

### 타이포그래피
- **Pretendard Variable** (한글, 본문)
- **JetBrains Mono** (코드, 데이터)

### 모양
- **Radius**: 6 / 10 / 14 / 18px
- **Shadow**: 3단계 (sh-1, sh-2, sh-3) + 모달 전용

토큰은 `src/assets/styles/tokens.css`에 정의되어 있으며 CSS 변수로 모든 컴포넌트에서 사용됩니다.

### 모션
`prefers-reduced-motion`을 존중하는 가벼운 모션 언어를 씁니다(라이브러리 없음). 재사용 자산:
- `useReveal.js`의 **`v-reveal`**(뷰포트 진입 fade-up) · **`v-stagger`**(자식 순차 등장)
- `global.css`의 트랜지션 프리셋 — 모달 `overlay`, 드롭다운 `pop`, 라우트 전환 `fade`

## 🛠 기술 스택

| 영역 | 기술 |
|---|---|
| 프레임워크 | Vue 3 (Composition API · `<script setup>`) |
| 빌드 도구 | Vite 5 |
| 라우팅 | Vue Router 4 (권한 가드) |
| 상태 관리 | Pinia |
| HTTP | axios (JWT 인터셉터 · refresh 재발급) |
| 실시간 | STOMP over WebSocket (`@stomp/stompjs`) |
| 지도 | NAVER Maps SDK |
| 스타일 | Plain CSS · Scoped Styles · CSS Variables |
| 폰트 | Pretendard Variable, JetBrains Mono |

> 의도적으로 Tailwind / 컴포넌트 라이브러리를 쓰지 않았습니다. 디자인 토큰을 명확히 드러내기 위함입니다.

## 🔌 백엔드 연동

모든 화면이 백엔드 REST/WebSocket API에 연동되어 있습니다.

- **인증** — `api/http.js`의 axios 인터셉터가 요청에 JWT를 실어 보내고, `401` 응답 시 refresh 토큰으로 재발급 후 원 요청을 재시도합니다. 소셜 로그인은 `/oauth/callback`에서 일회용 코드를 JWT로 교환합니다.
- **실시간** — `usePresence.js`가 단일 STOMP 연결로 공동 편집 프레즌스 + 장소 변경을 구독합니다(`/topic/plans/{id}/...`).
- **에러** — Circuit Breaker/오프라인 등은 `/errors/:type` 단일 에러 페이지로 라우팅됩니다.
- **권한** — 라우터 가드(`meta.roles`)가 UI 레벨에서 접근을 차단하지만, 진짜 방어선은 서버 인가입니다.

## 📌 알려진 제약

- **모바일 최적화 진행 예정** — 데스크톱 기준으로 구현되어 있습니다(향후 반응형 확장 예정).
- **관광 데이터는 별도 import** — 관광지 데이터(약 5만 건)는 저장소에 포함되지 않고 DB에 별도로 적재합니다(루트 README·백엔드 README 참고).

## 📄 라이선스

학습용 프로젝트 — 자유롭게 참고/수정 가능합니다.

---

**TripCrew** · 2026

# 🧳 TripCrew - 함께 만드는 여행 계획 플랫폼

> 공공데이터 + AI 챗봇 + 동선 최적화를 결합한 협업형 여행 계획 서비스
>
> **"공공데이터로 정확하게, 챗봇으로 빠르게, 동선 최적화로 효율적으로"**

---

## 📌 프로젝트 개요

**TripCrew**는 여행 계획을 짜기 귀찮거나 어려운 사용자에게 편리하고 특색 있는 맞춤형 여행 계획을 제안하는 서비스입니다. 혼자서도, 친구와 함께서도 짤 수 있는 협업형 여행 계획 플랫폼을 지향합니다.

### 차별점

- **하이브리드 추천**: 챗봇 자연어 입력 + 직접 필터링 선택을 모두 지원
- **동선 최적화**: TSP 근사 알고리즘 기반 효율적 이동 경로 제안
- **공동 편집**: 친구와 함께 실시간으로 여행 계획 편집
- **공공데이터 기반 신뢰성**: 한국관광공사 TourAPI 활용

---

## 🛠 기술 스택

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Persistence**: MyBatis + MySQL 8
- **Cache / Pub-Sub**: Redis 7
- **Security**: Spring Security + JWT (Refresh Token Rotation)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, TimeLimiter)
- **Real-time**: WebSocket (STOMP) + Redis Pub/Sub
- **Rate Limit**: Bucket4j

### Frontend
- **Framework**: Vue 3
- **State**: Pinia

### Infra & DevOps
- **Build**: Maven
- **Container**: Docker, Docker Compose
- **Monitoring**: Spring Actuator + Prometheus + Grafana (예정)

### External APIs
- 한국관광공사 TourAPI (국문관광정보, 관광사진정보)
- OpenWeatherMap (날씨)
- 한국천문연구원 (일출/일몰)
- 한국환경공단 (전기차 충전소)
- Kakao Mobility (이동시간/동선)
- OpenAI (자연어 처리)

---

## 🎯 핵심 기능

| ID | 분류 | 기능 | 우선순위 |
|---|---|---|---|
| F01 | 회원 | 회원가입, 로그인, 정보 수정, 탈퇴 | 필수 |
| F02 | 여행 | 지역별 관광지 조회 (캐싱) | 필수 |
| F03 | 여행 | 여행 계획 CRUD | 필수 |
| F04 | 여행 | 동선 최적화 (비동기 + TSP 2-opt) | 필수 |
| F05 | 여행 | 챗봇 기반 추천 | 추가 |
| F06 | 여행 | 여행 계획 공동 편집 | 추가 |
| F07 | 여행 | 인기 관광지 실시간 랭킹 | 추가 |
| F08 | 여행 | 여행 후기 + 평점 + 이미지 업로드 | 추가 |
| F09 | 관리 | 관리자 페이지 (회원 관리, 신고 처리) | 필수 |
| F10 | 공통 | 공지사항 | 필수 |

---

## 🏗 시스템 아키텍처

```
Vue 3 Client
    ↓
Spring Security Filter (JWT, Rate Limiting, CORS)
    ↓
Controller Layer (REST + STOMP WebSocket)
    ↓
Service Layer (@Transactional, @Async, Resilience4j)
    ↓
┌────────────┬─────────────────┬──────────────────┐
│   Redis    │ MyBatis → MySQL │ External API     │
│ (Cache,    │   (영속성)       │ Adapter +        │
│  Pub/Sub,  │                 │ Circuit Breaker  │
│  Sorted    │                 │                  │
│   Set)     │                 │                  │
└────────────┴─────────────────┴──────────────────┘
                                       ↓
                          TourAPI / Weather / Kakao / OpenAI
```

---

## 💡 백엔드 핵심 시나리오

### 시나리오 A. 캐싱 전략 (Cache-Aside + Circuit Breaker)
관광지 조회 시 Redis 캐시 우선 조회 → MISS 시 외부 API 호출 → 응답 캐싱. 외부 API 장애 시 Circuit Breaker가 stale 캐시로 graceful degradation.

### 시나리오 B. 동시성 제어 (낙관적 락)
공동 편집 시 `version` 컬럼 기반 낙관적 락 적용. 충돌 발생 시 409 Conflict 응답 + WebSocket으로 최신 데이터 푸시.

### 시나리오 C. 비동기 처리 (@Async + WebSocket)
동선 최적화 요청 시 `202 Accepted` 즉시 반환 → 백엔드에서 비동기 처리 → 완료 시 WebSocket으로 결과 푸시.

---

## 📅 개발 로드맵

| 주차 | 기간 | 주요 작업 |
|---|---|---|
| **1주차** | 2026.05.15 ~ 05.21 | 프로젝트 기획, 요구사항 명세, WBS, 화면 설계 |
| **2주차** | 2026.05.22 ~ 05.28 | DB 설계 (ERD), Use Case Diagram, 기본 CRUD (F01~F03) |
| **3주차** | 2026.05.29 ~ 06.04 | 회복성 (F02 캐싱, Circuit Breaker), 동선 최적화 (F04) |
| **4주차** | 2026.06.05 ~ 06.11 | 챗봇 추천 (F05), 후기 (F08), 통합 테스트, 발표 준비 |

---

## 📂 프로젝트 문서

### 프로젝트 산출물

| 주차 | 산출물 |
|---|---|
| 1주차 | [프로젝트 기획안](https://www.notion.so/3682bace718c80b6850ee549cc57c08a) · [WBS / 간트차트](https://www.notion.so/WBS-3682bace718c80c597fefd5e8f835429) · [화면 설계서](https://www.notion.so/3682bace718c804f95d5d318cd783f92) |
| 2주차 | [ERD](예정) · [Use Case Diagram](예정) |
| 3주차 | (예정) |
| 4주차 | (예정) |

---

## 🚀 실행 방법

### 사전 요구사항
- JDK 17
- MySQL 8
- Redis 7
- Node.js 20+

### Backend
```bash
cd tripcrew-backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd tripcrew-frontend
npm install
npm run dev
```

### Docker Compose (전체 스택)
```bash
docker-compose up -d
```

---

## 👥 팀원

| 이름 | 역할 | GitLab |
|---|---|---|
| 이정현 | 조장 | [@kyn05165] |
| 고용훈 | 팀원 | [@ita010] |

---

## 📄 라이선스

이 프로젝트는 학습 목적의 비상업적 프로젝트입니다.

공공데이터는 각 기관의 이용약관을 따릅니다.
- 한국관광공사 TourAPI: 공공누리 제1유형
- 한국환경공단: 공공누리 제1유형
- 한국천문연구원: 공공누리 제1유형

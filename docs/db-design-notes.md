# TripCrew DB 설계 결정 사항

> 대상: MySQL 8 / InnoDB / utf8mb4 · Spring Boot 3.3 + JPA(Hibernate)
> 산출물: [erd.md](./erd.md) · [db/schema.sql](./db/schema.sql)

## 0. 스택 관련 메모 (불일치 주의)

README의 기술 스택에는 Persistence가 **MyBatis**, 인증이 **Refresh Token Rotation**, 토큰 저장에 **Redis** 사용으로 적혀 있다.
이번 DB 설계는 작업 지시 기준인 **JPA**(`@Version` 낙관적 락 / `@Enumerated(STRING)`)와 **refresh_tokens DB 저장(단순화)** 을 따른다.
추후 실제 구현에서 MyBatis/Redis로 가더라도 본 스키마는 그대로 쓸 수 있으나, `@Version` 자동 증가는 JPA 전제이므로 MyBatis 채택 시 version 증가 로직을 직접 작성해야 한다.

## 1. 네이밍 컨벤션 (실무 기준)

업계에 테이블 네이밍 단일 표준은 없으나(단수 vs 복수 양립), 본 프로젝트는 다음을 채택한다.

| 항목 | 규칙 | 근거 |
|------|------|------|
| 테이블명 | **snake_case 복수형** (`users`, `trip_plans` …) | 테이블 = 행의 집합 관점. Rails/Django/Laravel 등 다수 ORM 관례. `user` 예약어 충돌 자연 회피 |
| 컬럼명 | snake_case | 표준 |
| FK 컬럼 | 논리 엔티티 단수 + `_id` (`user_id`, `trip_plan_id`) | FK는 "어떤 엔티티를 가리키냐"를 표현 → 단수 유지가 관례 |
| Boolean | `is_` / `has_` 접두사 (`is_pinned`) | 의미 명확화 |
| PK | `id` (BIGINT AUTO_INCREMENT) | 단순/일관 |
| 시각 | `created_at` / `updated_at` (DATETIME, NOT NULL) | JPA Auditing으로 채움 |
| 제약/인덱스 | `pk_` / `fk_` / `uk_` / `idx_` / `chk_` prefix | 식별 용이 |
| ENUM | **MySQL ENUM 대신 VARCHAR + 주석** | ENUM은 값 추가 시 ALTER 비용·이식성 문제. `@Enumerated(STRING)` 호환 + 확장 자유 |

> 단수형 규칙에서 복수형으로 변경한 이력: 초기엔 "테이블 단수형" 규칙이었으나, `user` 예약어 리스크(특히 PostgreSQL 이식 시)와 다수 ORM 관례를 고려해 **전 테이블 복수형**으로 전환.

문자셋은 `utf8mb4`(이모지 포함 다국어). `updated_at`은 JPA `@LastModifiedDate`로 채우는 전제라 DB DEFAULT를 두지 않았다.

## 2. 테이블 개요 (9개)

| 테이블 | 기능 | 비고 |
|--------|------|------|
| `users` | F01, F09 | role(USER/ADMIN)로 관리자 권한 처리 |
| `refresh_tokens` | F01 | DB 저장 방식 (단순화) |
| `attractions` | F02, F07 | 외부 API 캐시, 자체 PK + (source, external_id) UNIQUE |
| `trip_plans` | F03, F06, F07 | version(@Version), view_count(랭킹 원천) |
| `trip_members` | F06 | 공동편집 참여자 N:M, role(OWNER/EDITOR/VIEWER) |
| `trip_places` | F04 | 동선 — order_index로 방문 순서 |
| `reviews` | F08, F07 | 폴리모픽 대상 + rating 1~5 |
| `notices` | F10 | is_pinned 상단 고정 |
| `chat_messages` | F05 | **옵션** 기능 |

**테이블을 만들지 않은 기능**
- **F07 실시간 랭킹**: 랭킹 값 자체는 Redis ZSet에서 관리. DB는 원천 데이터(`trip_plans.view_count`, `reviews`의 평점/개수)만 보관 → 별도 ranking 테이블 없음.
- **F09 관리자**: 별도 테이블 없이 `users.role = 'ADMIN'` 기반 권한 처리.

## 3. 주요 설계 결정

### 3.1 낙관적 락 (F03 / F06)
- `trip_plans.version BIGINT NOT NULL DEFAULT 0` → JPA `@Version` 매핑.
- 공동편집 시 동시에 같은 계획을 수정하면 version 충돌로 `OptimisticLockException` 발생 → 클라이언트 재시도/머지 유도.

### 3.2 공동편집 참여자 (F06)
- `trip_members`로 users ↔ trip_plans N:M.
- `role`: OWNER / EDITOR / VIEWER. 권한 분리.
- `UNIQUE(trip_plan_id, user_id)`로 동일 계획 중복 참여 방지.
- 소유자(owner)는 `trip_plans.owner_id`로도 갖고 있고, trip_members에도 OWNER 행을 둔다(권한 검사 일원화). 동기화는 앱에서 보장.

### 3.3 동선 (F04)
- `trip_places`는 `trip_plans`의 자식. `order_index`로 방문 순서 저장(동선 최적화 결과 반영).
- `visit_day`로 N일차 그룹화 가능.
- `attraction_id`는 nullable — 관광지를 참조하거나, 참조 없는 커스텀 장소도 허용.
- `name`/`latitude`/`longitude`를 자체 보관(스냅샷) → 외부 캐시(attractions)가 갱신·삭제돼도 동선이 깨지지 않음.

### 3.4 외부 데이터 캐싱 (F02)
- `attractions`은 외부 API id(`external_id`)를 그대로 PK로 쓰지 않고 **자체 `id`(BIGINT)** 를 둔다(내부 FK 안정성).
- `UNIQUE(source, external_id)`로 출처별 외부 id 중복 방지(여러 외부 소스 확장 대비).
- `cached_at`(최초 캐싱) / `synced_at`(마지막 갱신)으로 캐시 신선도 판단·재갱신 트리거.

### 3.5 후기/평점 — 폴리모픽 (F08)
- `reviews`는 `target_type`(ATTRACTION/TRIP_PLAN) + `target_id`로 두 종류 대상을 한 테이블에서 처리.
- **트레이드오프**: 대상 테이블이 둘이라 `target_id`에 DB FK 제약을 걸 수 없다.
  - → 대상 존재 검증, 대상 삭제 시 리뷰 정리(orphan 방지)는 **애플리케이션 레이어 책임**.
  - 대안(채택 안 함): `attraction_id`/`trip_plan_id` 두 nullable FK + CHECK(정확히 하나) → FK 제약은 살지만 대상 종류가 늘 때마다 컬럼 추가 필요. 단순성을 위해 폴리모픽 선택.
- `rating`은 `TINYINT` + `CHECK (rating BETWEEN 1 AND 5)` (MySQL 8은 CHECK 강제).

### 3.6 비밀번호 컬럼 길이
- `password VARCHAR(255)`. BCrypt는 60자지만 길이를 못박지 않고 Argon2 등 해시 알고리즘 교체에 대비(실무 관례).

### 3.7 관리자/공지 (F09 / F10)
- 관리자 권한은 `users.role`로 처리(별도 테이블 X).
- `notices.author_id`는 nullable + ON DELETE SET NULL → 작성 관리자가 탈퇴해도 공지 본문 유지.
- `is_pinned`로 상단 고정, `idx_notices_pinned_created`로 고정+최신순 목록 조회.

## 4. ON DELETE 정책 요약

| FK | 정책 | 근거 |
|----|------|------|
| `refresh_tokens.user_id → users` | CASCADE | 회원 삭제 시 토큰 즉시 폐기 |
| `trip_plans.owner_id → users` | **RESTRICT** | 참여자가 있는 계획이 소유자 탈퇴로 소실되면 안 됨. 앱에서 소유권 이전/소프트삭제 처리 후 삭제 |
| `trip_members.trip_plan_id → trip_plans` | CASCADE | 계획 삭제 시 참여 매핑 정리 |
| `trip_members.user_id → users` | CASCADE | 회원 삭제 시 참여 매핑만 정리(계획 본체는 유지) |
| `trip_places.trip_plan_id → trip_plans` | CASCADE | 계획 삭제 시 동선 전체 삭제(자식) |
| `trip_places.attraction_id → attractions` | SET NULL | 캐시 관광지 삭제돼도 동선 항목은 스냅샷으로 유지 |
| `reviews.user_id → users` | CASCADE | 작성자 삭제 시 후기 제거 |
| `reviews.target_id` | (FK 없음) | 폴리모픽 — 앱레벨 정합성 관리 |
| `notices.author_id → users` | SET NULL | 작성 관리자 탈퇴해도 공지 유지 |
| `chat_messages.user_id → users` | CASCADE | 회원 삭제 시 대화 로그 제거 |
| `chat_messages.trip_plan_id → trip_plans` | SET NULL | 계획 삭제돼도 로그 보존 |

## 5. 인덱스 요약 (조회 패턴 기준)

| 테이블 | 인덱스 | 목적 |
|--------|--------|------|
| `users` | UNIQUE(email) | 로그인/중복가입 체크 |
| `refresh_tokens` | UNIQUE(token), INDEX(user_id), INDEX(expires_at) | 토큰 검증 / 만료 청소 |
| `attractions` | UNIQUE(source, external_id), INDEX(area_code) | 외부 id 멱등 upsert / 지역별 조회 |
| `trip_plans` | INDEX(owner_id), INDEX(view_count) | 사용자별 계획 / 랭킹 정렬 원천 |
| `trip_members` | UNIQUE(trip_plan_id, user_id), INDEX(user_id) | 중복참여 방지 / 내 참여 계획 |
| `trip_places` | INDEX(trip_plan_id, order_index) | 계획별 동선 순서 조회 |
| `reviews` | INDEX(target_type, target_id), INDEX(user_id) | 대상별 평점 집계 / 내 후기 |
| `notices` | INDEX(is_pinned, created_at) | 고정+최신순 목록 |
| `chat_messages` | INDEX(user_id), INDEX(trip_plan_id) | 대화 이력 조회 |

## 6. 후속 작업 (TODO)

- Spring Boot 셋업 후 `schema.sql`을 `src/main/resources/db/migration/V1__init.sql`(Flyway)로 이동.
- JPA Auditing(`@CreatedDate`/`@LastModifiedDate`) 설정 — `created_at`/`updated_at` 자동 관리.
- `reviews` 폴리모픽 대상 존재 검증 + 대상 삭제 시 후기 정리 로직(앱레벨).
- (실무 확장 고려) 소프트 삭제(`deleted_at`) 도입 검토 — RESTRICT 정책과 잘 맞음. 현재는 미적용.
- (시간 부족 시) `trip_plans.view_count` 제거 검토 — 랭킹 원천을 reviews 집계만으로 단순화 가능.
- (옵션) `chat_messages`는 F05 미구현 시 생성 보류 가능.

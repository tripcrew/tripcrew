# TripCrew ERD

> Spring Boot 3.3 + MySQL 8 + MyBatis 기준 데이터 모델.
> 표기는 Mermaid `erDiagram`. 자세한 설계 근거는 [db-design-notes.md](./db-design-notes.md), DDL은 [db/schema.sql](./db/schema.sql) 참고.

## 다이어그램

```mermaid
erDiagram
    users ||--o{ refresh_tokens : "has"
    users ||--o{ trip_plans : "owns (owner_id, RESTRICT)"
    users ||--o{ trip_members : "participates"
    trip_plans ||--o{ trip_members : "has members"
    trip_plans ||--o{ trip_places : "has places"
    attractions |o--o{ trip_places : "referenced by (nullable)"
    users ||--o{ reviews : "writes"
    users ||--o{ notices : "authors (nullable)"
    users ||--o{ chat_messages : "sends (optional feature)"
    trip_plans |o--o{ chat_messages : "context (optional feature)"

    users {
        bigint id PK
        varchar email UK "UNIQUE"
        varchar password "해시 비밀번호"
        varchar nickname
        varchar role "USER | ADMIN"
        datetime created_at
        datetime updated_at
    }

    refresh_tokens {
        bigint id PK
        bigint user_id FK "ON DELETE CASCADE"
        varchar token UK "UNIQUE"
        datetime expires_at
        datetime created_at
        datetime updated_at
    }

    attractions {
        bigint id PK "자체 PK"
        varchar source "외부 출처 e.g. TOUR_API"
        varchar external_id "외부 API id"
        varchar name
        varchar category
        varchar address
        varchar area_code
        decimal latitude
        decimal longitude
        varchar image_url
        datetime cached_at "최초 캐싱 시각"
        datetime synced_at "마지막 갱신 시각"
        datetime created_at
        datetime updated_at
    }

    trip_plans {
        bigint id PK
        bigint owner_id FK "ON DELETE RESTRICT"
        varchar title
        text description
        date start_date
        date end_date
        bigint view_count "랭킹(F07) 원천"
        bigint version "낙관적 락 (UPDATE 시 수동 증가)"
        datetime created_at
        datetime updated_at
    }

    trip_members {
        bigint id PK
        bigint trip_plan_id FK "ON DELETE CASCADE"
        bigint user_id FK "ON DELETE CASCADE"
        varchar role "OWNER | EDITOR | VIEWER"
        datetime created_at
        datetime updated_at
    }

    trip_places {
        bigint id PK
        bigint trip_plan_id FK "ON DELETE CASCADE"
        bigint attraction_id FK "nullable, ON DELETE SET NULL"
        varchar name "스냅샷/커스텀 장소명"
        decimal latitude
        decimal longitude
        int visit_day "일자 그룹(nullable)"
        int order_index "방문 순서 (F04 동선)"
        varchar memo
        datetime created_at
        datetime updated_at
    }

    reviews {
        bigint id PK
        bigint user_id FK "ON DELETE CASCADE"
        varchar target_type "ATTRACTION | TRIP_PLAN (폴리모픽)"
        bigint target_id "FK 제약 없음 - 앱레벨 검증"
        tinyint rating "1~5 CHECK"
        text content
        datetime created_at
        datetime updated_at
    }

    notices {
        bigint id PK
        bigint author_id FK "nullable, ON DELETE SET NULL"
        varchar title
        text content
        boolean is_pinned
        datetime created_at
        datetime updated_at
    }

    chat_messages {
        bigint id PK "옵션 기능 (F05)"
        bigint user_id FK "ON DELETE CASCADE"
        bigint trip_plan_id FK "nullable, ON DELETE SET NULL"
        varchar role "USER | ASSISTANT"
        text content
        datetime created_at
        datetime updated_at
    }
```

## 폴리모픽 관계 주의

`reviews`는 `target_type` + `target_id`로 `attractions` 또는 `trip_plans`을 가리키는 폴리모픽 구조다.
대상이 둘이라 DB FK 제약을 걸 수 없으므로 ERD 상에서도 연결선을 그리지 않았다.
유효성/삭제 정합성은 애플리케이션 레이어에서 보장한다. (상세: [db-design-notes.md](./db-design-notes.md))

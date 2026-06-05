-- =============================================================
-- TripCrew Database Schema
-- Target: MySQL 8 / InnoDB / utf8mb4
-- Stack : Spring Boot 3.2 + MyBatis + REST API
--
-- Flyway 초기 마이그레이션 (V1). 설계 원본: docs/db/schema.sql, docs/erd.md
-- 한번 적용된 마이그레이션은 수정 금지 — 스키마 변경은 V2__*.sql 로 누적한다.
--
-- 네이밍 컨벤션 (실무 기준):
--   - 테이블: snake_case 복수형 (테이블 = 행의 집합). 'user' 예약어 충돌도 회피.
--   - 컬럼  : snake_case. FK 컬럼은 논리 엔티티 기준 단수 + _id (user_id 등).
--   - Boolean: is_ / has_ 접두사.
--   - 제약/인덱스: pk_/fk_/uk_/idx_/chk_ prefix.
--   - 모든 테이블: id BIGINT AUTO_INCREMENT PK, created_at / updated_at (DATETIME, NOT NULL).
--   - ENUM 은 MySQL ENUM 대신 VARCHAR + 주석 (MyBatis EnumTypeHandler 호환 / 확장성).
--   - 생성 순서는 FK 의존성 순서를 따름.
--
-- MyBatis 관련:
--   - JPA Auditing 이 없으므로 created_at/updated_at 은 DB DEFAULT 로 채운다
--     (DEFAULT CURRENT_TIMESTAMP / ON UPDATE CURRENT_TIMESTAMP).
--   - 낙관적 락(version)은 자동 증가가 없으므로 UPDATE 문에서 수동 처리한다:
--     UPDATE ... SET ..., version = version + 1 WHERE id = ? AND version = ?  -> affected rows 0 이면 충돌.
-- =============================================================

-- -------------------------------------------------------------
-- 1. users  (F01 인증 / F09 관리자 role)
-- -------------------------------------------------------------
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL COMMENT '해시 비밀번호 (BCrypt/Argon2 등, 알고리즘 교체 대비 넉넉히)',
    nickname    VARCHAR(50)  NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER | ADMIN',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원';

-- -------------------------------------------------------------
-- 2. refresh_tokens  (F01 - DB 저장 방식, 단순화)
-- -------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  DATETIME     NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    KEY idx_refresh_tokens_user (user_id),
    KEY idx_refresh_tokens_expires (expires_at) COMMENT '만료 토큰 청소용',
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='리프레시 토큰';

-- -------------------------------------------------------------
-- 3. attractions  (F02 관광지 - 외부 API 캐시 / F07 랭킹 원천)
-- -------------------------------------------------------------
CREATE TABLE attractions (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '자체 PK (외부 id와 별도)',
    source      VARCHAR(30)  NOT NULL COMMENT '외부 출처 e.g. TOUR_API',
    external_id VARCHAR(64)  NOT NULL COMMENT '외부 API의 id (예: TourAPI contentId)',
    name        VARCHAR(255) NOT NULL,
    category    VARCHAR(50)  NULL,
    address     VARCHAR(255) NULL,
    area_code   VARCHAR(20)  NULL COMMENT '지역 코드 (지역별 조회용)',
    latitude    DECIMAL(10,7) NULL,
    longitude   DECIMAL(10,7) NULL,
    image_url   VARCHAR(500) NULL,
    cached_at   DATETIME     NOT NULL COMMENT '최초 캐싱 시각',
    synced_at   DATETIME     NOT NULL COMMENT '마지막 갱신 시각',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attractions_source_external (source, external_id),
    KEY idx_attractions_area (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관광지 (외부 API 캐시)';

-- -------------------------------------------------------------
-- 4. trip_plans  (F03 여행계획 / F06 공동편집 version / F07 랭킹 원천)
-- -------------------------------------------------------------
CREATE TABLE trip_plans (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    owner_id    BIGINT       NOT NULL,
    title       VARCHAR(150) NOT NULL,
    description TEXT         NULL,
    start_date  DATE         NULL,
    end_date    DATE         NULL,
    view_count  BIGINT       NOT NULL DEFAULT 0 COMMENT 'F07 랭킹 집계 원천',
    version     BIGINT       NOT NULL DEFAULT 0 COMMENT '낙관적 락 (MyBatis: UPDATE 시 version=version+1 + WHERE version=? 검사)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trip_plans_owner (owner_id) COMMENT '사용자별 계획 조회',
    KEY idx_trip_plans_view_count (view_count) COMMENT '랭킹 정렬 원천',
    CONSTRAINT fk_trip_plans_owner FOREIGN KEY (owner_id)
        REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 계획';

-- -------------------------------------------------------------
-- 5. trip_members  (F06 공동편집 참여자 N:M)
-- -------------------------------------------------------------
CREATE TABLE trip_members (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    trip_plan_id BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    role         VARCHAR(20) NOT NULL COMMENT 'OWNER | EDITOR | VIEWER',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_trip_members (trip_plan_id, user_id) COMMENT '한 계획에 동일 유저 1행',
    KEY idx_trip_members_user (user_id) COMMENT '내가 참여중인 계획 조회',
    CONSTRAINT fk_trip_members_plan FOREIGN KEY (trip_plan_id)
        REFERENCES trip_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_members_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 계획 참여자';

-- -------------------------------------------------------------
-- 6. trip_places  (F04 동선 - 계획 내 장소 + 방문 순서)
-- -------------------------------------------------------------
CREATE TABLE trip_places (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    trip_plan_id  BIGINT       NOT NULL,
    attraction_id BIGINT       NULL COMMENT '관광지 참조 (NULL=커스텀 장소)',
    name          VARCHAR(255) NOT NULL COMMENT '장소명 스냅샷 (attraction 삭제돼도 유지)',
    latitude      DECIMAL(10,7) NULL,
    longitude     DECIMAL(10,7) NULL,
    visit_day     INT          NULL COMMENT '여행 N일차 그룹',
    order_index   INT          NOT NULL COMMENT '방문 순서 (동선 최적화 결과)',
    memo          VARCHAR(255) NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_trip_places_plan_order (trip_plan_id, order_index) COMMENT '계획별 동선 순서 조회',
    KEY idx_trip_places_attraction (attraction_id),
    CONSTRAINT fk_trip_places_plan FOREIGN KEY (trip_plan_id)
        REFERENCES trip_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_places_attraction FOREIGN KEY (attraction_id)
        REFERENCES attractions (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='여행 계획 내 장소(동선)';

-- -------------------------------------------------------------
-- 7. reviews  (F08 후기/평점 - 폴리모픽 대상 / F07 평점 집계 원천)
--    target_type + target_id 로 attractions 또는 trip_plans 를 가리킨다.
--    가리키는 테이블이 둘이라 DB FK 제약 불가 -> 앱레벨 검증.
-- -------------------------------------------------------------
CREATE TABLE reviews (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'ATTRACTION | TRIP_PLAN',
    target_id   BIGINT      NOT NULL COMMENT '대상 PK (FK 제약 없음, 앱레벨 검증)',
    rating      TINYINT     NOT NULL COMMENT '별점 1~5',
    content     TEXT        NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_reviews_target (target_type, target_id) COMMENT '대상별 평점 집계',
    KEY idx_reviews_user (user_id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='후기/평점';

-- -------------------------------------------------------------
-- 8. notices  (F10 공지)
-- -------------------------------------------------------------
CREATE TABLE notices (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    author_id   BIGINT       NULL COMMENT '작성 관리자 (탈퇴 시 NULL)',
    title       VARCHAR(200) NOT NULL,
    content     TEXT         NOT NULL,
    is_pinned   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '상단 고정',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notices_pinned_created (is_pinned, created_at) COMMENT '고정+최신순 목록',
    CONSTRAINT fk_notices_author FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='공지사항';

-- -------------------------------------------------------------
-- 9. chat_messages  (F05 챗봇 - [옵션] 대화 로그)
-- -------------------------------------------------------------
CREATE TABLE chat_messages (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    trip_plan_id BIGINT      NULL COMMENT '관련 계획 (없을 수 있음)',
    role         VARCHAR(20) NOT NULL COMMENT 'USER | ASSISTANT',
    content      TEXT        NOT NULL,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_chat_messages_user (user_id),
    KEY idx_chat_messages_plan (trip_plan_id),
    CONSTRAINT fk_chat_messages_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_plan FOREIGN KEY (trip_plan_id)
        REFERENCES trip_plans (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='챗봇 대화 로그 (옵션 기능)';

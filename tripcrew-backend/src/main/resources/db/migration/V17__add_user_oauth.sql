-- =============================================================
-- OAuth 소셜 로그인(Kakao/Naver) 토대: users 에 provider 식별 추가
--   기존 가입은 전부 이메일+비밀번호(LOCAL). 여기에 소셜 로그인을 더한다.
--   - provider     : 가입/로그인 경로. LOCAL | KAKAO | NAVER (기존 행은 전부 LOCAL).
--   - provider_id  : 소셜 제공자 고유 식별자(Kakao 의 id, Naver 의 response.id 등).
--                    LOCAL 계정은 NULL.
--   - password 를 NULL 허용으로 완화: 소셜 전용 계정은 비밀번호가 없다.
--     (LOCAL 계정은 여전히 회원가입 시 NOT NULL 을 앱레벨에서 보장)
--
--   ENUM 컬럼은 프로젝트 규칙상 MySQL ENUM 금지 → VARCHAR + 주석 + MyBatis EnumTypeHandler.
--   같은 제공자 내에서 provider_id 는 유일 → (provider, provider_id) 복합 유니크.
--     (LOCAL 은 provider_id 가 NULL 이고, MySQL 유니크는 NULL 을 중복 허용하므로
--      LOCAL 계정 다건이 충돌하지 않는다.)
-- =============================================================
ALTER TABLE users
    ADD COLUMN provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL'
        COMMENT 'LOCAL | KAKAO | NAVER (가입/로그인 경로)' AFTER role,
    ADD COLUMN provider_id VARCHAR(255) NULL
        COMMENT '소셜 제공자 고유 식별자. LOCAL 은 NULL' AFTER provider,
    MODIFY COLUMN password  VARCHAR(255) NULL
        COMMENT '해시 비밀번호(BCrypt). 소셜 전용 계정은 NULL';

ALTER TABLE users
    ADD UNIQUE KEY uk_users_provider (provider, provider_id);

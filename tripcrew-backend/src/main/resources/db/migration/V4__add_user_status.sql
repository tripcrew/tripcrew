-- -------------------------------------------------------------
-- F09 확장: 사용자 제재(밴) - users.status 추가
-- ENUM 금지 규칙대로 VARCHAR + 주석 (MyBatis EnumTypeHandler 가 enum name <-> VARCHAR 매핑)
-- 기존 행은 DEFAULT 'ACTIVE' 로 채워진다.
-- -------------------------------------------------------------
ALTER TABLE users
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE | BANNED' AFTER role;

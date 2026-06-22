-- F09 관리자 - 최고 책임자(SUPER_ADMIN) 역할 도입.
--
-- role 은 VARCHAR + MyBatis EnumTypeHandler 매핑이라 스키마 변경은 없고,
-- 허용 값 문서(COMMENT)만 갱신한다. 위계는 USER < ADMIN < SUPER_ADMIN.
--
-- ⭐ SUPER_ADMIN 은 API 로 절대 부여할 수 없다(엔드포인트는 USER↔ADMIN 토글만).
--    최고 책임자 지정/복구는 아래처럼 DB 직접 수정으로만 한다(의도된 break-glass):
--      UPDATE users SET role = 'SUPER_ADMIN' WHERE email = '<owner-email>';
ALTER TABLE users
    MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'
    COMMENT 'USER | ADMIN | SUPER_ADMIN (SUPER_ADMIN 은 DB 직접 지정만, API 부여 불가)';

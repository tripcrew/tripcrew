-- =============================================================
-- F09 Phase 2 후속: 신고 처리완료 시 후기 soft-delete(숨김)
--   관리자가 후기(REVIEW) 신고를 '처리완료(RESOLVED)'하면 해당 후기를 하드삭제하지 않고
--   status=HIDDEN 으로 숨긴다. 공개 목록 조회에서는 제외되지만 row 는 보존되어
--   (1) 증거 보존(신고 목록의 후기 원문 표시 유지)
--   (2) 폴리모픽 dangling 방지(reports → reviews 조인이 그대로 동작)
--   가 가능하다.
--
--   ENUM 컬럼은 프로젝트 규칙상 MySQL ENUM 금지 → VARCHAR + 주석 + MyBatis EnumTypeHandler.
--     status : VISIBLE | HIDDEN   (DB DEFAULT 'VISIBLE')
-- =============================================================
ALTER TABLE reviews
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE'
    COMMENT 'VISIBLE | HIDDEN. 신고 처리완료로 숨겨진 후기는 HIDDEN(공개 목록 제외, row 보존).';

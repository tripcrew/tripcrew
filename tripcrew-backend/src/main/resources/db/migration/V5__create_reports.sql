-- =============================================================
-- F09 Phase 2: 신고(reports)
--   유저가 부적절한 후기(REVIEW)나 사용자(USER)를 신고하면 관리자가 검토 후 제재한다.
--   대상은 폴리모픽(target_type + target_id) — 가리키는 테이블이 둘이라 DB FK 제약 불가,
--   대상 존재 검증은 앱레벨(ReportService)에서 한다. (reviews 폴리모픽 규칙과 동일)
--
--   ENUM 컬럼은 프로젝트 규칙상 MySQL ENUM 금지 → VARCHAR + 주석 + MyBatis EnumTypeHandler.
--     target_type : REVIEW | USER
--     reason      : SPAM | ABUSE | ADVERTISING | INAPPROPRIATE | OTHER
--     status      : OPEN | RESOLVED | DISMISSED   (DB DEFAULT 'OPEN')
-- =============================================================
CREATE TABLE reports (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT       NOT NULL COMMENT '신고한 사용자',
    target_type VARCHAR(20)  NOT NULL COMMENT 'REVIEW | USER',
    target_id   BIGINT       NOT NULL COMMENT '대상 PK (FK 제약 없음, 앱레벨 검증)',
    reason      VARCHAR(20)  NOT NULL COMMENT 'SPAM | ABUSE | ADVERTISING | INAPPROPRIATE | OTHER',
    detail      VARCHAR(500) NULL     COMMENT '상세 사유(선택)',
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | RESOLVED | DISMISSED',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    -- 같은 사람이 같은 대상을 중복 신고하지 못하게 막는다.
    UNIQUE KEY uq_reports_reporter_target (reporter_id, target_type, target_id),
    KEY idx_reports_status (status) COMMENT '관리자 미처리(OPEN) 목록 조회',
    KEY idx_reports_target (target_type, target_id) COMMENT '대상별 신고 집계',
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='신고';

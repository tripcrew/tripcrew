-- =============================================================
-- 로드맵 ③ 1:1 문의(Q&A): inquiries
--   사용자가 1:1 문의를 작성하면 관리자가 검토 후 답변한다(양방향).
--   공지(관리자→사용자 일방향 안내)와 성격이 달라 별도 테이블/섹션으로 분리한다.
--
--   ENUM 컬럼은 프로젝트 규칙상 MySQL ENUM 금지 → VARCHAR + 주석 + MyBatis EnumTypeHandler.
--     status : OPEN | ANSWERED   (DB DEFAULT 'OPEN')
--   답변 시 status=ANSWERED 로 전환하고 answer/answered_by/answered_at 을 채운다.
--   작성자 탈퇴 시 문의도 함께 정리(ON DELETE CASCADE). 답변 관리자(answered_by)는
--   FK 를 두지 않는다(관리자 계정이 사라져도 답변 이력은 보존 — 감사 추적).
-- =============================================================
CREATE TABLE inquiries (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '문의 작성자(users.id)',
    title       VARCHAR(150) NOT NULL COMMENT '문의 제목',
    content     TEXT         NOT NULL COMMENT '문의 내용',
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | ANSWERED',
    answer      TEXT         NULL     COMMENT '관리자 답변(미답변이면 NULL)',
    answered_by BIGINT       NULL     COMMENT '답변한 관리자(users.id). FK 없음 — 관리자 삭제돼도 이력 보존',
    answered_at DATETIME     NULL     COMMENT '답변 시각(미답변이면 NULL)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_inquiries_user (user_id, created_at) COMMENT '내 문의 목록 조회(최신순)',
    KEY idx_inquiries_status (status) COMMENT '관리자 미답변(OPEN) 목록/집계 조회',
    CONSTRAINT fk_inquiries_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1:1 문의(Q&A)';

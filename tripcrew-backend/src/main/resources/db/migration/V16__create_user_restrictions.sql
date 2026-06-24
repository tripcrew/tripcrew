-- =============================================================
-- 로드맵 ④ 신고 단계별 제재(graduated sanctions): user_restrictions
--   기존 제재는 "신고 누적 3회 → 즉시 영구 BANNED" 이분법(users.status)뿐이었다.
--   이를 단계화한다 — 누적 횟수에 따라 능력제재(후기/계획 작성 금지)→계정 임시정지로
--   점진 강화하고, 영구정지(최고 임계)는 관리자가 수동 확정한다(오신고 누적 자동 영구밴 방지).
--
--   능력제재(REVIEW_WRITE/PLAN_CREATE)는 해당 POST 엔드포인트에서만 막고(브라우징은 허용),
--   계정 임시정지(ACCOUNT_SUSPEND)는 login/reissue 에서 막는다(인증 필터는 무변경 —
--   기발급 access token 은 만료 ≤30분 후 무효, 기존 BANNED 와 같은 트레이드오프).
--   영구정지는 기존 users.status='BANNED' 를 그대로 재사용하므로 status enum 은 건드리지 않는다.
--
--   ENUM 컬럼은 프로젝트 규칙상 MySQL ENUM 금지 → VARCHAR + 주석 + MyBatis EnumTypeHandler.
--     type : REVIEW_WRITE | PLAN_CREATE | ACCOUNT_SUSPEND
--   until 은 제재 만료 시각(NULL=영구). 현재 단계화는 모두 기한제(7일)지만,
--   수동 영구 능력제재 등 확장을 위해 NULL 을 허용해 둔다. 만료 행은 삭제하지 않고
--   "until 이 지났으면 무시"로 판단한다(이력/감사 추적 보존).
--   대상 사용자 탈퇴 시 제재도 함께 정리(ON DELETE CASCADE).
-- =============================================================
CREATE TABLE user_restrictions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '제재 대상(users.id)',
    type        VARCHAR(30)  NOT NULL COMMENT 'REVIEW_WRITE | PLAN_CREATE | ACCOUNT_SUSPEND',
    until       DATETIME     NULL     COMMENT '제재 만료 시각. NULL=영구. 지난 행은 비활성으로 간주',
    reason      VARCHAR(255) NULL     COMMENT '제재 사유(예: 신고 누적 10회 자동 단계 제재)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_restrictions_active (user_id, type, until)
        COMMENT '대상+종류별 활성 제재 조회(until IS NULL OR until > NOW())',
    CONSTRAINT fk_user_restrictions_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 단계별 제재(신고 누적 graduated sanctions)';

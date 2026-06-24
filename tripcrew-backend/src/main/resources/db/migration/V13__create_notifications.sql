-- 회원 알림(범용). 한 행 = 한 수신자에게 보낸 알림 한 건.
-- 신고 처리완료/리뷰 격려/초대 등 여러 트리거가 공용으로 적재한다(type 으로 구분).
-- type 은 MySQL ENUM 금지 규칙에 따라 VARCHAR + 주석 + MyBatis EnumTypeHandler(name 저장).
-- ref_id 는 type 에 따라 의미가 다른 연관 엔티티 식별자(없을 수 있어 NULL 허용).
-- 수신자 삭제 시 알림도 함께 정리(ON DELETE CASCADE).
CREATE TABLE notifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL COMMENT '수신자(users.id)',
    type       VARCHAR(40)  NOT NULL COMMENT '알림 유형: REPORT_RESOLVED|REVIEW_NUDGE|INVITE 등(name 저장)',
    ref_id     BIGINT       NULL     COMMENT '연관 엔티티 id(type 별 의미 상이: 신고 id/계획 id 등). 없으면 NULL',
    message    VARCHAR(255) NOT NULL COMMENT '표시 문구(서버 생성)',
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notifications_user (user_id, is_read, created_at) COMMENT '수신자별 미읽음 카운트/최신 목록 조회',
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 알림(범용)';

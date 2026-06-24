-- F06 공동편집 초대 수락/거절(P4). trip_members 에 초대 상태를 추가한다.
-- 초대 시 PENDING 으로 추가 → 피초대자가 수락하면 ACCEPTED, 거절하면 행 삭제.
-- 수락 전(PENDING)에는 권한 판정·공유 목록에서 제외돼 계획에 접근할 수 없다.
-- MySQL ENUM 금지 규칙에 따라 VARCHAR + 주석 + MyBatis EnumTypeHandler(name 저장).
ALTER TABLE trip_members
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT '초대 상태: PENDING(수락 대기) | ACCEPTED(수락됨)'
        AFTER role;

-- 기존 멤버는 이미 함께 편집 중이던 협업자이므로 모두 수락 상태로 백필한다.
UPDATE trip_members SET status = 'ACCEPTED';

-- =============================================================
-- F09 Phase 2.1: 신고 누적 자동 제재
--   관리자가 신고를 '처리완료(RESOLVED)'할 때마다 피신고 유저의 report_count 를 +1 하고,
--   3회 이상 누적되면 자동으로 제재(status=BANNED)한다. (자동 제재는 일반 USER 한정)
--   '기각(DISMISSED)'은 카운트를 늘리지 않는다.
-- =============================================================
ALTER TABLE users
    ADD COLUMN report_count INT NOT NULL DEFAULT 0
    COMMENT '관리자가 처리완료한 신고 누적 횟수. 3회 이상이면 자동 제재(BANNED).';

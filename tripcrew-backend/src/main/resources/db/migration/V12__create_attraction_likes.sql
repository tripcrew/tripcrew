-- 관광지 좋아요(찜). 한 사용자가 한 관광지에 한 번만(유니크). 토글로 추가/취소.
-- 좋아요 수는 COUNT 로 집계(상세 페이지 단건 조회 — 목록 카드에 붙일 땐 추후 비정규화/배치조회 협의).
-- 사용자·관광지 삭제 시 함께 정리(ON DELETE CASCADE).
CREATE TABLE attraction_likes (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    attraction_no INT      NOT NULL COMMENT '관광지(attractions.no)',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_attraction_likes (user_id, attraction_no) COMMENT '중복 좋아요 방지',
    KEY idx_attraction_likes_attraction (attraction_no) COMMENT '관광지별 좋아요 수 집계',
    CONSTRAINT fk_attraction_likes_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_attraction_likes_attraction FOREIGN KEY (attraction_no)
        REFERENCES attractions (no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='관광지 좋아요';

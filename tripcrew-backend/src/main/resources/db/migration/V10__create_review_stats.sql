-- =============================================================
-- F08 후기/평점 본작업: 평점 비정규화 집계 테이블(B안)
--   후기 목록/대상 화면에서 평균·개수를 매번 GROUP BY 로 집계하지 않도록
--   대상(폴리모픽)별 집계를 별도 테이블에 비정규화해 둔다.
--   create/update/delete/hide 트랜잭션 안에서 증분 갱신하며 VISIBLE 후기만 집계한다.
--
--   - PK 는 폴리모픽 대상 (target_type, target_id) 복합키. reviews 와 동일 규칙(FK 없음, 앱레벨 일관성).
--   - review_count : 집계된(VISIBLE) 후기 수
--   - rating_sum   : 집계된 후기 평점 합(평균 재계산 기준값)
--   - avg_rating   : rating_sum / review_count (count=0 이면 0). DECIMAL(3,2): 0.00 ~ 5.00
--   - ENUM 컬럼은 프로젝트 규칙상 VARCHAR + 주석(MyBatis EnumTypeHandler).
-- =============================================================
CREATE TABLE review_stats (
    target_type  VARCHAR(20)   NOT NULL COMMENT 'ATTRACTION | TRIP_PLAN (reviews.target_type 과 동일)',
    target_id    BIGINT        NOT NULL COMMENT '대상 PK(종류별 테이블, FK 없음)',
    review_count INT           NOT NULL DEFAULT 0 COMMENT '집계된(VISIBLE) 후기 수',
    rating_sum   BIGINT        NOT NULL DEFAULT 0 COMMENT '집계된 후기 평점 합(평균 재계산 기준)',
    avg_rating   DECIMAL(3, 2) NOT NULL DEFAULT 0.00 COMMENT 'rating_sum / review_count, count=0 이면 0.00',
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'F08 후기 평점 비정규화 집계(대상별)';

-- 기존 VISIBLE 후기로 초기 집계값 백필(이미 쌓인 후기 반영).
INSERT INTO review_stats (target_type, target_id, review_count, rating_sum, avg_rating)
SELECT target_type,
       target_id,
       COUNT(*),
       SUM(rating),
       SUM(rating) / COUNT(*)
  FROM reviews
 WHERE status = 'VISIBLE'
 GROUP BY target_type, target_id;

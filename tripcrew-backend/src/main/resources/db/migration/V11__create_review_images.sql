-- F08 후기 이미지 업로드.
-- 후기 1건에 여러 이미지(첨부 순서 보존). reviews 하드삭제 시 함께 삭제(ON DELETE CASCADE).
-- 이미지 경로는 로컬 파일시스템에 저장된 정적 리소스의 상대 URL(예: /uploads/reviews/{uuid}.jpg)을 보관한다.
CREATE TABLE review_images (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    review_id  BIGINT       NOT NULL,
    image_url  VARCHAR(512) NOT NULL COMMENT '정적 리소스 상대 URL (/uploads/reviews/...)',
    sort_order INT          NOT NULL DEFAULT 0 COMMENT '첨부 순서(0부터)',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_review_images_review (review_id, sort_order) COMMENT '후기별 이미지 순서 조회',
    CONSTRAINT fk_review_images_review FOREIGN KEY (review_id)
        REFERENCES reviews (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='후기 첨부 이미지';

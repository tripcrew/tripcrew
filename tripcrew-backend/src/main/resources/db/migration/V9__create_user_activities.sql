CREATE TABLE user_activities (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    activity_type   VARCHAR(30)  NOT NULL COMMENT 'PLAN_CREATED | PLACE_ADDED | PLACE_SCHEDULED | ROUTE_OPTIMIZED',
    trip_plan_id    BIGINT       NULL,
    trip_plan_title VARCHAR(150) NULL,
    place_name      VARCHAR(255) NULL,
    visit_day       INT          NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_activities_user_created (user_id, created_at DESC),
    CONSTRAINT fk_user_activities_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_activities_plan
        FOREIGN KEY (trip_plan_id) REFERENCES trip_plans (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='홈 최근 활동 로그';

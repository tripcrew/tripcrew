-- attractions -> guguns FK should include sido_code because gugun_code is only unique within a sido.

ALTER TABLE guguns
    ADD UNIQUE KEY uk_guguns_sido_gugun (sido_code, gugun_code);

ALTER TABLE attractions
    DROP FOREIGN KEY fk_attractions_sigungu;

ALTER TABLE attractions
    DROP INDEX idx_attractions_sigungu_code,
    ADD KEY idx_attractions_area_sigungu (area_code, si_gun_gu_code);

ALTER TABLE attractions
    ADD CONSTRAINT fk_attractions_sigungu
        FOREIGN KEY (area_code, si_gun_gu_code)
        REFERENCES guguns (sido_code, gugun_code);

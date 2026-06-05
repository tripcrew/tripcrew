package com.tripcrew;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TripCrew 백엔드 진입점.
 *
 * <p>스택: Spring Boot 3.2 / MyBatis / MySQL 8 / REST API.
 * 기능별(feature) 패키지 구성: auth, user, attraction, tripplan, review, notice, chat.
 */
@SpringBootApplication
@MapperScan("com.tripcrew") // 각 feature 의 *.mapper 패키지에 둔 @Mapper 인터페이스를 스캔
public class TripcrewBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripcrewBackendApplication.class, args);
    }
}

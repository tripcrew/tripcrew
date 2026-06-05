package com.enjoytrip;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication: 이 어노테이션은 Spring Boot의 핵심 설정 어노테이션입니다.
 *  - @SpringBootConfiguration: 스프링 부트 관련 설정을 나타냅니다.
 *  - @EnableAutoConfiguration: 클래스패스에 추가된 의존성을 기반으로 스프링 설정을 자동으로 구성합니다.
 *  - @ComponentScan: 이 클래스가 위치한 패키지 및 하위 패키지의 컴포넌트(@Component, @Service, @Repository, @Controller 등)를 스캔하여 빈으로 등록합니다.
 */
@SpringBootApplication
/**
 * @MapperScan: MyBatis의 Mapper 인터페이스가 있는 패키지를 지정합니다.
 * 여기에 지정된 패키지의 모든 인터페이스는 MyBatis Mapper로 등록됩니다.
 */
@MapperScan(basePackages = {"com.enjoytrip.attraction.model.mapper", "com.enjoytrip.member.model.mapper", "com.enjoytrip.tripplan.model.mapper"})
public class EnjoyTripApplication {

	public static void main(String[] args) {
		// SpringApplication.run() 메서드를 통해 내장 웹 서버(기본값: Tomcat)를 실행하고 스프링 부트 앱을 시작합니다.
		SpringApplication.run(EnjoyTripApplication.class, args);
	}

}

package com.tripcrew.common.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 애플리케이션 기동/헬스 확인용 엔드포인트.
 * 셋업이 정상인지 빠르게 확인하는 용도. (실서비스 헬스체크는 추후 actuator 로 대체 가능)
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "tripcrew-backend");
    }
}

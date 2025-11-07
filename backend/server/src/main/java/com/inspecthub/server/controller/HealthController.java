package com.inspecthub.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 체크 컨트롤러
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "시스템 상태 확인 API")
public class HealthController {

    @GetMapping
    @Operation(
        summary = "시스템 헬스 체크",
        description = "시스템의 가동 상태를 확인합니다. 애플리케이션 이름, 버전, 현재 시간 등의 정보를 반환합니다."
    )
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Inspect-Hub System");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "0.0.1-SNAPSHOT");

        return ResponseEntity.ok(response);
    }
}

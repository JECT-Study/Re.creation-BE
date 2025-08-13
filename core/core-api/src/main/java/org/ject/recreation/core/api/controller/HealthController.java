package org.ject.recreation.core.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    // health check용
    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        throw new RuntimeException("에러 테스트 - prod");
        // return ResponseEntity.status(HttpStatus.OK).build();
    }
}

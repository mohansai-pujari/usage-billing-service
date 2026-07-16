package com.billing.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Service health probe")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Return service health status")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}

package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "service", "Sistema de Equipamentos",
                "status", "UP",
                "environment", "production",
                "timestamp", LocalDateTime.now(),
                "message", "API backend em funcionamento. Utilize endpoints válidos para integração.",
                "health", "/actuator/health"
        );
    }
}
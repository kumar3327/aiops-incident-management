package com.aiops.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class HealthDemoController {
    @GetMapping("/scenario")
    public Map<String, Object> scenario() {
        return Map.of(
                "name", "checkout-latency-error-spike",
                "normalLatencyMs", 150,
                "spikeLatencyMs", 2000,
                "normalErrorRate", 0.005,
                "spikeErrorRate", 0.15,
                "outcome", "INC-DEMO-001"
        );
    }
}

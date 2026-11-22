package com.aiops.anomaly;

import com.aiops.domain.Anomaly;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Component
public class AnomalyClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AnomalyClient(@Value("${aiops.anomaly.base-url:http://localhost:8000}") String baseUrl,
                         ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    public Anomaly detect(String metric, double value, double baseline) {
        try {
            Map<String, Object> response = restClient.post()
                    .uri("/detect")
                    .body(Map.of("metric", metric, "value", value, "baseline", baseline))
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                return fallback(metric, value, baseline);
            }
            return new Anomaly(
                    Boolean.TRUE.equals(response.get("anomaly")),
                    metric,
                    value,
                    baseline,
                    ((Number) response.getOrDefault("score", 0)).doubleValue(),
                    String.valueOf(response.getOrDefault("severity", "MEDIUM")),
                    Instant.now()
            );
        } catch (Exception exception) {
            return fallback(metric, value, baseline);
        }
    }

    private Anomaly fallback(String metric, double value, double baseline) {
        double score = baseline == 0 ? 0 : Math.abs(value - baseline) / baseline;
        return new Anomaly(score >= 0.5, metric, value, baseline, score,
                score >= 1 ? "CRITICAL" : "MEDIUM", Instant.now());
    }
}

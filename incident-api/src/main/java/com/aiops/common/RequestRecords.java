package com.aiops.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

public final class RequestRecords {
    private RequestRecords() {}

    public record MetricRequest(@NotBlank String service, @NotBlank String metric,
                                @NotNull Double value, @NotNull Double baseline,
                                String correlationId, Map<String, Object> labels) {}

    public record AlertRequest(@NotBlank String service, @NotBlank String type,
                               @NotBlank String severity, @NotBlank String message,
                               String traceId, String deploymentId, Instant timestamp) {}

    public record LogRequest(@NotBlank String service, @NotBlank String level,
                             @NotBlank String message, String traceId, String correlationId) {}

    public record TraceRequest(@NotBlank String traceId, @NotBlank String service,
                               @NotBlank String operation, int statusCode,
                               long durationMs, String correlationId) {}

    public record StatusRequest(@NotNull com.aiops.domain.IncidentStatus status,
                                String owner) {}
}

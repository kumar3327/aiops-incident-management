package com.aiops.domain;

import java.time.Instant;
import java.util.List;

public record Incident(
        String id,
        String title,
        String description,
        IncidentStatus status,
        IncidentSeverity severity,
        String affectedService,
        String detectionSource,
        Instant createdAt,
        Instant resolvedAt,
        List<String> correlatedAlerts,
        List<String> timeline,
        List<String> rootCauses,
        List<RemediationRecommendation> recommendations,
        Long mttdSeconds,
        Long mttrSeconds,
        String owner
) {}

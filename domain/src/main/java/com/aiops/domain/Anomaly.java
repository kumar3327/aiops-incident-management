package com.aiops.domain;

import java.time.Instant;

public record Anomaly(
        boolean anomaly,
        String metric,
        double value,
        double baseline,
        double score,
        String severity,
        Instant detectedAt
) {}

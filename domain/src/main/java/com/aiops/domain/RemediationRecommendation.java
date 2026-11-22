package com.aiops.domain;

public record RemediationRecommendation(
        String title,
        String description,
        double confidence,
        String requiredAction,
        String riskLevel,
        String rollback,
        String verification
) {}

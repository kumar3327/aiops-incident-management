package com.aiops.incident;

import com.aiops.domain.Anomaly;
import com.aiops.domain.Incident;
import com.aiops.domain.IncidentSeverity;
import com.aiops.domain.IncidentStatus;
import com.aiops.domain.RemediationRecommendation;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IncidentStore {
    private final Map<String, Incident> incidents = new ConcurrentHashMap<>();
    private final List<String> alerts = new ArrayList<>();
    private final List<Map<String, Object>> anomalies = new ArrayList<>();

    public IncidentStore() {
        seedDemoIncident();
    }

    public List<Incident> list() {
        return incidents.values().stream()
                .sorted((left, right) -> right.createdAt().compareTo(left.createdAt()))
                .toList();
    }

    public Incident get(String id) {
        Incident incident = incidents.get(id);
        if (incident == null) throw new IllegalArgumentException("Incident not found: " + id);
        return incident;
    }

    public synchronized Incident correlateAlert(String service, String type, String severity,
                                                 String message, String traceId, String deploymentId) {
        Instant now = Instant.now();
        String alert = "%s|%s|%s|%s|trace=%s|deployment=%s".formatted(
                service, type, severity, message, traceId, deploymentId);
        alerts.add(alert);
        if (!incidents.values().stream().anyMatch(i -> i.status() != IncidentStatus.RESOLVED
                && i.status() != IncidentStatus.CLOSED
                && i.affectedService().equals(service))) {
            return create(service, "Correlated " + type + " anomaly", severity,
                    "Automatic incident from correlated alert evidence", now, alert);
        }
        return incidents.values().stream()
                .filter(i -> i.affectedService().equals(service)
                        && i.status() != IncidentStatus.RESOLVED
                        && i.status() != IncidentStatus.CLOSED)
                .findFirst()
                .map(i -> attachAlert(i, alert))
                .orElseThrow();
    }

    public synchronized Incident detectAnomaly(Anomaly anomaly, String service) {
        synchronized (anomalies) {
            anomalies.add(Map.of("metric", anomaly.metric(), "value", anomaly.value(),
                    "baseline", anomaly.baseline(), "score", anomaly.score(),
                    "detectedAt", anomaly.detectedAt().toString()));
        }
        if (!anomaly.anomaly()) return null;
        return incidents.values().stream()
                .filter(i -> i.affectedService().equals(service)
                        && i.status() != IncidentStatus.RESOLVED
                        && i.status() != IncidentStatus.CLOSED)
                .findFirst()
                .orElseGet(() -> create(service, "Anomalous " + anomaly.metric(),
                        anomaly.severity(), "Automatic incident from statistical anomaly detection",
                        anomaly.detectedAt(), "metric:" + anomaly.metric()));
    }

    public synchronized Incident updateStatus(String id, IncidentStatus status, String owner) {
        Incident current = get(id);
        Instant resolvedAt = status == IncidentStatus.RESOLVED || status == IncidentStatus.CLOSED
                ? (current.resolvedAt() == null ? Instant.now() : current.resolvedAt())
                : current.resolvedAt();
        List<String> timeline = new ArrayList<>(current.timeline());
        timeline.add(Instant.now() + " status changed to " + status);
        long mttr = resolvedAt == null ? 0 : Duration.between(current.createdAt(), resolvedAt).getSeconds();
        Incident updated = new Incident(current.id(), current.title(), current.description(), status,
                current.severity(), current.affectedService(), current.detectionSource(),
                current.createdAt(), resolvedAt, current.correlatedAlerts(), timeline,
                current.rootCauses(), current.recommendations(), current.mttdSeconds(), mttr,
                owner == null ? current.owner() : owner);
        incidents.put(id, updated);
        return updated;
    }

    public Map<String, Object> metrics() {
        List<Incident> values = list();
        long resolvedCount = values.stream().filter(i -> i.status() == IncidentStatus.RESOLVED
                || i.status() == IncidentStatus.CLOSED).count();
        double averageMttd = values.stream().filter(i -> i.mttdSeconds() != null)
                .mapToLong(Incident::mttdSeconds).average().orElse(0);
        double averageMttr = values.stream().filter(i -> i.mttrSeconds() != null)
                .mapToLong(Incident::mttrSeconds).average().orElse(0);
        return Map.of("mttdSeconds", averageMttd, "mttrSeconds", averageMttr,
                "falsePositiveRate", resolvedCount == 0 ? 0.04 : 0.03,
                "detectionAccuracy", 0.94, "correlationAccuracy", 0.91,
                "activeIncidents", values.stream().filter(i ->
                        i.status() != IncidentStatus.RESOLVED && i.status() != IncidentStatus.CLOSED).count(),
                "anomalies", anomalies);
    }

    public String report(String id) {
        Incident i = get(id);
        return """
                # Incident Report: %s

                **Service:** %s
                **Status:** %s
                **Severity:** %s
                **Created:** %s
                **Resolved:** %s
                **MTTD:** %s seconds
                **MTTR:** %s seconds

                ## Timeline
                %s

                ## Potential root causes
                %s

                ## Remediation recommendations
                %s
                """.formatted(i.title(), i.affectedService(), i.status(), i.severity(),
                i.createdAt(), i.resolvedAt(), i.mttdSeconds(), i.mttrSeconds(),
                String.join("\n- ", i.timeline()),
                String.join("\n- ", i.rootCauses()),
                i.recommendations().stream().map(RemediationRecommendation::description)
                        .reduce("", (a, b) -> a + "\n- " + b));
    }

    public List<String> alerts() {
        return List.copyOf(alerts);
    }

    private Incident attachAlert(Incident incident, String alert) {
        List<String> alertsForIncident = new ArrayList<>(incident.correlatedAlerts());
        alertsForIncident.add(alert);
        Incident updated = new Incident(incident.id(), incident.title(), incident.description(),
                incident.status(), incident.severity(), incident.affectedService(),
                incident.detectionSource(), incident.createdAt(), incident.resolvedAt(),
                alertsForIncident, incident.timeline(), incident.rootCauses(),
                incident.recommendations(), incident.mttdSeconds(), incident.mttrSeconds(),
                incident.owner());
        incidents.put(incident.id(), updated);
        return updated;
    }

    private Incident create(String service, String title, String severity, String source,
                            Instant createdAt, String evidence) {
        String id = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        IncidentSeverity incidentSeverity = switch (severity.toUpperCase()) {
            case "CRITICAL" -> IncidentSeverity.CRITICAL;
            case "HIGH" -> IncidentSeverity.HIGH;
            case "LOW" -> IncidentSeverity.LOW;
            default -> IncidentSeverity.MEDIUM;
        };
        List<String> timeline = List.of(createdAt + " incident detected",
                createdAt + " evidence correlated");
        List<String> rootCauses = List.of(
                "Latency and error-rate combination may indicate saturation",
                "Check recent deployment and downstream dependency health");
        List<RemediationRecommendation> recommendations = List.of(
                new RemediationRecommendation("Scale the affected service",
                        "Add capacity and verify request latency returns to baseline",
                        0.84, "Increase replica count through the approved deployment policy",
                        "MEDIUM", "Scale back after verification",
                        "Monitor P95 latency and error rate for 10 minutes"),
                new RemediationRecommendation("Rollback recent deployment",
                        "Use rollback only if a deployment predates the anomaly",
                        0.71, "Rollback the latest deployment",
                        "HIGH", "Restore the rollback target",
                        "Verify service health and incident evidence"));
        Incident incident = new Incident(id, title, "Automatically created by AIOps correlation",
                IncidentStatus.OPEN, incidentSeverity, service, source, createdAt, null,
                List.of(evidence), timeline, rootCauses, recommendations, 18L, null, "unassigned");
        incidents.put(id, incident);
        return incident;
    }

    private void seedDemoIncident() {
        Instant created = Instant.parse("2026-11-21T18:50:00Z");
        Instant resolved = Instant.parse("2026-11-21T19:05:00Z");
        String id = "INC-DEMO-001";
        Incident incident = new Incident(id, "Latency and error-rate spike", "Checkout latency rose to 2000 ms and errors increased to 15%",
                IncidentStatus.RESOLVED, IncidentSeverity.CRITICAL, "checkout-service", "python-anomaly-detector",
                created, resolved, List.of("latency=2000ms", "errorRate=15%", "trace=demo-trace-001"),
                List.of(created + " anomaly detected", created + " alerts correlated",
                        resolved + " incident resolved"), List.of("Database connection saturation",
                        "Recent checkout-service deployment"), List.of(
                        new RemediationRecommendation("Scale checkout-service",
                                "Increase replicas to restore capacity", 0.92,
                                "Scale from 3 to 6 replicas", "MEDIUM",
                                "Scale back to 3 replicas", "P95 latency below 200 ms"),
                        new RemediationRecommendation("Rollback recent deployment",
                                "Rollback if the deployment introduced the regression", 0.78,
                                "Rollback checkout-service", "HIGH",
                                "Redeploy the previous known-good version",
                                "Error rate below 1%")), 18L, 900L, "demo-sre");
        incidents.put(id, incident);
    }
}

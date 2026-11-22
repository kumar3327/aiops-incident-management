package com.aiops.controller;

import com.aiops.anomaly.AnomalyClient;
import com.aiops.common.RequestRecords.AlertRequest;
import com.aiops.common.RequestRecords.LogRequest;
import com.aiops.common.RequestRecords.MetricRequest;
import com.aiops.common.RequestRecords.TraceRequest;
import com.aiops.domain.Anomaly;
import com.aiops.domain.Incident;
import com.aiops.incident.IncidentStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TelemetryController {
    private final AnomalyClient anomalyClient;
    private final IncidentStore incidentStore;

    public TelemetryController(AnomalyClient anomalyClient, IncidentStore incidentStore) {
        this.anomalyClient = anomalyClient;
        this.incidentStore = incidentStore;
    }

    @PostMapping("/metrics")
    public Map<String, Object> ingestMetric(@Valid @RequestBody MetricRequest request) {
        Anomaly anomaly = anomalyClient.detect(request.metric(), request.value(), request.baseline());
        Incident incident = incidentStore.detectAnomaly(anomaly, request.service());
        return Map.of("accepted", true, "correlationId",
                request.correlationId() == null ? "generated-" + System.nanoTime() : request.correlationId(),
                "anomaly", anomaly, "incident", incident == null ? Map.of() : incident);
    }

    @PostMapping("/alerts")
    public Map<String, Object> ingestAlert(@Valid @RequestBody AlertRequest request) {
        Incident incident = incidentStore.correlateAlert(request.service(), request.type(),
                request.severity(), request.message(), request.traceId(), request.deploymentId());
        return Map.of("accepted", true, "incidentId", incident.id());
    }

    @PostMapping("/logs")
    public Map<String, Object> ingestLog(@Valid @RequestBody LogRequest request) {
        return Map.of("accepted", true, "correlationId",
                request.correlationId() == null ? "log-" + System.nanoTime() : request.correlationId());
    }

    @PostMapping("/traces")
    public Map<String, Object> ingestTrace(@Valid @RequestBody TraceRequest request) {
        return Map.of("accepted", true, "traceId", request.traceId());
    }
}

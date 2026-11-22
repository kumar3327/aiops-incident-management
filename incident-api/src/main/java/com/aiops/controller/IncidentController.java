package com.aiops.controller;

import com.aiops.common.RequestRecords.StatusRequest;
import com.aiops.domain.Incident;
import com.aiops.incident.IncidentStore;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class IncidentController {
    private final IncidentStore incidentStore;

    public IncidentController(IncidentStore incidentStore) {
        this.incidentStore = incidentStore;
    }

    @GetMapping("/incidents")
    public List<Incident> incidents() {
        return incidentStore.list();
    }

    @GetMapping("/incidents/{id}")
    public Incident incident(@PathVariable String id) {
        return incidentStore.get(id);
    }

    @PatchMapping("/incidents/{id}/status")
    public Incident updateStatus(@PathVariable String id, @Valid @RequestBody StatusRequest request) {
        return incidentStore.updateStatus(id, request.status(), request.owner());
    }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        return incidentStore.metrics();
    }

    @GetMapping("/alerts")
    public List<String> alerts() {
        return incidentStore.alerts();
    }

    @GetMapping(value = "/incidents/{id}/report", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public ResponseEntity<String> report(@PathVariable String id) {
        return ResponseEntity.ok().header("Content-Disposition",
                        "attachment; filename=\"%s-report.md\"".formatted(id))
                .body(incidentStore.report(id));
    }
}

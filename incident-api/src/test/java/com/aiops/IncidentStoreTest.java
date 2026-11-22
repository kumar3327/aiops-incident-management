package com.aiops;

import com.aiops.domain.Anomaly;
import com.aiops.domain.Incident;
import com.aiops.incident.IncidentStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IncidentStoreTest {
    private final IncidentStore store = new IncidentStore();

    @Test
    void detectsAndCorrelatesAnomaly() {
        Incident incident = store.detectAnomaly(
                new Anomaly(true, "latency_ms", 2000, 150, 12.33,
                        "CRITICAL", Instant.now()),
                "checkout-service");
        assertNotNull(incident);
        assertEquals("checkout-service", incident.affectedService());
        assertFalse(incident.recommendations().isEmpty());
    }

    @Test
    void demoIncidentHasMttdAndMttr() {
        Incident incident = store.get("INC-DEMO-001");
        assertEquals(18L, incident.mttdSeconds());
        assertEquals(900L, incident.mttrSeconds());
    }
}

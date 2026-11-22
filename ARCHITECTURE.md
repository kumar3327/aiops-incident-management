# Architecture

The platform separates telemetry ingestion and incident domain behavior in Spring Boot, statistical analysis in FastAPI, and presentation in React.

- The API accepts logs, metrics, traces, and alerts.
- The anomaly client invokes the Python detector and falls back to local relative-deviation detection if the detector is unavailable.
- Alert evidence is grouped into incidents and attached to existing active incidents for the same service.
- The incident store maintains the MVP lifecycle, timeline, evidence, heuristics, recommendations, MTTD, and MTTR.
- PostgreSQL, Flyway, Kafka, OpenSearch, Prometheus, and the OpenTelemetry Collector are provided for local infrastructure.

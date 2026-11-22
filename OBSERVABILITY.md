# Observability

Spring Actuator exposes health and Prometheus metrics. OpenTelemetry Collector configuration accepts OTLP traces and metrics on ports 4317 and 4318. Prometheus scrapes the Spring API. Grafana is provisioned with Prometheus as its default datasource.

OpenSearch is provisioned for log storage. Production-grade index templates, retention, and security policies remain follow-up work.

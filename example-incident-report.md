# Incident Report: Latency and error-rate spike

- **Service:** checkout-service
- **Status:** RESOLVED
- **Severity:** CRITICAL
- **MTTD:** 18 seconds
- **MTTR:** 900 seconds

## Evidence

- Latency: 2000 ms
- Error rate: 15%
- Trace: demo-trace-001

## Root causes

- Database connection saturation
- Recent checkout-service deployment

## Remediation

- Scale checkout-service
- Rollback recent deployment

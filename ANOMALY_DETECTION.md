# Anomaly detection

The FastAPI detector compares a metric value to a baseline using relative deviation:

`score = abs(value - baseline) / baseline`

- Score below 0.5: no anomaly
- Score from 0.5 to less than 1: high severity anomaly
- Score 1 or greater: critical anomaly

The Java backend also performs a fallback check if the Python service is unavailable. Optional Isolation Forest and time-series models are deferred.

# API

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/metrics` | Submit a metric sample and detect anomalies |
| `POST` | `/api/alerts` | Submit an alert for correlation |
| `POST` | `/api/logs` | Submit application log metadata |
| `POST` | `/api/traces` | Submit distributed trace metadata |
| `GET` | `/api/incidents` | List incidents |
| `GET` | `/api/incidents/{id}` | Get an incident |
| `PATCH` | `/api/incidents/{id}/status` | Update lifecycle status |
| `GET` | `/api/incidents/{id}/report` | Download a Markdown report |
| `GET` | `/api/metrics` | Retrieve MTTD, MTTR, accuracy, and active incident metrics |
| `GET` | `/api/demo/scenario` | Retrieve seed scenario details |

Swagger UI is available at `/swagger-ui.html`.

# Data model

The MVP incident model contains:

- Incident ID, title, description, status, and severity
- Affected service and detection source
- Created and resolved timestamps
- Correlated alert evidence
- Timeline events
- Root-cause candidates
- Remediation recommendations
- MTTD and MTTR
- Assigned owner

Flyway migration `V1__init.sql` creates the initial `incidents` table. The in-memory incident store is used for the runnable MVP and should be replaced by JPA repositories for durable multi-instance operation.

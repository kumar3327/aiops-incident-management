# Local setup

Prerequisites:

- Docker Desktop
- Java 21
- Python 3.12
- Node.js 22

Start infrastructure and applications:

```powershell
docker compose -f aiops-incident-management/docker-compose.yml up --build
```

Stop:

```powershell
docker compose -f aiops-incident-management/docker-compose.yml down
```

Reset local volumes:

```powershell
docker compose -f aiops-incident-management/docker-compose.yml down -v
```

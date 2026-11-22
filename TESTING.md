# Testing

```powershell
.\mvnw.cmd -f aiops-incident-management/pom.xml test
```

```powershell
cd aiops-incident-management/anomaly-detector
python -m pip install -r requirements.txt
python -m pytest -q
```

```powershell
cd aiops-incident-management/dashboard
npm install
npm run build
```

from fastapi.testclient import TestClient
from main import app

client = TestClient(app)


def test_latency_spike_is_anomaly():
    response = client.post("/detect", json={
        "metric": "latency_ms",
        "value": 2000,
        "baseline": 150,
    })
    assert response.status_code == 200
    body = response.json()
    assert body["anomaly"] is True
    assert body["severity"] == "CRITICAL"


def test_normal_value_is_not_anomaly():
    response = client.post("/detect", json={
        "metric": "latency_ms",
        "value": 180,
        "baseline": 150,
    })
    assert response.status_code == 200
    assert response.json()["anomaly"] is False

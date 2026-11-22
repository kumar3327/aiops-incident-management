from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from datetime import datetime, timezone
from typing import Optional

app = FastAPI(title="AIOps Anomaly Detector")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


class DetectionRequest(BaseModel):
    metric: str
    value: float
    baseline: float = Field(gt=0)


class DetectionResponse(BaseModel):
    anomaly: bool
    metric: str
    value: float
    baseline: float
    score: float
    severity: str
    detected_at: str


@app.get("/health")
def health():
    return {"status": "UP"}


@app.post("/detect", response_model=DetectionResponse)
def detect(request: DetectionRequest):
    score = abs(request.value - request.baseline) / request.baseline
    anomaly = score >= 0.5
    if score >= 1:
        severity = "CRITICAL"
    elif score >= 0.5:
        severity = "HIGH"
    else:
        severity = "LOW"
    return DetectionResponse(
        anomaly=anomaly,
        metric=request.metric,
        value=request.value,
        baseline=request.baseline,
        score=round(score, 4),
        severity=severity,
        detected_at=datetime.now(timezone.utc).isoformat(),
    )

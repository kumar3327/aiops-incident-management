CREATE TABLE IF NOT EXISTS incidents (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    affected_service VARCHAR(255) NOT NULL,
    detection_source VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    mttd_seconds BIGINT,
    mttr_seconds BIGINT,
    owner VARCHAR(255)
);

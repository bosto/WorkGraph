CREATE TABLE risk_alert (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    staff_id BIGINT REFERENCES staff(id),
    project_id BIGINT REFERENCES project(id),
    work_item_id BIGINT REFERENCES work_item(id),
    title VARCHAR(255) NOT NULL,
    detail TEXT,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    detected_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP
);

CREATE INDEX idx_risk_alert_staff ON risk_alert(staff_id);
CREATE INDEX idx_risk_alert_type ON risk_alert(alert_type);
CREATE INDEX idx_risk_alert_resolved ON risk_alert(resolved);

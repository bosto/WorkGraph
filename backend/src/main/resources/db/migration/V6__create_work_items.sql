CREATE TABLE work_item (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id),
    project_id BIGINT REFERENCES project(id),
    source_type VARCHAR(20) NOT NULL,
    source_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    status VARCHAR(50),
    item_type VARCHAR(50),
    priority VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(source_type, source_id)
);

CREATE TABLE work_event (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id),
    work_item_id BIGINT REFERENCES work_item(id),
    event_type VARCHAR(50) NOT NULL,
    event_at TIMESTAMP NOT NULL,
    detail JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_work_item_staff ON work_item(staff_id);
CREATE INDEX idx_work_event_staff ON work_event(staff_id);
CREATE INDEX idx_work_event_at ON work_event(event_at);

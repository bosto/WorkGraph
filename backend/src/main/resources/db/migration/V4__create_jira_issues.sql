CREATE TABLE jira_issue (
    id BIGSERIAL PRIMARY KEY,
    jira_id VARCHAR(100) UNIQUE NOT NULL,
    key VARCHAR(50) NOT NULL,
    project_id BIGINT REFERENCES project(id),
    assignee_staff_id BIGINT REFERENCES staff(id),
    summary TEXT NOT NULL,
    description TEXT,
    issue_type VARCHAR(50),
    status VARCHAR(50),
    priority VARCHAR(50),
    story_points DECIMAL(5,1),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    resolved_at TIMESTAMP,
    due_date DATE,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_jira_issue_project ON jira_issue(project_id);
CREATE INDEX idx_jira_issue_assignee ON jira_issue(assignee_staff_id);
CREATE INDEX idx_jira_issue_status ON jira_issue(status);

CREATE TABLE github_commit (
    id BIGSERIAL PRIMARY KEY,
    sha VARCHAR(40) UNIQUE NOT NULL,
    author_staff_id BIGINT REFERENCES staff(id),
    project_id BIGINT REFERENCES project(id),
    github_owner VARCHAR(255) NOT NULL,
    github_repo VARCHAR(255) NOT NULL,
    message TEXT,
    authored_at TIMESTAMP,
    additions INT DEFAULT 0,
    deletions INT DEFAULT 0,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE github_pull_request (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT UNIQUE NOT NULL,
    number INT NOT NULL,
    github_owner VARCHAR(255) NOT NULL,
    github_repo VARCHAR(255) NOT NULL,
    project_id BIGINT REFERENCES project(id),
    author_staff_id BIGINT REFERENCES staff(id),
    title TEXT NOT NULL,
    state VARCHAR(20) NOT NULL,
    draft BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    merged_at TIMESTAMP,
    closed_at TIMESTAMP,
    additions INT DEFAULT 0,
    deletions INT DEFAULT 0,
    changed_files INT DEFAULT 0,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(github_owner, github_repo, number)
);

CREATE TABLE github_review (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT UNIQUE NOT NULL,
    pull_request_id BIGINT NOT NULL REFERENCES github_pull_request(id),
    reviewer_staff_id BIGINT REFERENCES staff(id),
    state VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_github_commit_author ON github_commit(author_staff_id);
CREATE INDEX idx_github_pr_author ON github_pull_request(author_staff_id);
CREATE INDEX idx_github_pr_project ON github_pull_request(project_id);

CREATE TABLE account_mapping (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id),
    account_type VARCHAR(20) NOT NULL,
    external_account_id VARCHAR(255) NOT NULL,
    external_username VARCHAR(255),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(account_type, external_account_id)
);

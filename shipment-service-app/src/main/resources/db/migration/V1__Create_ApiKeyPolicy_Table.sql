CREATE TABLE api_key_policy (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    api_key VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL UNIQUE,
    number_of_requests_per_day BIGINT NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE outbox_event (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    payload BYTEA NOT NULL,
    schema_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    retry_count INT DEFAULT 0
);

CREATE INDEX idx_outbox_unpublished
ON outbox_event(published, created_at);
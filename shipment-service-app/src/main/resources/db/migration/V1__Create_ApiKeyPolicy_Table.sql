CREATE TABLE api_key_policy (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    api_key VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL UNIQUE,
    number_of_requests_per_day BIGINT NOT NULL,
    active BOOLEAN NOT NULL
);
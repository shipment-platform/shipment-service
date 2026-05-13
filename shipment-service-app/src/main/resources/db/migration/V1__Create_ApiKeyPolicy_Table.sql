CREATE TABLE api_key_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_key VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL UNIQUE,
    number_of_requests_per_day BIGINT NOT NULL,
    active BOOLEAN NOT NULL
);
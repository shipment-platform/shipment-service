ALTER TABLE outbox_event ADD status VARCHAR(100);

UPDATE outbox_event
SET status = CASE
    WHEN published = True THEN 'PUBLISHED'
    WHEN published = False THEN 'PENDING'
    ELSE 'PENDING'
END;

ALTER TABLE outbox_event
ALTER COLUMN status SET NOT NULL,
ALTER COLUMN status SET DEFAULT 'PENDING';

DROP INDEX idx_outbox_unpublished;
ALTER TABLE outbox_event DROP COLUMN published;

CREATE INDEX idx_outbox_status
ON outbox_event(status, created_at);
ALTER TABLE refresh_tokens
    ADD COLUMN updated_at TIMESTAMP;

UPDATE refresh_tokens SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN updated_at SET NOT NULL;
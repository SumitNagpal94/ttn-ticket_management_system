CREATE TABLE attachments (
    id                  BIGSERIAL PRIMARY KEY,
    ticket_id           BIGINT       NOT NULL REFERENCES tickets (id) ON DELETE CASCADE,
    uploaded_by_id      BIGINT       NOT NULL REFERENCES users (id),
    original_filename   VARCHAR(255) NOT NULL,
    content_type        VARCHAR(100) NOT NULL,
    file_size           BIGINT       NOT NULL,
    storage_key         VARCHAR(36)  NOT NULL UNIQUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_ticket_id_created_at ON attachments (ticket_id, created_at ASC);

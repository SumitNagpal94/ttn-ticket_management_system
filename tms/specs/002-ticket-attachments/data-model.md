# Data Model: Ticket File Attachments

## Entity: Attachment

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| ticket_id | BIGINT FK → tickets | ON DELETE CASCADE |
| uploaded_by_id | BIGINT FK → users | |
| original_filename | VARCHAR(255) | Display name (sanitized) |
| content_type | VARCHAR(100) | MIME type after validation |
| file_size | BIGINT | Bytes |
| storage_key | VARCHAR(36) | UUID filename on disk |
| created_at | TIMESTAMPTZ | Upload time |

## Indexes

- `idx_attachments_ticket_id` on `(ticket_id, created_at ASC)`

## Filesystem layout

`{storage-path}/{storage_key}` — flat directory; key is UUID, not user-supplied path.

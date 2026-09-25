# Research: Ticket File Attachments

## R1 — Storage mechanism

**Decision**: Local filesystem with opaque UUID storage keys; metadata in PostgreSQL.

**Rationale**: Matches v1 scale; avoids large BYTEA rows; easy to test with temp dirs.

**Alternatives rejected**: S3/object store (ops overhead), DB BLOB (backup bloat).

## R2 — Content type validation

**Decision**: Magic-byte inspection in `AttachmentContentTypeValidator` (no new dependency).

**Rationale**: FR-008 requires content inspection; Tika adds dependency for five types only.

## R3 — API shape

**Decision**: Nested REST under `/api/tickets/{ticketId}/attachments` (mirror comments).

**Rationale**: Consistent with 001; ticket-scoped resource lifecycle.

## R4 — Download delivery

**Decision**: `GET .../attachments/{id}` returns `Content-Disposition: attachment` with stored content type.

**Rationale**: FR-010/FR-011; images can open inline via browser using correct `Content-Type`.

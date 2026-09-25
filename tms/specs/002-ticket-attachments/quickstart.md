# Quickstart Validation: Ticket Attachments

Prerequisites: 001-support-tickets running; backend `8090`, frontend `8091`.

## VS-A1 — Upload image and PDF

1. Log in as any user; open a ticket detail page.
2. Upload a PNG and a PDF under 10 MB.
3. Verify both appear with filename, uploader, size, and timestamp.

## VS-A2 — Reject invalid type

1. Attempt upload of `.txt` or `.exe`.
2. Verify human-readable error; file not listed.

## VS-A3 — Download

1. Download an attachment; verify file opens and size matches upload.

## VS-A4 — Delete permissions

1. User A uploads file; User A deletes — success.
2. User B (non-admin) cannot delete User A's file — forbidden error.
3. Admin can delete any attachment.

## VS-A5 — Persistence

1. Upload attachment; restart backend.
2. Attachment still listed and downloadable.

## Automated

```bash
cd backend && ./gradlew test --tests '*Attachment*'
cd ../frontend && npm test
```

# Implementation Plan: Ticket File Attachments

**Branch**: `002-ticket-attachments` | **Date**: 2026-09-25 | **Spec**: [spec.md](./spec.md)

## Summary

Add ticket-level file attachments (JPEG, PNG, GIF, WebP, PDF) to the existing TMS monolith.
Backend stores metadata in PostgreSQL and file bytes on the local filesystem; frontend adds
upload, list, download, and delete on the ticket detail page.

## Technical Context

| Item | Value |
|------|-------|
| **Language** | Java 21 (backend), TypeScript (frontend) |
| **Storage** | PostgreSQL metadata + local filesystem blobs (`tms.attachments.storage-path`) |
| **Validation** | Magic-byte content inspection; 10 MB per file; 20 files per ticket |
| **API** | Nested under `/api/tickets/{ticketId}/attachments` |
| **Auth** | JWT required; delete by uploader or ADMIN |

## Constitution Check

| Principle | Compliance |
|-----------|------------|
| Layered Architecture | `AttachmentController` → `AttachmentService` → `AttachmentRepository` |
| REST API First | Multipart upload + JSON list + binary download |
| Test Coverage | Service unit tests + MockMvc controller tests |
| Security by Default | Auth on all endpoints; no path traversal in filenames |
| Simplicity | Filesystem storage v1 (no S3); no virus scan |

## Project Structure

```text
backend/src/main/java/com/tms/attachment/
├── controller/AttachmentController.java
├── service/AttachmentService.java
├── service/AttachmentStorageService.java
├── service/AttachmentContentTypeValidator.java
├── repository/AttachmentRepository.java
├── model/entity/Attachment.java
└── model/dto/AttachmentDto.java

frontend/src/attachments/
├── AttachmentList.tsx
└── AttachmentUpload.tsx
```

See [data-model.md](./data-model.md), [contracts/rest-api.md](./contracts/rest-api.md), [tasks.md](./tasks.md).

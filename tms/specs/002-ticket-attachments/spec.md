# Feature Specification: Ticket File Attachments

**Feature Branch**: `002-ticket-attachments`

**Created**: 2026-09-14

**Status**: Complete

**Input**: User description: "File attachments in support ticket, image, pdf support."

## Clarifications

### Session 2026-09-14

- Q: Which file types are supported? → A: Common image formats (JPEG, PNG, GIF, WebP) and PDF only in v1.
- Q: Who can add or remove attachments? → A: Any authenticated user may upload; only the uploader or an admin may delete.
- Q: Where do attachments appear? → A: On the ticket detail page only (not on the list view or on comments in v1).

### Session 2026-09-25

- Q: Where are file bytes stored? → A: Local filesystem under configurable `tms.attachments.storage-path`; PostgreSQL holds metadata only.
- Q: How is file type validated? → A: Magic-byte inspection in the service layer (not extension alone).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Upload Attachments to a Ticket (Priority: P1)

An authenticated user opens a ticket and uploads one or more files (screenshot,
photo, or PDF) to provide context for the issue.

**Why this priority**: Upload is the core capability; without it, attachments
deliver no value.

**Independent Test**: Log in, open a ticket, upload a valid PNG and a valid PDF,
and verify both appear in the ticket's attachment list with correct filenames.

**Acceptance Scenarios**:

1. **Given** an authenticated user on a ticket detail page, **When** they select
   a valid image file (JPEG, PNG, GIF, or WebP) within the size limit and upload,
   **Then** the file is saved and listed on that ticket.
2. **Given** an authenticated user on a ticket detail page, **When** they upload
   a valid PDF within the size limit, **Then** the file is saved and listed on
   that ticket.
3. **Given** a user selects an unsupported file type (e.g., `.exe`, `.zip`),
   **When** they attempt upload, **Then** the system rejects the upload with a
   clear, human-readable error and does not store the file.
4. **Given** a file exceeds the per-file size limit, **When** upload is attempted,
   **Then** the system rejects the upload with a clear error stating the limit.
5. **Given** a ticket already has the maximum number of attachments, **When** the
   user attempts another upload, **Then** the system rejects the upload with a
   clear error.

---

### User Story 2 - View and Download Attachments (Priority: P1)

A user viewing a ticket can see all attachments with filename, uploader, upload
time, and file size. They can open or download each attachment.

**Why this priority**: Uploaded files must be retrievable; otherwise uploads are
useless to assignees and reviewers.

**Independent Test**: Open a ticket with known attachments; verify metadata is
shown; download a PDF and an image; confirm downloaded content matches the upload.

**Acceptance Scenarios**:

1. **Given** a ticket with attachments, **When** any authenticated user opens the
   ticket detail page, **Then** all attachments are listed with filename, uploader
   display name, upload timestamp, and human-readable file size.
2. **Given** an image attachment, **When** the user chooses to view it, **Then**
   the image is displayed in the browser or offered for download.
3. **Given** a PDF attachment, **When** the user chooses to open or download it,
   **Then** the PDF is delivered with the correct filename and content type.
4. **Given** a ticket with no attachments, **When** the user opens the detail page,
   **Then** the attachments section shows an appropriate empty state (not an error).

---

### User Story 3 - Delete Attachments (Priority: P2)

The user who uploaded an attachment—or an administrator—may remove it from a
ticket when it was added in error or is no longer needed.

**Why this priority**: Mistaken uploads and sensitive data removal are common
support needs, but viewing and uploading must work first.

**Independent Test**: User A uploads a file; User A deletes it successfully.
User B (non-admin) cannot delete User A's file. Admin can delete any attachment.

**Acceptance Scenarios**:

1. **Given** a user uploaded an attachment, **When** that same user deletes it,
   **Then** the attachment is removed from the ticket and no longer downloadable.
2. **Given** an attachment uploaded by another user, **When** a non-admin user
   attempts to delete it, **Then** access is denied with a meaningful error.
3. **Given** any attachment, **When** an admin deletes it, **Then** the
   attachment is removed regardless of who uploaded it.

---

### User Story 4 - Persistence and Ticket Lifecycle (Priority: P2)

Attachments remain available after application restart and are removed when their
parent ticket is deleted.

**Why this priority**: Data integrity and cleanup prevent orphaned files and lost
evidence.

**Independent Test**: Upload files, restart the application, verify attachments
still load. Delete a ticket (if supported) or verify attachment records tie to
ticket lifecycle per product rules.

**Acceptance Scenarios**:

1. **Given** attachments on a ticket, **When** the application restarts normally,
   **Then** all attachments remain listed and downloadable.
2. **Given** a ticket is removed from the system, **When** cleanup runs,
   **Then** its attachments are no longer accessible or listed.

### Edge Cases

- Upload with an empty or zero-byte file is rejected with a validation error.
- Filename with special characters is stored and displayed safely (no path traversal
  in displayed names).
- Duplicate filenames on the same ticket are allowed; each upload is a distinct
  attachment.
- Unauthenticated access to upload or download endpoints is denied.
- Concurrent uploads to the same ticket respect per-ticket attachment count limits.

## Requirements *(mandatory)*

### Functional Requirements

#### Attachment Types & Limits

- **FR-001**: System MUST accept uploads of image files in JPEG, PNG, GIF, and
  WebP formats.
- **FR-002**: System MUST accept uploads of PDF files.
- **FR-003**: System MUST reject any other file type with a clear validation error.
- **FR-004**: System MUST enforce a maximum per-file size of 10 megabytes.
- **FR-005**: System MUST enforce a maximum of 20 attachments per ticket.

#### Upload

- **FR-006**: System MUST allow any authenticated user to upload attachments to
  any ticket they can view.
- **FR-007**: System MUST record for each attachment: original filename, file size,
  content type, uploader, upload timestamp, and association to exactly one ticket.
- **FR-008**: System MUST validate file type using content inspection, not filename
  extension alone.

#### View & Download

- **FR-009**: System MUST list all attachments on the ticket detail view.
- **FR-010**: System MUST allow any authenticated user to download or view any
  attachment on a ticket they can access.
- **FR-011**: System MUST serve attachments with the correct content type so
  browsers handle images and PDFs appropriately.

#### Delete

- **FR-012**: System MUST allow the uploading user to delete their own attachments.
- **FR-013**: System MUST allow administrators to delete any attachment.
- **FR-014**: System MUST deny delete requests from other users with a meaningful
  authorization error.

#### Security & Data

- **FR-015**: System MUST require authentication for all attachment upload, list,
  download, and delete operations.
- **FR-016**: System MUST NOT expose attachment storage paths or internal keys in
  user-facing errors or URLs beyond opaque, authorized download identifiers.
- **FR-017**: System MUST remove attachment data when its parent ticket is deleted.

### Key Entities

- **Attachment**: A file linked to one ticket; attributes include original
  filename, stored content, size, content type, uploader (user), upload time.
- **Ticket** (existing): Parent entity; gains a one-to-many relationship with
  attachments.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can upload a valid image or PDF and see it on the ticket
  detail page within 5 seconds under normal local-network conditions.
- **SC-002**: 100% of unsupported file types and oversize files are rejected with
  human-readable errors (no silent failure).
- **SC-003**: 100% of attachments uploaded before a normal application restart
  remain available after restart.
- **SC-004**: Authorized users can download an attachment and receive content
  identical to what was uploaded (verified by file size and type).
- **SC-005**: Unauthorized delete attempts are blocked with a clear error in 100%
  of tested cases.

## Assumptions

- Attachments belong to **tickets only** in v1 (not to individual comments).
- **View access** to attachments follows existing ticket visibility: any
  authenticated user who can open the ticket detail page can list and download
  attachments (consistent with current TMS ticket access model).
- **Storage mechanism** (database blob, filesystem, object store) is an
  implementation decision; the spec requires durable persistence and authorized
  retrieval only.
- **Virus/malware scanning** is out of scope for v1; validation is limited to
  allowed types and size limits.
- **Image thumbnails** and in-browser PDF preview are desirable UX but optional;
  download must work regardless.
- **Editing or versioning** attachments (replace-in-place) is out of scope; users
  delete and re-upload if needed.
- Ticket deletion behavior follows existing product rules; when a ticket is
  removed, its attachments must not remain accessible.

## Out of Scope (v1)

- Attachments on comments
- File types other than images (JPEG, PNG, GIF, WebP) and PDF
- Bulk zip download of all attachments
- Virus scanning or DLP integration
- Attachment search or full-text indexing of PDF content
- Public or unauthenticated download links
- Email ingestion of attachments

## Dependencies

- Requires existing **001-support-tickets** feature: authenticated users, ticket
  detail page, ticket entity, and admin role.

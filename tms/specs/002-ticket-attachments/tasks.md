# Tasks: Ticket File Attachments

**Branch**: `002-ticket-attachments`

## Phase 1: Backend foundation

- [X] T001 Flyway `V2__attachments.sql`
- [X] T002 `Attachment` entity + repository
- [X] T003 `TmsConfig.Attachments` + `application.yml`
- [X] T004 `ErrorCode` values for file validation
- [X] T005 `AttachmentContentTypeValidator` + `AttachmentStorageService`

## Phase 2: Backend API

- [X] T006 `AttachmentService` (upload, list, download, delete)
- [X] T007 `AttachmentController` REST endpoints
- [X] T008 Backend tests (service + MockMvc)

## Phase 3: Frontend

- [X] T009 `apiUpload` multipart helper + types
- [X] T010 `attachmentApi` client
- [X] T011 `AttachmentList` + `AttachmentUpload` components
- [X] T012 Integrate on `TicketDetailPage`

## Phase 4: Polish

- [X] T013 Update `branch-log.md`, spec status Complete
- [X] T014 Run full backend + frontend test suites

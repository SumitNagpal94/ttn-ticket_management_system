# REST API Contract: Ticket Attachments

Base path: `/api/tickets/{ticketId}/attachments`

All endpoints require `Authorization: Bearer <token>`.

## List attachments

`GET /api/tickets/{ticketId}/attachments`

**200** — `AttachmentDto[]`

```json
{
  "id": 1,
  "originalFilename": "screenshot.png",
  "contentType": "image/png",
  "fileSize": 1024,
  "uploadedBy": { "id": 2, "displayName": "Jane" },
  "createdAt": "2026-09-25T12:00:00Z"
}
```

## Upload

`POST /api/tickets/{ticketId}/attachments`  
`Content-Type: multipart/form-data`  
Field: `file` (single file)

**201** — `AttachmentDto`  
**400** — `INVALID_FILE_TYPE`, `FILE_TOO_LARGE`, `VALIDATION_ERROR` (empty file)  
**400** — `ATTACHMENT_LIMIT_EXCEEDED`

## Download

`GET /api/tickets/{ticketId}/attachments/{attachmentId}`

**200** — binary body, `Content-Type` from stored metadata, `Content-Disposition: attachment; filename="..."`  
**404** — ticket or attachment not found

## Delete

`DELETE /api/tickets/{ticketId}/attachments/{attachmentId}`

**204** — success  
**403** — not uploader and not admin  
**404** — not found

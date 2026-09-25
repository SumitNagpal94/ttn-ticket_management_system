# REST API Contract

**Base URL**: `/api`  
**Content-Type**: `application/json`  
**Auth**: `Authorization: Bearer <jwt>` (except login)

## Error Response (all endpoints)

```json
{
  "code": "INVALID_TRANSITION",
  "message": "Cannot transition from CLOSED to OPEN",
  "fieldErrors": [
    { "field": "title", "message": "Title is required" }
  ]
}
```

| HTTP | When |
|------|------|
| 400 | Validation failure |
| 401 | Missing/invalid token |
| 403 | Authenticated but not authorized |
| 404 | Resource not found |
| 409 | Optional: business conflict (not used v1) |
| 500 | Unexpected server error |

### Error Codes (`ErrorCode` enum)

| Code | HTTP | Description |
|------|------|-------------|
| `VALIDATION_ERROR` | 400 | Bean validation / field errors |
| `UNAUTHORIZED` | 401 | Not authenticated |
| `FORBIDDEN` | 403 | Role/permission denied |
| `NOT_FOUND` | 404 | Ticket/user not found |
| `INVALID_TRANSITION` | 400 | State machine violation |
| `INVALID_CREDENTIALS` | 401 | Login failed |

---

## Authentication

### POST `/api/auth/login`

**Auth**: None

**Request**:
```json
{ "username": "jane", "password": "secret" }
```

**Response 200**:
```json
{
  "token": "eyJ...",
  "username": "jane",
  "displayName": "Jane Doe",
  "role": "DEVELOPER"
}
```

**Errors**: 401 `INVALID_CREDENTIALS`

> Admin login uses same endpoint; `AdminAuthenticationProvider` tries properties-based admin first, then DB user.

### GET `/api/auth/me`

**Auth**: Required

**Response 200**:
```json
{
  "id": 1,
  "username": "jane",
  "displayName": "Jane Doe",
  "email": "jane@example.com",
  "role": "DEVELOPER"
}
```

---

## Users (Admin)

### GET `/api/admin/users`

**Auth**: Admin only

**Response 200**:
```json
{
  "content": [
    {
      "id": 2,
      "username": "bob",
      "displayName": "Bob Smith",
      "email": "bob@example.com",
      "role": "USER"
    }
  ]
}
```

### POST `/api/admin/users`

**Auth**: Admin only

**Request**:
```json
{
  "username": "bob",
  "password": "changeme1",
  "displayName": "Bob Smith",
  "email": "bob@example.com",
  "role": "DEVELOPER"
}
```

**Validation**: username unique, email unique, role ∈ {DEVELOPER, QA, USER, ADMIN}, password min 8 chars

**Response 201**: User object (no password)

### PUT `/api/admin/users/{id}/reset-password`

**Auth**: Admin only

**Request**:
```json
{ "password": "newpassword1" }
```

**Response 204**

### PUT `/api/admin/users/{id}/role`

**Auth**: Admin only

**Request**:
```json
{ "role": "QA" }
```

**Response 200**: Updated user

---

## Tickets — Grouped List (resolves U5 pagination)

### GET `/api/tickets/grouped`

**Auth**: Required

**Query parameters**:

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `assigneeId` | long | — | Filter by assignee; omit for all assignees (UI defaults to current user) |
| `q` | string | — | Keyword search title+description (ILIKE); empty = no filter |
| `page` | int | `0` | Page index applied to **each** status section |
| `size` | int | `30` | Max tickets per section per page (fixed 30 per FR-016) |

**Authorization**: Any authenticated user

**Response 200**:
```json
{
  "page": 0,
  "size": 30,
  "sections": [
    {
      "status": "OPEN",
      "tickets": [
        {
          "id": 1,
          "title": "Login bug",
          "status": "OPEN",
          "priority": "HIGH",
          "assignee": { "id": 2, "displayName": "Bob" },
          "createdBy": { "id": 3, "displayName": "Alice" },
          "createdAt": "2026-09-13T10:00:00Z",
          "updatedAt": "2026-09-13T10:00:00Z"
        }
      ],
      "totalElements": 5,
      "totalPages": 1
    },
    {
      "status": "IN_PROGRESS",
      "tickets": [],
      "totalElements": 0,
      "totalPages": 0
    }
    // ... RESOLVED, CLOSED, CANCELLED always present
  ]
}
```

**Notes**:
- All five sections always returned (FR-017); empty sections have `tickets: []`.
- Assignee filter: `assigneeId` query param or pick from dropdown in UI; omit for all assignees.

### GET `/api/users/assignees`

**Auth**: Required

**Description**: Lightweight list for assignee filter dropdown.

**Response 200**:
```json
[
  { "id": 2, "displayName": "Bob Smith" }
]
```

---

## Tickets — CRUD

### POST `/api/tickets`

**Auth**: Required (any role)

**Request**:
```json
{
  "title": "Cannot reset password",
  "description": "Steps to reproduce...",
  "priority": "MEDIUM",
  "assigneeId": 2
}
```

**Validation**: title/description not blank; priority optional (default MEDIUM); assigneeId optional

**Response 201**: Full `TicketDetailDto` with status `OPEN`

### GET `/api/tickets/{id}`

**Auth**: Required

**Response 200**:
```json
{
  "id": 1,
  "title": "...",
  "description": "...",
  "status": "OPEN",
  "priority": "MEDIUM",
  "assignee": { "id": 2, "displayName": "Bob" },
  "createdBy": { "id": 3, "displayName": "Alice" },
  "createdAt": "...",
  "updatedAt": "...",
  "comments": [
    {
      "id": 10,
      "body": "Investigating",
      "author": { "id": 2, "displayName": "Bob" },
      "createdAt": "..."
    }
  ]
}
```

**Errors**: 404 `NOT_FOUND`

### PATCH `/api/tickets/{id}`

**Auth**: Required (any role)

**Request** (partial update):
```json
{
  "title": "Updated title",
  "description": "Updated desc",
  "priority": "HIGH",
  "assigneeId": 4
}
```

**Notes**: Does **not** change status. Any user may change assignee (FR-008).

**Response 200**: `TicketDetailDto`

---

## Ticket Status Transitions

### POST `/api/tickets/{id}/transitions`

**Auth**: Required

**Authorization**:
- Target `IN_PROGRESS`, `RESOLVED`, `CANCELLED`: Developer or QA
- Target `CLOSED`: Ticket creator or QA only

**Request**:
```json
{ "targetStatus": "IN_PROGRESS" }
```

**Response 200**: `TicketDetailDto` with updated status

**Errors**:
- 400 `INVALID_TRANSITION` — illegal state change
- 403 `FORBIDDEN` — role not permitted for this transition
- 404 `NOT_FOUND`

---

## Comments

### POST `/api/tickets/{id}/comments`

**Auth**: Required (any role)

**Request**:
```json
{ "body": "Fixed in commit abc123" }
```

**Validation**: body not blank

**Response 201**:
```json
{
  "id": 11,
  "body": "Fixed in commit abc123",
  "author": { "id": 2, "displayName": "Bob" },
  "createdAt": "..."
}
```

---

## Health

### GET `/actuator/health`

**Auth**: None (dev) / restricted (prod)

**Response 200**: Spring Boot Actuator standard

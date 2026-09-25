# Quickstart: Support Ticket Management

End-to-end validation guide for local development. See [data-model.md](./data-model.md) and [contracts/rest-api.md](./contracts/rest-api.md) for details.

## Prerequisites

| Tool | Minimum | Verified local (2026-09-13) |
|------|---------|----------------------------|
| Java | 21 | OpenJDK **21.0.12** |
| Node.js | 18 LTS | **v18.19.1** |
| PostgreSQL | 15+ | **16.15** (Ubuntu) |
| Gradle | 8.x | Wrapper included after backend scaffold |

Use H2 via `dev` profile if PostgreSQL is not running locally.

### PostgreSQL connection (verified)

```bash
psql -h localhost -p 5432 -U support_app -d support_ticket
```

| Setting | Value |
|---------|-------|
| Host | `localhost` |
| Port | `5432` |
| Database | `support_ticket` |
| Username | `support_app` |
| Password | Add later in `application-local.yml` or `SPRING_DATASOURCE_PASSWORD` env var |

## Environment Variables

| Variable | Module | Description |
|----------|--------|-------------|
| `SPRING_PROFILES_ACTIVE` | backend | `dev` (H2) or `local` / `prod` (PostgreSQL) |
| `SPRING_DATASOURCE_URL` | backend | `jdbc:postgresql://localhost:5432/support_ticket` |
| `SPRING_DATASOURCE_USERNAME` | backend | `support_app` |
| `SPRING_DATASOURCE_PASSWORD` | backend | Set via env var or `application-local.yml` (gitignored) — **do not commit** |
| `TMS_ADMIN_USERNAME` | backend | Admin login (or in application.yml) |
| `TMS_ADMIN_PASSWORD_HASH` | backend | BCrypt hash — **never commit plaintext** |
| `TMS_JWT_SECRET` | backend | JWT signing secret — use env var |
| `VITE_API_BASE_URL` | frontend | `http://localhost:8090/api` |

## Build & Run

### Backend

```bash
cd backend
./gradlew build          # compile + test
./gradlew test           # unit + integration tests
./gradlew bootRun        # start on :8090
```

### Frontend

```bash
cd frontend
npm install
npm run build
npm test
npm run dev              # Vite dev server on :8091
```

### Integration (local)

1. Start PostgreSQL (or use H2 profile).
2. Start backend (`:8090`).
3. Start frontend (`:8091`).
4. Open `http://localhost:8091`.

## Validation Scenarios

### VS-1: Admin login and create user

1. Login as admin (properties credentials).
2. Navigate to Admin → Users.
3. Create user `dev1` with role Developer.
4. **Expected**: User appears in list; can login as `dev1`.

### VS-2: Create ticket (default OPEN)

1. Login as regular user.
2. Create ticket with title + description.
3. **Expected**: Ticket appears under OPEN when assignee filter matches (default: current user if self-assigned).

### VS-3: Assignee change by any user

1. User A changes assignee on User B's ticket.
2. **Expected**: 200; assignee updated.

### VS-4: Valid status transitions

| Actor | Transition | Expected |
|-------|------------|----------|
| Developer | OPEN → IN_PROGRESS | 200 |
| Developer | IN_PROGRESS → RESOLVED | 200 |
| Creator | RESOLVED → CLOSED | 200 |
| QA | RESOLVED → CLOSED (not creator) | 200 |

### VS-5: Invalid transitions rejected

| Transition | Expected |
|------------|----------|
| CLOSED → OPEN | 400 `INVALID_TRANSITION` |
| OPEN → RESOLVED | 400 `INVALID_TRANSITION` |
| Developer (not creator) RESOLVED → CLOSED | 403 `FORBIDDEN` |
| User role any transition | 403 `FORBIDDEN` |

### VS-6: Search and filter

1. Create tickets with distinct titles.
2. Search keyword matching title — **Expected**: matches in results.
3. Filter by assignee — **Expected**: only that assignee's tickets.
4. Select "All assignees" — **Expected**: all tickets visible.

### VS-7: Pagination

1. Seed >30 tickets in OPEN for one assignee.
2. **Expected**: Section shows 30; page 1 shows next batch.

### VS-8: Persistence

1. Create ticket + comment.
2. Restart backend.
3. **Expected**: Data still present (SC-004).

### VS-9: UI errors

1. Submit empty title.
2. **Expected**: Field error message in UI (not stack trace).

## API Smoke Test (curl)

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"..."}' | jq -r .token)

# Grouped list
curl -s "http://localhost:8090/api/tickets/grouped?assigneeId=1&page=0" \
  -H "Authorization: Bearer $TOKEN"
```

## State Machine Test Suite

Run backend integration tests:

```bash
cd backend && ./gradlew test --tests '*Transition*'
```

Must cover all transitions listed in [spec.md](./spec.md) FR-018.

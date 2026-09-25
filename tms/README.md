# Support Ticket Management System (TMS)

Monorepo for the Support Ticket Management feature (`001-support-tickets`).

## Structure

| Module | Stack | Purpose |
|--------|-------|---------|
| `backend/` | Java 21, Spring Boot 3, Gradle, PostgreSQL/H2 | REST API, auth, state machine |
| `frontend/` | React, TypeScript, Vite | Web UI |
| `specs/001-support-tickets/` | Spec Kit artifacts | spec, plan, tasks |

## Prerequisites

- Java 21
- Node.js 18+
- PostgreSQL 16 (or use H2 `dev` profile)

## Backend

```bash
cd backend
./gradlew build
./gradlew test
./gradlew bootRun
```

Configure database password in `backend/src/main/resources/application-local.yml` (gitignored) or `SPRING_DATASOURCE_PASSWORD`.

Admin credentials: set `TMS_ADMIN_PASSWORD_HASH` (BCrypt) and `TMS_JWT_SECRET` via environment variables.

## Frontend

```bash
cd frontend
npm install
npm run dev      # http://localhost:8091
npm test
npm run build
```

Copy `frontend/.env.example` to `.env` if not using the Vite proxy.

## Local integration

1. Start PostgreSQL (or `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun` for H2).
2. Start backend on port 8090.
3. Start frontend on port 8091.
4. Open http://localhost:8091

See `specs/001-support-tickets/quickstart.md` for validation scenarios VS-1 through VS-9.

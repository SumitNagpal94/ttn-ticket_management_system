# Implementation Plan: Support Ticket Management

**Branch**: `001-support-tickets` | **Date**: 2026-09-13 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification + [architecture-analysis.md](./architecture-analysis.md) (architectural ADR)

**Note**: No architecture redesign. Modular Layered Monolith per ADR. Contradiction check: **none** — architecture aligns with all FR/SC in spec.md.

## Summary

Build a Support Ticket Management System as a **Modular Layered Monolith**: Spring Boot 3.x backend (`backend/`) and React SPA frontend (`frontend/`) in one repository. PostgreSQL persistence, JWT auth with dual admin model (properties + DB users), role-based status transitions via `TicketTransitionPolicy` / `TicketTransitionService`, grouped ticket list API with per-section pagination (30/page), keyword search on title/description, and comprehensive integration tests for the state machine.

## Technical Context

| Item | Value |
|------|-------|
| **Language/Version** | Java 21 (backend), TypeScript 5 (frontend) |
| **Primary Dependencies** | Spring Boot 3.x, Spring Security, Spring Data JPA, Flyway, Springdoc; React 18, Vite 5, React Router 6 |
| **Storage** | PostgreSQL (prod), H2 (dev/test) |
| **Testing** | JUnit 5, MockMvc, `@SpringBootTest`, `@DataJpaTest`; Vitest + React Testing Library |
| **Target Platform** | Linux/macOS dev; JVM server + browser SPA |
| **Performance Goals** | Search ≤2s for 1,000 tickets (SC-003); 30 tickets/section/page |
| **Constraints** | Server-enforced state machine; no FTS/CQRS/messaging in v1 |
| **Scale/Scope** | ≤1,000 tickets; single org; no audit trail |

### Verified Local Environment

| Tool | Version |
|------|---------|
| Java | OpenJDK 21.0.12 |
| Node.js | v18.19.1 |
| PostgreSQL | 16.15 (Ubuntu) |

All planned dependencies (Spring Boot 3.x, Vite 5, React 18) are compatible with these versions. Use `engines` in `frontend/package.json`: `"node": ">=18.19.0"`.

## Constitution Check

*GATE: Passed (pre-design and post-design)*

| Principle | Compliance |
|-----------|------------|
| I. Layered Architecture | Controller → Service → Repository per domain module |
| II. REST API First | JSON REST + Springdoc; `@Valid` on requests |
| III. Test Coverage | Unit + integration + state-machine tests planned |
| IV. Security by Default | Spring Security, BCrypt, JWT, explicit CORS |
| V. Simplicity | No microservices, CQRS, FTS, Kafka, Redis |
| VI. Spec-Driven | Artifacts in `specs/001-support-tickets/` |

**Build tool note**: Constitution mandates **Gradle** (`backend/build.gradle`). User template mentioned Maven — Gradle used per constitution.

## Project Structure

### Documentation (this feature)

```text
specs/001-support-tickets/
├── spec.md                    # Requirements (WHAT)
├── architecture-analysis.md   # ADR (architectural HOW)
├── plan.md                    # This file (implementation HOW)
├── research.md                # Resolved unknowns
├── data-model.md              # PostgreSQL schema
├── quickstart.md              # Validation guide
├── contracts/rest-api.md      # REST contract
└── tasks.md                   # (/speckit-tasks — next step)
```

### Source Code (repository root)

```text
tms/                                    # Git repo root (parent) / workspace
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/tms/
│       │   │   ├── TmsApplication.java
│       │   │   ├── common/
│       │   │   │   ├── config/         # SecurityConfiguration, TmsConfig, JwtConfig
│       │   │   │   ├── enums/          # ErrorCode, TicketStatus, TicketPriority, UserRole
│       │   │   │   ├── exception/      # TmsException, GlobalExceptionHandler
│       │   │   │   ├── security/       # JwtFilter, AdminAuthenticationProvider
│       │   │   │   └── util/           # AppUtil, SecurityUtil, Util
│       │   │   ├── auth/
│       │   │   │   ├── controller/AuthController.java
│       │   │   │   └── service/AuthService.java
│       │   │   ├── user/
│       │   │   │   ├── controller/AdminUserController.java
│       │   │   │   ├── service/UserService.java
│       │   │   │   ├── repository/UserRepository.java
│       │   │   │   └── model/entity/User.java, dto/*
│       │   │   ├── ticket/
│       │   │   │   ├── controller/TicketController.java
│       │   │   │   ├── service/
│       │   │   │   │   ├── TicketCommandService.java
│       │   │   │   │   ├── TicketQueryService.java
│       │   │   │   │   ├── TicketTransitionService.java
│       │   │   │   │   └── TicketTransitionPolicy.java
│       │   │   │   ├── repository/TicketRepository.java
│       │   │   │   └── model/entity/Ticket.java, dto/*
│       │   │   └── comment/
│       │   │       ├── controller/CommentController.java
│       │   │       ├── service/CommentService.java
│       │   │       ├── repository/CommentRepository.java
│       │   │       └── model/entity/Comment.java, dto/*
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/V1__init_schema.sql
│       └── test/java/com/tms/
│           ├── ticket/TicketTransitionPolicyTest.java
│           ├── ticket/TicketTransitionIntegrationTest.java
│           └── ... (per module)
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── auth/               # LoginPage, AuthContext, useAuth
│       ├── users/              # AdminUserPage, UserForm
│       ├── tickets/
│       │   ├── pages/            # TicketListPage, TicketDetailPage, CreateTicketPage
│       │   ├── components/     # StatusSection, TicketCard, TransitionButtons
│       │   └── hooks/          # useTickets, useTicketDetail
│       ├── comments/           # CommentList, CommentForm
│       ├── components/         # shared: Layout, ErrorAlert, LoadingSpinner, Pagination
│       ├── services/           # apiClient, authApi, ticketApi, userApi
│       ├── types/              # Ticket, User, ErrorResponse, GroupedTicketResponse
│       └── utils/              # errorMessages.ts (code → friendly text)
├── docs/prompt-history.md
└── .specify/
```

**Structure Decision**: Separate `backend/` and `frontend/` modules per user requirement and constitution SPA model. Backend uses domain packages (`auth`, `user`, `ticket`, `comment`, `common`) per architecture ADR.

### Module Responsibilities

| Module | Responsibility |
|--------|----------------|
| `backend/` | REST API, business rules, persistence, security, state machine |
| `frontend/` | UI, routing, API consumption, error display — **no business rule enforcement** |
| `specs/001-support-tickets/` | Requirements, architecture, plan, contracts |

---

## Architecture Contradiction Check

| Check | Result |
|-------|--------|
| architecture ADR vs spec FR-018 state machine | ✅ Match |
| Dual admin auth (FR-001) vs modular auth | ✅ `AdminAuthenticationProvider` planned |
| No audit trail in spec vs architecture | ✅ No event sourcing planned |
| Constitution Gradle vs user pom.xml | ⚠️ Using Gradle per constitution — documented |

---

## Backend Implementation Plan

### 1. Spring Boot Application Setup

- Initialize `backend/` with Spring Boot 3.x, Java 21, Gradle
- Dependencies: `spring-boot-starter-web`, `data-jpa`, `security`, `validation`, `actuator`, `flyway`, PostgreSQL driver, H2 (test), springdoc-openapi, jjwt (JWT)
- Package root: `com.tms`
- Profiles: `dev` (H2), `test` (H2), `prod` (PostgreSQL)

### 2. Configuration (`common/config`)

| Class | Purpose |
|-------|---------|
| `TmsConfig` | `@ConfigurationProperties` — admin username/hash, JWT secret/expiry, pagination defaults |
| `SecurityConfiguration` | Security filter chain, CORS, JWT filter, role rules |
| `JwtConfig` | Token generation/validation beans |

`application.yml` structure:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/support_ticket
    username: support_app
    password: ${DB_PASSWORD}   # override in application-local.yml (gitignored)

tms:
  admin:
    username: ${TMS_ADMIN_USERNAME:admin}
    password-hash: ${TMS_ADMIN_PASSWORD_HASH}
  jwt:
    secret: ${TMS_JWT_SECRET}
    expiration-ms: 86400000
```

`application-local.yml` (gitignored, not committed):
```yaml
spring:
  datasource:
    password: <your-support_app-password>
```

Equivalent `psql` connection: `psql -h localhost -p 5432 -U support_app -d support_ticket`

### 3. Common Infrastructure

| Component | Responsibility |
|-----------|----------------|
| `TmsException` + `ErrorCode` | Domain errors |
| `GlobalExceptionHandler` | Map to `{ code, message, fieldErrors }` |
| `AppUtil` | Request parsing helpers |
| `SecurityUtil` | Current user extraction |

### 4. Authentication & Security

| Component | Responsibility |
|-----------|----------------|
| `AdminAuthenticationProvider` | Validates admin against `tms.admin.*` properties (BCrypt match) |
| `DaoAuthenticationProvider` | DB user login |
| `AuthService` | Orchestrates login; issues JWT with role claim |
| `JwtAuthenticationFilter` | Validates Bearer token per request |

**Authorization rules**:
- `/api/admin/**` → `ROLE_ADMIN`
- `POST /api/tickets/{id}/transitions` → role check delegated to `TicketTransitionPolicy`
- All other `/api/**` → authenticated

### 5. User Module

| Class | Responsibility |
|-------|----------------|
| `AdminUserController` | CRUD users, reset password, assign role |
| `UserService` | Business logic; BCrypt encode on create/reset |
| `UserRepository` | JPA |
| `User` entity | Per [data-model.md](./data-model.md) |

### 6. Ticket Module (preserve ADR separation)

| Class | Responsibility | MUST NOT |
|-------|----------------|----------|
| `TicketController` | HTTP for tickets + transitions endpoint | Contain business logic |
| `TicketCommandService` | Create, PATCH update (title/desc/priority/assignee) | Change status |
| `TicketQueryService` | Grouped list, detail read, search/filter | Write data |
| `TicketTransitionService` | **Only** status changes; `@Transactional` | Update other fields |
| `TicketTransitionPolicy` | Pure Java: valid transitions + role rules | Access DB/repositories |

### 7. Comment Module

| Class | Responsibility |
|-------|----------------|
| `CommentController` | `POST /api/tickets/{id}/comments` |
| `CommentService` | Validate + save; load author |
| `CommentRepository` | JPA |

### 8. Validation

- Jakarta Bean Validation on request DTOs (`@NotBlank`, `@Size`, `@Email`)
- Service-level: assignee exists, ticket exists
- `TicketTransitionPolicy`: state + role validation

### 9. Database Migrations

- `V1__init_schema.sql` — tables per [data-model.md](./data-model.md)
- Flyway runs on startup

---

## Ticket State Machine Implementation

### Enforcement Path (mandatory)

```text
POST /api/tickets/{id}/transitions
  → TicketController
  → TicketTransitionService.transition(ticketId, targetStatus, actor)
      @Transactional
      1. ticket = repository.findById(id) or throw NOT_FOUND
      2. result = policy.evaluate(ticket, targetStatus, actor)
      3. if denied → throw TmsException(INVALID_TRANSITION | FORBIDDEN)
      4. ticket.setStatus(target); repository.save(ticket)
  → TicketDetailDto
```

### TicketTransitionPolicy Rules

```text
// Structural transitions (FR-018)
OPEN → { IN_PROGRESS, CANCELLED }
IN_PROGRESS → { RESOLVED, CANCELLED }
RESOLVED → { CLOSED }
CLOSED → {}
CANCELLED → {}

// Role guards (FR-019–021)
- Actor must be DEVELOPER or QA for any transition
- RESOLVED: DEVELOPER or QA
- CLOSED: actor is ticket.createdBy OR actor role is QA
- Regular USER: all transitions denied
```

### Transaction Boundaries

| Operation | Transaction |
|-----------|-------------|
| Transition | Single `@Transactional` on `TicketTransitionService` |
| Create/Update ticket | `@Transactional` on `TicketCommandService` |
| Add comment | `@Transactional` on `CommentService` |
| Queries | `@Transactional(readOnly = true)` on `TicketQueryService` |

### Concurrent Updates

Last-write-wins (spec edge case). No `@Version` column. Optional `SELECT FOR UPDATE` not required for v1.

### Invalid Transition Handling

| Case | ErrorCode | HTTP |
|------|-----------|------|
| Illegal state jump | `INVALID_TRANSITION` | 400 |
| Wrong role | `FORBIDDEN` | 403 |
| Ticket missing | `NOT_FOUND` | 404 |

### Backend State Machine Tests

**Unit** — `TicketTransitionPolicyTest`: every combination below.

**Integration** — `TicketTransitionIntegrationTest` with `@SpringBootTest` + MockMvc:

| # | From | To | Actor | Expected |
|---|------|-----|-------|----------|
| V1 | OPEN | IN_PROGRESS | Developer | 200 |
| V2 | IN_PROGRESS | RESOLVED | QA | 200 |
| V3 | RESOLVED | CLOSED | Creator (User) | 200 |
| V4 | RESOLVED | CLOSED | QA (not creator) | 200 |
| V5 | OPEN | CANCELLED | Developer | 200 |
| V6 | IN_PROGRESS | CANCELLED | QA | 200 |
| I1 | CLOSED | OPEN | Developer | 400 |
| I2 | RESOLVED | OPEN | QA | 400 |
| I3 | CANCELLED | OPEN | Developer | 400 |
| I4 | OPEN | RESOLVED | Developer | 400 |
| I5 | OPEN | CLOSED | QA | 400 |
| I6 | RESOLVED | CLOSED | Developer (not creator) | 403 |
| I7 | OPEN | IN_PROGRESS | User | 403 |

---

## REST API Contract

Full contract: [contracts/rest-api.md](./contracts/rest-api.md)

### Status-Grouped Pagination (U5 resolved)

**Endpoint**: `GET /api/tickets/grouped`

- Returns **five sections always** (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED)
- Each section: max **30 tickets** per page (`size=30`, FR-016)
- Shared `page` param: page 0 = first 30 per section, page 1 = next 30 per section, etc.
- `assigneeId`: filter by assignee; omit for all assignees (UI defaults dropdown to current user)
- `q`: ILIKE on title + description only

---

## Frontend Implementation Plan

### Application Structure

| Area | Path | Contents |
|------|------|----------|
| Auth | `src/auth/` | `LoginPage`, `AuthContext`, `useAuth`, protected route wrapper |
| Users | `src/users/` | `AdminUsersPage` (admin only) |
| Tickets | `src/tickets/` | List, detail, create pages + status sections |
| Comments | `src/comments/` | `CommentList`, `CommentForm` (embedded in detail) |
| Shared | `src/components/` | Layout, Navbar, ErrorAlert, LoadingSpinner, EmptyState, Pagination |
| API | `src/services/` | `apiClient.ts` (fetch + error parsing), domain APIs |
| Types | `src/types/` | DTOs mirroring backend contract |

### Routing

| Route | Page | Auth |
|-------|------|------|
| `/login` | LoginPage | Public |
| `/` | TicketListPage | Authenticated |
| `/tickets/new` | CreateTicketPage | Authenticated |
| `/tickets/:id` | TicketDetailPage | Authenticated |
| `/admin/users` | AdminUsersPage | Admin |

### UI Features (mapped to spec)

| Feature | Implementation |
|---------|----------------|
| Default my-assignments | Assignee dropdown defaults to current user → `assigneeId` param |
| All assignees | Assignee dropdown "All assignees" → omit `assigneeId` |
| Assignee filter | Dropdown → `assigneeId` param |
| Status sections | Render each `sections[]` from API |
| Pagination | Per-section controls; shared `page` state |
| Keyword search | Debounced input → `q` param |
| Status transitions | Buttons shown per role; **backend is source of truth** |
| Assignee change | Dropdown on detail; any user |
| Errors | Map `ErrorResponse.code` via `errorMessages.ts` |

### State Management

- `AuthContext`: token, user, role, login/logout
- Page-level `useState` + custom hooks (`useTickets`, `useTicketDetail`)
- No global store required for v1 scope

### Loading / Empty States

| State | UI |
|-------|-----|
| Loading | `LoadingSpinner` while fetch in flight |
| Empty section | "No tickets in {status}" within section |
| Empty search | "No tickets match your search" |
| Error | `ErrorAlert` with friendly message |

---

## Frontend ↔ Backend Integration

```text
React (Vite :8091)
    ↓ fetch + Bearer JWT
Spring Boot REST (:8090/api)
    ↓ JPA
PostgreSQL (:5432)
```

### Error Handling Matrix

| Backend Response | Frontend Action |
|------------------|-------------------|
| 200/201 | Update UI state |
| 400 + `VALIDATION_ERROR` | Show `fieldErrors` on form |
| 400 + `INVALID_TRANSITION` | Toast/alert: friendly transition message |
| 401 | Redirect to `/login` |
| 403 | Show "You don't have permission..." |
| 404 | Show "Ticket not found" |
| 500 | Show generic error; log details to console |
| Network failure | Show "Unable to reach server" |

Error contract:
```json
{ "code": "...", "message": "...", "fieldErrors": [] }
```

---

## Testing Strategy

### Backend

| Layer | Tool | Scope |
|-------|------|-------|
| `TicketTransitionPolicyTest` | JUnit 5 | All V1–V6, I1–I7 transitions (no Spring) |
| `TicketCommandServiceTest` | JUnit + Mockito | Create, update, assignee |
| `TicketQueryServiceTest` | JUnit + @DataJpaTest | Search, filter, grouped pagination |
| `CommentServiceTest` | JUnit + Mockito | Add comment validation |
| `UserServiceTest` | JUnit + Mockito | Register, reset, role |
| `AuthControllerTest` | MockMvc | Login success/failure |
| `TicketControllerTest` | MockMvc | CRUD + transition endpoints |
| `SecurityIntegrationTest` | @SpringBootTest | Admin-only routes, JWT |
| `TicketTransitionIntegrationTest` | @SpringBootTest | Full state machine E2E via API |

### Frontend

| Area | Tool | Tests |
|------|------|-------|
| Login | Vitest + RTL | Form submit, error display |
| Ticket list | Vitest + RTL | Sections render, empty state, pagination |
| Search/filter | Vitest + RTL | Params sent to API mock |
| Create/edit | Vitest + RTL | Validation errors from API |
| Transitions | Vitest + RTL | Buttons visibility by role; error on 400/403 |
| Comments | Vitest + RTL | Add comment, empty rejection |
| API client | Vitest | Error parsing, 401 redirect |

---

## Build and Run

### Backend

| Command | Action |
|---------|--------|
| `./gradlew build` | Compile + all tests |
| `./gradlew test` | Run tests |
| `./gradlew bootRun` | Start API on :8090 |

### Frontend

| Command | Action |
|---------|--------|
| `npm install` | Install dependencies |
| `npm run build` | Production build |
| `npm test` | Vitest |
| `npm run dev` | Dev server :8091 |

### Local Development Flow

```text
PostgreSQL (or H2 dev profile)
       ↑
Spring Boot :8090  ←── CORS ──→  Vite :8091
```

Env vars: see [quickstart.md](./quickstart.md). Never commit `TMS_JWT_SECRET` or password hashes.

---

## Implementation Order

Dependency-aware sequence for `/speckit-tasks`:

| Phase | Step | Deliverable |
|-------|------|-------------|
| 1 | Repository scaffold | `backend/`, `frontend/` directories, Gradle, Vite |
| 2 | Backend foundation | `TmsApplication`, profiles, Flyway, health |
| 3 | Database schema | `V1__init_schema.sql` |
| 4 | Common infra | `TmsException`, `ErrorCode`, `GlobalExceptionHandler`, `TmsConfig` |
| 5 | Security | JWT, `AdminAuthenticationProvider`, `SecurityConfiguration` |
| 6 | Auth module | `AuthController`, login flow |
| 7 | User module | Admin user CRUD, password reset, roles |
| 8 | Ticket entities + repos | `Ticket`, `TicketRepository` |
| 9 | `TicketTransitionPolicy` + tests | All transition unit tests |
| 10 | `TicketTransitionService` + integration tests | API transition tests |
| 11 | `TicketCommandService` | Create, update, assignee |
| 12 | `TicketQueryService` | Grouped list, search, filter, pagination |
| 13 | `TicketController` | Wire REST endpoints |
| 14 | Comment module | Entity, service, controller |
| 15 | Springdoc OpenAPI | API docs at `/swagger-ui.html` |
| 16 | Frontend foundation | Vite, router, `apiClient`, types |
| 17 | Auth UI | Login, `AuthContext`, protected routes |
| 18 | Admin users UI | User management page |
| 19 | Ticket list UI | Grouped sections, search, filters, pagination |
| 20 | Ticket detail/create UI | Edit, assignee, transitions |
| 21 | Comments UI | List + add form |
| 22 | Error/loading states | Global error handling |
| 23 | Integration testing | Full quickstart scenarios (VS-1–VS-9) |
| 24 | E2E validation | Manual + automated per quickstart |

---

## Complexity Tracking

No constitution violations requiring justification.

---

## Generated Artifacts (Phase 0 + Phase 1)

| Artifact | Path | Status |
|----------|------|--------|
| Research | [research.md](./research.md) | ✅ |
| Data model | [data-model.md](./data-model.md) | ✅ |
| REST contract | [contracts/rest-api.md](./contracts/rest-api.md) | ✅ |
| Quickstart | [quickstart.md](./quickstart.md) | ✅ |
| Plan | [plan.md](./plan.md) | ✅ |

## Next Step

```
/speckit-tasks
```

Generate dependency-ordered `tasks.md` from this plan.

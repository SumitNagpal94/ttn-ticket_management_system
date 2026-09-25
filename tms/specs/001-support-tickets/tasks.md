---
description: "Task list for Support Ticket Management (001-support-tickets)"
---

# Tasks: Support Ticket Management

**Input**: Design documents from `/specs/001-support-tickets/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/rest-api.md, quickstart.md

**Tests**: Included — constitution Principle III (Test Coverage) and SC-006 (state-machine verification) require automated tests.

**Organization**: Tasks grouped by user story (US1–US6 from spec.md).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no incomplete-task dependencies)
- **[Story]**: US1–US6 maps to spec.md user stories
- Paths are relative to workspace root `tms/` (Spec Kit project directory)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize `backend/` and `frontend/` as independent modules

- [X] T001 Create `backend/` Gradle project with Java 21 and Spring Boot 3.x in `backend/build.gradle` and `backend/settings.gradle`
- [X] T002 Create `backend/src/main/java/com/tms/TmsApplication.java` entry point
- [X] T003 [P] Create `frontend/` Vite + React 18 + TypeScript project with `frontend/package.json` and `frontend/vite.config.ts` (Node >=18.19.0)
- [X] T004 [P] Add `backend/.gitignore` entries for `build/`, `.gradle/`, and `src/main/resources/application-local.yml`
- [X] T005 [P] Add `frontend/.gitignore` entries for `node_modules/`, `dist/`
- [X] T006 [P] Create root `README.md` documenting `backend/` and `frontend/` build commands per `quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure required before any user story

**⚠️ CRITICAL**: No user story work until this phase is complete

- [X] T007 Add Spring dependencies (web, data-jpa, security, validation, actuator, flyway, postgresql, h2, springdoc-openapi, jjwt) in `backend/build.gradle`
- [X] T008 Create `backend/src/main/resources/application.yml` with datasource `jdbc:postgresql://localhost:5432/support_ticket`, username `support_app`, password `${DB_PASSWORD}`
- [X] T009 Create `backend/src/main/resources/application-local.yml.example` template and document gitignored `application-local.yml` for DB password
- [X] T010 Create `backend/src/main/resources/application-dev.yml` H2 profile for tests
- [X] T011 Create Flyway migration `backend/src/main/resources/db/migration/V1__init_schema.sql` per `data-model.md` (users, tickets, comments tables, indexes, check constraints)
- [X] T012 [P] Create enums `TicketStatus`, `TicketPriority`, `UserRole` in `backend/src/main/java/com/tms/common/enums/`
- [X] T013 [P] Create `ErrorCode` enum in `backend/src/main/java/com/tms/common/enums/ErrorCode.java`
- [X] T014 [P] Create `TmsException` in `backend/src/main/java/com/tms/common/exception/TmsException.java`
- [X] T015 Create `GlobalExceptionHandler` in `backend/src/main/java/com/tms/common/exception/GlobalExceptionHandler.java` returning `{code, message, fieldErrors}`
- [X] T016 Create `TmsConfig` in `backend/src/main/java/com/tms/common/config/TmsConfig.java` for admin, JWT, and pagination settings
- [X] T017 [P] Create `User` JPA entity in `backend/src/main/java/com/tms/user/model/entity/User.java` (username UK, password_hash, display_name, email UK, role, timestamps)
- [X] T018 Create `UserRepository` in `backend/src/main/java/com/tms/user/repository/UserRepository.java`
- [X] T019 Create `JwtConfig` and `JwtAuthenticationFilter` in `backend/src/main/java/com/tms/common/security/`
- [X] T020 Create `AdminAuthenticationProvider` in `backend/src/main/java/com/tms/common/security/AdminAuthenticationProvider.java` matching BCrypt hash from `tms.admin.password-hash`
- [X] T021 Create `SecurityConfiguration` in `backend/src/main/java/com/tms/common/config/SecurityConfiguration.java` with JWT filter, CORS for `http://localhost:5173`, role rules
- [X] T022 Create `AuthService` in `backend/src/main/java/com/tms/auth/service/AuthService.java`
- [X] T023 Create `AuthController` in `backend/src/main/java/com/tms/auth/controller/AuthController.java` with `POST /api/auth/login` and `GET /api/auth/me`
- [X] T024 [P] Create `Ticket` entity in `backend/src/main/java/com/tms/ticket/model/entity/Ticket.java` (title max 200 NOT NULL, description NOT NULL, status default OPEN, priority default MEDIUM, assignee FK nullable, created_by FK NOT NULL)
- [X] T025 [P] Create `Comment` entity in `backend/src/main/java/com/tms/comment/model/entity/Comment.java` (body NOT NULL, ticket_id FK, author_id FK, created_at)
- [X] T026 [P] Create `TicketRepository` in `backend/src/main/java/com/tms/ticket/repository/TicketRepository.java`
- [X] T027 [P] Create `CommentRepository` in `backend/src/main/java/com/tms/comment/repository/CommentRepository.java`
- [X] T028 [P] Create shared DTOs `ErrorResponse`, `UserSummaryDto` in `backend/src/main/java/com/tms/common/dto/`
- [X] T029 [P] Create `frontend/src/types/api.ts` with `ErrorResponse`, `User`, `Ticket`, `GroupedTicketResponse` types
- [X] T030 [P] Create `frontend/src/services/apiClient.ts` with JWT header injection and error parsing
- [X] T031 Create `frontend/src/main.tsx` and `frontend/src/App.tsx` with React Router shell
- [X] T032 Configure Vite proxy or `VITE_API_BASE_URL=http://localhost:8080/api` in `frontend/.env.example`

**Checkpoint**: Backend starts, Flyway migrates, login endpoint reachable, frontend dev server runs

---

## Phase 3: User Story 1 — Admin Authentication & User Management (P1) 🎯 MVP

**Goal**: Admin logs in via properties credentials; admin page registers users, resets passwords, assigns roles (Developer, QA, User)

**Independent Test**: Admin login → create Developer user → reset password → new user can login (VS-1 in quickstart.md)

### Tests for User Story 1

- [X] T033 [P] [US1] Create `AdminUserControllerTest` MockMvc tests in `backend/src/test/java/com/tms/user/controller/AdminUserControllerTest.java` (403 for non-admin, 201 for create user)
- [X] T034 [P] [US1] Create `AuthControllerTest` in `backend/src/test/java/com/tms/auth/controller/AuthControllerTest.java` (admin login success/failure)

### Implementation for User Story 1

- [X] T035 [P] [US1] Create user DTOs `CreateUserRequest`, `ResetPasswordRequest`, `UserResponse` in `backend/src/main/java/com/tms/user/model/dto/`
- [X] T036 [US1] Implement `UserService` in `backend/src/main/java/com/tms/user/service/UserService.java` (register, reset password BCrypt, assign role; roles ADMIN/DEVELOPER/QA/USER)
- [X] T037 [US1] Implement `AdminUserController` in `backend/src/main/java/com/tms/user/controller/AdminUserController.java` per `contracts/rest-api.md` (`GET/POST /api/admin/users`, reset-password, role)
- [X] T038 [P] [US1] Create `frontend/src/auth/AuthContext.tsx` and `frontend/src/auth/useAuth.ts` for token and role state
- [X] T039 [P] [US1] Create `frontend/src/auth/LoginPage.tsx` with validation error display
- [X] T040 [P] [US1] Create `frontend/src/services/authApi.ts` (login, me)
- [X] T041 [US1] Create `frontend/src/users/AdminUsersPage.tsx` with user list, create form, reset password, role selector
- [X] T042 [US1] Create `frontend/src/services/userApi.ts` for admin user endpoints
- [X] T043 [US1] Add protected route wrapper and `/admin/users` route (admin-only) in `frontend/src/App.tsx`

**Checkpoint**: US1 complete — admin workflow end-to-end

---

## Phase 4: User Story 2 — Create and List Tickets (P1)

**Goal**: Authenticated users create tickets (default OPEN); grouped list defaults to my-assignments, 30/page per status section

**Independent Test**: Create ticket → appears in OPEN section of assigned view after refresh; survives restart (VS-2, VS-8)

### Tests for User Story 2

- [X] T044 [P] [US2] Create `TicketCommandServiceTest` in `backend/src/test/java/com/tms/ticket/service/TicketCommandServiceTest.java` (create sets OPEN and MEDIUM default)
- [X] T045 [P] [US2] Create `TicketQueryServiceTest` in `backend/src/test/java/com/tms/ticket/service/TicketQueryServiceTest.java` (view=assigned filter, 30/page per section)

### Implementation for User Story 2

- [X] T046 [P] [US2] Create ticket DTOs `CreateTicketRequest`, `TicketSummaryDto`, `GroupedTicketResponse` in `backend/src/main/java/com/tms/ticket/model/dto/`
- [X] T047 [US2] Implement `TicketCommandService.create()` in `backend/src/main/java/com/tms/ticket/service/TicketCommandService.java`
- [X] T048 [US2] Implement `TicketQueryService.findGrouped()` in `backend/src/main/java/com/tms/ticket/service/TicketQueryService.java` (five sections always, page/size=30, view=assigned default)
- [X] T049 [US2] Implement `TicketController` create and grouped list in `backend/src/main/java/com/tms/ticket/controller/TicketController.java` (`POST /api/tickets`, `GET /api/tickets/grouped`)
- [X] T050 [P] [US2] Create `frontend/src/services/ticketApi.ts` (create, grouped list)
- [X] T051 [P] [US2] Create `frontend/src/tickets/components/StatusSection.tsx` and `TicketCard.tsx`
- [X] T052 [US2] Create `frontend/src/tickets/pages/TicketListPage.tsx` with status sections and pagination controls
- [X] T053 [US2] Create `frontend/src/tickets/pages/CreateTicketPage.tsx` with title/description validation errors
- [X] T054 [US2] Add routes `/` and `/tickets/new` in `frontend/src/App.tsx`

**Checkpoint**: US2 complete — create and list with grouped sections

---

## Phase 5: User Story 3 — View, Update, and Reassign Tickets (P2)

**Goal**: View ticket details; update title, description, priority; any user may change assignee

**Independent Test**: Open ticket, update fields and assignee, verify persistence (VS-3)

### Tests for User Story 3

- [X] T055 [P] [US3] Create `TicketCommandServiceUpdateTest` in `backend/src/test/java/com/tms/ticket/service/TicketCommandServiceUpdateTest.java` (reject blank title, assignee any user)
- [X] T056 [P] [US3] Create `TicketControllerDetailTest` MockMvc in `backend/src/test/java/com/tms/ticket/controller/TicketControllerDetailTest.java`

### Implementation for User Story 3

- [X] T057 [P] [US3] Create `TicketDetailDto`, `UpdateTicketRequest` in `backend/src/main/java/com/tms/ticket/model/dto/`
- [X] T058 [US3] Implement `TicketQueryService.findById()` with comments in `backend/src/main/java/com/tms/ticket/service/TicketQueryService.java`
- [X] T059 [US3] Implement `TicketCommandService.update()` in `backend/src/main/java/com/tms/ticket/service/TicketCommandService.java` (title, description, priority, assigneeId; no status change)
- [X] T060 [US3] Add `GET /api/tickets/{id}` and `PATCH /api/tickets/{id}` to `backend/src/main/java/com/tms/ticket/controller/TicketController.java`
- [X] T061 [US3] Create `frontend/src/tickets/pages/TicketDetailPage.tsx` with edit form and assignee dropdown
- [X] T062 [US3] Create `frontend/src/tickets/hooks/useTicketDetail.ts`
- [X] T063 [US3] Add route `/tickets/:id` in `frontend/src/App.tsx`

**Checkpoint**: US3 complete — detail view and field updates

---

## Phase 6: User Story 4 — Role-Based Status Transitions (P2)

**Goal**: Developer/QA perform transitions; RESOLVED by Dev/QA; CLOSED by creator or QA only; all invalid transitions rejected

**Independent Test**: Run transition matrix from plan.md (V1–V6 valid, I1–I7 invalid)

### Tests for User Story 4 (SC-006 — write before implementation)

- [X] T064 [P] [US4] Create `TicketTransitionPolicyTest` unit tests in `backend/src/test/java/com/tms/ticket/service/TicketTransitionPolicyTest.java` covering all FR-018 transitions and role rules
- [X] T065 [US4] Create `TicketTransitionIntegrationTest` in `backend/src/test/java/com/tms/ticket/TicketTransitionIntegrationTest.java` with `@SpringBootTest` + MockMvc for V1–V6 and I1–I7

### Implementation for User Story 4

- [X] T066 [US4] Implement `TicketTransitionPolicy` in `backend/src/main/java/com/tms/ticket/service/TicketTransitionPolicy.java` (pure Java; no DB access)
- [X] T067 [US4] Implement `TicketTransitionService` in `backend/src/main/java/com/tms/ticket/service/TicketTransitionService.java` (`@Transactional`; load-evaluate-save)
- [X] T068 [US4] Create `TransitionRequest` DTO in `backend/src/main/java/com/tms/ticket/model/dto/TransitionRequest.java`
- [X] T069 [US4] Add `POST /api/tickets/{id}/transitions` to `backend/src/main/java/com/tms/ticket/controller/TicketController.java`
- [X] T070 [P] [US4] Create `frontend/src/tickets/components/TransitionButtons.tsx` showing allowed targets by role (UX only; backend enforces)
- [X] T071 [US4] Wire transition API in `frontend/src/services/ticketApi.ts` and integrate in `TicketDetailPage.tsx` with error messages for INVALID_TRANSITION and FORBIDDEN

**Checkpoint**: US4 complete — all state machine tests pass (`./gradlew test --tests '*Transition*'`)

---

## Phase 7: User Story 5 — Add Comments (P3)

**Goal**: Authenticated users add comments; chronological display on detail view

**Independent Test**: Add comment → appears with author/timestamp; empty rejected (VS-5 partial)

### Tests for User Story 5

- [X] T072 [P] [US5] Create `CommentServiceTest` in `backend/src/test/java/com/tms/comment/service/CommentServiceTest.java` (reject empty body)
- [X] T073 [P] [US5] Create `CommentControllerTest` MockMvc in `backend/src/test/java/com/tms/comment/controller/CommentControllerTest.java`

### Implementation for User Story 5

- [X] T074 [P] [US5] Create `CreateCommentRequest`, `CommentDto` in `backend/src/main/java/com/tms/comment/model/dto/`
- [X] T075 [US5] Implement `CommentService` in `backend/src/main/java/com/tms/comment/service/CommentService.java`
- [X] T076 [US5] Implement `CommentController` in `backend/src/main/java/com/tms/comment/controller/CommentController.java` (`POST /api/tickets/{id}/comments`)
- [X] T077 [P] [US5] Create `frontend/src/comments/CommentList.tsx` and `CommentForm.tsx`
- [X] T078 [US5] Integrate comments into `frontend/src/tickets/pages/TicketDetailPage.tsx`

**Checkpoint**: US5 complete — comments on ticket detail

---

## Phase 8: User Story 6 — Search, Filter, and Browse (P3)

**Goal**: Keyword search title/description; filter by assignee; ALL view; combined filters

**Independent Test**: VS-6 scenarios — search, assignee filter, ALL option, empty state

### Tests for User Story 6

- [X] T079 [P] [US6] Extend `TicketQueryServiceTest` in `backend/src/test/java/com/tms/ticket/service/TicketQueryServiceTest.java` for ILIKE search (title+description only, not comments) and assigneeId filter
- [X] T080 [P] [US6] Create `AssigneeControllerTest` in `backend/src/test/java/com/tms/user/controller/AssigneeControllerTest.java` for `GET /api/users/assignees`

### Implementation for User Story 6

- [X] T081 [US6] Extend `TicketQueryService.findGrouped()` with `q` (ILIKE title/description) and `assigneeId` params in `backend/src/main/java/com/tms/ticket/service/TicketQueryService.java`
- [X] T082 [US6] Add `GET /api/users/assignees` endpoint in `backend/src/main/java/com/tms/user/controller/AssigneeController.java`
- [X] T083 [P] [US6] Create `frontend/src/tickets/components/SearchBar.tsx` and `AssigneeFilter.tsx`
- [X] T084 [US6] Wire assignee filter (default current user, All assignees option) in `frontend/src/tickets/pages/TicketListPage.tsx`
- [X] T085 [US6] Create `frontend/src/components/EmptyState.tsx` for no-results display

**Checkpoint**: US6 complete — search and filter fully functional

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, observability, validation, E2E

- [X] T086 [P] Configure Springdoc OpenAPI in `backend/src/main/java/com/tms/common/config/OpenApiConfig.java`
- [X] T087 [P] Create `frontend/src/utils/errorMessages.ts` mapping `ErrorCode` to user-friendly strings
- [X] T088 [P] Create `frontend/src/components/LoadingSpinner.tsx` and `ErrorAlert.tsx`
- [X] T089 Add structured transition logging in `TicketTransitionService` (ticket id, from, to, actor)
- [X] T090 [P] Create frontend Vitest tests for `apiClient` error handling in `frontend/src/services/apiClient.test.ts`
- [X] T091 [P] Create frontend tests for `LoginPage` in `frontend/src/auth/LoginPage.test.tsx`
- [X] T092 Run all quickstart validation scenarios VS-1 through VS-9 documented in `specs/001-support-tickets/quickstart.md`
- [X] T093 Update `docs/prompt-history.md` with implementation session notes
- [X] T094 Verify no secrets in committed files (`application-local.yml`, passwords, JWT secret)

---

## Dependencies & Execution Order

### Phase Dependencies

```text
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) — BLOCKS all user stories
    ↓
Phase 3 (US1) ──┐
Phase 4 (US2) ──┼── US2 depends on auth from US1 for full UI flow; backend US2 can start after Phase 2
Phase 5 (US3) ──┤── depends on US2 ticket existence
Phase 6 (US4) ──┤── depends on US3 detail endpoint
Phase 7 (US5) ──┤── depends on US3 detail page
Phase 8 (US6) ──┘── extends US2 list; can parallel US5 after US2
    ↓
Phase 9 (Polish)
```

### User Story Dependencies

| Story | Depends On | Independent Test |
|-------|------------|------------------|
| US1 | Phase 2 | Admin login + user CRUD |
| US2 | Phase 2 (+ auth for UI) | Create ticket + grouped list |
| US3 | US2 (ticket exists) | Detail + update + assignee |
| US4 | US3 (detail + ticket) | State machine matrix |
| US5 | US3 (detail page) | Add comment |
| US6 | US2 (list page) | Search/filter/ALL |

### Parallel Opportunities

- **Phase 1**: T003, T004, T005, T006 parallel after T001–T002
- **Phase 2**: T012–T013, T017, T024–T025, T029–T030 parallel once T007–T011 done
- **US4 tests**: T064 parallel with T066 policy implementation prep
- **Frontend/Backend**: Different stories can split by team after Phase 2

### Parallel Example: User Story 4

```bash
# Tests first (must fail):
backend/src/test/java/com/tms/ticket/service/TicketTransitionPolicyTest.java
backend/src/test/java/com/tms/ticket/TicketTransitionIntegrationTest.java

# Then implementation:
backend/src/main/java/com/tms/ticket/service/TicketTransitionPolicy.java
backend/src/main/java/com/tms/ticket/service/TicketTransitionService.java
```

---

## Implementation Strategy

### MVP First (US1 + US2)

1. Complete Phase 1–2 (foundation)
2. Complete Phase 3 (US1 — admin + auth UI)
3. Complete Phase 4 (US2 — create + list)
4. **STOP and VALIDATE** quickstart VS-1, VS-2
5. Demo MVP

### Incremental Delivery

| Increment | Stories | Value |
|-----------|---------|-------|
| MVP | US1 + US2 | Auth, admin, create/list tickets |
| v0.2 | US3 | Edit and assign |
| v0.3 | US4 | State machine (critical business rules) |
| v0.4 | US5 + US6 | Comments + search |

### Suggested MVP Scope

**Minimum**: Phase 1 + Phase 2 + Phase 3 (US1) + Phase 4 (US2)  
**Production-ready**: All phases through US4 (state machine tested)

---

## Notes

- Commit `spec.md`, `plan.md`, `tasks.md` changes per constitution prompt-history rules
- Password: `support_app` DB password only in `application-local.yml` (gitignored)
- Do not merge `TicketTransitionPolicy` into `TicketCommandService` (architecture ADR)
- Comment body excluded from search (FR-011)
- Total tasks: **94** (T001–T094)

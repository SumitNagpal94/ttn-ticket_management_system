# Prompt History

Append-only log of every AI prompt and outcome for the Ticket Management System.
Supports requirement traceability, decision auditing, and session continuity.

## Entry Format

Each entry MUST include:

| Field | Description |
|-------|-------------|
| **Timestamp** | ISO 8601 with timezone (e.g., `2026-09-13T19:54:00+05:30`) |
| **Branch** | Active Git branch (or `main` if none) |
| **Feature** | `specs/<NNN-feature-name>/` if applicable, else `—` |
| **Agent** | `cursor` or `copilot` |
| **Prompt** | Verbatim user message (redact secrets) |
| **Outcome** | Brief summary of files changed or decisions made |

New entries are appended to the bottom. Commit `docs/prompt-history.md` with every
prompt record (same branch as related work).

---

## 2026-09-13T19:19:00+05:30

**Branch**: `main`
**Feature**: `—`
**Agent**: `cursor`

**Prompt**:

> Optimise and update constitution.md and rules for Ticket Management System.
> Stack: Java 21, Spring Boot, PostgreSQL/H2, REST API, React, Cursor, GitHub Copilot.
> Include coding standards, Spring Boot conventions, Effective Java alignment, AI developer
> profile, and overall guidelines (file references, exception patterns, layer rules).
> Step 3.1: Revise overall guidelines to align with coding style, project structure,
> and file references.

**Outcome**:

- Created `.specify/memory/constitution.md` v1.0.0 with 5 core principles
- Mapped TTN references to TMS (`com.tms`, `TmsException`, `TmsConfig`)
- Created `.cursor/rules/tms-core.mdc`, `java-spring-boot.mdc`, `react-frontend.mdc`

---

## 2026-09-13T19:30:00+05:30

**Branch**: `main`
**Feature**: `—`
**Agent**: `cursor`

**Prompt**:

> Add Spec Kit workflow governance: change tracking via Git commits, branch-based
> development with spec artifacts co-located with code, and PR workflows including
> spec.md, plan.md, tasks.md. Each feature gets its own branch and specs/ directory.
> Reference Microsoft Spec Kit enterprise training modules.

**Outcome**:

- Updated constitution to v1.1.0 — added Principle VI (Spec-Driven Development)
- Added Feature Branch & Artifact Workflow section with `specs/<NNN-feature>/` layout
- Created `.cursor/rules/spec-driven-development.mdc`
- Created `.github/pull_request_template.md`

---

## 2026-09-13T19:54:00+05:30

**Branch**: `main`
**Feature**: `—`
**Agent**: `cursor`

**Prompt**:

> Record every prompt to `docs/prompt-history.md` and maintain it as part of project governance.

**Outcome**:

- Created `docs/prompt-history.md` with entry format and backfilled prior prompts
- Updated constitution to v1.2.0 with Prompt History requirements
- Updated `.cursor/rules/spec-driven-development.mdc` and PR template

---

## 2026-09-13T19:59:00+05:30

**Branch**: `main`
**Feature**: `—`
**Agent**: `cursor`

**Prompt**:

> /speckit-clarify — Build a Support Ticket Management System with CRUD, comments,
> search, status filter, state machine (OPEN → IN_PROGRESS → RESOLVED → CLOSED,
> OPEN/IN_PROGRESS → CANCELLED), backend validation, UI errors, DB persistence.
> Custom spec layout: spec/requirements.md, architecture.md, data-model.md, etc.

**Outcome**:

- Clarify blocked: no active feature or `spec.md` found (prerequisites check failed)
- Advised running `/speckit-specify` first; flagged `spec/` vs `specs/<feature>/` layout conflict

---

## 2026-09-13T20:01:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> yes (proceed with /speckit-specify for Support Ticket Management System)

**Outcome**:

- Created `specs/001-support-tickets/spec.md` with 5 user stories, 17 FRs, state machine
- Created `specs/001-support-tickets/checklists/requirements.md` (16/16 passing)
- Feature registered as `001-support-tickets` in `.specify/feature.json`

---

## 2026-09-13T20:03:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> it is git repo

**Outcome**:

- Confirmed Git root is `/home/sumit-nagpal/Documents/learning/tms` (parent of workspace)
- Created and switched to branch `001-support-tickets`

---

## 2026-09-13T21:04:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> /speckit-clarify — Admin creds encoded in app properties; admin user management page;
> anyone changes assignee; Developer/QA do transitions; RESOLVED by Dev/QA; CLOSED by
> creator or QA; search title/description only; assignee filter; default my-assignments
> view with ALL option; 30 per page grouped by status.

**Outcome**:

- Integrated 10 clarifications into spec.md (Clarifications session 2026-09-13)
- Added roles/permissions matrix, admin user story, updated FRs FR-001–FR-027
- Checklist remains 16/16 passing

---

## 2026-09-13T21:24:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> Architecture analysis for Plan stage: analyze spec.md, design 5 architectural
> approaches, SWOT/trade-off/risk/failure/evolution analysis, comparison table,
> second-pass review, final recommendation, and ADR. Stack from spec/constitution.
> No implementation tasks yet.

**Outcome**:

- Created `specs/001-support-tickets/architecture-analysis.md` (Steps 1–8)
- Recommended: Modular Layered Monolith with TicketTransitionPolicy/Service
- Rejected: Spring State Machine, CQRS-lite, PG FTS for v1 scale

---

## 2026-09-13T21:46:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> /speckit-plan — Create implementation plan from spec.md and architecture-analysis.md.
> Separate backend/frontend modules, Modular Layered Monolith, full API contract,
> data model, state machine plan, frontend plan, testing, build/run, implementation order.

**Outcome**:

- Created plan.md, research.md, data-model.md, contracts/rest-api.md, quickstart.md
- Resolved status-grouped pagination (per-section, shared page index, 30/page)
- Gradle backend (constitution); React 18 + Vite frontend

---

## 2026-09-13T22:09:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> Shared local toolchain versions: PostgreSQL 16.15, Java 21.0.12, Node v18.19.1

**Outcome**:

- Updated quickstart.md, research.md (R11), plan.md with verified environment versions
- Confirmed compatibility; Node prerequisite lowered to 18 LTS

---

## 2026-09-13T22:22:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> PostgreSQL: localhost:5432, user support_app, database support_ticket;
> password to be added later in application properties.

**Outcome**:

- Updated quickstart.md, plan.md, research.md (R12) with datasource settings
- Documented gitignored application-local.yml pattern for password

---

## 2026-09-13T22:23:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> /speckit-tasks

**Outcome**:

- Generated `specs/001-support-tickets/tasks.md` with 94 tasks (T001–T094)
- 9 phases: Setup, Foundational, US1–US6, Polish
- Test tasks included for SC-006 state machine and constitution coverage

---

## 2026-09-13T22:47:00+05:30

**Branch**: `001-support-tickets`
**Feature**: `specs/001-support-tickets/`
**Agent**: `cursor`

**Prompt**:

> /speckit-implement (continue on feature branch; specification-first workflow)

**Outcome**:

- Scaffolded `backend/` (Spring Boot 3, Gradle wrapper, Flyway V1 schema, JWT auth, full REST API)
- Scaffolded `frontend/` (Vite + React + TypeScript, auth, admin users, tickets, comments, filters)
- Fixed `TicketTransitionPolicy` so ticket creators (USER role) can close RESOLVED tickets
- Added JSON 401/403 handlers in `SecurityConfiguration`
- Backend: 16 tests passing (`TicketTransitionPolicyTest`, auth/admin MockMvc, transition integration)
- Frontend: build + Vitest tests passing
- Created root `README.md`; updated `tasks.md` (82/94 complete — remaining: service-level unit tests T044–T045, T055–T056, T072–T073, T079–T080, quickstart VS-1–VS-9 manual run T092, secrets audit T094)

---

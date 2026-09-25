# Research: Support Ticket Management (001-support-tickets)

**Date**: 2026-09-13  
**Status**: Complete — all Technical Context items resolved

## R1: Build tool — Gradle vs Maven

**Decision**: Gradle (`backend/build.gradle`)  
**Rationale**: Project constitution mandates Gradle. User plan template mentioned `pom.xml`; constitution takes precedence for this repository.  
**Alternatives considered**: Maven — rejected to align with constitution.

## R2: Status-grouped pagination API shape (U5 from architecture analysis)

**Decision**: Single endpoint `GET /api/tickets/grouped` returns five fixed sections (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED). Each section paginates independently at 30 items/page using a shared `page` query parameter (same page index applied to every section).  
**Rationale**: Satisfies FR-016 (30/page), FR-017 (status sections), and FR-014–015 (assignee filter + ALL). Keeps one round-trip for the list UI.  
**Alternatives considered**:
- Global pagination across all tickets — breaks section grouping UX.
- Five separate endpoints per status — more chatty, harder to render one screen.

## R3: Admin authentication model

**Decision**: `AdminAuthenticationProvider` validates admin username/password against BCrypt hash in `application.yml` (`tms.admin.username`, `tms.admin.password-hash`). DB users use `DaoAuthenticationProvider`. Admin role inferred from successful admin-provider login.  
**Rationale**: FR-001 requires encoded credentials in application properties; separates bootstrap admin from DB users.  
**Alternatives considered**: Admin as DB user with seed migration — rejected; contradicts FR-001 explicit properties requirement.

## R4: Keyword search implementation

**Decision**: PostgreSQL `ILIKE '%keyword%'` on `title` and `description` with B-tree indexes; no FTS in v1.  
**Rationale**: SC-003 targets ≤1,000 tickets; architecture ADR defers FTS.  
**Alternatives considered**: PostgreSQL `tsvector` — rejected for v1 per architecture analysis S5.

## R5: Frontend stack versions

**Decision**: React 18, TypeScript 5, Vite 5, React Router 6, fetch-based API client (no Redux; React Context for auth state).  
**Rationale**: Constitution requires React SPA; Vite is standard for modern React tooling. Spec does not mandate state library.  
**Alternatives considered**: Create React App (deprecated), Next.js (SSR unnecessary).

## R12: PostgreSQL database naming

**Decision**: Database `support_ticket`, user `support_app`, host `localhost:5432`.  
**Rationale**: Matches developer's provisioned PostgreSQL instance.  
**Password**: Stored in gitignored `application-local.yml` or `DB_PASSWORD` env var — never committed to Git (FR-027, constitution).

## R11: Verified local toolchain

**Decision**: Target development environment matches developer machine: Java 21.0.12, Node 18.19.1, PostgreSQL 16.15.  
**Rationale**: Vite 5 and React 18 support Node 18 LTS; Spring Boot 3.x supports Java 21 and PostgreSQL 16. No version bumps required.  
**Alternatives considered**: Require Node 20+ — unnecessary given verified Node 18.19.1.

## R6: Session / auth token strategy

**Decision**: Stateless JWT issued on login (`POST /api/auth/login`); stored in memory + `sessionStorage`; sent as `Authorization: Bearer`.  
**Rationale**: Separates frontend/backend cleanly; Spring Security JWT filter validates on each request.  
**Alternatives considered**: Server-side sessions — acceptable but adds session store; JWT simpler for SPA.

## R7: Concurrent updates

**Decision**: Last-write-wins; no optimistic locking column in v1.  
**Rationale**: Explicit in spec edge cases.  
**Alternatives considered**: `@Version` column — out of scope unless spec changes.

## R8: Default list view (my assignments)

**Decision**: UI defaults assignee dropdown to current user (`assigneeId` query param). "All assignees" omits the param. Unassigned tickets appear only when no assignee filter is applied.  
**Rationale**: Matches spec assumptions section; single assignee control replaces separate view toggle.

## R9: CORS and local dev

**Decision**: Spring Security CORS allows `http://localhost:8091` (Vite dev server) in `dev` profile. Frontend proxies optional via Vite `server.proxy`.  
**Rationale**: Constitution requires explicit CORS; standard Vite port.

## R10: Database migrations

**Decision**: Flyway migrations in `backend/src/main/resources/db/migration/`.  
**Rationale**: Constitution lists Flyway or Liquibase; Flyway is common with Spring Boot.

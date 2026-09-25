<!--
Sync Impact Report
- Version change: 1.1.0 → 1.2.0
- Modified principles: Expanded Principle VI with prompt history requirement
- Added sections: Prompt History (docs/prompt-history.md)
- Removed sections: None
- Deferred TODOs: None
-->

# Ticket Management System (TMS) Constitution

## Core Principles

### I. Layered Architecture

All backend code MUST follow a strict layered structure: controllers → services →
repositories → entities. Controllers handle HTTP concerns only; business logic
belongs exclusively in service classes. Repositories handle persistence; entities
and DTOs carry data. Constructor injection is mandatory; field injection is
prohibited. Rationale: separation of concerns enables testability and maintainability.

### II. REST API First

All external integrations MUST use RESTful APIs with proper HTTP methods, status
codes, and resource-oriented URLs. Request and response bodies MUST be validated
using Bean Validation (`@Valid`). API contracts MUST be documented with Springdoc
OpenAPI. Global exception handling MUST use `@ControllerAdvice` and
`@ExceptionHandler`, routing domain errors through `TmsException` and `ErrorCode`.
Rationale: consistent APIs reduce integration friction and support parallel frontend
development.

### III. Test Coverage (NON-NEGOTIABLE)

Every feature MUST include tests before merge. Unit tests use JUnit 5; web layer
tests use MockMvc; integration tests use `@SpringBootTest`; repository tests use
`@DataJpaTest`. Tests MUST be independent, repeatable, and cover happy paths plus
meaningful edge cases. Rationale: a ticket system handles state transitions and
authorization—untested code risks data corruption and security gaps.

### IV. Security by Default

Authentication and authorization MUST use Spring Security. Passwords MUST be
encoded with BCrypt. CORS MUST be explicitly configured, never left permissive in
production. All endpoints MUST declare required roles or remain explicitly public.
Secrets MUST never appear in source code, logs, or configuration committed to Git.
Rationale: ticket data is sensitive; OWASP-aligned defaults prevent common exploits.

### V. Simplicity and Maintainability

Follow SOLID, DRY, KISS, and YAGNI. Prefer immutable objects and pure functions
where practical. Methods MUST NOT exceed 50 lines—decompose into focused helpers.
Use Java 21 features (records, sealed classes, pattern matching) when they clarify
intent. Add dependencies, abstractions, or infrastructure only when a concrete need
exists. Rationale: premature complexity slows delivery and increases defect surface.

### VI. Spec-Driven Development (NON-NEGOTIABLE)

Every feature MUST be developed through GitHub Spec Kit artifacts before and
alongside implementation. Requirements live in version-controlled markdown, not
only in chat or issue comments. Each feature branch MUST contain its own
`spec.md`, `plan.md`, and `tasks.md` under `specs/<feature-id>/`, co-located with
the code that implements it. Rationale: persistent artifacts maintain context
across AI sessions, enable requirement traceability, and let reviewers verify
that implementation matches intent.

## Feature Branch & Artifact Workflow

### Project Structure

```
tms/
├── .specify/
│   ├── memory/constitution.md    # Project-wide governance (this file)
│   ├── templates/                # Templates for spec, plan, tasks
│   └── scripts/                  # Spec Kit automation scripts
├── docs/
│   └── prompt-history.md         # Append-only log of every AI prompt
├── specs/
│   └── <NNN-feature-short-name>/ # One directory per feature
│       ├── spec.md               # What to build (requirements)
│       ├── plan.md               # How to build (architecture)
│       └── tasks.md              # Ordered implementation steps
└── src/                          # Application code (backend + frontend)
```

Feature directories use sequential numbering (`001`, `002`, `003`) matching the
Git branch name (e.g., branch `001-ticket-crud` → `specs/001-ticket-crud/`).
Spec Kit infers the active feature from the branch name or `SPECIFY_FEATURE`
environment variable.

### Change Tracking

Every modification to `spec.md`, `plan.md`, or `tasks.md` MUST be committed to
Git with a descriptive message. This creates an auditable history of requirement
changes—reviewers can see why decisions were made and revert problematic changes.
When requirements change mid-development:

1. Update `spec.md` first
2. Regenerate or update `plan.md` to reflect architectural impact
3. Regenerate or update `tasks.md` with revised steps
4. Then update implementation code and tests

Never patch code without updating the corresponding specification artifacts.

### Prompt History

Every AI prompt (Cursor, GitHub Copilot, or other assistants) MUST be recorded in
`docs/prompt-history.md` before or during the agent's response. This file is
append-only and version-controlled alongside spec artifacts and code.

Each entry MUST include:

| Field | Required | Example |
|-------|----------|---------|
| Timestamp | Yes | `2026-09-13T19:54:00+05:30` |
| Branch | Yes | `001-ticket-crud` or `main` |
| Feature | If applicable | `specs/001-ticket-crud/` |
| Agent | Yes | `cursor`, `copilot` |
| Prompt | Yes | Verbatim user message (redact secrets) |
| Outcome | Yes | Files changed, decisions made |

Commit `docs/prompt-history.md` on the same branch as the work it describes.
Never record API keys, passwords, tokens, or other secrets in prompt history.

### Branch-Based Development

1. Create a feature branch from `main` using Spec Kit (`create-new-feature.sh`
   or `/speckit.specify`)
2. Branch name and `specs/` directory MUST match (e.g., `002-user-auth`)
3. Develop spec → plan → tasks → implementation on the same branch
4. Keep requirements and implementation synchronized throughout development
5. Never commit spec artifacts for one feature on another feature's branch

### Pull Request Requirements

Every feature PR MUST include:

| Artifact | Purpose |
|----------|---------|
| `specs/<feature>/spec.md` | Requirements the PR satisfies |
| `specs/<feature>/plan.md` | Architectural approach taken |
| `specs/<feature>/tasks.md` | Tasks completed (checkboxes marked) |
| Source code | Implementation |
| Tests | Verification of acceptance criteria |

Reviewers MUST verify:

- Implementation matches `spec.md` acceptance criteria
- Architecture follows `plan.md`
- All completed tasks in `tasks.md` are reflected in the code
- Changes align with this constitution

PR descriptions MUST link to the feature spec directory and list which
acceptance criteria are addressed.

### Spec Kit Command Workflow

| Phase | Command | Output |
|-------|---------|--------|
| Constitution | `/speckit.constitution` | `.specify/memory/constitution.md` |
| Specify | `/speckit.specify` | `specs/<feature>/spec.md` |
| Clarify | `/speckit.clarify` | Updated `spec.md` |
| Plan | `/speckit.plan` | `specs/<feature>/plan.md` |
| Analyze | `/speckit.analyze` | Cross-artifact consistency report |
| Tasks | `/speckit.tasks` | `specs/<feature>/tasks.md` |
| Implement | `/speckit.implement` | Code + tests per `tasks.md` |

Run `/speckit.analyze` after plan and tasks generation, before implementation.
If requirements change after implementation begins, return to the appropriate
phase and propagate updates downstream.

## Technical Stack

### Required

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build | Gradle (`build.gradle`) |
| Database | PostgreSQL (production), H2 (local/test) |
| ORM | Spring Data JPA |
| Migrations | Flyway or Liquibase |
| API | REST (JSON) |
| API Docs | Springdoc OpenAPI |
| Security | Spring Security |
| Logging | SLF4J + Logback |
| Monitoring | Spring Boot Actuator |
| Frontend | React (or equivalent SPA) |
| VCS & AI | Git, Cursor, GitHub Copilot |

### Optional (Adopt When Needed)

| Concern | Technology |
|---------|------------|
| Caching | Spring Cache (Redis or Aerospike) |
| Messaging | Apache Kafka |
| Metrics | Prometheus |
| Containers | Docker |

### Package Layout

```
src/main/java/com/tms/
├── TmsApplication.java
├── common/
│   ├── config/          # SecurityConfiguration, CommonConfiguration, TmsConfig
│   ├── enums/           # ErrorCode
│   ├── exception/       # TmsException
│   └── util/            # AppUtil, SecurityUtil, Util
└── <domain>/            # e.g., ticket, user, assignment
    ├── controller/
    ├── service/
    ├── repository/
    └── model/
        ├── entity/
        └── dto/
```

## Development Standards

### Code Style

- **Classes**: PascalCase (`TicketController`, `TicketService`)
- **Methods/variables**: camelCase (`findTicketById`, `isAssigned`)
- **Constants**: ALL_CAPS (`MAX_PAGE_SIZE`, `DEFAULT_STATUS`)
- **Lombok**: permitted on DTOs only; avoid on entities and services

### Spring Boot Conventions

- Use Spring Boot starters and auto-configuration
- Annotate layers: `@RestController`, `@Service`, `@Repository`
- Type-safe config via `@ConfigurationProperties` in `TmsConfig`
- Environment profiles for dev, test, and prod (`application.yml`)
- Constructor injection for all dependencies
- Async work via `@Async`; caching via Spring Cache when justified

### Data Access

- Define entity relationships and cascading explicitly
- Index columns used in frequent queries
- Return empty collections, never `null`
- Use `Optional` judiciously in service/repository APIs

### Effective Java Alignment

Apply Joshua Bloch's *Effective Java* guidance, especially:

- Prefer dependency injection over hardwiring
- Minimize mutability; favor composition over inheritance
- Use enums instead of int constants
- Prefer lambdas and streams judiciously (side-effect-free)
- Validate parameters; throw `TmsException` with appropriate `ErrorCode`
- Use try-with-resources; avoid unnecessary object creation
- Document exceptions on public API methods

### Functional and Data-Oriented Practices

- Keep service methods stateless; avoid mutable shared state
- Separate data structures from behavior where it aids clarity
- Make data transformations explicit and traceable
- Maintain unidirectional data flow between layers

## Overall Guidelines

These file references are authoritative. Search existing utilities before creating new ones.

| Concern | Location |
|---------|----------|
| Dependencies | `build.gradle` |
| Application properties | `src/main/resources/application.yml` |
| Type-safe configuration | `com.tms.common.config.TmsConfig` |
| Security configuration | `com.tms.common.config.SecurityConfiguration`, `CommonConfiguration` |
| API request helpers | `com.tms.common.util.AppUtil` |
| Security helpers | `com.tms.common.util.SecurityUtil` |
| Shared utilities | `com.tms.common.util.Util` (search before adding) |
| Domain exceptions | `com.tms.common.exception.TmsException` |
| Error codes | `com.tms.common.enums.ErrorCode` |
| Prompt history | `docs/prompt-history.md` |

### Exception Pattern

```java
throw new TmsException(ErrorCode.TICKET_NOT_FOUND);
```

### Layer Rules

- Controllers: HTTP mapping, validation, response mapping only
- Services: all business logic, transaction boundaries
- Repositories: persistence queries only
- DTOs: API contracts; entities: persistence model—never expose entities directly

### Frontend (React)

- Functional components with hooks
- Colocate API calls in service modules mirroring backend resources
- Handle loading, error, and empty states for every async operation
- Environment-specific API base URLs via build-time config

## AI Developer Profile

**Role**: Senior Java / Spring Boot Developer

**Guiding principles**: SOLID, DRY, KISS, YAGNI, OWASP, DDD

**Behavior**:

- Record every user prompt to `docs/prompt-history.md` before completing the response
- Read `build.gradle` and `application.yml` before adding dependencies or properties
- Reuse `com.tms.common.*` utilities and patterns—do not duplicate
- Propose the smallest change that satisfies the requirement
- Flag security, performance, or migration impacts in PR descriptions

## Governance

This constitution supersedes ad-hoc conventions. All pull requests MUST verify
compliance with these principles—including spec-driven workflow requirements.
Amendments require:

1. Documented rationale in a commit or PR description
2. Version bump per semantic versioning below
3. Update of `.cursor/rules/` if overall guidelines change

**Complexity justification**: Any deviation from YAGNI (new dependency, new
infrastructure service, or abstraction layer) MUST be explained in the plan or PR.

**Specification ownership**: Each feature's `spec.md` is the authoritative
requirement source for that feature. Disputes between code and spec are resolved
by updating the spec first, then the code.

### Versioning Policy

| Bump | When |
|------|------|
| MAJOR | Principle removed or redefined incompatibly |
| MINOR | New principle or materially expanded guidance |
| PATCH | Clarifications, typo fixes, non-semantic edits |

**Version**: 1.2.0 | **Ratified**: 2026-09-13 | **Last Amended**: 2026-09-13

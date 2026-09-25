# Feature Specification: Support Ticket Management

**Feature Branch**: `001-support-tickets`

**Created**: 2026-09-13

**Status**: Draft

**Input**: User description: "Build a Support Ticket Management System with ticket CRUD, comments, keyword search, status filtering, enforced status state machine, backend validation, meaningful UI errors, and database persistence."

## Clarifications

### Session 2026-09-13

- Q: How are admin credentials stored and validated at login? → A: Encoded form in application properties; login matches against encoded value.
- Q: How are users provisioned and roles assigned? → A: Admin has a separate page to register users, reset passwords, and assign roles (Developer, QA, or default User).
- Q: Who can change ticket assignee? → A: Any authenticated user.
- Q: Who can perform status transitions? → A: Developer and QA roles only (with additional close restrictions below).
- Q: Who can mark a ticket RESOLVED? → A: Developer or QA.
- Q: Who can mark a ticket CLOSED? → A: Ticket creator or QA only (when transitioning from RESOLVED).
- Q: What fields does keyword search cover? → A: Title and description only; comments excluded in v1.
- Q: What filters are available beyond status? → A: Assignee name filter plus an ALL option to view every ticket.
- Q: What is the default ticket list view? → A: Assignee filter defaults to current user; "All assignees" shows every ticket.
- Q: How are tickets paginated and displayed? → A: 30 tickets per page, grouped under status sections.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Admin Authentication and User Management (Priority: P1)

An administrator logs in using credentials stored in encoded form in application
properties. The admin accesses a dedicated management page to register new users,
reset user passwords, and assign roles (Developer, QA, or User).

**Why this priority**: User provisioning and role assignment underpin all
authorization rules for ticket transitions.

**Independent Test**: Admin logs in, creates a user with Developer role, resets
another user's password, and verifies the new credentials work.

**Acceptance Scenarios**:

1. **Given** encoded admin credentials in application properties, **When** admin
   submits matching username and password at login, **Then** authentication succeeds.
2. **Given** admin is authenticated, **When** admin opens the user management page,
   **Then** admin can register a new user with a chosen role.
3. **Given** an existing user, **When** admin resets their password, **Then** the
   user can log in with the new password.
4. **Given** a non-admin user, **When** they attempt to access the user management
   page, **Then** access is denied with a meaningful error.

---

### User Story 2 - Create and List Tickets (Priority: P1)

An authenticated user creates a ticket with title and description (default status
OPEN). The ticket list defaults to tickets assigned to the current user, grouped
by status sections, with 30 tickets per page.

**Why this priority**: Core ticket workflow; default view focuses each user on their
assigned work.

**Independent Test**: Create a ticket, verify it appears under the OPEN status
section in the default (my-assignments) view after refresh and application restart.

**Acceptance Scenarios**:

1. **Given** a user on the create-ticket form, **When** they submit a valid title
   and description, **Then** a new ticket is created with status OPEN.
2. **Given** tickets assigned to the current user, **When** they open the ticket
   list (default view), **Then** only their assigned tickets are shown, grouped
   under status sections (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED).
3. **Given** more than 30 assigned tickets in one status section, **When** the user
   views that section, **Then** pagination shows 30 tickets per page.
4. **Given** a ticket was created, **When** the application restarts, **Then**
   the ticket persists.

---

### User Story 3 - View, Update, and Reassign Tickets (Priority: P2)

A user opens ticket details and updates title, description, and priority. Any
authenticated user may change the assignee on any ticket.

**Why this priority**: Ticket maintenance and flexible assignment are daily
operations once tickets exist.

**Independent Test**: Open a ticket, update fields and assignee, verify changes
persist on reload.

**Acceptance Scenarios**:

1. **Given** an existing ticket, **When** a user opens its detail view, **Then**
   title, description, status, priority, assignee, creator, and timestamps are
   displayed.
2. **Given** a ticket detail view, **When** any authenticated user changes the
   assignee to a valid user and saves, **Then** the assignee update persists.
3. **Given** invalid input (e.g., empty title), **When** a user attempts to save,
   **Then** the system rejects the update with a meaningful UI error.

---

### User Story 4 - Role-Based Status Transitions (Priority: P2)

Developer and QA roles advance tickets through the state machine. RESOLVED may be
set by Developer or QA. CLOSED may be set only by the ticket creator or QA.
All other users are rejected for status transitions.

**Why this priority**: Status integrity with role-based enforcement is a core
business rule.

**Independent Test**: As Developer, perform valid transitions; as regular User,
attempt transitions and confirm rejection; as creator, close own RESOLVED ticket;
as QA, close any RESOLVED ticket.

**Acceptance Scenarios**:

1. **Given** a ticket in OPEN status and a Developer or QA user, **When** they
   transition to IN_PROGRESS, **Then** the status updates successfully.
2. **Given** a ticket in IN_PROGRESS and a Developer or QA user, **When** they
   transition to RESOLVED, **Then** the status updates successfully.
3. **Given** a ticket in RESOLVED and the ticket creator, **When** they transition
   to CLOSED, **Then** the status updates successfully.
4. **Given** a ticket in RESOLVED and a QA user (not the creator), **When** they
   transition to CLOSED, **Then** the status updates successfully.
5. **Given** a ticket in RESOLVED and a Developer who is not the creator, **When**
   they attempt to transition to CLOSED, **Then** the system rejects the transition.
6. **Given** a regular User (no Developer/QA role), **When** they attempt any
   status transition, **Then** the system rejects with an authorization error.
7. **Given** a ticket in CLOSED status, **When** any user attempts to transition
   it to OPEN, **Then** the system rejects the transition.

---

### User Story 5 - Add Comments (Priority: P3)

Authenticated users add comments to tickets. Comments appear chronologically on the
detail view.

**Why this priority**: Collaboration enriches tickets but depends on tickets
existing.

**Independent Test**: Add a comment, verify it appears with author and timestamp,
persists after restart.

**Acceptance Scenarios**:

1. **Given** an existing ticket, **When** a user submits a non-empty comment,
   **Then** it appears on the detail view with author and timestamp.
2. **Given** an empty comment, **When** a user attempts to save, **Then** the
   system rejects it with a meaningful error.

---

### User Story 6 - Search, Filter, and Browse Tickets (Priority: P3)

Users search by keyword (title/description), filter by status and assignee, and
switch between default (current user), a specific assignee, and all assignees via one dropdown.

**Why this priority**: Discoverability across the team once ticket volume grows.

**Independent Test**: Create tickets with varied titles, assignees, and statuses;
verify search, filters, and ALL view return correct subsets.

**Acceptance Scenarios**:

1. **Given** tickets with varied titles, **When** a user searches by a keyword in
   a title, **Then** matching tickets appear (comments not searched).
2. **Given** tickets with varied descriptions, **When** a user searches by a keyword
   in a description, **Then** matching tickets appear.
3. **Given** tickets assigned to multiple users, **When** a user filters by a
   specific assignee name, **Then** only that assignee's tickets are shown.
4. **Given** the default list view, **When** a user selects All assignees, **Then** every
   ticket in the system is visible (subject to pagination).
5. **Given** active keyword, status, and assignee filters, **When** applied
   together, **Then** results match all criteria (intersection).
6. **Given** a search matching no tickets, **When** results display, **Then** the
   UI shows an empty state (not an error).

---

### Edge Cases

- Creating a ticket with only whitespace in title or description is rejected.
- Updating or transitioning a non-existent ticket returns a not-found error.
- Assigning a ticket to a non-existent user is rejected.
- Search with empty or whitespace-only keyword applies no keyword filter.
- Comment body is never included in keyword search results.
- A Developer who is not the ticket creator cannot close a ticket (only QA or
  creator can transition RESOLVED → CLOSED).
- Tickets in terminal states (CLOSED, CANCELLED) cannot transition further.
- RESOLVED tickets cannot transition to CANCELLED or OPEN (only to CLOSED).
- OPEN tickets cannot jump to RESOLVED or CLOSED (must go through IN_PROGRESS).
- Concurrent updates: last write wins; no merge conflict UI in v1.
- Admin credentials in application properties MUST be encoded (never plaintext).

## Requirements *(mandatory)*

### Functional Requirements

#### Authentication & User Management

- **FR-001**: System MUST store admin credentials in application properties in
  encoded form (never plaintext) and validate login by matching against the encoded
  value.
- **FR-002**: System MUST provide an admin-only page to register users, reset
  passwords, and assign roles.
- **FR-003**: System MUST support roles: Admin, Developer, QA, and User (default).
- **FR-004**: System MUST require authentication before any ticket or admin
  operation.

#### Ticket CRUD

- **FR-005**: System MUST allow authenticated users to create tickets with title
  and description; new tickets default to OPEN status.
- **FR-006**: System MUST display ticket details: title, description, status,
  priority, assignee, creator, timestamps, and comments.
- **FR-007**: System MUST allow updating title, description, and priority on
  existing tickets.
- **FR-008**: System MUST allow any authenticated user to change the assignee on
  any ticket (to a valid user or unassigned).
- **FR-009**: System MUST support priority values: LOW, MEDIUM, HIGH, CRITICAL
  (default: MEDIUM on create).

#### Comments

- **FR-010**: System MUST allow authenticated users to add comments; each comment
  records author and timestamp.

#### Search & Filter

- **FR-011**: System MUST support keyword search on title and description only
  (case-insensitive partial match); comment body is excluded.
- **FR-012**: System MUST support filtering by status: OPEN, IN_PROGRESS, RESOLVED,
  CLOSED, CANCELLED.
- **FR-013**: System MUST support filtering by assignee name.
- **FR-014**: System MUST default the ticket list to tickets assigned to the
  current user.
- **FR-015**: System MUST provide an ALL option to view every ticket regardless
  of assignee.
- **FR-016**: System MUST paginate ticket lists at 30 tickets per page.
- **FR-017**: System MUST display tickets grouped under their status sections in
  the list view.

#### Status State Machine & Authorization

- **FR-018**: System MUST enforce the following transitions on the server:

  | From | Allowed To |
  |------|------------|
  | OPEN | IN_PROGRESS, CANCELLED |
  | IN_PROGRESS | RESOLVED, CANCELLED |
  | RESOLVED | CLOSED |
  | CLOSED | *(none — terminal)* |
  | CANCELLED | *(none — terminal)* |

- **FR-019**: Only users with Developer or QA role MAY initiate status transitions,
  except CLOSED which has additional restrictions in FR-020.
- **FR-020**: Transition to RESOLVED MUST be performed by a Developer or QA user.
- **FR-021**: Transition to CLOSED MUST be performed by the ticket creator or a
  QA user (Developer who is not the creator cannot close).
- **FR-022**: System MUST reject any invalid or unauthorized transition with an
  explicit error message.

#### Persistence & Validation

- **FR-023**: System MUST persist tickets, comments, users, and status changes to
  a database; data survives application restart.
- **FR-024**: System MUST validate all input on the server.
- **FR-025**: System MUST return structured error responses and display meaningful,
  user-readable messages in the UI for all rejected operations.
- **FR-026**: Admin encoded credentials in application properties are the sole
  exception to FR-027; all other secrets MUST NOT be stored in source code or
  committed configuration.
- **FR-027**: System MUST NOT store plaintext passwords, API keys, or tokens in
  source code or committed configuration files.

### Key Entities

- **User**: Authenticated person. Attributes: username, encoded password, display
  name, email (unique), role (Admin | Developer | QA | User). Admin credentials
  are configured separately in application properties.
- **Ticket**: Support request. Attributes: title (required), description (required),
  status (enum), priority (enum), assignee (optional User), created by (User),
  created at, updated at.
- **Comment**: Note on a ticket. Attributes: body (required), author (User),
  ticket (parent), created at.

### Roles & Permissions

| Action | Admin | Developer | QA | User |
|--------|-------|-----------|-----|------|
| User management (register/reset/roles) | ✓ | ✗ | ✗ | ✗ |
| Create ticket | ✓ | ✓ | ✓ | ✓ |
| View tickets (default: own assignments) | ✓ | ✓ | ✓ | ✓ |
| View all tickets (ALL filter) | ✓ | ✓ | ✓ | ✓ |
| Update title/description/priority | ✓ | ✓ | ✓ | ✓ |
| Change assignee (any ticket) | ✓ | ✓ | ✓ | ✓ |
| Add comment | ✓ | ✓ | ✓ | ✓ |
| Status transition (OPEN/IN_PROGRESS/CANCELLED) | ✗* | ✓ | ✓ | ✗ |
| Transition to RESOLVED | ✗* | ✓ | ✓ | ✗ |
| Transition to CLOSED | ✗* | ✓† | ✓ | ✓† |

\*Admin uses the management page; ticket transitions follow Developer/QA rules if
Admin also holds those roles.
†Only when Admin/User is the ticket creator; Developer can close only own tickets.

### Status State Machine

```
OPEN ──────────► IN_PROGRESS ──────► RESOLVED ──────► CLOSED
 │                    │                                  ▲
 │                    │                                  │
 ▼                    ▼                          creator or QA
CANCELLED          CANCELLED
```

**Transition authority**: Developer or QA for all transitions except CLOSED
(creator or QA only). **Invalid examples**: CLOSED → OPEN, RESOLVED → OPEN,
CANCELLED → OPEN, OPEN → RESOLVED, OPEN → CLOSED.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a ticket and see it in their assigned list within
  30 seconds of opening the application.
- **SC-002**: 100% of valid status transitions succeed for authorized roles; 100%
  of invalid or unauthorized transitions are rejected.
- **SC-003**: Keyword search (title/description) returns results within 2 seconds
  for up to 1,000 tickets.
- **SC-004**: All data survives application restart with zero loss under normal
  shutdown.
- **SC-005**: 100% of validation and authorization failures show human-readable UI
  errors.
- **SC-006**: Every valid and invalid transition in FR-018 is automatically
  verified before each release with zero failures.
- **SC-007**: Ticket list displays a maximum of 30 tickets per page with correct
  status-section grouping.

## Assumptions

- **Admin bootstrap**: Initial admin username and encoded password are configured
  in application properties before first startup; no admin self-registration.
- **User passwords**: Non-admin user passwords are stored encoded in the database;
  admin resets generate a new encoded password.
- **Role assignment**: Admin assigns exactly one role per user at registration;
  role changes require admin action.
- **Default list view**: Assignee dropdown defaults to current user; "All assignees"
  omits the filter; unassigned tickets appear only when no assignee filter is applied.
- **Status sections**: All five statuses always appear as section headers; empty
  sections show an empty state within that section.
- **Search scope**: Comment body search is explicitly out of scope for v1.
- **Mobile**: Responsive web UI; native apps out of scope.
- **Notifications**: Email/push out of scope for v1.
- **Attachments**: File attachments out of scope for v1.
- **Audit trail**: Field-level change history out of scope for v1.

## Out of Scope (v1)

- File attachments
- Email/push notifications
- Comment keyword search
- Ticket merge or duplicate detection
- Custom workflows beyond the defined state machine
- Multi-tenant / organization separation
- Audit log of all field changes
- Self-registration (only admin creates users)

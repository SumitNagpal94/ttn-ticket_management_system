CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    role            VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'DEVELOPER', 'QA', 'USER'))
);

CREATE INDEX idx_users_display_name ON users (display_name);

CREATE TABLE tickets (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    priority        VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    assignee_id     BIGINT       REFERENCES users (id),
    created_by_id   BIGINT       NOT NULL REFERENCES users (id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tickets_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_assignee_id ON tickets (assignee_id);
CREATE INDEX idx_tickets_created_by_id ON tickets (created_by_id);
CREATE INDEX idx_tickets_assignee_status ON tickets (assignee_id, status);

CREATE TABLE comments (
    id          BIGSERIAL PRIMARY KEY,
    ticket_id   BIGINT      NOT NULL REFERENCES tickets (id) ON DELETE CASCADE,
    author_id   BIGINT      NOT NULL REFERENCES users (id),
    body        TEXT        NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_ticket_id_created_at ON comments (ticket_id, created_at ASC);

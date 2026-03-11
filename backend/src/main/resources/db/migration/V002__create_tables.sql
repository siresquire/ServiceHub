--==========================================================================
-- CORE TABLES
-- ===========================================================================

-- DEPARTMENTS
CREATE TABLE IF NOT EXISTS departments (
    id          BIGSERIAL        PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
    );


-- USERS
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL        PRIMARY KEY,
    email         VARCHAR(30)  NOT NULL UNIQUE,
    name          VARCHAR(50)  NOT NULL,
    password      VARCHAR(255)  NOT NULL,          -- bcrypt hash
    role          user_role     NOT NULL DEFAULT 'USER',
    department_id INT           REFERENCES departments (id) ON DELETE SET NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    avatar_url    TEXT,
    phone         VARCHAR(30),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_email         ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role          ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_department_id ON users (department_id);


-- SLA POLICIES
CREATE TABLE IF NOT EXISTS sla_policies (
    id                   BIGSERIAL           PRIMARY KEY,
    category             ticket_category  NOT NULL,
    priority             ticket_priority  NOT NULL,
    response_hours       NUMERIC(6, 2)    NOT NULL CHECK (response_hours > 0),
    resolution_hours     NUMERIC(6, 2)    NOT NULL CHECK (resolution_hours > 0),
    resolution_time_hours NUMERIC(6, 2)
    GENERATED ALWAYS AS (resolution_hours) STORED,
    created_at           TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sla_category_priority UNIQUE (category, priority)
    );

CREATE INDEX IF NOT EXISTS idx_sla_category  ON sla_policies (category);
CREATE INDEX IF NOT EXISTS idx_sla_priority  ON sla_policies (priority);
CREATE INDEX IF NOT EXISTS idx_sla_category_priority    ON sla_policies (category, priority);


-- SERVICE REQUESTS (Core Tickets Table)
-- Tracks the status, priority, and life-cycle of every helpdesk request.

CREATE TABLE IF NOT EXISTS service_requests (
    -- Core Identity
    id                    BIGSERIAL           PRIMARY KEY,
    title                 VARCHAR(120)     NOT NULL,
    description           TEXT,
    category              ticket_category  NOT NULL,
    priority              ticket_priority  NOT NULL DEFAULT 'MEDIUM',
    status                ticket_status    NOT NULL DEFAULT 'OPEN',

    -- Assignments (Who is involved?)
    requester_id          INT              NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    assignee_id           INT              REFERENCES users (id) ON DELETE SET NULL,  -- Support Agent
    department_id         INT              REFERENCES departments (id) ON DELETE SET NULL,

    -- Life-cycle Timestamps
    created_at              TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    assigned_at             TIMESTAMPTZ,
    resolved_at             TIMESTAMPTZ,


    -- Performance & SLA Tracking (In Hours)
    sla_breached          BOOLEAN          NOT NULL DEFAULT FALSE,
    response_sla_deadline   TIMESTAMPTZ,
    resolution_sla_deadline TIMESTAMPTZ,

-- Behavioral Tracking
    reopened_count        SMALLINT         NOT NULL DEFAULT 0 CHECK (reopened_count >= 0),
    is_archived           BOOLEAN          NOT NULL DEFAULT FALSE,
    resolved              BOOLEAN          NOT NULL DEFAULT FALSE

    );

CREATE INDEX IF NOT EXISTS idx_sr_status        ON service_requests (status);
CREATE INDEX IF NOT EXISTS idx_sr_priority      ON service_requests (priority);
CREATE INDEX IF NOT EXISTS idx_sr_category      ON service_requests (category);
CREATE INDEX IF NOT EXISTS idx_sr_requester     ON service_requests (requester_id);
CREATE INDEX IF NOT EXISTS idx_sr_assignee      ON service_requests (assignee_id);
CREATE INDEX IF NOT EXISTS idx_sr_department    ON service_requests (department_id);
CREATE INDEX IF NOT EXISTS idx_sr_created_at    ON service_requests (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_sr_resolved_at   ON service_requests (resolved_at DESC);
CREATE INDEX IF NOT EXISTS idx_sr_sla_breached  ON service_requests (sla_breached) WHERE sla_breached = TRUE;
CREATE INDEX IF NOT EXISTS idx_sr_category_priority ON service_requests (category, priority);


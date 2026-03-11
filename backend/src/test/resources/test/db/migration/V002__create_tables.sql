--==========================================================================
-- CORE TABLES
-- ===========================================================================

-- DEPARTMENTS
CREATE TABLE IF NOT EXISTS departments (
    id          BIGSERIAL        PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    category    VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
    );


-- USERS
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL        PRIMARY KEY,
    email         VARCHAR(30)  NOT NULL UNIQUE,
    name          VARCHAR(50)  NOT NULL,
    password      VARCHAR(255)  NOT NULL,          -- argon hash
    role          VARCHAR(20)     NOT NULL DEFAULT 'USER',
    department_id BIGINT           REFERENCES departments (id) ON DELETE SET NULL,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    avatar_url    TEXT,
    phone         VARCHAR(30),
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_email         ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role          ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_department_id ON users (department_id);


-- SLA POLICIES

CREATE TABLE IF NOT EXISTS sla_policies (
    id                   BIGSERIAL           PRIMARY KEY,
    category             VARCHAR(20)      NOT NULL,
    priority             VARCHAR(20)      NOT NULL,
    response_hours       NUMERIC(6, 2)    NOT NULL CHECK (response_hours > 0),
    resolution_hours     NUMERIC(6, 2)    NOT NULL CHECK (resolution_hours > 0),
    resolution_time_hours NUMERIC(6, 2),
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sla_category_priority UNIQUE (category, priority)
    );


-- SERVICE REQUESTS (Core Tickets Table)
-- Tracks the status, priority, and life-cycle of every helpdesk request.

CREATE TABLE IF NOT EXISTS service_requests (
    -- Core Identity
    id                    BIGSERIAL           PRIMARY KEY,
    title                 VARCHAR(120)     NOT NULL,
    description           TEXT,
    category              VARCHAR(20)  NOT NULL,
    priority              VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    status                VARCHAR(20)    NOT NULL DEFAULT 'OPEN',

    -- Assignments (Who is involved?)
    requester_id          BIGINT              NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    assignee_id           BIGINT              REFERENCES users (id) ON DELETE SET NULL,  -- Support Agent
    department_id         BIGINT              REFERENCES departments (id) ON DELETE SET NULL,

    -- Life-cycle Timestamps
    created_at              TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP      NOT NULL DEFAULT NOW(),
    assigned_at             TIMESTAMP,
    resolved_at             TIMESTAMP,


    -- Performance & SLA Tracking (In Hours)
    sla_breached          BOOLEAN          NOT NULL DEFAULT FALSE,
    response_sla_deadline   TIMESTAMP,
    resolution_sla_deadline TIMESTAMP,

-- Behavioral Tracking
    reopened_count        SMALLINT         NOT NULL DEFAULT 0 CHECK (reopened_count >= 0),
    is_archived           BOOLEAN          NOT NULL DEFAULT FALSE,
    resolved              BOOLEAN          NOT NULL DEFAULT FALSE

    );

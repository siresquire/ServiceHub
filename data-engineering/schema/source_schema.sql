-- =============================================================================
-- ServiceHub Source Database Schema
-- Database: servicehub (PostgreSQL)
-- Description: Core tables for departments, users, SLA policies, and requests.
-- =============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid(), crypt()
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";  -- query performance monitoring

-- Utility: auto-update updated_at on every row change

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================================
-- ENUMERATIONS
-- ===========================================================================

CREATE TYPE user_role AS ENUM ('ADMIN', 'AGENT', 'USER');

CREATE TYPE ticket_status AS ENUM (
  'OPEN',
  'ASSIGNED',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED'
);

CREATE TYPE ticket_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

CREATE TYPE ticket_category AS ENUM (
  'IT_SUPPORT',
  'FACILITIES',
  'HR_REQUEST'
);

--==========================================================================
-- CORE TABLES
-- ===========================================================================

-- DEPARTMENTS
CREATE TABLE IF NOT EXISTS departments (
  id          SERIAL        PRIMARY KEY,
  name        VARCHAR(100)  NOT NULL UNIQUE,
  description TEXT,
  created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TRIGGER set_departments_updated_at
  BEFORE UPDATE ON departments
  FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


-- USERS
CREATE TABLE IF NOT EXISTS users (
  id            SERIAL        PRIMARY KEY,
  email         VARCHAR(255)  NOT NULL UNIQUE,
  name          VARCHAR(150)  NOT NULL,
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

CREATE TRIGGER set_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


-- SLA POLICIES
CREATE TABLE IF NOT EXISTS sla_policies (
  id                   SERIAL           PRIMARY KEY,
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

CREATE TRIGGER set_sla_policies_updated_at
  BEFORE UPDATE ON sla_policies
  FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();


-- SERVICE REQUESTS (Core Tickets Table)
-- Tracks the status, priority, and life-cycle of every helpdesk request.

CREATE TABLE IF NOT EXISTS service_requests (
  -- Core Identity
  id                    SERIAL           PRIMARY KEY,
  title                 VARCHAR(255)     NOT NULL,
  description           TEXT,
  category              ticket_category  NOT NULL,
  priority              ticket_priority  NOT NULL DEFAULT 'MEDIUM',
  status                ticket_status    NOT NULL DEFAULT 'OPEN',

  -- Assignments (Who is involved?)
  requester_id          INT              NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
  assignee_id           INT              REFERENCES users (id) ON DELETE SET NULL,  -- Support Agent
  department_id         INT              REFERENCES departments (id) ON DELETE SET NULL,

  -- Life-cycle Timestamps
  created_at            TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  first_response_at     TIMESTAMPTZ,
  resolved_at           TIMESTAMPTZ,

  -- Performance & SLA Tracking (In Hours)
  sla_hours             NUMERIC(6, 2),   -- Target threshold at time of creation
  sla_breached          BOOLEAN          NOT NULL DEFAULT FALSE,
  response_time_hours   NUMERIC(8, 2),   -- Time until first response
  resolution_time_hours NUMERIC(8, 2),   -- Time until resolved

  -- Behavioral Tracking
  reopened_count        SMALLINT         NOT NULL DEFAULT 0 CHECK (reopened_count >= 0),
  is_archived           BOOLEAN          NOT NULL DEFAULT FALSE
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

CREATE TRIGGER set_sr_updated_at
  BEFORE UPDATE ON service_requests
  FOR EACH ROW EXECUTE FUNCTION trigger_set_timestamp();

-- ===========================================================================
-- SEED DATA
-- ===========================================================================

INSERT INTO departments (id, name, description) VALUES
  (1, 'IT Support',  'Technical support and infrastructure'),
  (2, 'HR',          'Human resources and people operations'),
  (3, 'Facilities',  'Office facilities and maintenance'),
  (4, 'Finance',     'Financial services and reimbursements')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sla_policies (id, category, priority, response_hours, resolution_hours) VALUES
  (1,  'IT_SUPPORT',  'HIGH',     1,  4),
  (2,  'IT_SUPPORT',  'MEDIUM',   4, 24),
  (3,  'IT_SUPPORT',  'LOW',      8, 48),
  (4,  'IT_SUPPORT',  'CRITICAL', 1,  4),
  (5,  'HR_REQUEST',  'HIGH',     2,  8),
  (6,  'HR_REQUEST',  'MEDIUM',   8, 48),
  (7,  'HR_REQUEST',  'LOW',      8, 96),
  (8,  'HR_REQUEST',  'CRITICAL', 2, 24),
  (9,  'FACILITIES',  'HIGH',     1,  8),
  (10, 'FACILITIES',  'MEDIUM',   4, 24),
  (11, 'FACILITIES',  'LOW',      8, 72),
  (12, 'FACILITIES',  'CRITICAL', 1, 12)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, email, name, password, role, created_at) VALUES
  (1, 'admin@amalitech.com', 'Admin User',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW()),
  (2, 'agent@amalitech.com', 'Support Agent', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT', NOW()),
  (3, 'user@amalitech.com',  'Test User',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER',  NOW())
ON CONFLICT (id) DO NOTHING;

-- ===========================================================================
-- USEFUL VIEWS
-- ===========================================================================

CREATE OR REPLACE VIEW vw_tickets_full AS
SELECT
  sr.id,
  sr.title,
  sr.category,
  sr.priority,
  sr.status,
  sr.sla_breached,
  sr.reopened_count,
  sr.response_time_hours,
  sr.resolution_time_hours,
  sr.created_at,
  sr.first_response_at,
  sr.resolved_at,
  sr.updated_at,
  req.name  AS requester_name,
  req.email AS requester_email,
  ag.name   AS agent_name,
  ag.email  AS agent_email,
  d.name    AS department_name
FROM service_requests sr
JOIN  users       req ON sr.requester_id  = req.id
LEFT JOIN users   ag  ON sr.assignee_id   = ag.id
LEFT JOIN departments d ON sr.department_id = d.id;

CREATE OR REPLACE VIEW vw_active_sla_breaches AS
SELECT
  sr.id,
  sr.title,
  sr.category,
  sr.priority,
  sr.status,
  sr.created_at,
  EXTRACT(EPOCH FROM (NOW() - sr.created_at)) / 3600 AS hours_elapsed,
  sp.resolution_hours AS sla_threshold_hours,
  ag.name AS assigned_agent
FROM service_requests sr
JOIN sla_policies sp ON sr.category = sp.category AND sr.priority = sp.priority
LEFT JOIN users ag ON sr.assignee_id = ag.id
WHERE sr.resolved_at IS NULL
  AND EXTRACT(EPOCH FROM (NOW() - sr.created_at)) / 3600 > sp.resolution_hours;

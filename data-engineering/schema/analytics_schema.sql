-- ============================================================================
-- ServiceHub Analytics Schema — Star Schema
-- Description: Fact + dimension tables refreshed by the ETL pipeline.
-- Purpose: Optimized for dashboards and reporting (Grafana, Streamlit, etc.).
-- ============================================================================

-- ─── DIMENSION TABLES ────────────────────────────────────────────────────────

-- DIM 1: DATE
-- Standard date dimension for all time-based slicing.

CREATE TABLE IF NOT EXISTS dim_date (
  date_key        INT          PRIMARY KEY,  -- Surrogate key: YYYYMMDD integer
  full_date       DATE         NOT NULL UNIQUE,
  day_of_week     SMALLINT     NOT NULL,     -- 1 (Mon) – 7 (Sun)
  day_name        VARCHAR(9)   NOT NULL,
  week_start_date DATE         NOT NULL,     -- ISO Monday of the week
  week_number     SMALLINT     NOT NULL,     -- ISO week number
  month           SMALLINT     NOT NULL,
  month_name      VARCHAR(9)   NOT NULL,
  quarter         SMALLINT     NOT NULL,
  year            SMALLINT     NOT NULL,
  is_weekend      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_dim_date_full ON dim_date (full_date);
CREATE INDEX IF NOT EXISTS idx_dim_date_week ON dim_date (week_start_date);


-- DIM 2: CATEGORY
-- Ticket category lookup — replaces inline ticket_category enum usage.

CREATE TABLE IF NOT EXISTS dim_category (
  category_key  SERIAL       PRIMARY KEY,
  category_name VARCHAR(100)    NOT NULL UNIQUE,
  description   TEXT
);


-- DIM 3: PRIORITY
-- Ticket priority lookup — replaces inline ticket_priority enum usage.

CREATE TABLE IF NOT EXISTS dim_priority (
  priority_key  SERIAL       PRIMARY KEY,
  priority_name VARCHAR(100)    NOT NULL UNIQUE,
  sla_hours     NUMERIC(6,2),  -- Target SLA threshold in hours for this priority
  description   TEXT
);


-- DIM 4: AGENT
-- Support agent dimension, sourced from the users table.

CREATE TABLE IF NOT EXISTS dim_agent (
  agent_key     SERIAL       PRIMARY KEY,
  user_id       INT          NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
  display_name  VARCHAR(200),
  department    VARCHAR(100),
  is_active     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_dim_agent_user ON dim_agent (user_id);


-- DIM 5: DEPARTMENT
-- Department dimension for workload and efficiency reporting.

CREATE TABLE IF NOT EXISTS dim_department (
  department_key  SERIAL       PRIMARY KEY,
  department_name VARCHAR(100) NOT NULL UNIQUE
);


-- ─── FACT TABLE ──────────────────────────────────────────────────────────────

-- FACT: TICKET EVENTS
-- Grain: one row per resolved ticket event.
-- All measures are additive unless noted.

CREATE TABLE IF NOT EXISTS fact_ticket_events (
  event_id                    BIGSERIAL    PRIMARY KEY,

  -- Dimension foreign keys
  date_key                    INT          NOT NULL REFERENCES dim_date       (date_key),
  category_key                INT          NOT NULL REFERENCES dim_category   (category_key),
  priority_key                INT          NOT NULL REFERENCES dim_priority   (priority_key),
  agent_key                   INT          REFERENCES dim_agent               (agent_key),
  department_key              INT          REFERENCES dim_department          (department_key),

  -- Additive measures
  resolution_hours            NUMERIC(10, 4),    -- Null if not yet resolved
  response_hours              NUMERIC(10, 4),    -- Time to first response

  -- Semi-additive / status flags
  is_resolved                 BOOLEAN      NOT NULL DEFAULT FALSE,
  is_sla_breached             BOOLEAN      NOT NULL DEFAULT FALSE,
  is_first_contact_resolved   BOOLEAN      NOT NULL DEFAULT FALSE,

  -- Metadata
  refreshed_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fact_date      ON fact_ticket_events (date_key);
CREATE INDEX IF NOT EXISTS idx_fact_category  ON fact_ticket_events (category_key);
CREATE INDEX IF NOT EXISTS idx_fact_priority  ON fact_ticket_events (priority_key);
CREATE INDEX IF NOT EXISTS idx_fact_agent     ON fact_ticket_events (agent_key);
CREATE INDEX IF NOT EXISTS idx_fact_dept      ON fact_ticket_events (department_key);



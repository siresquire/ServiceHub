-- ============================================================================
-- ServiceHub Analytics Schema
-- Description: Pre-aggregated tables refreshed by the ETL pipeline.
-- Purpose: Optimized for dashboards and reporting (Grafana, Streamlit, etc.).
-- ===========================================================================

-- 1. SLA COMPLIANCE METRICS
-- Aggregates resolution performance by category and priority.

CREATE TABLE IF NOT EXISTS analytics_sla_metrics (
  id                   SERIAL          PRIMARY KEY,
  -- Classifiers
  category             ticket_category NOT NULL,
  priority             ticket_priority NOT NULL,
  
  -- Counts
  total_resolved       INT             NOT NULL DEFAULT 0,
  total_breached       INT             NOT NULL DEFAULT 0,
  
  -- Calculations (Hours)
  avg_resolution_hours NUMERIC(10, 4),
  max_resolution_hours NUMERIC(10, 4),
  compliance_rate      NUMERIC(5, 4)   CHECK (compliance_rate BETWEEN 0 AND 1),
  
  -- Metadata
  refreshed_at         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_sla_metrics_cat_pri UNIQUE (category, priority)
);


-- 2. DAILY VOLUME TRENDS
-- Tracks daily request throughput by category.

CREATE TABLE IF NOT EXISTS analytics_daily_volume (
  id            SERIAL          PRIMARY KEY,
  date          DATE            NOT NULL,
  category      ticket_category NOT NULL,
  request_count INT             NOT NULL DEFAULT 0,
  
  refreshed_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_daily_vol_date_cat UNIQUE (date, category)
);

CREATE INDEX IF NOT EXISTS idx_daily_vol_date ON analytics_daily_volume (date DESC);


-- 3. AGENT PERFORMANCE LEADERBOARD
-- Detailed metrics for support agent efficiency and quality.

CREATE TABLE IF NOT EXISTS analytics_agent_performance (
  id                          SERIAL       PRIMARY KEY,
  assignee_id                 INT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  
  -- Volume & Speed
  total_resolved              INT          NOT NULL DEFAULT 0,
  avg_resolution_hours        NUMERIC(10, 4),
  min_resolution_hours        NUMERIC(10, 4),
  max_resolution_hours        NUMERIC(10, 4),
  
  -- Quality
  first_contact_resolution_rate NUMERIC(5, 4) CHECK (
    first_contact_resolution_rate BETWEEN 0 AND 1
  ),
  
  refreshed_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_agent_perf_assignee UNIQUE (assignee_id)
);


-- 4. DEPARTMENT WORKLOAD & EFFICIENCY
-- High-level overview of department-level throughput.

CREATE TABLE IF NOT EXISTS analytics_department_workload (
  id                   SERIAL       PRIMARY KEY,
  department_name      VARCHAR(100) NOT NULL UNIQUE,
  
  -- Volume
  total_requests       INT          NOT NULL DEFAULT 0,
  open_requests        INT          NOT NULL DEFAULT 0,
  resolved_requests    INT          NOT NULL DEFAULT 0,
  
  -- Efficiency
  avg_resolution_hours NUMERIC(10, 4),
  resolution_rate      NUMERIC(5, 4) CHECK (resolution_rate BETWEEN 0 AND 1),
  
  refreshed_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


-- 5. WEEKLY ROLLING TRENDS
-- Long-term request volume and resolution trends.

CREATE TABLE IF NOT EXISTS analytics_weekly_trends (
  id                   SERIAL          PRIMARY KEY,
  week_start_date      DATE            NOT NULL,
  category             ticket_category NOT NULL,
  
  -- Averages & Volume
  avg_resolution_hours NUMERIC(10, 4),
  avg_response_hours   NUMERIC(10, 4),
  request_count        INT             NOT NULL DEFAULT 0,
  
  refreshed_at         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_weekly_trends_week_cat UNIQUE (week_start_date, category)
);

CREATE INDEX IF NOT EXISTS idx_weekly_trends_date ON analytics_weekly_trends (week_start_date DESC);

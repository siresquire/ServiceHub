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


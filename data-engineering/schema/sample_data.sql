-- =============================================================
-- Helpdesk system – full seed data
-- Generated at: 2026-03-11 09:49:11 UTC
-- Password for all accounts: password123
-- =============================================================

BEGIN;

-- ── Departments ──────────────────────────────────────────────
INSERT INTO departments (id, name, category, description) VALUES
  (1, 'IT Support',  'IT_SUPPORT',  'Technical support and infrastructure'),
  (2, 'HR',          'HR_REQUEST',  'Human resources and people operations'),
  (3, 'Facilities',  'FACILITIES',  'Office facilities and maintenance')
ON CONFLICT (id) DO NOTHING;

-- ── SLA Policies (3 categories × 4 priorities = 12) ────────
INSERT INTO sla_policies (id, category, priority, response_hours, resolution_hours) VALUES
  (1,  'IT_SUPPORT',  'CRITICAL', 1,  4),
  (2,  'IT_SUPPORT',  'HIGH',     2,  8),
  (3,  'IT_SUPPORT',  'MEDIUM',   4, 24),
  (4,  'IT_SUPPORT',  'LOW',      8, 48),
  (5,  'HR_REQUEST',  'CRITICAL', 1,  4),
  (6,  'HR_REQUEST',  'HIGH',     2,  8),
  (7,  'HR_REQUEST',  'MEDIUM',   4, 24),
  (8,  'HR_REQUEST',  'LOW',      8, 48),
  (9,  'FACILITIES',  'CRITICAL', 1,  4),
  (10, 'FACILITIES',  'HIGH',     2,  8),
  (11, 'FACILITIES',  'MEDIUM',   4, 24),
  (12, 'FACILITIES',  'LOW',      8, 48)
ON CONFLICT (id) DO NOTHING;

-- ── Users (ADMIN / AGENT / USER) ────────────────────────────
INSERT INTO users (id, email, name, password, role, department_id, created_at) VALUES
  (1, 'admin@amalitech.com',  'Manager User',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1, '2026-02-09 09:49:11'),
  (2, 'agent@amalitech.com',  'Support Agent', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT', 1, '2026-02-09 09:49:11'),
  (3, 'user@amalitech.com',   'Test User',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER',  2, '2026-02-10 09:49:11'),
  (4, 'agent2@amalitech.com', 'Jane Mensah',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT', 3, '2026-02-11 09:49:11'),
  (5, 'agent3@amalitech.com', 'Kwame Asante',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT', 2, '2026-02-12 09:49:11'),
  (6, 'alice@amalitech.com',  'Alice Boateng', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER',  1, '2026-02-14 09:49:11'),
  (7, 'bob@amalitech.com',    'Bob Tetteh',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER',  3, '2026-02-15 09:49:11'),
  (8, 'carol@amalitech.com',  'Carol Owusu',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER',  2, '2026-02-19 09:49:11')
ON CONFLICT (id) DO NOTHING;

-- ── Service Requests (15 rows – all categories, all priorities) ──
-- SLA deadlines derived from sla_policies:
--   CRITICAL : response +1 h, resolution +4 h
--   HIGH     : response +2 h, resolution +8 h
--   MEDIUM   : response +4 h, resolution +24 h
--   LOW      : response +8 h, resolution +48 h
--
-- sla_breached = TRUE when resolved_at (or NOW() for open tickets) > resolution_sla_deadline
-- resolved     = TRUE only for RESOLVED / CLOSED status
-- is_archived  = TRUE only for CLOSED status
-- reopened_count reflects tickets that were re-opened after an initial resolution
--
-- Column order:
--   id, title, description, category, priority, status,
--   requester_id, assignee_id, department_id,
--   created_at, updated_at, assigned_at, resolved_at,
--   sla_breached, response_sla_deadline, resolution_sla_deadline,
--   reopened_count, is_archived, resolved
INSERT INTO service_requests (
    id, title, description, category, priority, status,
    requester_id, assignee_id, department_id,
    created_at, updated_at, assigned_at, resolved_at,
    sla_breached, response_sla_deadline, resolution_sla_deadline,
    reopened_count, is_archived, resolved
) VALUES
  -- 1 · CRITICAL IT – still IN_PROGRESS, assigned quickly, SLA not yet breached
  (1,
   'Production database server unreachable',
   'The primary PostgreSQL instance stopped accepting connections at 02:14 UTC. All production services are affected. Failover has not triggered automatically.',
   'IT_SUPPORT', 'CRITICAL', 'IN_PROGRESS',
   6, 2, 1,
   '2026-03-11 05:01:11', '2026-03-11 09:40:11', '2026-03-11 05:25:11', NULL,
   false, '2026-03-11 06:01:11', '2026-03-11 09:01:11',
   0, false, false),

  -- 2 · HIGH IT – ASSIGNED, responded within SLA
  (2,
   'VPN access broken for remote workers',
   'Since the firewall update last night, employees working from home cannot establish VPN tunnels. Approximately 40 staff are affected.',
   'IT_SUPPORT', 'HIGH', 'ASSIGNED',
   3, 2, 1,
   '2026-03-10 09:49:11', '2026-03-10 10:37:11', '2026-03-10 10:37:11', NULL,
   false, '2026-03-10 11:49:11', '2026-03-10 17:49:11',
   0, false, false),

  -- 3 · MEDIUM IT – OPEN, assigned but SLA breached (no resolution in 24 h)
  (3,
   'Printer on 3rd floor offline',
   'The HP LaserJet M507dn on the 3rd floor has not been reachable on the network since Monday morning. Restarting the device did not help.',
   'IT_SUPPORT', 'MEDIUM', 'OPEN',
   7, 2, 1,
   '2026-03-09 09:49:11', '2026-03-09 13:05:11', '2026-03-09 13:05:11', NULL,
   true, '2026-03-09 13:49:11', '2026-03-10 09:49:11',
   0, false, false),

  -- 4 · LOW IT – RESOLVED on time, archived
  (4,
   'Request to install VS Code on laptop',
   'Please install Visual Studio Code (latest stable) on my assigned laptop (asset tag: LT-2047).',
   'IT_SUPPORT', 'LOW', 'CLOSED',
   8, 4, 1,
   '2026-03-06 09:49:11', '2026-03-07 19:25:11', '2026-03-06 13:01:11', '2026-03-07 19:25:11',
   false, '2026-03-06 17:49:11', '2026-03-08 09:49:11',
   0, true, true),

  -- 5 · CRITICAL FACILITIES – IN_PROGRESS, assigned immediately
  (5,
   'Water leak in server room ceiling',
   'A pipe burst above the ceiling tiles in server room B-04. Water is dripping near the UPS units. Emergency containment needed immediately.',
   'FACILITIES', 'CRITICAL', 'IN_PROGRESS',
   6, 4, 3,
   '2026-03-11 07:25:11', '2026-03-11 09:44:23', '2026-03-11 07:49:11', NULL,
   false, '2026-03-11 08:25:11', '2026-03-11 11:25:11',
   0, false, false),

  -- 6 · HIGH FACILITIES – ASSIGNED, within SLA
  (6,
   'Air conditioning unit failure – 2nd floor open plan',
   'The main HVAC unit for the 2nd floor open-plan area has stopped cooling. Indoor temperature is 31 °C and rising. Engineers and equipment are at risk.',
   'FACILITIES', 'HIGH', 'ASSIGNED',
   7, 4, 3,
   '2026-03-10 21:49:11', '2026-03-11 09:25:11', '2026-03-10 22:37:11', NULL,
   false, '2026-03-10 23:49:11', '2026-03-11 05:49:11',
   0, false, false),

  -- 7 · MEDIUM FACILITIES – OPEN, SLA breached (>24 h without resolution)
  (7,
   'Broken window latch in meeting room C',
   'The latch on the south-facing window in meeting room C is broken and the window cannot be secured. This is a security and weather risk.',
   'FACILITIES', 'MEDIUM', 'OPEN',
   3, 4, 3,
   '2026-03-08 09:49:11', '2026-03-08 14:22:11', '2026-03-08 14:22:11', NULL,
   true, '2026-03-08 13:49:11', '2026-03-09 09:49:11',
   0, false, false),

  -- 8 · LOW FACILITIES – CLOSED on time
  (8,
   'Replace fluorescent tube in corridor 1B',
   'One of the fluorescent tubes in corridor 1B is flickering and needs replacement.',
   'FACILITIES', 'LOW', 'CLOSED',
   8, 4, 3,
   '2026-03-01 09:49:11', '2026-03-02 19:25:11', '2026-03-01 13:01:11', '2026-03-02 19:25:11',
   false, '2026-03-01 17:49:11', '2026-03-03 09:49:11',
   0, true, true),

  -- 9 · HIGH HR – IN_PROGRESS, SLA still live
  (9,
   'Incorrect salary disbursement for March',
   'My March payslip shows a deduction that was not communicated. The amount is GHS 450. Please investigate and correct before the end of the month.',
   'HR_REQUEST', 'HIGH', 'IN_PROGRESS',
   3, 5, 2,
   '2026-03-09 09:49:11', '2026-03-11 08:19:11', '2026-03-09 10:37:11', NULL,
   true, '2026-03-09 11:49:11', '2026-03-09 17:49:11',
   0, false, false),

  -- 10 · MEDIUM HR – ASSIGNED, within SLA
  (10,
   'Request for annual leave approval',
   'I would like to request 5 days of annual leave from 2026-04-07 to 2026-04-11. My tasks have been delegated to a colleague.',
   'HR_REQUEST', 'MEDIUM', 'ASSIGNED',
   6, 5, 2,
   '2026-03-10 09:49:11', '2026-03-11 08:55:11', '2026-03-10 11:25:11', NULL,
   false, '2026-03-10 13:49:11', '2026-03-11 09:49:11',
   0, false, false),

  -- 11 · LOW HR – OPEN, assigned, within SLA
  (11,
   'Update emergency contact details',
   'Please update my emergency contact in the HR system: Name – Abena Mensah, Relationship – Spouse, Phone – +233 24 000 1234.',
   'HR_REQUEST', 'LOW', 'OPEN',
   7, 5, 2,
   '2026-03-07 09:49:11', '2026-03-07 17:34:11', '2026-03-07 17:34:11', NULL,
   true, '2026-03-07 17:49:11', '2026-03-09 09:49:11',
   0, false, false),

  -- 12 · CRITICAL HR – IN_PROGRESS, responded within SLA
  (12,
   'Workplace harassment complaint – urgent',
   'I need to formally report a repeated harassment incident. This has been escalated to urgent due to a second occurrence today. Requesting immediate HR intervention.',
   'HR_REQUEST', 'CRITICAL', 'IN_PROGRESS',
   8, 5, 2,
   '2026-03-11 02:37:11', '2026-03-11 09:37:11', '2026-03-11 03:01:11', NULL,
   false, '2026-03-11 03:37:11', '2026-03-11 06:37:11',
   0, false, false),

  -- 13 · HIGH IT – RESOLVED, reopened once, SLA met on second resolution
  (13,
   'Email server rejecting outbound messages',
   'Our SMTP relay started bouncing outbound emails with error 550 5.7.1. External recipients are not receiving messages sent since 08:00 today.',
   'IT_SUPPORT', 'HIGH', 'RESOLVED',
   3, 2, 1,
   '2026-03-05 09:49:11', '2026-03-05 15:25:11', '2026-03-05 10:37:11', '2026-03-05 15:25:11',
   false, '2026-03-05 11:49:11', '2026-03-05 17:49:11',
   1, false, true),

  -- 14 · MEDIUM FACILITIES – RESOLVED, SLA breached (resolved in ~17 h vs 24 h limit – actually met; no breach)
  (14,
   'Parking barrier stuck in closed position',
   'The automated barrier at the north car park entrance is stuck closed, blocking staff access. Manual override is required.',
   'FACILITIES', 'MEDIUM', 'RESOLVED',
   7, 4, 3,
   '2026-03-03 09:49:11', '2026-03-04 02:37:11', '2026-03-03 11:25:11', '2026-03-04 02:37:11',
   false, '2026-03-03 13:49:11', '2026-03-04 09:49:11',
   0, false, true),

  -- 15 · LOW HR – CLOSED on time
  (15,
   'Request copy of employment letter',
   'I require an official employment confirmation letter for a mortgage application. Please issue on company letterhead.',
   'HR_REQUEST', 'LOW', 'CLOSED',
   6, 5, 2,
   '2026-02-27 09:49:11', '2026-02-28 19:25:11', '2026-02-27 13:01:11', '2026-02-28 19:25:11',
   false, '2026-02-27 17:49:11', '2026-03-01 09:49:11',
   0, true, true)

ON CONFLICT (id) DO NOTHING;

COMMIT;

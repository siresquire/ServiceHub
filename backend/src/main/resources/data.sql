-- Seed departments
INSERT INTO departments (id, name, category, contact_email, is_active) VALUES
  (1, 'IT Support', 'IT', 'it@amalitech.local', true),
  (2, 'HR', 'HR', 'hr@amalitech.local', true),
  (3, 'Facilities', 'FACILITIES', 'facilities@amalitech.local', true)
ON CONFLICT (id) DO NOTHING;

-- Seed SLA policies
INSERT INTO sla_policies (id, priority, response_time_hours, resolution_time_hours) VALUES
  (1, 'CRITICAL', 1, 4),
  (2, 'HIGH', 2, 8),
  (3, 'MEDIUM', 4, 24),
  (4, 'LOW', 8, 48)
ON CONFLICT (id) DO NOTHING;

-- Seed original users, mapped to proper schema (password: password123)
INSERT INTO users (id, email, full_name, password, role, department, created_at) VALUES
  (1, 'admin@amalitech.com', 'Manager User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'IT Support', NOW()),
  (2, 'agent@amalitech.com', 'Support Agent', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT', 'IT Support', NOW()),
  (3, 'user@amalitech.com', 'Test User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 'HR', NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequence so new users start from id 4
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

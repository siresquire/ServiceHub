-- Seed departments
INSERT INTO departments (id, name, category, contact_email, is_active) VALUES
  (1, 'IT Support',  'IT',         'it@amalitech.local',         true),
  (2, 'HR',          'HR',         'hr@amalitech.local',         true),
  (3, 'Facilities',  'FACILITIES', 'facilities@amalitech.local', true)
ON CONFLICT (id) DO NOTHING;

-- Seed SLA policies
INSERT INTO sla_policies (id, priority, response_time_hours, resolution_time_hours) VALUES
  (1, 'CRITICAL', 1, 4),
  (2, 'HIGH',     2, 8),
  (3, 'MEDIUM',   4, 24),
  (4, 'LOW',      8, 48)
ON CONFLICT (id) DO NOTHING;
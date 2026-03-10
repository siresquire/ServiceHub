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

-- Seed users (password for all: password123)
-- bcrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (id, email, full_name, password, role, department, created_at) VALUES
  -- Managers (3)
  (1,  'admin@amalitech.com',          'Manager User',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',  'IT Support',  NOW()),
  (2,  'sarah.williams@amalitech.com', 'Sarah Williams',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',  'HR',          NOW()),
  (3,  'david.jones@amalitech.com',    'David Jones',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MANAGER',  'Facilities',  NOW()),

  -- Agents (8 — matching data_generator AGENTS list)
  (4,  'alex.turner@amalitech.com',    'Alex Turner',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'IT Support',  NOW()),
  (5,  'jordan.kim@amalitech.com',     'Jordan Kim',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'IT Support',  NOW()),
  (6,  'priya.sharma@amalitech.com',   'Priya Sharma',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'HR',          NOW()),
  (7,  'chris.nguyen@amalitech.com',   'Chris Nguyen',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'HR',          NOW()),
  (8,  'taylor.brooks@amalitech.com',  'Taylor Brooks',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'Facilities',  NOW()),
  (9,  'morgan.patel@amalitech.com',   'Morgan Patel',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'Facilities',  NOW()),
  (10, 'sam.okafor@amalitech.com',     'Sam Okafor',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'IT Support',  NOW()),
  (11, 'casey.reyes@amalitech.com',    'Casey Reyes',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT',    'HR',          NOW()),

  -- Employees (20 — spread across departments for realistic ticket distribution)
  (12, 'james.smith@amalitech.com',       'James Smith',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (13, 'maria.johnson@amalitech.com',     'Maria Johnson',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW()),
  (14, 'michael.white@amalitech.com',     'Michael White',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'Facilities',  NOW()),
  (15, 'emily.davis@amalitech.com',       'Emily Davis',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (16, 'robert.thompson@amalitech.com',   'Robert Thompson',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW()),
  (17, 'jessica.garcia@amalitech.com',    'Jessica Garcia',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (18, 'william.martinez@amalitech.com',  'William Martinez',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'Facilities',  NOW()),
  (19, 'ashley.brown@amalitech.com',      'Ashley Brown',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW()),
  (20, 'daniel.wilson@amalitech.com',     'Daniel Wilson',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (21, 'amanda.anderson@amalitech.com',   'Amanda Anderson',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'Facilities',  NOW()),
  (22, 'matthew.taylor@amalitech.com',    'Matthew Taylor',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW()),
  (23, 'stephanie.thomas@amalitech.com',  'Stephanie Thomas',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (24, 'christopher.moore@amalitech.com', 'Christopher Moore', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'Facilities',  NOW()),
  (25, 'rebecca.jackson@amalitech.com',   'Rebecca Jackson',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW()),
  (26, 'andrew.martin@amalitech.com',     'Andrew Martin',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (27, 'laura.lee@amalitech.com',         'Laura Lee',         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'Facilities',  NOW()),
  (28, 'kevin.perez@amalitech.com',       'Kevin Perez',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW()),
  (29, 'jennifer.harris@amalitech.com',   'Jennifer Harris',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'IT Support',  NOW()),
  (30, 'brian.clark@amalitech.com',       'Brian Clark',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'Facilities',  NOW()),
  (31, 'linda.lewis@amalitech.com',       'Linda Lewis',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'HR',          NOW())
ON CONFLICT (id) DO NOTHING;

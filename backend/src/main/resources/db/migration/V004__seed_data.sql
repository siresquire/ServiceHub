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

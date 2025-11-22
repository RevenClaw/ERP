-- IMPORTANT: Replace these placeholder hashes with real BCrypt hashes!
-- Use PasswordHashGenerator utility or online BCrypt generator
-- Password for all test accounts: password123
-- Example hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users_auth (username, role, password_hash, status, failed_attempts, last_login)
VALUES
    ('admin1', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL),
    ('inst1',  'INSTRUCTOR', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL),
    ('stu1',   'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL),
    ('stu2',   'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL);

-- Password history entries mirror initial hashes for audit trail.
INSERT INTO password_history (user_id, password_hash)
SELECT user_id, password_hash FROM users_auth;


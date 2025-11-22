-- Script to update password hashes for all test users
-- Password for all accounts: password123
-- 
-- IMPORTANT: Generate a new BCrypt hash for "password123" first!
-- Use: https://bcrypt-generator.com/ or the Java utility
-- Then replace YOUR_NEW_HASH_HERE below

USE univ_auth;

-- Update all user passwords
UPDATE users_auth 
SET password_hash = 'YOUR_NEW_HASH_HERE'
WHERE username IN ('admin1', 'inst1', 'stu1', 'stu2');

-- Verify the update
SELECT username, role, LEFT(password_hash, 20) as hash_preview, status 
FROM users_auth 
WHERE username IN ('admin1', 'inst1', 'stu1', 'stu2');


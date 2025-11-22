-- Quick verification script - Run this to check if setup is correct

-- Check Auth Database
USE univ_auth;
SELECT 'Auth Database Check' AS Status;
SELECT COUNT(*) AS 'Users Count' FROM users_auth;
SELECT username, role, status FROM users_auth;
SELECT COUNT(*) AS 'Password History Count' FROM password_history;

-- Check ERP Database
USE univ_erp;
SELECT 'ERP Database Check' AS Status;
SELECT COUNT(*) AS 'Students Count' FROM students;
SELECT COUNT(*) AS 'Instructors Count' FROM instructors;
SELECT COUNT(*) AS 'Courses Count' FROM courses;
SELECT COUNT(*) AS 'Sections Count' FROM sections;
SELECT COUNT(*) AS 'Enrollments Count' FROM enrollments;
SELECT setting_key, setting_value FROM settings;

-- Expected Results:
-- Users: 4 (admin1, inst1, stu1, stu2)
-- Students: 2
-- Instructors: 1
-- Courses: 2
-- Sections: 2
-- Enrollments: 2
-- Settings: 1 (maintenanceMode)


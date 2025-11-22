-- Complete seed data with error handling
-- Run this in your univ_erp database

USE univ_erp;

-- Step 1: Update schema first (if not already done)
ALTER TABLE sections 
MODIFY COLUMN semester ENUM('MONSOON', 'WINTER') NOT NULL;

-- Step 2: Clear existing data (if needed)
DELETE FROM final_grades;
DELETE FROM grades;
DELETE FROM assessments;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;

-- Step 3: Insert courses
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS201', 'Data Structures', 4.0, 'Core undergraduate course covering data structures.'),
    ('CS205', 'Database Systems', 3.0, 'Introduction to relational databases and SQL.'),
    ('MTH203', 'Multivariate Calculus', 4.0, 'Advanced calculus covering multiple variables, partial derivatives, and integrals.'),
    ('ECE201', 'Operating Systems', 3.0, 'Fundamentals of operating systems, process management, and memory systems.'),
    ('CSE201', 'Advanced Programming', 4.0, 'Advanced programming concepts including design patterns and software architecture.');

-- Verify courses inserted
SELECT 'Courses inserted:' as status, COUNT(*) as count FROM courses;

-- Step 4: Insert sections for MONSOON 2025
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'MONSOON', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2026-01-15'),
    (2, 1, 'MONSOON', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2026-01-15'),
    (3, 1, 'MONSOON', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2026-01-15'),
    (4, 1, 'MONSOON', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2026-01-15'),
    (5, 1, 'MONSOON', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2026-01-15');

-- Step 5: Insert sections for WINTER 2025
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'WINTER', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2025-05-15'),
    (2, 1, 'WINTER', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2025-05-15'),
    (3, 1, 'WINTER', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2025-05-15'),
    (4, 1, 'WINTER', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2025-05-15'),
    (5, 1, 'WINTER', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2025-05-15');

-- Verify sections inserted
SELECT 'Sections inserted:' as status, COUNT(*) as count FROM sections;
SELECT 'MONSOON sections:' as status, COUNT(*) as count FROM sections WHERE semester = 'MONSOON';
SELECT 'WINTER sections:' as status, COUNT(*) as count FROM sections WHERE semester = 'WINTER';

-- Step 6: Insert sample enrollments (optional - for testing)
INSERT INTO enrollments (student_id, section_id, status)
VALUES
    (1, 1, 'ENROLLED'),
    (2, 2, 'ENROLLED')
ON DUPLICATE KEY UPDATE status = 'ENROLLED';

-- Step 7: Insert assessments and grades (optional - for testing)
INSERT INTO assessments (section_id, name, weight_percent, max_score)
VALUES
    (1, 'Quiz', 20.0, 20.0),
    (1, 'Midterm', 30.0, 100.0),
    (1, 'End-Sem', 50.0, 100.0),
    (2, 'Project', 40.0, 50.0),
    (2, 'Final Exam', 60.0, 100.0)
ON DUPLICATE KEY UPDATE name = name;

-- Final verification
SELECT 
    'Total Courses' as item, COUNT(*) as count FROM courses
UNION ALL
SELECT 'Total Sections', COUNT(*) FROM sections
UNION ALL
SELECT 'MONSOON 2025 Sections', COUNT(*) FROM sections WHERE semester = 'MONSOON' AND year = 2025
UNION ALL
SELECT 'WINTER 2025 Sections', COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025;


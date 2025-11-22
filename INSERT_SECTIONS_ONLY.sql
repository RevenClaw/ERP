-- Insert sections only (assuming courses already exist)
-- Run this in your univ_erp database

USE univ_erp;

-- First, verify courses exist
SELECT 'Courses in database:' as info;
SELECT course_id, code, title FROM courses ORDER BY course_id;

-- Insert sections for MONSOON 2025 (deadline in January 2026)
-- Note: course_id values should match the course_id from the courses table
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'MONSOON', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2026-01-15'),
    (2, 1, 'MONSOON', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2026-01-15'),
    (3, 1, 'MONSOON', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2026-01-15'),
    (4, 1, 'MONSOON', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2026-01-15'),
    (5, 1, 'MONSOON', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2026-01-15');

-- Insert sections for WINTER 2025 (deadline passed 6 months ago - May 2025)
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'WINTER', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2025-05-15'),
    (2, 1, 'WINTER', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2025-05-15'),
    (3, 1, 'WINTER', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2025-05-15'),
    (4, 1, 'WINTER', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2025-05-15'),
    (5, 1, 'WINTER', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2025-05-15');

-- Verify sections were inserted
SELECT 'Sections inserted:' as info;
SELECT section_id, course_id, semester, year, section_code 
FROM sections 
ORDER BY semester, year, course_id;

-- Count by semester
SELECT semester, year, COUNT(*) as count 
FROM sections 
GROUP BY semester, year;


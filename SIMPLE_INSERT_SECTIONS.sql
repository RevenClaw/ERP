-- Simple script to insert sections
-- First, check your course_id values, then run the INSERT statements

USE univ_erp;

-- Step 1: Check what course_id values you have
SELECT course_id, code, title FROM courses ORDER BY course_id;

-- Step 2: Check if instructor_id = 1 exists
SELECT instructor_id FROM instructors WHERE instructor_id = 1;

-- Step 3: Insert sections for MONSOON 2025
-- Replace the course_id values (1, 2, 3, 4, 5) with your actual course_id values from Step 1
-- Make sure you have exactly 5 courses, or adjust the INSERT statements

INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'MONSOON', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2026-01-15'),
    (2, 1, 'MONSOON', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2026-01-15'),
    (3, 1, 'MONSOON', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2026-01-15'),
    (4, 1, 'MONSOON', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2026-01-15'),
    (5, 1, 'MONSOON', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2026-01-15');

-- Step 4: Insert sections for WINTER 2025
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'WINTER', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2025-05-15'),
    (2, 1, 'WINTER', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2025-05-15'),
    (3, 1, 'WINTER', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2025-05-15'),
    (4, 1, 'WINTER', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2025-05-15'),
    (5, 1, 'WINTER', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2025-05-15');

-- Step 5: Verify
SELECT section_id, course_id, semester, year, section_code 
FROM sections 
ORDER BY semester, year, course_id;


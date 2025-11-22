-- Insert sections with correct course_id values
-- This script first checks what course_id values exist, then inserts sections

USE univ_erp;

-- Step 1: Check what course_id values exist
SELECT '=== Current Courses ===' as info;
SELECT course_id, code, title FROM courses ORDER BY course_id;

-- Step 2: Check if instructor_id = 1 exists
SELECT '=== Instructor Check ===' as info;
SELECT instructor_id, user_id, department FROM instructors WHERE instructor_id = 1;

-- Step 3: Insert sections
-- IMPORTANT: The course_id values (1, 2, 3, 4, 5) must match your actual course_id values
-- If your course_id values are different, update the INSERT statements below

-- For MONSOON 2025
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
SELECT 
    course_id,  -- Use the actual course_id from courses table
    1 as instructor_id,  -- Assuming instructor_id = 1 exists
    'MONSOON' as semester,
    2025 as year,
    'A' as section_code,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN 'MON'
        WHEN 2 THEN 'TUE'
        WHEN 3 THEN 'WED'
        WHEN 4 THEN 'THU'
        WHEN 5 THEN 'FRI'
    END as day_of_week,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN '10:00:00'
        WHEN 2 THEN '12:00:00'
        WHEN 3 THEN '09:00:00'
        WHEN 4 THEN '14:00:00'
        WHEN 5 THEN '11:00:00'
    END as start_time,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN '11:30:00'
        WHEN 2 THEN '13:30:00'
        WHEN 3 THEN '10:30:00'
        WHEN 4 THEN '15:30:00'
        WHEN 5 THEN '12:30:00'
    END as end_time,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN 'C-201'
        WHEN 2 THEN 'C-105'
        WHEN 3 THEN 'M-301'
        WHEN 4 THEN 'E-205'
        WHEN 5 THEN 'C-302'
    END as room,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN 40
        WHEN 2 THEN 35
        WHEN 3 THEN 30
        WHEN 4 THEN 25
        WHEN 5 THEN 35
    END as capacity,
    '2026-01-15' as enrollment_deadline
FROM courses
ORDER BY course_id
LIMIT 5;

-- For WINTER 2025
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
SELECT 
    course_id,
    1 as instructor_id,
    'WINTER' as semester,
    2025 as year,
    'A' as section_code,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN 'MON'
        WHEN 2 THEN 'TUE'
        WHEN 3 THEN 'WED'
        WHEN 4 THEN 'THU'
        WHEN 5 THEN 'FRI'
    END as day_of_week,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN '10:00:00'
        WHEN 2 THEN '12:00:00'
        WHEN 3 THEN '09:00:00'
        WHEN 4 THEN '14:00:00'
        WHEN 5 THEN '11:00:00'
    END as start_time,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN '11:30:00'
        WHEN 2 THEN '13:30:00'
        WHEN 3 THEN '10:30:00'
        WHEN 4 THEN '15:30:00'
        WHEN 5 THEN '12:30:00'
    END as end_time,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN 'C-201'
        WHEN 2 THEN 'C-105'
        WHEN 3 THEN 'M-301'
        WHEN 4 THEN 'E-205'
        WHEN 5 THEN 'C-302'
    END as room,
    CASE (ROW_NUMBER() OVER (ORDER BY course_id))
        WHEN 1 THEN 40
        WHEN 2 THEN 35
        WHEN 3 THEN 30
        WHEN 4 THEN 25
        WHEN 5 THEN 35
    END as capacity,
    '2025-05-15' as enrollment_deadline
FROM courses
ORDER BY course_id
LIMIT 5;

-- Step 4: Verify sections were inserted
SELECT '=== Sections Inserted ===' as info;
SELECT section_id, course_id, semester, year, section_code, day_of_week
FROM sections 
ORDER BY semester, year, course_id;

SELECT '=== Count by Semester ===' as info;
SELECT semester, year, COUNT(*) as count 
FROM sections 
GROUP BY semester, year;


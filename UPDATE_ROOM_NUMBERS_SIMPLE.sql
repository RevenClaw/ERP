-- Simple script to update room numbers in existing database
-- This uses direct course codes to identify sections

USE univ_erp;

-- Update MONSOON 2025 sections by joining with courses
UPDATE sections s
JOIN courses c ON s.course_id = c.course_id
SET s.room = CASE c.code
    WHEN 'CS205' THEN 'C-102'
    WHEN 'MTH203' THEN 'C-201'
    WHEN 'ECE201' THEN 'B-003'
    WHEN 'CSE201' THEN 'A-102'
    ELSE s.room  -- Keep existing room for others
END
WHERE s.semester = 'MONSOON' AND s.year = 2025
  AND c.code IN ('CS205', 'MTH203', 'ECE201', 'CSE201');

-- Update WINTER 2025 sections
UPDATE sections s
JOIN courses c ON s.course_id = c.course_id
SET s.room = CASE c.code
    WHEN 'MTH301' THEN 'C-102'
    WHEN 'ECE301' THEN 'C-201'
    WHEN 'CS303' THEN 'B-003'
    WHEN 'PHY201' THEN 'A-102'
    ELSE s.room  -- Keep existing room for others
END
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND c.code IN ('MTH301', 'ECE301', 'CS303', 'PHY201');

-- Verify the updates
SELECT 
    s.semester,
    s.year,
    c.code,
    c.title,
    s.section_code,
    s.room,
    s.day_of_week,
    s.start_time,
    s.end_time
FROM sections s
JOIN courses c ON s.course_id = c.course_id
WHERE (s.semester = 'MONSOON' OR s.semester = 'WINTER') AND s.year = 2025
ORDER BY s.semester, c.code;


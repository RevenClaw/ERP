-- Script to update room numbers in existing database
-- Run this to update the room numbers without losing data

USE univ_erp;

-- Update MONSOON 2025 sections
UPDATE sections 
SET room = 'C-102'
WHERE semester = 'MONSOON' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'CS205' LIMIT 1);

UPDATE sections 
SET room = 'C-201'
WHERE semester = 'MONSOON' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'MTH203' LIMIT 1);

UPDATE sections 
SET room = 'B-003'
WHERE semester = 'MONSOON' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'ECE201' LIMIT 1);

UPDATE sections 
SET room = 'A-102'
WHERE semester = 'MONSOON' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'CSE201' LIMIT 1);

-- Update WINTER 2025 sections
UPDATE sections 
SET room = 'C-102'
WHERE semester = 'WINTER' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'MTH301' LIMIT 1);

UPDATE sections 
SET room = 'C-201'
WHERE semester = 'WINTER' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'ECE301' LIMIT 1);

UPDATE sections 
SET room = 'B-003'
WHERE semester = 'WINTER' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'CS303' LIMIT 1);

UPDATE sections 
SET room = 'A-102'
WHERE semester = 'WINTER' AND year = 2025 
  AND course_id = (SELECT course_id FROM courses WHERE code = 'PHY201' LIMIT 1);

-- Verify the updates
SELECT 
    s.semester,
    s.year,
    c.code,
    c.title,
    s.section_code,
    s.room
FROM sections s
JOIN courses c ON s.course_id = c.course_id
WHERE (s.semester = 'MONSOON' OR s.semester = 'WINTER') AND s.year = 2025
ORDER BY s.semester, c.code;


-- Run this to verify your database has the data
-- Use this in your univ_erp database

USE univ_erp;

-- Check courses
SELECT 'Courses' as table_name, COUNT(*) as count FROM courses
UNION ALL
SELECT 'Sections', COUNT(*) FROM sections
UNION ALL
SELECT 'Students', COUNT(*) FROM students
UNION ALL
SELECT 'Instructors', COUNT(*) FROM instructors;

-- Show all courses
SELECT course_id, code, title, credits FROM courses;

-- Show all sections
SELECT section_id, course_id, semester, year, section_code, enrollment_deadline 
FROM sections 
ORDER BY semester, year, course_id;

-- Check if schema is updated
SHOW COLUMNS FROM sections LIKE 'semester';


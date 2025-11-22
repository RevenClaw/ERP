-- Quick check for what's actually in the database

USE univ_erp;

-- Check what semester values exist
SELECT DISTINCT semester as semester_value FROM sections;

-- Check what years exist  
SELECT DISTINCT year as year_value FROM sections;

-- Check MONSOON 2025 specifically
SELECT 'MONSOON 2025 sections:' as check_type, COUNT(*) as count 
FROM sections 
WHERE semester = 'MONSOON' AND year = 2025;

-- Show all sections with their semester and year
SELECT section_id, course_id, semester, year, section_code 
FROM sections 
ORDER BY year, semester, course_id;


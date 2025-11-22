-- Diagnostic queries to check why courses aren't showing

USE univ_erp;

-- 1. Check courses exist
SELECT '=== COURSES ===' as info;
SELECT course_id, code, title, credits FROM courses;

-- 2. Check sections exist and their semester values
SELECT '=== SECTIONS (all) ===' as info;
SELECT section_id, course_id, semester, year, section_code, enrollment_deadline 
FROM sections 
ORDER BY semester, year, course_id;

-- 3. Check MONSOON 2025 sections specifically
SELECT '=== MONSOON 2025 SECTIONS ===' as info;
SELECT section_id, course_id, semester, year, section_code 
FROM sections 
WHERE semester = 'MONSOON' AND year = 2025;

-- 4. Check what semester values actually exist
SELECT '=== UNIQUE SEMESTER VALUES ===' as info;
SELECT DISTINCT semester FROM sections;

-- 5. Check if instructor_id = 1 exists
SELECT '=== INSTRUCTOR ID 1 ===' as info;
SELECT instructor_id, user_id, department, title FROM instructors WHERE instructor_id = 1;

-- 6. Check if courses match section course_ids
SELECT '=== COURSE-SECTION MATCH ===' as info;
SELECT s.section_id, s.course_id, c.code, c.title, s.semester, s.year
FROM sections s
LEFT JOIN courses c ON s.course_id = c.course_id
WHERE s.semester = 'MONSOON' AND s.year = 2025;

-- 7. Check enrollments count
SELECT '=== ENROLLMENT COUNTS ===' as info;
SELECT section_id, COUNT(*) as enrolled_count 
FROM enrollments 
WHERE status = 'ENROLLED'
GROUP BY section_id;


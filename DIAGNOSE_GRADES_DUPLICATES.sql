-- Diagnostic script to find ALL sources of duplicates
USE univ_erp;

-- Check 1: Duplicate assessments
SELECT '=== DUPLICATE ASSESSMENTS ===' as check_type;
SELECT section_id, name, COUNT(*) as count, GROUP_CONCAT(assessment_id) as assessment_ids
FROM assessments
WHERE section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
GROUP BY section_id, name
HAVING COUNT(*) > 1;

-- Check 2: Duplicate enrollments
SELECT '=== DUPLICATE ENROLLMENTS ===' as check_type;
SELECT student_id, section_id, COUNT(*) as count, GROUP_CONCAT(enrollment_id) as enrollment_ids
FROM enrollments
WHERE section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
GROUP BY student_id, section_id
HAVING COUNT(*) > 1;

-- Check 3: Duplicate grades
SELECT '=== DUPLICATE GRADES ===' as check_type;
SELECT enrollment_id, assessment_id, COUNT(*) as count, GROUP_CONCAT(grade_id) as grade_ids
FROM grades
WHERE enrollment_id IN (
    SELECT e.enrollment_id 
    FROM enrollments e
    JOIN sections s ON e.section_id = s.section_id
    WHERE s.semester = 'WINTER' AND s.year = 2025
)
GROUP BY enrollment_id, assessment_id
HAVING COUNT(*) > 1;

-- Check 4: Show all WINTER grades with details
SELECT '=== ALL WINTER GRADES (with details) ===' as check_type;
SELECT 
    g.grade_id,
    e.enrollment_id,
    st.roll_no,
    c.code,
    c.title,
    s.section_id,
    a.assessment_id,
    a.name as assessment_name,
    g.score,
    a.max_score
FROM enrollments e
JOIN students st ON e.student_id = st.student_id
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
JOIN assessments a ON a.section_id = s.section_id
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
ORDER BY st.roll_no, c.code, a.name, g.grade_id;

-- Check 5: Count grades per enrollment-assessment
SELECT '=== GRADE COUNTS PER ENROLLMENT-ASSESSMENT ===' as check_type;
SELECT 
    e.enrollment_id,
    st.roll_no,
    c.code,
    a.name as assessment,
    COUNT(*) as grade_count
FROM enrollments e
JOIN students st ON e.student_id = st.student_id
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
JOIN assessments a ON a.section_id = s.section_id
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
GROUP BY e.enrollment_id, a.assessment_id
HAVING COUNT(*) > 1
ORDER BY st.roll_no, c.code, a.name;


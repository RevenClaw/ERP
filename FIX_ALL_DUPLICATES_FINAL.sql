-- Final comprehensive fix for all duplicates
-- This removes ALL duplicates from assessments, enrollments, and grades

USE univ_erp;

-- Step 1: Remove duplicate assessments (keep the one with lowest assessment_id)
-- This is the root cause - multiple assessments with same name in same section
DELETE a1 FROM assessments a1
INNER JOIN assessments a2 
WHERE a1.assessment_id > a2.assessment_id 
  AND a1.section_id = a2.section_id 
  AND a1.name = a2.name;

-- Step 2: Remove duplicate enrollments (keep the one with lowest enrollment_id)
DELETE e1 FROM enrollments e1
INNER JOIN enrollments e2 
WHERE e1.enrollment_id > e2.enrollment_id 
  AND e1.student_id = e2.student_id 
  AND e1.section_id = e2.section_id;

-- Step 3: Remove duplicate grades (keep the one with lowest grade_id)
-- This will also remove grades that reference deleted assessments
DELETE g1 FROM grades g1
INNER JOIN grades g2 
WHERE g1.grade_id > g2.grade_id 
  AND g1.enrollment_id = g2.enrollment_id 
  AND g1.assessment_id = g2.assessment_id;

-- Step 4: Clean up orphaned grades (grades for assessments that no longer exist)
DELETE FROM grades 
WHERE assessment_id NOT IN (SELECT assessment_id FROM assessments);

-- Step 5: Recalculate all final grades for WINTER
DELETE FROM final_grades 
WHERE enrollment_id IN (
    SELECT e.enrollment_id 
    FROM enrollments e
    JOIN sections s ON e.section_id = s.section_id
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

INSERT INTO final_grades (enrollment_id, final_score, letter_grade)
SELECT 
    e.enrollment_id,
    ROUND(SUM(g.score * a.weight_percent / 100.0), 2) as final_score,
    CASE 
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 90 THEN 'A'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 85 THEN 'A-'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 80 THEN 'B+'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 75 THEN 'B'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 70 THEN 'B-'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 65 THEN 'C+'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 60 THEN 'C'
        ELSE 'C-'
    END as letter_grade
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED'
GROUP BY e.enrollment_id;

-- Step 6: Verify - check for remaining duplicates
SELECT 'Checking for duplicate assessments...' as check_type;
SELECT section_id, name, COUNT(*) as count
FROM assessments
GROUP BY section_id, name
HAVING COUNT(*) > 1;

SELECT 'Checking for duplicate enrollments...' as check_type;
SELECT student_id, section_id, COUNT(*) as count
FROM enrollments
GROUP BY student_id, section_id
HAVING COUNT(*) > 1;

SELECT 'Checking for duplicate grades...' as check_type;
SELECT enrollment_id, assessment_id, COUNT(*) as count
FROM grades
GROUP BY enrollment_id, assessment_id
HAVING COUNT(*) > 1;

-- If all three queries above return no rows, duplicates are fixed!

-- Step 7: Show final result
SELECT 
    st.roll_no,
    c.code,
    c.title,
    s.section_code,
    a.name as assessment,
    g.score,
    a.max_score,
    fg.final_score,
    fg.letter_grade
FROM enrollments e
JOIN students st ON e.student_id = st.student_id
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
JOIN assessments a ON a.section_id = s.section_id
LEFT JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
LEFT JOIN final_grades fg ON fg.enrollment_id = e.enrollment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
ORDER BY st.roll_no, c.code, a.assessment_id;


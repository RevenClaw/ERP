-- Immediate fix for duplicate assessments and grades
-- Run this to clean up all duplicates RIGHT NOW

USE univ_erp;

-- Step 1: Remove ALL duplicate assessments (keep only the first one per section+name)
-- This is the root cause - multiple assessments with same name in same section
DELETE a1 FROM assessments a1
INNER JOIN assessments a2 
WHERE a1.assessment_id > a2.assessment_id 
  AND a1.section_id = a2.section_id 
  AND a1.name = a2.name;

-- Step 2: Remove ALL duplicate enrollments (keep only the first one per student+section)
DELETE e1 FROM enrollments e1
INNER JOIN enrollments e2 
WHERE e1.enrollment_id > e2.enrollment_id 
  AND e1.student_id = e2.student_id 
  AND e1.section_id = e2.section_id;

-- Step 3: Remove ALL duplicate grades (keep only the first one per enrollment+assessment)
DELETE g1 FROM grades g1
INNER JOIN grades g2 
WHERE g1.grade_id > g2.grade_id 
  AND g1.enrollment_id = g2.enrollment_id 
  AND g1.assessment_id = g2.assessment_id;

-- Step 4: Clean up orphaned grades (grades pointing to deleted assessments)
DELETE FROM grades 
WHERE assessment_id NOT IN (SELECT assessment_id FROM assessments);

-- Step 5: Recalculate ALL final grades for WINTER
DELETE FROM final_grades 
WHERE enrollment_id IN (
    SELECT DISTINCT e.enrollment_id 
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

-- Verify: Should return 0 rows if no duplicates
SELECT 'Duplicate assessments:' as check_type;
SELECT section_id, name, COUNT(*) as count
FROM assessments
GROUP BY section_id, name
HAVING COUNT(*) > 1;

SELECT 'Duplicate enrollments:' as check_type;
SELECT student_id, section_id, COUNT(*) as count
FROM enrollments
GROUP BY student_id, section_id
HAVING COUNT(*) > 1;

SELECT 'Duplicate grades:' as check_type;
SELECT enrollment_id, assessment_id, COUNT(*) as count
FROM grades
GROUP BY enrollment_id, assessment_id
HAVING COUNT(*) > 1;


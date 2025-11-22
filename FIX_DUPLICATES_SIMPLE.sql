-- Simple and direct fix for duplicate WINTER grades
-- This removes all duplicates by keeping only the lowest grade_id for each enrollment-assessment pair

USE univ_erp;

-- Step 1: Remove duplicate grades (keep the one with lowest grade_id)
DELETE g1 FROM grades g1
INNER JOIN grades g2 
WHERE g1.grade_id > g2.grade_id 
  AND g1.enrollment_id = g2.enrollment_id 
  AND g1.assessment_id = g2.assessment_id
  AND g1.enrollment_id IN (
      SELECT e.enrollment_id 
      FROM enrollments e
      JOIN sections s ON e.section_id = s.section_id
      WHERE s.semester = 'WINTER' AND s.year = 2025
  );

-- Step 2: Remove duplicate assessments (keep the one with lowest assessment_id)
DELETE a1 FROM assessments a1
INNER JOIN assessments a2 
WHERE a1.assessment_id > a2.assessment_id 
  AND a1.section_id = a2.section_id 
  AND a1.name = a2.name
  AND a1.section_id IN (
      SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
  );

-- Step 3: Remove duplicate enrollments (keep the one with lowest enrollment_id)
DELETE e1 FROM enrollments e1
INNER JOIN enrollments e2 
WHERE e1.enrollment_id > e2.enrollment_id 
  AND e1.student_id = e2.student_id 
  AND e1.section_id = e2.section_id
  AND e1.section_id IN (
      SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
  );

-- Step 4: Recalculate final grades
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

-- Verify: Check for remaining duplicates
SELECT 
    e.enrollment_id,
    a.assessment_id,
    COUNT(*) as duplicate_count
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
GROUP BY e.enrollment_id, a.assessment_id
HAVING COUNT(*) > 1;

-- If the above returns no rows, duplicates are fixed!


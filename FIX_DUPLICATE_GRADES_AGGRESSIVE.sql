-- Aggressive fix for duplicate WINTER grades
-- This removes ALL duplicates and keeps only the first one

USE univ_erp;

-- Step 1: Check for duplicate assessments (same name in same section)
SELECT 
    section_id,
    name,
    COUNT(*) as count
FROM assessments
WHERE section_id IN (
    SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
)
GROUP BY section_id, name
HAVING COUNT(*) > 1;

-- Step 2: Check for duplicate enrollments (same student in same section)
SELECT 
    student_id,
    section_id,
    COUNT(*) as count
FROM enrollments
WHERE section_id IN (
    SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
)
GROUP BY student_id, section_id
HAVING COUNT(*) > 1;

-- Step 3: Delete ALL WINTER grades and final grades
DELETE FROM final_grades 
WHERE enrollment_id IN (
    SELECT e.enrollment_id 
    FROM enrollments e
    JOIN sections s ON e.section_id = s.section_id
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

-- Delete duplicate grades, keeping only the one with the lowest grade_id
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

-- Step 4: Delete duplicate assessments (keep only the first one)
DELETE a1 FROM assessments a1
INNER JOIN assessments a2 
WHERE a1.assessment_id > a2.assessment_id 
  AND a1.section_id = a2.section_id 
  AND a1.name = a2.name
  AND a1.section_id IN (
      SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
  );

-- Step 5: Delete duplicate enrollments (keep only the first one)
DELETE e1 FROM enrollments e1
INNER JOIN enrollments e2 
WHERE e1.enrollment_id > e2.enrollment_id 
  AND e1.student_id = e2.student_id 
  AND e1.section_id = e2.section_id
  AND e1.section_id IN (
      SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
  );

-- Step 6: Re-insert grades with proper deduplication
-- First, ensure we only have one assessment per name per section
INSERT IGNORE INTO grades (enrollment_id, assessment_id, score)
SELECT 
    e.enrollment_id,
    MIN(a.assessment_id) as assessment_id,  -- Use MIN to pick one if duplicates exist
    CASE 
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Final Exam' THEN 92.0
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Final Exam' THEN 89.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Final Exam' THEN 87.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Final Exam' THEN 83.0
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Final Exam' THEN 80.0
    END as score
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
JOIN (
    SELECT section_id, name, MIN(assessment_id) as assessment_id
    FROM assessments
    WHERE section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
    GROUP BY section_id, name
) a ON a.section_id = s.section_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED'
  AND CASE 
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Final Exam' THEN 92.0
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Final Exam' THEN 89.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Final Exam' THEN 87.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Final Exam' THEN 83.0
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Final Exam' THEN 80.0
        ELSE NULL
    END IS NOT NULL
GROUP BY e.enrollment_id, a.name;

-- Step 7: Recalculate final grades
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
JOIN (
    SELECT section_id, name, MIN(assessment_id) as assessment_id
    FROM assessments
    WHERE section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
    GROUP BY section_id, name
) a_grouped ON a_grouped.section_id = s.section_id
JOIN assessments a ON a.assessment_id = a_grouped.assessment_id
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED'
GROUP BY e.enrollment_id;

-- Step 8: Verify - should show no duplicates
SELECT 
    st.roll_no,
    c.code,
    c.title,
    a.name as assessment,
    COUNT(*) as count
FROM enrollments e
JOIN students st ON e.student_id = st.student_id
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
JOIN assessments a ON a.section_id = s.section_id
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
GROUP BY st.roll_no, c.code, a.name
HAVING COUNT(*) > 1;

-- If the above returns no rows, duplicates are fixed!
-- Show final result
SELECT 
    st.roll_no,
    c.code,
    c.title,
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
JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
LEFT JOIN final_grades fg ON fg.enrollment_id = e.enrollment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
ORDER BY st.roll_no, c.code, a.assessment_id;


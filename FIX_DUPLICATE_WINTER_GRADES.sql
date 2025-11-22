-- Script to fix duplicate WINTER grades
-- This removes duplicates and re-inserts correct grades

USE univ_erp;

-- Step 1: Delete all existing WINTER grades to start fresh
DELETE FROM final_grades 
WHERE enrollment_id IN (
    SELECT e.enrollment_id 
    FROM enrollments e
    JOIN sections s ON e.section_id = s.section_id
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

DELETE FROM grades 
WHERE enrollment_id IN (
    SELECT e.enrollment_id 
    FROM enrollments e
    JOIN sections s ON e.section_id = s.section_id
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

-- Step 2: Re-insert WINTER grades using INSERT IGNORE to prevent duplicates
-- This ensures only one grade per enrollment-assessment pair
INSERT IGNORE INTO grades (enrollment_id, assessment_id, score)
SELECT DISTINCT
    e.enrollment_id,
    a.assessment_id,
    CASE 
        -- Student 1, CS301
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Final Exam' THEN 92.0
        -- Student 1, MTH301
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Final Exam' THEN 89.0
        -- Student 1, ECE301
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Final Exam' THEN 87.0
        -- Student 2, CS303
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Final Exam' THEN 83.0
        -- Student 2, PHY201
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND c.code = 'PHY201' AND a.name = 'Final Exam' THEN 80.0
    END
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
JOIN assessments a ON a.section_id = s.section_id
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
    END IS NOT NULL;

-- Step 3: Recalculate and insert final grades
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
GROUP BY e.enrollment_id
ON DUPLICATE KEY UPDATE 
    final_score = VALUES(final_score),
    letter_grade = VALUES(letter_grade);

-- Step 4: Verify - check for duplicates
SELECT 
    e.enrollment_id,
    st.roll_no,
    c.code,
    c.title,
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
HAVING COUNT(*) > 1;

-- If the above query returns no rows, there are no duplicates
-- If it returns rows, we need to investigate further

-- Step 5: Show all WINTER grades to verify
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


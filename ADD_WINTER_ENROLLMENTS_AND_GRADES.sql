-- Script to add WINTER enrollments and grades
-- Run this AFTER the main erp_seed.sql has been executed
-- This script assumes WINTER sections have been created

USE univ_erp;

-- Step 1: Enroll students in WINTER sections
-- Student 1 (student_id = 1) in first 3 WINTER sections
-- Student 2 (student_id = 2) in last 2 WINTER sections
INSERT INTO enrollments (student_id, section_id, status)
SELECT 
    CASE 
        WHEN (@row_num := @row_num + 1) <= 3 THEN 1
        ELSE 2
    END,
    section_id,
    'ENROLLED'
FROM sections, (SELECT @row_num := 0) r
WHERE semester = 'WINTER' AND year = 2025
ORDER BY section_id;

-- Step 2: Create assessments for WINTER sections (if not already created)
INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Quiz', 20.0, 20.0
FROM sections 
WHERE semester = 'WINTER' AND year = 2025
  AND section_id NOT IN (SELECT DISTINCT section_id FROM assessments WHERE section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025));

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Midterm', 30.0, 100.0
FROM sections 
WHERE semester = 'WINTER' AND year = 2025
  AND section_id NOT IN (SELECT DISTINCT section_id FROM assessments WHERE name = 'Midterm' AND section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025));

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Final Exam', 50.0, 100.0
FROM sections 
WHERE semester = 'WINTER' AND year = 2025
  AND section_id NOT IN (SELECT DISTINCT section_id FROM assessments WHERE name = 'Final Exam' AND section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025));

-- Step 3: Add grades for WINTER enrollments
-- Using course codes to identify which grades to assign
INSERT INTO grades (enrollment_id, assessment_id, score)
SELECT 
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

-- Step 4: Calculate and insert final grades for WINTER
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

-- Verify
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


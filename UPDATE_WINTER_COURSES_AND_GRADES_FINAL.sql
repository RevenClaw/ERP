-- Script to update WINTER courses with different courses and add random grades
-- This script assumes you'll run it step by step and adjust course/section IDs as needed

USE univ_erp;

-- Step 1: Add 5 new courses for WINTER semester
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS301', 'Computer Networks', 3.0, 'Fundamentals of computer networking, protocols, and network architecture.'),
    ('MTH301', 'Linear Algebra', 4.0, 'Vector spaces, matrices, eigenvalues, and linear transformations.'),
    ('ECE301', 'Digital Signal Processing', 3.0, 'Signal analysis, filtering, and digital signal processing techniques.'),
    ('CS303', 'Software Engineering', 4.0, 'Software development lifecycle, design patterns, and project management.'),
    ('PHY201', 'Electromagnetic Theory', 4.0, 'Maxwell equations, electromagnetic fields, and wave propagation.');

-- Step 2: Get the new course IDs (run this query to see them)
-- SELECT course_id, code, title FROM courses WHERE code IN ('CS301', 'MTH301', 'ECE301', 'CS303', 'PHY201');

-- Step 3: Delete existing WINTER 2025 sections and related data
DELETE FROM final_grades WHERE enrollment_id IN (
    SELECT e.enrollment_id FROM enrollments e 
    JOIN sections s ON e.section_id = s.section_id 
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

DELETE FROM grades WHERE enrollment_id IN (
    SELECT e.enrollment_id FROM enrollments e 
    JOIN sections s ON e.section_id = s.section_id 
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

DELETE FROM assessments WHERE section_id IN (
    SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
);

DELETE FROM enrollments WHERE section_id IN (
    SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025
);

DELETE FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Step 4: Insert WINTER sections using the new courses
-- This uses a subquery to get course IDs dynamically
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
SELECT 
    course_id,
    1,
    'WINTER',
    2025,
    'A',
    CASE code
        WHEN 'CS301' THEN 'MON'
        WHEN 'MTH301' THEN 'TUE'
        WHEN 'ECE301' THEN 'WED'
        WHEN 'CS303' THEN 'THU'
        WHEN 'PHY201' THEN 'FRI'
    END,
    CASE code
        WHEN 'CS301' THEN '10:00:00'
        WHEN 'MTH301' THEN '12:00:00'
        WHEN 'ECE301' THEN '09:00:00'
        WHEN 'CS303' THEN '14:00:00'
        WHEN 'PHY201' THEN '11:00:00'
    END,
    CASE code
        WHEN 'CS301' THEN '13:30:00'
        WHEN 'MTH301' THEN '15:00:00'
        WHEN 'ECE301' THEN '10:30:00'
        WHEN 'CS303' THEN '15:30:00'
        WHEN 'PHY201' THEN '12:30:00'
    END,
    CASE code
        WHEN 'CS301' THEN 'C-201'
        WHEN 'MTH301' THEN 'C-105'
        WHEN 'ECE301' THEN 'M-301'
        WHEN 'CS303' THEN 'E-205'
        WHEN 'PHY201' THEN 'C-302'
    END,
    CASE code
        WHEN 'CS301' THEN 40
        WHEN 'MTH301' THEN 35
        WHEN 'ECE301' THEN 30
        WHEN 'CS303' THEN 25
        WHEN 'PHY201' THEN 35
    END,
    '2025-05-15'
FROM courses
WHERE code IN ('CS301', 'MTH301', 'ECE301', 'CS303', 'PHY201')
ORDER BY code;

-- Step 5: Get section IDs for verification
-- SELECT section_id, c.code, c.title FROM sections s JOIN courses c ON s.course_id = c.course_id WHERE s.semester = 'WINTER' AND s.year = 2025 ORDER BY s.section_id;

-- Step 6: Enroll students in WINTER sections
-- Student 1 (student_id = 1) in first 3 sections, Student 2 (student_id = 2) in last 2 sections
INSERT INTO enrollments (student_id, section_id, status)
SELECT 
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY section_id) <= 3 THEN 1
        ELSE 2
    END AS student_id,
    section_id,
    'ENROLLED'
FROM sections
WHERE semester = 'WINTER' AND year = 2025
ORDER BY section_id;

-- Step 7: Create assessments for each WINTER section
-- Quiz (20%), Midterm (30%), Final Exam (50%)
INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Quiz', 20.0, 20.0
FROM sections WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Midterm', 30.0, 100.0
FROM sections WHERE sections.semester = 'WINTER' AND sections.year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Final Exam', 50.0, 100.0
FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Step 8: Add grades for WINTER enrollments
-- We'll use a stored procedure approach or multiple INSERTs with specific values
-- For simplicity, let's use a temp table approach or direct INSERTs

-- First, let's create grades using a join that gives us predictable values
INSERT INTO grades (enrollment_id, assessment_id, score)
SELECT 
    e.enrollment_id,
    a.assessment_id,
    -- Generate scores based on student and section position
    CASE 
        -- Student 1, first section (CS301) - good grades
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 1 AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 1 AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 1 AND a.name = 'Final Exam' THEN 92.0
        -- Student 1, second section (MTH301)
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 2 AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 2 AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 2 AND a.name = 'Final Exam' THEN 89.0
        -- Student 1, third section (ECE301)
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 3 AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 3 AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 3 AND a.name = 'Final Exam' THEN 87.0
        -- Student 2, fourth section (CS303)
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 4 AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 4 AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 4 AND a.name = 'Final Exam' THEN 83.0
        -- Student 2, fifth section (PHY201)
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 5 AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 5 AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 5 AND a.name = 'Final Exam' THEN 80.0
        ELSE NULL
    END
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED'
  AND CASE 
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 1 AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 1 AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 1 AND a.name = 'Final Exam' THEN 92.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 2 AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 2 AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 2 AND a.name = 'Final Exam' THEN 89.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 3 AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 3 AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 3 AND a.name = 'Final Exam' THEN 87.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 4 AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 4 AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 4 AND a.name = 'Final Exam' THEN 83.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 5 AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 5 AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND (SELECT COUNT(*) FROM sections WHERE semester = 'WINTER' AND year = 2025 AND section_id <= s.section_id) = 5 AND a.name = 'Final Exam' THEN 80.0
        ELSE NULL
    END IS NOT NULL;

-- Step 9: Calculate and insert final grades
-- Calculate weighted average and assign letter grades
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

-- Step 10: Verify the data
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
LEFT JOIN assessments a ON a.section_id = s.section_id
LEFT JOIN grades g ON g.enrollment_id = e.enrollment_id AND g.assessment_id = a.assessment_id
LEFT JOIN final_grades fg ON fg.enrollment_id = e.enrollment_id
WHERE s.semester = 'WINTER' AND s.year = 2025
ORDER BY st.roll_no, c.code, a.assessment_id;


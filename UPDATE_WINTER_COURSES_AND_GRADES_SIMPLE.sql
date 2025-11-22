-- Simple script to update WINTER courses and add grades
-- Run this after verifying your course IDs

USE univ_erp;

-- Step 1: Add 5 new courses for WINTER semester
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS301', 'Computer Networks', 3.0, 'Fundamentals of computer networking, protocols, and network architecture.'),
    ('MTH301', 'Linear Algebra', 4.0, 'Vector spaces, matrices, eigenvalues, and linear transformations.'),
    ('ECE301', 'Digital Signal Processing', 3.0, 'Signal analysis, filtering, and digital signal processing techniques.'),
    ('CS303', 'Software Engineering', 4.0, 'Software development lifecycle, design patterns, and project management.'),
    ('PHY201', 'Electromagnetic Theory', 4.0, 'Maxwell equations, electromagnetic fields, and wave propagation.');

-- Step 2: Get the course IDs for the new courses
-- Run this query to see the course IDs:
-- SELECT course_id, code, title FROM courses WHERE code IN ('CS301', 'MTH301', 'ECE301', 'CS303', 'PHY201');

-- Step 3: Delete existing WINTER 2025 sections (if any)
DELETE FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Step 4: Insert WINTER sections using the new courses
-- Replace the course_id values (6, 7, 8, 9, 10) with the actual IDs from Step 2
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
SELECT 
    course_id,
    1,
    'WINTER',
    2025,
    'A',
    CASE 
        WHEN code = 'CS301' THEN 'MON'
        WHEN code = 'MTH301' THEN 'TUE'
        WHEN code = 'ECE301' THEN 'WED'
        WHEN code = 'CS303' THEN 'THU'
        WHEN code = 'PHY201' THEN 'FRI'
    END,
    CASE 
        WHEN code = 'CS301' THEN '10:00:00'
        WHEN code = 'MTH301' THEN '12:00:00'
        WHEN code = 'ECE301' THEN '09:00:00'
        WHEN code = 'CS303' THEN '14:00:00'
        WHEN code = 'PHY201' THEN '11:00:00'
    END,
    CASE 
        WHEN code = 'CS301' THEN '13:30:00'
        WHEN code = 'MTH301' THEN '15:00:00'
        WHEN code = 'ECE301' THEN '10:30:00'
        WHEN code = 'CS303' THEN '15:30:00'
        WHEN code = 'PHY201' THEN '12:30:00'
    END,
    CASE 
        WHEN code = 'CS301' THEN 'C-201'
        WHEN code = 'MTH301' THEN 'C-105'
        WHEN code = 'ECE301' THEN 'M-301'
        WHEN code = 'CS303' THEN 'E-205'
        WHEN code = 'PHY201' THEN 'C-302'
    END,
    CASE 
        WHEN code = 'CS301' THEN 40
        WHEN code = 'MTH301' THEN 35
        WHEN code = 'ECE301' THEN 30
        WHEN code = 'CS303' THEN 25
        WHEN code = 'PHY201' THEN 35
    END,
    '2025-05-15'
FROM courses
WHERE code IN ('CS301', 'MTH301', 'ECE301', 'CS303', 'PHY201');

-- Step 5: Enroll students in WINTER sections
-- Get section IDs for WINTER 2025
-- SELECT section_id, c.code, c.title FROM sections s JOIN courses c ON s.course_id = c.course_id WHERE s.semester = 'WINTER' AND s.year = 2025;

-- Enroll student 1 (UG2023001) in first 3 WINTER sections
-- Enroll student 2 (UG2023002) in last 2 WINTER sections
INSERT INTO enrollments (student_id, section_id, status)
SELECT 
    s.student_id,
    sec.section_id,
    'ENROLLED'
FROM students s
CROSS JOIN (
    SELECT section_id, ROW_NUMBER() OVER (ORDER BY section_id) as rn
    FROM sections
    WHERE semester = 'WINTER' AND year = 2025
) sec
WHERE (s.student_id = 1 AND sec.rn <= 3)
   OR (s.student_id = 2 AND sec.rn > 3);

-- Step 6: Create assessments for WINTER sections
-- For each WINTER section, create 3 assessments: Quiz, Midterm, Final
INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT 
    section_id,
    'Quiz',
    20.0,
    20.0
FROM sections
WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT 
    section_id,
    'Midterm',
    30.0,
    100.0
FROM sections
WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT 
    section_id,
    'Final Exam',
    50.0,
    100.0
FROM sections
WHERE semester = 'WINTER' AND year = 2025;

-- Step 7: Add random grades for WINTER enrollments
-- Generate random scores between 60-95 for each assessment
INSERT INTO grades (enrollment_id, assessment_id, score)
SELECT 
    e.enrollment_id,
    a.assessment_id,
    CASE a.name
        WHEN 'Quiz' THEN 15.0 + (RAND() * 5.0)  -- 15-20
        WHEN 'Midterm' THEN 65.0 + (RAND() * 25.0)  -- 65-90
        WHEN 'Final Exam' THEN 70.0 + (RAND() * 20.0)  -- 70-90
    END
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED';

-- Step 8: Calculate and insert final grades
-- Calculate final score based on weighted assessments and assign letter grades
INSERT INTO final_grades (enrollment_id, final_score, letter_grade)
SELECT 
    e.enrollment_id,
    ROUND(
        SUM(g.score * a.weight_percent / 100.0), 2
    ) as final_score,
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

-- Verify the data
SELECT 
    c.code,
    c.title,
    s.section_code,
    st.roll_no,
    a.name as assessment,
    g.score,
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


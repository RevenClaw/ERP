-- Simple script to update WINTER courses and add grades
-- Run this after the sections are created

USE univ_erp;

-- Step 1: Add 5 new courses for WINTER semester
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS301', 'Computer Networks', 3.0, 'Fundamentals of computer networking, protocols, and network architecture.'),
    ('MTH301', 'Linear Algebra', 4.0, 'Vector spaces, matrices, eigenvalues, and linear transformations.'),
    ('ECE301', 'Digital Signal Processing', 3.0, 'Signal analysis, filtering, and digital signal processing techniques.'),
    ('CS303', 'Software Engineering', 4.0, 'Software development lifecycle, design patterns, and project management.'),
    ('PHY201', 'Electromagnetic Theory', 4.0, 'Maxwell equations, electromagnetic fields, and wave propagation.');

-- Step 2: Clean up existing WINTER data
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

-- Step 3: Insert WINTER sections
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
SELECT 
    course_id, 1, 'WINTER', 2025, 'A',
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

-- Step 4: Enroll students (student 1 in first 3, student 2 in last 2)
INSERT INTO enrollments (student_id, section_id, status)
SELECT 
    CASE WHEN (@row_number := @row_number + 1) <= 3 THEN 1 ELSE 2 END,
    section_id,
    'ENROLLED'
FROM sections, (SELECT @row_number := 0) r
WHERE semester = 'WINTER' AND year = 2025
ORDER BY section_id;

-- Step 5: Create assessments
INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Quiz', 20.0, 20.0 FROM sections WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Midterm', 30.0, 100.0 FROM sections WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Final Exam', 50.0, 100.0 FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Step 6: Add grades using a simpler approach
-- We'll use the enrollment and assessment IDs directly
INSERT INTO grades (enrollment_id, assessment_id, score)
SELECT 
    e.enrollment_id,
    a.assessment_id,
    -- Use modulo to create variation based on IDs
    CASE a.name
        WHEN 'Quiz' THEN 16.0 + (e.enrollment_id % 5)  -- 16-20
        WHEN 'Midterm' THEN 70.0 + (e.enrollment_id * 3 % 25)  -- 70-95
        WHEN 'Final Exam' THEN 75.0 + (e.enrollment_id * 2 % 20)  -- 75-95
    END
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED';

-- Step 7: Calculate final grades
INSERT INTO final_grades (enrollment_id, final_score, letter_grade)
SELECT 
    e.enrollment_id,
    ROUND(SUM(g.score * a.weight_percent / 100.0), 2),
    CASE 
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 90 THEN 'A'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 85 THEN 'A-'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 80 THEN 'B+'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 75 THEN 'B'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 70 THEN 'B-'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 65 THEN 'C+'
        WHEN SUM(g.score * a.weight_percent / 100.0) >= 60 THEN 'C'
        ELSE 'C-'
    END
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
    a.name,
    g.score,
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


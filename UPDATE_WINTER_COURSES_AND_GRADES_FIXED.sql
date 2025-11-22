-- Script to update WINTER courses with different courses and add random grades
USE univ_erp;

-- Step 1: Add 5 new courses for WINTER semester
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS301', 'Computer Networks', 3.0, 'Fundamentals of computer networking, protocols, and network architecture.'),
    ('MTH301', 'Linear Algebra', 4.0, 'Vector spaces, matrices, eigenvalues, and linear transformations.'),
    ('ECE301', 'Digital Signal Processing', 3.0, 'Signal analysis, filtering, and digital signal processing techniques.'),
    ('CS303', 'Software Engineering', 4.0, 'Software development lifecycle, design patterns, and project management.'),
    ('PHY201', 'Electromagnetic Theory', 4.0, 'Maxwell equations, electromagnetic fields, and wave propagation.');

-- Step 2: Delete existing WINTER 2025 sections
DELETE FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Step 3: Insert WINTER sections using the new courses
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

-- Step 4: Enroll students in WINTER sections
-- Enroll student 1 in first 3 WINTER sections, student 2 in last 2
INSERT INTO enrollments (student_id, section_id, status)
SELECT 
    CASE 
        WHEN ROW_NUMBER() OVER (ORDER BY section_id) <= 3 THEN 1
        ELSE 2
    END,
    section_id,
    'ENROLLED'
FROM sections
WHERE semester = 'WINTER' AND year = 2025;

-- Step 5: Create assessments for WINTER sections
INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Quiz', 20.0, 20.0
FROM sections WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Midterm', 30.0, 100.0
FROM sections WHERE semester = 'WINTER' AND year = 2025;

INSERT INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Final Exam', 50.0, 100.0
FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Step 6: Add random grades for WINTER enrollments
-- Student 1 grades (better performance)
INSERT INTO grades (enrollment_id, assessment_id, score)
SELECT 
    e.enrollment_id,
    a.assessment_id,
    CASE 
        WHEN a.name = 'Quiz' AND e.student_id = 1 THEN 18.0 + (e.enrollment_id % 3)  -- 18-20
        WHEN a.name = 'Midterm' AND e.student_id = 1 THEN 75.0 + (e.enrollment_id % 15)  -- 75-90
        WHEN a.name = 'Final Exam' AND e.student_id = 1 THEN 80.0 + (e.enrollment_id % 12)  -- 80-92
        WHEN a.name = 'Quiz' AND e.student_id = 2 THEN 15.0 + (e.enrollment_id % 4)  -- 15-19
        WHEN a.name = 'Midterm' AND e.student_id = 2 THEN 65.0 + (e.enrollment_id % 20)  -- 65-85
        WHEN a.name = 'Final Exam' AND e.student_id = 2 THEN 70.0 + (e.enrollment_id % 18)  -- 70-88
    END
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED';

-- Better approach: Use specific values for each enrollment
-- Delete any existing grades for WINTER enrollments first
DELETE FROM grades 
WHERE enrollment_id IN (
    SELECT e.enrollment_id 
    FROM enrollments e 
    JOIN sections s ON e.section_id = s.section_id 
    WHERE s.semester = 'WINTER' AND s.year = 2025
);

-- Insert grades with specific random values
-- For each enrollment, we'll create grades for all assessments
INSERT INTO grades (enrollment_id, assessment_id, score)
SELECT 
    e.enrollment_id,
    a.assessment_id,
    CASE 
        -- Student 1, Section 1 (CS301)
        WHEN e.student_id = 1 AND s.section_id = (SELECT MIN(section_id) FROM sections WHERE semester = 'WINTER' AND year = 2025) AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT MIN(section_id) FROM sections WHERE semester = 'WINTER' AND year = 2025) AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT MIN(section_id) FROM sections WHERE semester = 'WINTER' AND year = 2025) AND a.name = 'Final Exam' THEN 92.0
        -- Student 1, Section 2 (MTH301)
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 1) AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 1) AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 1) AND a.name = 'Final Exam' THEN 89.0
        -- Student 1, Section 3 (ECE301)
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 2) AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 2) AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 2) AND a.name = 'Final Exam' THEN 87.0
        -- Student 2, Section 4 (CS303)
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 3) AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 3) AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 3) AND a.name = 'Final Exam' THEN 83.0
        -- Student 2, Section 5 (PHY201)
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 4) AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 4) AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 4) AND a.name = 'Final Exam' THEN 80.0
        ELSE 0
    END
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN assessments a ON a.section_id = s.section_id
WHERE s.semester = 'WINTER' AND s.year = 2025
  AND e.status = 'ENROLLED'
  AND CASE 
        WHEN e.student_id = 1 AND s.section_id = (SELECT MIN(section_id) FROM sections WHERE semester = 'WINTER' AND year = 2025) AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT MIN(section_id) FROM sections WHERE semester = 'WINTER' AND year = 2025) AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT MIN(section_id) FROM sections WHERE semester = 'WINTER' AND year = 2025) AND a.name = 'Final Exam' THEN 92.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 1) AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 1) AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 1) AND a.name = 'Final Exam' THEN 89.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 2) AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 2) AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 2) AND a.name = 'Final Exam' THEN 87.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 3) AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 3) AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 3) AND a.name = 'Final Exam' THEN 83.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 4) AND a.name = 'Quiz' THEN 16.5
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 4) AND a.name = 'Midterm' THEN 75.0
        WHEN e.student_id = 2 AND s.section_id = (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025 ORDER BY section_id LIMIT 1 OFFSET 4) AND a.name = 'Final Exam' THEN 80.0
        ELSE 0
    END > 0;


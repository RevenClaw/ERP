-- Note: user_id values must match the rows inserted into the Auth database.
INSERT INTO students (user_id, roll_no, program, year_of_study)
VALUES
    (3, '2024538', 'B.Tech CSE', 2),
    (4, '2024538', 'B.Tech ECE', 2);

INSERT INTO instructors (user_id, department, title)
VALUES
    (2, 'Computer Science', 'Assistant Professor');

-- Courses: Original 2 + 3 new courses
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS201', 'Data Structures', 4.0, 'Core undergraduate course covering data structures.'),
    ('CS205', 'Database Systems', 3.0, 'Introduction to relational databases and SQL.'),
    ('MTH203', 'Multivariate Calculus', 4.0, 'Advanced calculus covering multiple variables, partial derivatives, and integrals.'),
    ('ECE201', 'Operating Systems', 3.0, 'Fundamentals of operating systems, process management, and memory systems.'),
    ('CSE201', 'Advanced Programming', 4.0, 'Advanced programming concepts including design patterns and software architecture.');

-- Sections for MONSOON 2025 (deadline in January 2026)
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (1, 1, 'MONSOON', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2026-01-15'),
    (2, 1, 'MONSOON', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-102', 35, '2026-01-15'),
    (3, 1, 'MONSOON', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'C-201', 30, '2026-01-15'),
    (4, 1, 'MONSOON', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'B-003', 25, '2026-01-15'),
    (5, 1, 'MONSOON', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'A-102', 35, '2026-01-15');

-- Add 5 new courses for WINTER semester (different from MONSOON)
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS301', 'Computer Networks', 3.0, 'Fundamentals of computer networking, protocols, and network architecture.'),
    ('MTH301', 'Linear Algebra', 4.0, 'Vector spaces, matrices, eigenvalues, and linear transformations.'),
    ('ECE301', 'Digital Signal Processing', 3.0, 'Signal analysis, filtering, and digital signal processing techniques.'),
    ('CS303', 'Software Engineering', 4.0, 'Software development lifecycle, design patterns, and project management.'),
    ('PHY201', 'Electromagnetic Theory', 4.0, 'Maxwell equations, electromagnetic fields, and wave propagation.');

-- Sections for WINTER 2025 (deadline passed 6 months ago - May 2025)
-- Using new courses (course_id 6-10, assuming 5 courses already exist)
-- Note: Adjust course_id values if your course IDs are different
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
        WHEN 'MTH301' THEN 'C-102'
        WHEN 'ECE301' THEN 'C-201'
        WHEN 'CS303' THEN 'B-003'
        WHEN 'PHY201' THEN 'A-102'
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

-- Enrollments for MONSOON 2025
INSERT INTO enrollments (student_id, section_id, status)
VALUES
    (1, 1, 'ENROLLED'),  -- Student 1 in CS201 MONSOON
    (2, 2, 'ENROLLED');  -- Student 2 in CS205 MONSOON

-- Enrollments for WINTER 2025 will be added by ADD_WINTER_ENROLLMENTS_AND_GRADES.sql
-- This is done separately because we need the section IDs to be created first

-- Assessments for MONSOON 2025 sections
INSERT INTO assessments (section_id, name, weight_percent, max_score)
VALUES
    (1, 'Quiz', 20.0, 20.0),
    (1, 'Midterm', 30.0, 100.0),
    (1, 'End-Sem', 50.0, 100.0),
    (2, 'Project', 40.0, 50.0),
    (2, 'Final Exam', 60.0, 100.0);

-- Assessments for WINTER 2025 sections (Quiz, Midterm, Final Exam for each)
-- Using INSERT IGNORE to prevent duplicates if script is run multiple times
INSERT IGNORE INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Quiz', 20.0, 20.0
FROM sections WHERE semester = 'WINTER' AND year = 2025
  AND section_id NOT IN (
      SELECT DISTINCT section_id FROM assessments WHERE name = 'Quiz' 
      AND section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
  );

INSERT IGNORE INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Midterm', 30.0, 100.0
FROM sections WHERE semester = 'WINTER' AND year = 2025
  AND section_id NOT IN (
      SELECT DISTINCT section_id FROM assessments WHERE name = 'Midterm' 
      AND section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
  );

INSERT IGNORE INTO assessments (section_id, name, weight_percent, max_score)
SELECT section_id, 'Final Exam', 50.0, 100.0
FROM sections WHERE semester = 'WINTER' AND year = 2025
  AND section_id NOT IN (
      SELECT DISTINCT section_id FROM assessments WHERE name = 'Final Exam' 
      AND section_id IN (SELECT section_id FROM sections WHERE semester = 'WINTER' AND year = 2025)
  );

-- Grades for MONSOON 2025 enrollments
INSERT INTO grades (enrollment_id, assessment_id, score)
VALUES
    (1, 1, 18.0),
    (1, 2, 72.0),
    (1, 3, 85.0),
    (2, 4, 40.0),
    (2, 5, 78.0);

-- Grades for WINTER 2025 enrollments (random grades)
-- Student 1: Good grades (A range)
-- Student 2: Good grades (B+ range)
-- Using INSERT IGNORE to prevent duplicates if script is run multiple times
INSERT IGNORE INTO grades (enrollment_id, assessment_id, score)
SELECT DISTINCT
    e.enrollment_id,
    a.assessment_id,
    CASE 
        -- Student 1, first WINTER section - CS301
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Quiz' THEN 19.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Midterm' THEN 88.0
        WHEN e.student_id = 1 AND c.code = 'CS301' AND a.name = 'Final Exam' THEN 92.0
        -- Student 1, second WINTER section - MTH301
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Quiz' THEN 18.5
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Midterm' THEN 85.0
        WHEN e.student_id = 1 AND c.code = 'MTH301' AND a.name = 'Final Exam' THEN 89.0
        -- Student 1, third WINTER section - ECE301
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Quiz' THEN 20.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Midterm' THEN 82.0
        WHEN e.student_id = 1 AND c.code = 'ECE301' AND a.name = 'Final Exam' THEN 87.0
        -- Student 2, fourth WINTER section - CS303
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Quiz' THEN 17.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Midterm' THEN 78.0
        WHEN e.student_id = 2 AND c.code = 'CS303' AND a.name = 'Final Exam' THEN 83.0
        -- Student 2, fifth WINTER section - PHY201
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

-- Final grades for MONSOON 2025
INSERT INTO final_grades (enrollment_id, final_score, letter_grade)
VALUES
    (1, 84.0, 'A'),
    (2, 79.0, 'B+');

-- Final grades for WINTER 2025 (calculated from weighted assessments)
-- Using ON DUPLICATE KEY UPDATE to handle re-runs
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

INSERT INTO settings (setting_key, setting_value)
VALUES
    ('maintenanceMode', 'OFF');

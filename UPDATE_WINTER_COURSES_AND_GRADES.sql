-- Update WINTER 2025 sections to use different courses and add grades
USE univ_erp;

-- First, add 5 new courses for WINTER semester
INSERT INTO courses (code, title, credits, description)
VALUES
    ('CS301', 'Computer Networks', 3.0, 'Fundamentals of computer networking, protocols, and network architecture.'),
    ('MTH301', 'Linear Algebra', 4.0, 'Vector spaces, matrices, eigenvalues, and linear transformations.'),
    ('ECE301', 'Digital Signal Processing', 3.0, 'Signal analysis, filtering, and digital signal processing techniques.'),
    ('CS303', 'Software Engineering', 4.0, 'Software development lifecycle, design patterns, and project management.'),
    ('PHY201', 'Electromagnetic Theory', 4.0, 'Maxwell equations, electromagnetic fields, and wave propagation.');

-- Get the new course IDs (assuming they are 6, 7, 8, 9, 10 after the existing 5 courses)
-- Update WINTER sections to use the new courses
-- First, delete existing WINTER sections (if any)
DELETE FROM sections WHERE semester = 'WINTER' AND year = 2025;

-- Insert new WINTER sections with different courses
-- Note: course_id values 6-10 correspond to the new courses we just added
-- If your course IDs are different, adjust these values
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
SELECT 
    c.course_id,
    1, -- instructor_id
    'WINTER',
    2025,
    'A',
    CASE (ROW_NUMBER() OVER (ORDER BY c.course_id))
        WHEN 1 THEN 'MON'
        WHEN 2 THEN 'TUE'
        WHEN 3 THEN 'WED'
        WHEN 4 THEN 'THU'
        WHEN 5 THEN 'FRI'
    END,
    CASE (ROW_NUMBER() OVER (ORDER BY c.course_id))
        WHEN 1 THEN '10:00:00'
        WHEN 2 THEN '12:00:00'
        WHEN 3 THEN '09:00:00'
        WHEN 4 THEN '14:00:00'
        WHEN 5 THEN '11:00:00'
    END,
    CASE (ROW_NUMBER() OVER (ORDER BY c.course_id))
        WHEN 1 THEN '13:30:00'
        WHEN 2 THEN '15:00:00'
        WHEN 3 THEN '10:30:00'
        WHEN 4 THEN '15:30:00'
        WHEN 5 THEN '12:30:00'
    END,
    CASE (ROW_NUMBER() OVER (ORDER BY c.course_id))
        WHEN 1 THEN 'C-201'
        WHEN 2 THEN 'C-105'
        WHEN 3 THEN 'M-301'
        WHEN 4 THEN 'E-205'
        WHEN 5 THEN 'C-302'
    END,
    CASE (ROW_NUMBER() OVER (ORDER BY c.course_id))
        WHEN 1 THEN 40
        WHEN 2 THEN 35
        WHEN 3 THEN 30
        WHEN 4 THEN 25
        WHEN 5 THEN 35
    END,
    '2025-05-15'
FROM courses c
WHERE c.code IN ('CS301', 'MTH301', 'ECE301', 'CS303', 'PHY201')
ORDER BY c.course_id;

-- Alternative simpler approach if the above doesn't work:
-- Use specific course IDs (adjust based on your actual course IDs)
-- First, find the course IDs:
-- SELECT course_id, code FROM courses WHERE code IN ('CS301', 'MTH301', 'ECE301', 'CS303', 'PHY201');

-- For now, let's use a simpler approach with explicit course IDs
-- Assuming the new courses get IDs 6, 7, 8, 9, 10
-- We'll use a more direct approach


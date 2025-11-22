-- Insert sections with correct course_id values (3, 4, 5, 6, 7)
USE univ_erp;

-- Insert sections for MONSOON 2025 (deadline in January 2026)
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (3, 1, 'MONSOON', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2026-01-15'),
    (4, 1, 'MONSOON', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2026-01-15'),
    (5, 1, 'MONSOON', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2026-01-15'),
    (6, 1, 'MONSOON', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2026-01-15'),
    (7, 1, 'MONSOON', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2026-01-15');

-- Insert sections for WINTER 2025 (deadline passed 6 months ago - May 2025)
INSERT INTO sections (course_id, instructor_id, semester, year, section_code, day_of_week, start_time, end_time, room, capacity, enrollment_deadline)
VALUES
    (3, 1, 'WINTER', 2025, 'A', 'MON', '10:00:00', '11:30:00', 'C-201', 40, '2025-05-15'),
    (4, 1, 'WINTER', 2025, 'A', 'TUE', '12:00:00', '13:30:00', 'C-105', 35, '2025-05-15'),
    (5, 1, 'WINTER', 2025, 'A', 'WED', '09:00:00', '10:30:00', 'M-301', 30, '2025-05-15'),
    (6, 1, 'WINTER', 2025, 'A', 'THU', '14:00:00', '15:30:00', 'E-205', 25, '2025-05-15'),
    (7, 1, 'WINTER', 2025, 'A', 'FRI', '11:00:00', '12:30:00', 'C-302', 35, '2025-05-15');

-- Verify sections were inserted
SELECT '=== MONSOON 2025 Sections ===' as info;
SELECT section_id, course_id, semester, year, section_code, day_of_week
FROM sections 
WHERE semester = 'MONSOON' AND year = 2025
ORDER BY course_id;

SELECT '=== WINTER 2025 Sections ===' as info;
SELECT section_id, course_id, semester, year, section_code, day_of_week
FROM sections 
WHERE semester = 'WINTER' AND year = 2025
ORDER BY course_id;

SELECT '=== Count Summary ===' as info;
SELECT semester, year, COUNT(*) as count 
FROM sections 
GROUP BY semester, year;


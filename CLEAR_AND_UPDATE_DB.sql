-- ============================================
-- Database Update Script
-- Run this in your univ_erp database
-- ============================================

USE univ_erp;

-- Step 1: Delete data in dependency order (child tables first)
-- This ensures foreign key constraints are satisfied

DELETE FROM final_grades;
DELETE FROM grades;
DELETE FROM assessments;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;

-- Note: Keep students, instructors, and settings
-- (students and instructors are linked to auth users)
-- (settings contains maintenance mode)

-- Step 2: Update schema - Change semester enum to only MONSOON and WINTER
ALTER TABLE sections 
MODIFY COLUMN semester ENUM('MONSOON', 'WINTER') NOT NULL;

-- Step 3: Verify deletions
SELECT 'final_grades' as table_name, COUNT(*) as remaining FROM final_grades
UNION ALL
SELECT 'grades', COUNT(*) FROM grades
UNION ALL
SELECT 'assessments', COUNT(*) FROM assessments
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL
SELECT 'sections', COUNT(*) FROM sections
UNION ALL
SELECT 'courses', COUNT(*) FROM courses;

-- All should show 0 remaining rows

-- Step 4: Now run the updated erp_seed.sql file
-- (The INSERT statements from src/main/resources/db/erp_seed.sql)


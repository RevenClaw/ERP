-- Update the sections table to use MONSOON and WINTER only
-- Run this in your univ_erp database

USE univ_erp;

-- Update the semester enum
ALTER TABLE sections 
MODIFY COLUMN semester ENUM('MONSOON', 'WINTER') NOT NULL;

-- Verify the change
DESCRIBE sections;


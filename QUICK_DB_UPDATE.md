# Quick Database Update Guide

## What to Delete

You need to delete data from these tables in the `univ_erp` database:

### Tables to Clear (in this order):
1. `final_grades` - Final grade records
2. `grades` - Individual assessment grades
3. `assessments` - Assessment definitions
4. `enrollments` - Student enrollments
5. `sections` - Course sections
6. `courses` - Course definitions

### Tables to KEEP:
- `students` - Keep (linked to auth users)
- `instructors` - Keep (linked to auth users)
- `settings` - Keep (contains maintenance mode)

## Quick SQL Commands

```sql
-- Connect to your ERP database
USE univ_erp;

-- Delete in dependency order
DELETE FROM final_grades;
DELETE FROM grades;
DELETE FROM assessments;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;
```

## Then Run the Updated Seed Data

After clearing, run the contents of `src/main/resources/db/erp_seed.sql` which will insert:
- 5 courses (CS201, CS205, MTH203, ECE201, CSE201)
- 10 sections (5 MONSOON 2025 + 5 WINTER 2025)
- Sample enrollments, assessments, and grades

## Important: Schema Update

**You also need to update the database schema** because the `semester` enum changed:

```sql
-- Update the sections table enum
ALTER TABLE sections 
MODIFY COLUMN semester ENUM('MONSOON', 'WINTER') NOT NULL;
```

This changes the allowed values from (SPRING, SUMMER, FALL, WINTER) to (MONSOON, WINTER).

## Complete Update Script

I've created `CLEAR_AND_UPDATE_DB.sql` with all the commands. You can run that file.


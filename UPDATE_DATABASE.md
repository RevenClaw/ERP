# Database Update Instructions

## Step 1: Clear Existing Data

Run these SQL commands in your MySQL `univ_erp` database to clear existing data:

```sql
-- Clear data in dependency order (child tables first)
DELETE FROM final_grades;
DELETE FROM grades;
DELETE FROM assessments;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;
DELETE FROM instructors;
DELETE FROM students;
```

**Note:** Do NOT delete from `settings` table - keep the maintenance mode setting.

## Step 2: Run Updated Seed Data

Run the updated `src/main/resources/db/erp_seed.sql` file in your MySQL `univ_erp` database.

## Step 3: Verify

Check that you have:
- 5 courses (CS201, CS205, MTH203, ECE201, CSE201)
- 10 sections total:
  - 5 sections for MONSOON 2025 (deadline: 2026-01-15)
  - 5 sections for WINTER 2025 (deadline: 2025-05-15)

## Quick SQL Commands

```sql
-- Connect to univ_erp database
USE univ_erp;

-- Clear data
DELETE FROM final_grades;
DELETE FROM grades;
DELETE FROM assessments;
DELETE FROM enrollments;
DELETE FROM sections;
DELETE FROM courses;

-- Then run the erp_seed.sql file contents
```

## What Changed

1. **Term values changed**: FALL → MONSOON, removed SPRING/SUMMER
2. **New courses added**: MTH203, ECE201, CSE201
3. **New sections**: 10 total (5 MONSOON + 5 WINTER)
4. **Deadlines**: 
   - MONSOON 2025: January 15, 2026 (future)
   - WINTER 2025: May 15, 2025 (6 months ago - will show as unavailable)


# Troubleshooting: Courses Not Showing

## Step 1: Verify Data is in Database

Run this SQL in your `univ_erp` database:

```sql
USE univ_erp;

-- Check if courses exist
SELECT * FROM courses;

-- Check if sections exist
SELECT section_id, course_id, semester, year, section_code 
FROM sections 
ORDER BY semester, year;
```

**Expected:** You should see 5 courses and 10 sections.

## Step 2: If Data is Missing - Run Seed File

If the tables are empty, run the seed data:

```sql
USE univ_erp;

-- Make sure schema is updated first
ALTER TABLE sections 
MODIFY COLUMN semester ENUM('MONSOON', 'WINTER') NOT NULL;

-- Then run all the INSERT statements from erp_seed.sql
```

Or use command line:
```bash
mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_seed.sql
```

## Step 3: Check Application Connection

Verify your `application.properties` has:
```properties
erp.datasource.jdbcUrl=jdbc:mysql://localhost:3306/univ_erp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
erp.datasource.username=erp_user
erp.datasource.password=erp_pass123
```

## Step 4: Check Term/Year Selection

In the Course Catalog tab:
- Make sure **Term** is set to **MONSOON** (not WINTER)
- Make sure **Year** is set to **2025**

The sections should auto-load when you open the tab.

## Step 5: Check for Errors

Look for any error messages in:
- The application console/terminal
- Error dialogs in the UI
- Database connection errors

## Common Issues

1. **Schema not updated**: If you see errors about 'FALL' or 'SPRING', run:
   ```sql
   ALTER TABLE sections MODIFY COLUMN semester ENUM('MONSOON', 'WINTER') NOT NULL;
   ```

2. **Wrong database**: Make sure you're inserting into `univ_erp` database, not `univ_auth`

3. **Foreign key errors**: Make sure students and instructors exist before inserting sections


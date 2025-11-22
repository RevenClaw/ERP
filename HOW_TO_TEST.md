# How to Test the Login Flow

## Step-by-Step Testing Guide

### Prerequisites
- ✅ MySQL installed and running
- ✅ Project compiled successfully (`mvn compile` works)

---

## Step 1: Set Up MySQL Databases

Open MySQL command line or MySQL Workbench and run:

```sql
-- Create databases
CREATE DATABASE IF NOT EXISTS univ_auth;
CREATE DATABASE IF NOT EXISTS univ_erp;

-- Create users (you can change passwords)
CREATE USER IF NOT EXISTS 'auth_user'@'localhost' IDENTIFIED BY 'auth_pass123';
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_pass123';

-- Grant permissions
GRANT ALL PRIVILEGES ON univ_auth.* TO 'auth_user'@'localhost';
GRANT ALL PRIVILEGES ON univ_erp.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## Step 2: Create Database Tables

Execute the schema files:

**For Auth Database:**
```bash
mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_schema.sql
```

**For ERP Database:**
```bash
mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_schema.sql
```

Or use MySQL Workbench/HeidiSQL to execute the SQL files directly.

---

## Step 3: Load Seed Data

**Load Auth data:**
```bash
mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_seed.sql
```

**Load ERP data:**
```bash
mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_seed.sql
```

---

## Step 4: Update Application Configuration

Edit `src/main/resources/application.properties`:

```properties
# Change these passwords to match what you set in Step 1
auth.datasource.password=auth_pass123
erp.datasource.password=erp_pass123
```

---

## Step 5: Run the Application

**Option 1: Using the batch script (Windows)**
```bash
run.bat
```

**Option 2: Using Maven**
```bash
mvn compile exec:java -Dexec.mainClass="edu.univ.erp.App"
```

**Option 3: From your IDE**
- Open `src/main/java/edu/univ/erp/App.java`
- Right-click → Run As → Java Application

---

## Step 6: Test Login

When the login dialog appears:

### Test Case 1: Successful Admin Login
- **Username:** `admin1`
- **Password:** `password123`
- **Expected:** Admin Dashboard opens with welcome message

### Test Case 2: Successful Instructor Login
- **Username:** `inst1`
- **Password:** `password123`
- **Expected:** Instructor Dashboard opens

### Test Case 3: Successful Student Login
- **Username:** `stu1`
- **Password:** `password123`
- **Expected:** Student Dashboard opens

### Test Case 4: Failed Login (Wrong Password)
- **Username:** `admin1`
- **Password:** `wrongpassword`
- **Expected:** Error message "Incorrect username or password"

### Test Case 5: Failed Login (Wrong Username)
- **Username:** `nonexistent`
- **Password:** `password123`
- **Expected:** Error message "Incorrect username or password"

---

## Troubleshooting

### "Failed to start application"
**Check:**
1. MySQL is running: `mysql -u root -p`
2. Databases exist: `SHOW DATABASES LIKE 'univ%';`
3. Credentials in `application.properties` are correct

### "Incorrect username or password"
**Check:**
1. Seed data was loaded: `SELECT * FROM univ_auth.users_auth;`
2. Password hash is correct (should start with `$2a$10$`)
3. You're using password: `password123`

### Connection errors
**Check:**
1. MySQL service is running
2. User permissions: `SHOW GRANTS FOR 'auth_user'@'localhost';`
3. Database names match in `application.properties`

### Quick Database Check
```sql
-- Check auth database
USE univ_auth;
SELECT username, role, status FROM users_auth;

-- Check ERP database  
USE univ_erp;
SELECT * FROM students;
SELECT * FROM instructors;
```

---

## Success Indicators

✅ Login dialog appears when application starts  
✅ Can enter username and password  
✅ Successful login opens correct dashboard  
✅ Failed login shows error message  
✅ Password field clears after failed attempt  
✅ Can retry login after failure  

---

## Next Steps After Testing

Once login works:
1. Implement Student features (catalog, registration, timetable)
2. Implement Instructor features (gradebook, final grades)
3. Implement Admin features (user management, maintenance mode)


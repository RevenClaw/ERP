# Testing Login Flow - Quick Start Guide

## Prerequisites Check

Before testing, ensure:
1. ✅ MySQL is installed and running
2. ✅ Databases are created (see `src/main/resources/db/README_SETUP.md`)
3. ✅ `application.properties` has correct database credentials

## Quick Database Setup

### Step 1: Create Databases (run as MySQL root)

```sql
CREATE DATABASE IF NOT EXISTS univ_auth;
CREATE DATABASE IF NOT EXISTS univ_erp;

CREATE USER IF NOT EXISTS 'auth_user'@'localhost' IDENTIFIED BY 'auth_pass123';
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_pass123';

GRANT ALL PRIVILEGES ON univ_auth.* TO 'auth_user'@'localhost';
GRANT ALL PRIVILEGES ON univ_erp.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2: Run Schema Scripts

Execute these SQL files in order:
1. `src/main/resources/db/auth_schema.sql` → database: `univ_auth`
2. `src/main/resources/db/erp_schema.sql` → database: `univ_erp`

### Step 3: Generate Password Hashes

**Option A: Use the provided utility (recommended)**

1. Compile: `mvn compile`
2. Find your Maven repository path (usually `~/.m2/repository`)
3. Run with full classpath:

```bash
# Windows PowerShell
$cp = (mvn dependency:build-classpath -q -Dmdep.outputFile=-)
java -cp "target/classes;$cp" edu.univ.erp.util.PasswordHashGenerator password123 password123 password123 password123

# Or manually build classpath from ~/.m2/repository/org/mindrot/jbcrypt/jbcrypt/0.4/jbcrypt-0.4.jar
```

**Option B: Use online BCrypt generator**
- Visit: https://bcrypt-generator.com/
- Enter password: `password123`
- Copy the hash
- Use the same hash for all 4 users (or generate 4 different ones)

**Option C: Use this pre-generated hash (for quick testing)**
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

### Step 4: Update Seed Data

Edit `src/main/resources/db/auth_seed.sql` and replace the placeholder hashes with real ones.

Example (using the hash from Option C):
```sql
INSERT INTO users_auth (username, role, password_hash, status, failed_attempts, last_login)
VALUES
    ('admin1', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL),
    ('inst1',  'INSTRUCTOR', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL),
    ('stu1',   'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL),
    ('stu2',   'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', 0, NULL);
```

### Step 5: Load Seed Data

```bash
mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_seed.sql
mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_seed.sql
```

### Step 6: Update application.properties

Edit `src/main/resources/application.properties`:
```properties
auth.datasource.password=auth_pass123
erp.datasource.password=erp_pass123
```

## Running the Application

```bash
mvn compile exec:java -Dexec.mainClass="edu.univ.erp.App"
```

Or from your IDE, run `edu.univ.erp.App.main()`

## Test Accounts

After setup, login with:
- **Username:** `admin1` | **Password:** `password123` | **Role:** Admin
- **Username:** `inst1` | **Password:** `password123` | **Role:** Instructor  
- **Username:** `stu1` | **Password:** `password123` | **Role:** Student
- **Username:** `stu2` | **Password:** `password123` | **Role:** Student

## Expected Behavior

1. **Login Dialog appears** - Enter username and password
2. **On successful login:**
   - Dialog closes
   - Role-specific dashboard opens
   - Welcome message shows username
3. **On failed login:**
   - Error message appears
   - Password field clears
   - Can retry

## Troubleshooting

**"Failed to start application"**
- Check MySQL is running
- Verify database credentials in `application.properties`
- Ensure databases exist

**"Incorrect username or password"**
- Verify seed data was loaded correctly
- Check password hash matches in database
- Try regenerating hashes

**Connection errors**
- Verify MySQL is running: `mysql -u root -p`
- Check user permissions
- Verify database names match in `application.properties`


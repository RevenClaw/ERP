# Database Setup Guide

## Prerequisites
- MySQL 8.0+ installed and running
- MySQL root access or ability to create databases and users

## Step 1: Create Databases and Users

Run these SQL commands as MySQL root:

```sql
-- Create databases
CREATE DATABASE IF NOT EXISTS univ_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS univ_erp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create users (change passwords as needed)
CREATE USER IF NOT EXISTS 'auth_user'@'localhost' IDENTIFIED BY 'auth_pass123';
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_pass123';

-- Grant permissions
GRANT ALL PRIVILEGES ON univ_auth.* TO 'auth_user'@'localhost';
GRANT ALL PRIVILEGES ON univ_erp.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
```

## Step 2: Run Schema Scripts

```bash
# For Auth database
mysql -u auth_user -p univ_auth < src/main/resources/db/auth_schema.sql

# For ERP database
mysql -u erp_user -p univ_erp < src/main/resources/db/erp_schema.sql
```

Or use MySQL Workbench/HeidiSQL to execute the SQL files.

## Step 3: Generate Password Hashes

The seed data needs real BCrypt hashes. Generate them using:

```bash
# Compile the utility
mvn compile

# Run the generator (use simple passwords for testing)
java -cp target/classes edu.univ.erp.util.PasswordHashGenerator password123 password123 password123 password123
```

This will output hashes. Copy them into `auth_seed.sql`.

## Step 4: Update Seed Data

Edit `auth_seed.sql` and replace the placeholder hashes with real ones from Step 3.

Default test passwords (for development only):
- admin1: `password123`
- inst1: `password123`
- stu1: `password123`
- stu2: `password123`

## Step 5: Load Seed Data

```bash
mysql -u auth_user -p univ_auth < src/main/resources/db/auth_seed.sql
mysql -u erp_user -p univ_erp < src/main/resources/db/erp_seed.sql
```

## Step 6: Update application.properties

Edit `src/main/resources/application.properties`:
- Set `auth.datasource.password` to `auth_pass123` (or your chosen password)
- Set `erp.datasource.password` to `erp_pass123` (or your chosen password)

## Test Accounts

After setup, you can login with:
- **admin1** / password123 (Admin)
- **inst1** / password123 (Instructor)
- **stu1** / password123 (Student)
- **stu2** / password123 (Student)


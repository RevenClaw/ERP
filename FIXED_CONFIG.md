# Configuration Fixed!

## The Problem
Your `application.properties` had incorrect values:
- ❌ `auth.datasource.username=auth_pass123` (was using password as username!)
- ❌ `auth.datasource.password=erp_pass123` (wrong password)
- ❌ `erp.datasource.password=CHANGE_ME` (not set)

## The Fix
Updated to correct values:
- ✅ `auth.datasource.username=auth_user`
- ✅ `auth.datasource.password=auth_pass123`
- ✅ `erp.datasource.username=erp_user`
- ✅ `erp.datasource.password=erp_pass123`

## Next Steps

1. **Make sure MySQL databases and users exist:**
   ```sql
   -- Run as MySQL root
   CREATE DATABASE IF NOT EXISTS univ_auth;
   CREATE DATABASE IF NOT EXISTS univ_erp;
   CREATE USER IF NOT EXISTS 'auth_user'@'localhost' IDENTIFIED BY 'auth_pass123';
   CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_pass123';
   GRANT ALL PRIVILEGES ON univ_auth.* TO 'auth_user'@'localhost';
   GRANT ALL PRIVILEGES ON univ_erp.* TO 'erp_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Create tables and load data:**
   ```bash
   mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_schema.sql
   mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_schema.sql
   mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_seed.sql
   mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_seed.sql
   ```

3. **Run the application:**
   ```bash
   mvn exec:java
   ```

The login dialog should now appear! 🎉


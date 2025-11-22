# Quick Test Checklist

## Before Testing - Setup Checklist

- [ ] MySQL is installed and running
- [ ] Databases `univ_auth` and `univ_erp` are created
- [ ] Database users `auth_user` and `erp_user` are created
- [ ] Schema files executed (tables created)
- [ ] Seed data loaded (test users exist)
- [ ] `application.properties` updated with database passwords
- [ ] Project compiles: `mvn compile` succeeds

## Quick Setup Commands

Copy-paste these in order:

```sql
-- 1. Create databases and users (run as MySQL root)
CREATE DATABASE IF NOT EXISTS univ_auth;
CREATE DATABASE IF NOT EXISTS univ_erp;
CREATE USER IF NOT EXISTS 'auth_user'@'localhost' IDENTIFIED BY 'auth_pass123';
CREATE USER IF NOT EXISTS 'erp_user'@'localhost' IDENTIFIED BY 'erp_pass123';
GRANT ALL PRIVILEGES ON univ_auth.* TO 'auth_user'@'localhost';
GRANT ALL PRIVILEGES ON univ_erp.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
```

```bash
# 2. Create tables
mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_schema.sql
mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_schema.sql

# 3. Load seed data
mysql -u auth_user -pauth_pass123 univ_auth < src/main/resources/db/auth_seed.sql
mysql -u erp_user -perp_pass123 univ_erp < src/main/resources/db/erp_seed.sql
```

```properties
# 4. Update src/main/resources/application.properties
auth.datasource.password=auth_pass123
erp.datasource.password=erp_pass123
```

```bash
# 5. Run the application
mvn compile exec:java -Dexec.mainClass="edu.univ.erp.App"
```

## Test Accounts

| Username | Password | Role |
|----------|----------|------|
| admin1 | password123 | Admin |
| inst1 | password123 | Instructor |
| stu1 | password123 | Student |
| stu2 | password123 | Student |

## What to Expect

1. **Login Dialog** appears
2. **Enter credentials** → Click Login
3. **Success:** Dashboard opens (role-specific)
4. **Failure:** Error message appears

---

**See `HOW_TO_TEST.md` for detailed instructions and troubleshooting.**


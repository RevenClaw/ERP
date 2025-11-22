# Quick Login Test

## Fastest Way to Test (if databases are already set up)

1. **Verify seed data has real password hashes**
   - Check `src/main/resources/db/auth_seed.sql`
   - If you see placeholder hashes, replace them with real BCrypt hashes
   - Use: https://bcrypt-generator.com/ with password `password123`

2. **Update application.properties**
   ```properties
   auth.datasource.password=your_auth_password
   erp.datasource.password=your_erp_password
   ```

3. **Run the application**
   ```bash
   mvn compile exec:java -Dexec.mainClass="edu.univ.erp.App"
   ```

4. **Test login**
   - Username: `admin1`
   - Password: `password123`
   - Should open Admin Dashboard

## If You Get Connection Errors

Run this to check MySQL:
```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'univ%';"
```

If databases don't exist, follow `src/main/resources/db/README_SETUP.md`


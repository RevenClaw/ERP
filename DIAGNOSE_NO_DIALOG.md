# Diagnosing "No Dialog Appeared" Issue

## Quick Check

When you run the application, **check the console output**. You should see messages like:

```
Initializing application...
Application context initialized successfully.
FlatLaf look and feel set successfully.
Creating login dialog...
Showing login dialog...
Login dialog displayed.
```

## Common Issues and Solutions

### Issue 1: Database Connection Error

**Symptoms:**
- Console shows: "Failed to start application"
- Error mentions "Connection refused" or "Access denied"
- No dialog appears

**Solution:**
1. Check MySQL is running:
   ```bash
   mysql -u root -p
   ```

2. Verify databases exist:
   ```sql
   SHOW DATABASES LIKE 'univ%';
   ```

3. Check `application.properties` has correct passwords:
   ```properties
   auth.datasource.password=auth_pass123
   erp.datasource.password=erp_pass123
   ```

4. Test connection manually:
   ```bash
   mysql -u auth_user -pauth_pass123 univ_auth
   mysql -u erp_user -perp_pass123 univ_erp
   ```

### Issue 2: Dialog Behind Other Windows

**Symptoms:**
- No visible dialog, but console shows "Login dialog displayed"
- Application seems to be running

**Solution:**
- Check taskbar for the application window
- Try Alt+Tab to switch windows
- Check if dialog is minimized

### Issue 3: Configuration File Not Found

**Symptoms:**
- Error: "Configuration resource not found"
- Error: "Unable to load configuration resource"

**Solution:**
- Ensure `src/main/resources/application.properties` exists
- Rebuild: `mvn clean compile`

### Issue 4: Missing Dependencies

**Symptoms:**
- ClassNotFoundException errors
- NoClassDefFoundError

**Solution:**
```bash
mvn clean compile
mvn dependency:resolve
```

## Step-by-Step Diagnosis

### Step 1: Run with Console Output

```bash
mvn compile exec:java -Dexec.mainClass="edu.univ.erp.App"
```

**Look for:**
- ✅ "Initializing application..." → Context creation started
- ✅ "Application context initialized successfully" → Databases connected
- ✅ "Creating login dialog..." → UI initialization started
- ✅ "Login dialog displayed" → Dialog should be visible

### Step 2: Check for Error Messages

If you see an error, note:
- **What line** it fails on
- **What the error message says**
- **Any stack trace**

### Step 3: Test Database Connection

```bash
# Test Auth DB
mysql -u auth_user -pauth_pass123 -e "SELECT COUNT(*) FROM univ_auth.users_auth;"

# Test ERP DB  
mysql -u erp_user -perp_pass123 -e "SELECT COUNT(*) FROM univ_erp.students;"
```

Both should return numbers (not errors).

### Step 4: Verify Configuration

Check `src/main/resources/application.properties`:
```properties
# Should NOT say "CHANGE_ME"
auth.datasource.password=auth_pass123
erp.datasource.password=erp_pass123
```

## Quick Fixes

### Fix 1: Rebuild Everything
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.App"
```

### Fix 2: Check Java Version
```bash
java -version
```
Should be Java 17 or higher.

### Fix 3: Run from IDE

Instead of command line, try running from your IDE:
1. Open `src/main/java/edu/univ/erp/App.java`
2. Right-click → Run As → Java Application
3. Check IDE console for errors

## Still Not Working?

**Share these details:**
1. Full console output (all messages)
2. Any error messages
3. Whether MySQL is running
4. Whether databases exist
5. Contents of `application.properties` (hide passwords)


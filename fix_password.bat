@echo off
echo ========================================
echo Fix Password Hash for Login
echo ========================================
echo.
echo This will help you update the password hash in the database.
echo.
echo Step 1: Generate a BCrypt hash
echo   - Go to: https://bcrypt-generator.com/
echo   - Enter password: password123
echo   - Rounds: 10
echo   - Click Generate
echo   - Copy the hash
echo.
echo Step 2: Update the database
echo   Run this SQL command (replace YOUR_HASH with the hash from step 1):
echo.
echo   USE univ_auth;
echo   UPDATE users_auth SET password_hash = 'YOUR_HASH' WHERE username IN ('admin1', 'inst1', 'stu1', 'stu2');
echo.
echo Or use MySQL command line:
echo   mysql -u auth_user -pauth_pass123 univ_auth
echo   Then paste the UPDATE command above
echo.
pause


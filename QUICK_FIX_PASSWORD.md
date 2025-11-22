# Quick Fix for "Incorrect Password"

## The Issue
The password hash in your database doesn't match "password123".

## Fastest Solution

### Step 1: Generate a BCrypt Hash

**Option A: Online (Recommended)**
1. Visit: https://bcrypt-generator.com/
2. Password: `password123`
3. Rounds: `10`
4. Click "Generate Hash"
5. Copy the hash (looks like: `$2a$10$...`)

**Option B: Using Maven (if you have internet)**
```bash
# The hash in seed file might be wrong, let's verify
# Check what's in your database first
mysql -u auth_user -pauth_pass123 univ_auth -e "SELECT username, LEFT(password_hash, 30) as hash FROM users_auth;"
```

### Step 2: Update Database

Run this SQL (replace `YOUR_HASH` with the hash from Step 1):

```sql
USE univ_auth;
UPDATE users_auth 
SET password_hash = 'YOUR_HASH_HERE'
WHERE username IN ('admin1', 'inst1', 'stu1', 'stu2');
```

### Step 3: Test Login

Try logging in again:
- Username: `admin1`
- Password: `password123`

## Alternative: Delete and Re-insert Users

If updating doesn't work:

```sql
USE univ_auth;

-- Delete existing users
DELETE FROM password_history;
DELETE FROM users_auth;

-- Re-insert with new hash (replace YOUR_HASH)
INSERT INTO users_auth (username, role, password_hash, status, failed_attempts, last_login)
VALUES
    ('admin1', 'ADMIN', 'YOUR_HASH', 'ACTIVE', 0, NULL),
    ('inst1',  'INSTRUCTOR', 'YOUR_HASH', 'ACTIVE', 0, NULL),
    ('stu1',   'STUDENT', 'YOUR_HASH', 'ACTIVE', 0, NULL),
    ('stu2',   'STUDENT', 'YOUR_HASH', 'ACTIVE', 0, NULL);

-- Add to password history
INSERT INTO password_history (user_id, password_hash)
SELECT user_id, password_hash FROM users_auth;
```

## Verify Hash is Correct

After updating, verify the hash works:
```sql
-- This won't work in MySQL directly, but you can check the hash format
SELECT username, 
       CASE 
         WHEN password_hash LIKE '$2a$10$%' THEN 'Valid BCrypt format'
         ELSE 'Invalid format'
       END as hash_status
FROM users_auth;
```

The hash should:
- Start with `$2a$10$`
- Be exactly 60 characters long
- Have been generated for password "password123"


# Fix "Incorrect Password" - Do This Now

## Quick Fix (2 minutes)

### Step 1: Get a BCrypt Hash

**Go to:** https://bcrypt-generator.com/

1. Enter password: `password123`
2. Rounds: `10` 
3. Click **"Generate Hash"**
4. **Copy the entire hash** (starts with `$2a$10$` and is 60 characters)

### Step 2: Update Database

Open MySQL and run:

```sql
USE univ_auth;

UPDATE users_auth 
SET password_hash = 'PASTE_YOUR_HASH_HERE'
WHERE username IN ('admin1', 'inst1', 'stu1', 'stu2');
```

**Replace `PASTE_YOUR_HASH_HERE` with the hash you copied.**

### Step 3: Test Login

Run the application and try:
- Username: `admin1`
- Password: `password123`

---

## If You Don't Have Internet

You can verify the current hash in your database:

```sql
USE univ_auth;
SELECT username, password_hash FROM users_auth;
```

The hash should:
- Start with `$2a$10$`
- Be 60 characters long
- Match the password "password123"

If the hash looks wrong or is the placeholder, you need to generate a new one.

---

## Alternative: Check Current Hash

Run this to see what's in your database:

```bash
mysql -u auth_user -pauth_pass123 univ_auth -e "SELECT username, LEFT(password_hash, 30) as hash_start FROM users_auth;"
```

If the hash doesn't start with `$2a$10$`, it's invalid and needs to be replaced.


# Quick Test Guide - Student Features

## 🚀 Start the Application

```bash
mvn exec:java
```

## 📋 Test Account
- **Username:** `stu1`
- **Password:** `password123`

## ✅ Quick Test Steps

### 1. Login
1. Launch the app
2. Login with `stu1` / `password123`
3. ✅ Student Dashboard should open with 4 tabs

### 2. Course Catalog (Default: FALL 2025)
1. Go to "Course Catalog" tab
2. Click "Load Sections"
3. ✅ Should see 2 sections:
   - CS201 - Data Structures (Section A) - Already enrolled (stu1)
   - CS205 - Database Systems (Section A) - Available

### 3. Register for a Section
1. Select "CS205 - Database Systems" section
2. Click "Register for Selected Section"
3. ✅ Success message appears
4. Click "Refresh"
5. ✅ Status changes to "Enrolled"

### 4. My Registrations
1. Go to "My Registrations" tab
2. Click "Load My Registrations"
3. ✅ Should see your enrollments:
   - CS201 - Data Structures (already there from seed)
   - CS205 - Database Systems (just registered)

### 5. Drop a Section
1. Select a section in "My Registrations"
2. Click "Drop Selected Section"
3. Confirm the drop
4. ✅ Success message
5. ✅ Section removed from list

### 6. Timetable
1. Go to "Timetable" tab
2. Click "Load Timetable"
3. ✅ Should see your enrolled sections with day/time/room

### 7. Test Error Cases

#### Try to register for same section twice:
1. Go to Course Catalog
2. Select a section you're already enrolled in
3. Try to register
4. ✅ Should show: "Already enrolled in this section."

#### Try to register for full section:
- (Would need a section with 0 available seats - not in seed data)

## 🐛 If Something Doesn't Work

1. **No sections appear:**
   - Check Term = FALL, Year = 2025
   - Verify database has sections: `SELECT * FROM sections;`

2. **Registration fails:**
   - Check database connection in `application.properties`
   - Verify student exists: `SELECT * FROM students WHERE user_id = 3;`

3. **"Enrolled" status not showing:**
   - Check enrollment: `SELECT * FROM enrollments WHERE student_id = 1;`

## 📊 Expected Database State

After testing, you should have:
- 2 enrollments for stu1 (student_id = 1):
  - Section 1 (CS201) - from seed
  - Section 2 (CS205) - if you registered

Check with:
```sql
SELECT e.*, s.section_code, c.code as course_code
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
WHERE e.student_id = 1;
```


# Testing Student Features

## Test Accounts
- **Username:** `stu1` or `stu2`
- **Password:** `password123`
- **Role:** Student

## Test Checklist

### 1. Login as Student
- [ ] Launch the application
- [ ] Enter username: `stu1`
- [ ] Enter password: `password123`
- [ ] Click "Login"
- [ ] Verify: Student Dashboard opens with 4 tabs

### 2. Course Catalog Tab
- [ ] Navigate to "Course Catalog" tab
- [ ] Verify: Sections table displays
- [ ] Test: Select Term (FALL/SPRING) and Year (2024/2025/2026)
- [ ] Click "Load Sections"
- [ ] Verify: Sections appear in the table with columns:
  - Course Code, Course Title, Section, Day, Time, Room, Instructor, Available, Deadline, Status
- [ ] Test: Select a section that shows "Available" status
- [ ] Click "Register for Selected Section"
- [ ] Verify: Success message appears
- [ ] Verify: Section status changes to "Enrolled" after refresh

### 3. My Registrations Tab
- [ ] Navigate to "My Registrations" tab
- [ ] Click "Load My Registrations"
- [ ] Verify: Your enrolled sections appear in the table
- [ ] Test: Select an enrolled section
- [ ] Click "Drop Selected Section"
- [ ] Verify: Confirmation dialog appears
- [ ] Confirm the drop
- [ ] Verify: Success message and section is removed from list

### 4. Timetable Tab
- [ ] Navigate to "Timetable" tab
- [ ] Select Term and Year
- [ ] Click "Load Timetable"
- [ ] Verify: Your enrolled sections appear with:
  - Course, Section, Day, Time, Room, Instructor
- [ ] Verify: Sections are ordered by day and time

### 5. Grades Tab
- [ ] Navigate to "Grades" tab
- [ ] Verify: Grades table is displayed (may be empty if no grades entered)
- [ ] Test: Click "Download Transcript (CSV)" - should show "Coming soon" message
- [ ] Test: Click "Download Transcript (PDF)" - should show "Coming soon" message

### 6. Error Cases to Test

#### Registration Errors:
- [ ] Try to register for a section that's already full
  - Expected: "Section is full. No available seats." error
- [ ] Try to register for the same section twice
  - Expected: "Already enrolled in this section." error
- [ ] Try to register after enrollment deadline
  - Expected: "Enrollment deadline has passed" error

#### Drop Errors:
- [ ] Try to drop a section after drop deadline
  - Expected: "Drop deadline has passed" error

#### Maintenance Mode:
- [ ] (Admin) Toggle Maintenance Mode ON
- [ ] (Student) Try to register for a section
  - Expected: "System is in maintenance mode. Changes are temporarily disabled." error
- [ ] (Student) Try to drop a section
  - Expected: Same maintenance mode error
- [ ] Verify: Maintenance banner appears at top of dashboard

## Known Issues / Placeholders
- Course names in "My Registrations" show as "Course {sectionId}" - needs join with courses table
- Instructor names in timetable show as "Instructor {instructorId}" - needs join with instructors table
- Grades panel is a placeholder - needs implementation
- Transcript export buttons show "Coming soon" - needs CSV/PDF export implementation

## Quick Test Commands

If you need to check the database:
```sql
-- Check student enrollments
SELECT e.*, s.section_code, c.code as course_code
FROM enrollments e
JOIN sections s ON e.section_id = s.section_id
JOIN courses c ON s.course_id = c.course_id
WHERE e.student_id = (SELECT student_id FROM students WHERE user_id = (SELECT user_id FROM users_auth WHERE username = 'stu1'));

-- Check section capacity
SELECT s.section_id, s.section_code, s.capacity, 
       COUNT(e.enrollment_id) as enrolled_count,
       (s.capacity - COUNT(e.enrollment_id)) as available_seats
FROM sections s
LEFT JOIN enrollments e ON s.section_id = e.section_id AND e.status = 'ENROLLED'
GROUP BY s.section_id, s.section_code, s.capacity;
```


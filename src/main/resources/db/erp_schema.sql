CREATE TABLE students (
    student_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL UNIQUE,
    roll_no      VARCHAR(50) NOT NULL UNIQUE,
    program      VARCHAR(100) NOT NULL,
    year_of_study INT NOT NULL,
    status       ENUM ('ACTIVE', 'GRADUATED', 'ON_LEAVE') NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE instructors (
    instructor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT NOT NULL UNIQUE,
    department    VARCHAR(120) NOT NULL,
    title         VARCHAR(80),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    course_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    code          VARCHAR(20) NOT NULL UNIQUE,
    title         VARCHAR(150) NOT NULL,
    credits       DECIMAL(3,1) NOT NULL CHECK (credits > 0),
    description   TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sections (
    section_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id     BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    semester      ENUM ('MONSOON', 'WINTER') NOT NULL,
    year          SMALLINT NOT NULL,
    section_code  VARCHAR(10) NOT NULL,
    day_of_week   ENUM ('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT') NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    room          VARCHAR(40) NOT NULL,
    capacity      INT NOT NULL CHECK (capacity > 0),
    enrollment_deadline DATE NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (course_id, semester, year, section_code),
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id)
);

CREATE TABLE enrollments (
    enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id    BIGINT NOT NULL,
    section_id    BIGINT NOT NULL,
    status        ENUM ('ENROLLED', 'DROPPED', 'COMPLETED') NOT NULL DEFAULT 'ENROLLED',
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dropped_at    TIMESTAMP NULL,
    UNIQUE (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
);

CREATE TABLE assessments (
    assessment_id  BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id     BIGINT NOT NULL,
    name           VARCHAR(100) NOT NULL,
    weight_percent DECIMAL(5,2) NOT NULL CHECK (weight_percent >= 0),
    max_score      DECIMAL(7,2) NOT NULL CHECK (max_score > 0),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES sections(section_id)
);

CREATE TABLE grades (
    grade_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL,
    assessment_id BIGINT NOT NULL,
    score         DECIMAL(7,2) NOT NULL CHECK (score >= 0),
    recorded_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (enrollment_id, assessment_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id),
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id)
);

CREATE TABLE final_grades (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL UNIQUE,
    final_score   DECIMAL(7,2) NOT NULL CHECK (final_score >= 0),
    letter_grade  VARCHAR(5) NOT NULL,
    computed_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

CREATE TABLE settings (
    setting_key   VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(200) NOT NULL,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Performance indexes for common queries
CREATE INDEX idx_sections_semester_year ON sections(semester, year);
CREATE INDEX idx_sections_instructor ON sections(instructor_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id, status);
CREATE INDEX idx_enrollments_section ON enrollments(section_id, status);
CREATE INDEX idx_assessments_section ON assessments(section_id);
CREATE INDEX idx_grades_enrollment ON grades(enrollment_id);


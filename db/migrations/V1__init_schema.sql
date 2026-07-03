CREATE DATABASE IF NOT EXISTS placepro
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE placepro;

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    branch VARCHAR(50) NOT NULL,
    cgpa DECIMAL(4,2) NOT NULL,
    backlog_count INT NOT NULL DEFAULT 0,
    graduation_year INT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE placement_officers (
    officer_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('OFFICER','ADMIN') NOT NULL DEFAULT 'OFFICER',
    department VARCHAR(50) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE companies (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL UNIQUE,
    industry VARCHAR(100) NULL,
    contact_person VARCHAR(100) NULL,
    email VARCHAR(100) NULL,
    phone VARCHAR(15) NULL,
    website VARCHAR(200) NULL,
    address TEXT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE recruiters (
    recruiter_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NULL,
    password_hash VARCHAR(255) NOT NULL,
    designation VARCHAR(50) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recruiters_company
        FOREIGN KEY (company_id)
        REFERENCES companies(company_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE placement_drives (
    drive_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    job_description TEXT NULL,
    package_min DECIMAL(10,2) NULL,
    package_max DECIMAL(10,2) NULL,
    min_cgpa DECIMAL(4,2) NOT NULL,
    max_backlogs INT NOT NULL DEFAULT 0,
    allowed_branches VARCHAR(500) NOT NULL,
    visit_date DATE NULL,
    application_deadline DATETIME NOT NULL,
    status ENUM('DRAFT','PUBLISHED','CLOSED','COMPLETED') NOT NULL DEFAULT 'DRAFT',
    created_by INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_placement_drives_company
        FOREIGN KEY (company_id)
        REFERENCES companies(company_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_placement_drives_created_by
        FOREIGN KEY (created_by)
        REFERENCES placement_officers(officer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE resumes (
    resume_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(10) NOT NULL,
    file_size_kb INT NOT NULL,
    is_current TINYINT(1) NOT NULL DEFAULT 1,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resumes_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    drive_id INT NOT NULL,
    resume_id INT NULL,
    status ENUM('APPLIED','SHORTLISTED','INTERVIEW_SCHEDULED','SELECTED','REJECTED','ON_HOLD') NOT NULL DEFAULT 'APPLIED',
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_drive UNIQUE (student_id, drive_id),
    CONSTRAINT fk_applications_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_applications_drive
        FOREIGN KEY (drive_id)
        REFERENCES placement_drives(drive_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_applications_resume
        FOREIGN KEY (resume_id)
        REFERENCES resumes(resume_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE interview_schedule (
    interview_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    round_number INT NOT NULL DEFAULT 1,
    round_type VARCHAR(50) NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    venue VARCHAR(150) NULL,
    outcome ENUM('PENDING','SELECTED','REJECTED','ON_HOLD') NOT NULL DEFAULT 'PENDING',
    notes TEXT NULL,
    created_by_officer_id INT NULL,
    created_by_recruiter_id INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_application_round UNIQUE (application_id, round_number),
    CONSTRAINT fk_interview_schedule_application
        FOREIGN KEY (application_id)
        REFERENCES applications(application_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_interview_schedule_officer
        FOREIGN KEY (created_by_officer_id)
        REFERENCES placement_officers(officer_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_interview_schedule_recruiter
        FOREIGN KEY (created_by_recruiter_id)
        REFERENCES recruiters(recruiter_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NULL,
    officer_id INT NULL,
    recruiter_id INT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type ENUM('DRIVE_PUBLISHED','STATUS_CHANGE','INTERVIEW_SCHEDULED','GENERAL') NOT NULL,
    reference_id INT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_notifications_officer
        FOREIGN KEY (officer_id)
        REFERENCES placement_officers(officer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_notifications_recruiter
        FOREIGN KEY (recruiter_id)
        REFERENCES recruiters(recruiter_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_notifications_exactly_one_recipient
        CHECK (
            (student_id IS NOT NULL) +
            (officer_id IS NOT NULL) +
            (recruiter_id IS NOT NULL) = 1
        )
) ENGINE=InnoDB;

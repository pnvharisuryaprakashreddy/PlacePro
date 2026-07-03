USE placepro;

INSERT INTO placement_officers (
    employee_id, full_name, email, phone, password_hash, role, department, is_active
) VALUES
    ('EMP001', 'Anita Rao', 'anita.rao@placepro.local', '9876500001', '$2a$10$pJZ/r7pTYrCwj8vvJ4Pi6egnkoz9R2/SnADjiWTYouFBgbSntc4Ye', 'OFFICER', 'Training and Placement', 1),
    ('EMP002', 'Rahul Mehta', 'rahul.mehta@placepro.local', '9876500002', '$2a$10$NvZsT/5zlA1zpqrAx9DrLeZeXEOZUEj5yczvMOTrnXpVRKzwprujy', 'ADMIN', 'Training and Placement', 1);

INSERT INTO companies (
    company_name, industry, contact_person, email, phone, website, address, is_active
) VALUES
    ('TechNova Solutions', 'Information Technology', 'Sonia Kapoor', 'contact@technova.example', '9123400001', 'https://www.technova.example', 'Bengaluru, Karnataka', 1),
    ('FinEdge Analytics', 'Financial Services', 'Rohit Sinha', 'hr@finedge.example', '9123400002', 'https://www.finedge.example', 'Hyderabad, Telangana', 1),
    ('CloudAxis Systems', 'Cloud Infrastructure', 'Maya Iyer', 'careers@cloudaxis.example', '9123400003', 'https://www.cloudaxis.example', 'Chennai, Tamil Nadu', 1);

INSERT INTO recruiters (
    company_id, full_name, email, phone, password_hash, designation, is_active
) VALUES
    (1, 'Karan Malhotra', 'karan.malhotra@technova.example', '9988700001', '$2a$10$CAURhHe4ODzY1CITfOQoreE5Xhxgxp.erbUjWyF526/v1OHX75YOa', 'Talent Acquisition Lead', 1),
    (2, 'Neha Verma', 'neha.verma@finedge.example', '9988700002', '$2a$10$CAURhHe4ODzY1CITfOQoreE5Xhxgxp.erbUjWyF526/v1OHX75YOa', 'Senior Recruiter', 1),
    (3, 'Arjun Pillai', 'arjun.pillai@cloudaxis.example', '9988700003', '$2a$10$CAURhHe4ODzY1CITfOQoreE5Xhxgxp.erbUjWyF526/v1OHX75YOa', 'Campus Hiring Specialist', 1);

INSERT INTO students (
    roll_number, full_name, email, phone, password_hash, branch, cgpa, backlog_count, graduation_year, is_active
) VALUES
    ('CSE22001', 'Aarav Sharma', 'aarav.sharma@student.placepro.local', '9000000001', '$2a$10$IGTgAd9NO1B1EN8Ltl57UOX6RLuQuzKvcrYwoXYeAvEuQ3uj4krcu', 'CSE', 8.75, 0, 2027, 1),
    ('ECE22014', 'Diya Nair', 'diya.nair@student.placepro.local', '9000000002', '$2a$10$IGTgAd9NO1B1EN8Ltl57UOX6RLuQuzKvcrYwoXYeAvEuQ3uj4krcu', 'ECE', 8.10, 0, 2027, 1),
    ('IT22009', 'Ishaan Gupta', 'ishaan.gupta@student.placepro.local', '9000000003', '$2a$10$IGTgAd9NO1B1EN8Ltl57UOX6RLuQuzKvcrYwoXYeAvEuQ3uj4krcu', 'IT', 7.90, 1, 2027, 1),
    ('ME22005', 'Kavya Reddy', 'kavya.reddy@student.placepro.local', '9000000004', '$2a$10$IGTgAd9NO1B1EN8Ltl57UOX6RLuQuzKvcrYwoXYeAvEuQ3uj4krcu', 'ME', 8.20, 0, 2027, 1),
    ('CSE22022', 'Nikhil Das', 'nikhil.das@student.placepro.local', '9000000005', '$2a$10$IGTgAd9NO1B1EN8Ltl57UOX6RLuQuzKvcrYwoXYeAvEuQ3uj4krcu', 'CSE', 9.05, 0, 2027, 1);

INSERT INTO placement_drives (
    company_id, job_title, job_description, package_min, package_max, min_cgpa,
    max_backlogs, allowed_branches, visit_date, application_deadline, status, created_by
) VALUES
    (
        1,
        'Graduate Software Engineer',
        'Entry-level software engineering role for campus hires focused on Java backend development.',
        6.50,
        8.00,
        7.50,
        0,
        'CSE,ECE,IT',
        '2026-09-15',
        '2026-09-10 23:59:59',
        'PUBLISHED',
        1
    ),
    (
        2,
        'Data Analyst Trainee',
        'Analyst trainee role covering SQL, reporting, and business intelligence workflows.',
        5.00,
        6.50,
        7.00,
        1,
        'CSE,IT,ECE',
        '2026-09-22',
        '2026-09-18 23:59:59',
        'PUBLISHED',
        2
    );

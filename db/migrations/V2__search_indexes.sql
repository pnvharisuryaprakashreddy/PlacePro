-- Indexes supporting the global search screens (student directory and
-- company search). Foreign keys already index applications.student_id,
-- applications.drive_id, and placement_drives.company_id.

USE placepro;

-- Student directory filters
CREATE INDEX idx_students_branch ON students (branch);
CREATE INDEX idx_students_cgpa ON students (cgpa);
CREATE INDEX idx_students_full_name ON students (full_name);

-- Placement-status EXISTS subqueries (student_id prefix is covered by
-- uk_student_drive; this covers status-first scans used by reports too)
CREATE INDEX idx_applications_status ON applications (status);

-- Company search filters
CREATE INDEX idx_companies_industry ON companies (industry);

-- Active-drive EXISTS subquery (company_id + status)
CREATE INDEX idx_drives_company_status ON placement_drives (company_id, status);

package com.placepro.service.drive;

import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityServiceTest {

    private StubStudentDAO studentDAO;
    private StubPlacementDriveDAO placementDriveDAO;
    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        studentDAO = new StubStudentDAO();
        placementDriveDAO = new StubPlacementDriveDAO();
        eligibilityService = new EligibilityService(studentDAO, placementDriveDAO);
    }

    @Test
    void eligibleStudentSucceeds() {
        studentDAO.save(student(1, "CSE", new BigDecimal("8.50"), 0));
        placementDriveDAO.save(drive(10, new BigDecimal("7.50"), 0, "CSE,ECE,IT"));

        EligibilityResult result = eligibilityService.check(1, 10);

        assertTrue(result.isEligible());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void ineligibleStudentIsRejectedWithReasons() {
        studentDAO.save(student(2, "ME", new BigDecimal("6.50"), 2));
        placementDriveDAO.save(drive(20, new BigDecimal("7.50"), 1, "CSE,ECE,IT"));

        EligibilityResult result = eligibilityService.check(2, 20);

        assertFalse(result.isEligible());
        List<String> reasons = result.getReasons();
        assertEquals(3, reasons.size());
        assertTrue(reasons.stream().anyMatch(reason -> reason.contains("CGPA")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.contains("Backlog")));
        assertTrue(reasons.stream().anyMatch(reason -> reason.contains("Branch")));
    }

    private Student student(int id, String branch, BigDecimal cgpa, int backlogs) {
        Student student = new Student();
        student.setStudentId(id);
        student.setBranch(branch);
        student.setCgpa(cgpa);
        student.setBacklogCount(backlogs);
        return student;
    }

    private PlacementDrive drive(int id, BigDecimal minCgpa, int maxBacklogs, String branches) {
        PlacementDrive drive = new PlacementDrive();
        drive.setDriveId(id);
        drive.setMinCgpa(minCgpa);
        drive.setMaxBacklogs(maxBacklogs);
        drive.setAllowedBranches(branches);
        drive.setStatus(DriveStatus.PUBLISHED.name());
        drive.setJobTitle("Software Engineer");
        drive.setApplicationDeadline(LocalDateTime.now().plusDays(7));
        return drive;
    }

    private static final class StubStudentDAO implements StudentDAO {
        private final Map<Integer, Student> students = new HashMap<>();

        void save(Student student) {
            students.put(student.getStudentId(), student);
        }

        @Override
        public Student insert(Student student) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Student> findById(int studentId) {
            return Optional.ofNullable(students.get(studentId));
        }

        @Override
        public Optional<Student> findByEmail(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Student> findByRollNumber(String rollNumber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Student> findAllActive() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Student> findAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Student> searchByNameOrRollNumber(String keyword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<com.placepro.dao.StudentSearchRow> searchStudents(
                com.placepro.dao.StudentSearchCriteria criteria, int offset, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int countStudents(com.placepro.dao.StudentSearchCriteria criteria) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean update(Student student) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deactivate(int studentId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteById(int studentId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubPlacementDriveDAO implements PlacementDriveDAO {
        private final Map<Integer, PlacementDrive> drives = new HashMap<>();

        void save(PlacementDrive drive) {
            drives.put(drive.getDriveId(), drive);
        }

        @Override
        public PlacementDrive insert(PlacementDrive drive) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<PlacementDrive> findById(int driveId) {
            return Optional.ofNullable(drives.get(driveId));
        }

        @Override
        public List<PlacementDrive> findByCompanyId(int companyId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PlacementDrive> findByStatus(String status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PlacementDrive> findPublishedDrives() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PlacementDrive> findAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean update(PlacementDrive drive) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteById(int driveId) {
            throw new UnsupportedOperationException();
        }
    }
}

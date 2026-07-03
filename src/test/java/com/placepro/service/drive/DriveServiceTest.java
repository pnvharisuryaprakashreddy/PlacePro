package com.placepro.service.drive;

import com.placepro.dao.NotificationDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.Notification;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveServiceTest {

    private StubPlacementDriveDAO placementDriveDAO;
    private DriveService driveService;

    @BeforeEach
    void setUp() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.setSession(1, UserRole.OFFICER);

        placementDriveDAO = new StubPlacementDriveDAO();
        NotificationService notificationService = new NotificationService(
                new EmptyNotificationDAO(),
                sessionManager);

        driveService = new DriveService(
                placementDriveDAO,
                new EmptyStudentDAO(),
                notificationService,
                sessionManager);
    }

    @Test
    void publishDriveTransitionsDraftToPublished() {
        placementDriveDAO.save(drive(1, DriveStatus.DRAFT.name()));

        PlacementDrive published = driveService.publishDrive(1);

        assertEquals(DriveStatus.PUBLISHED.name(), published.getStatus());
        assertEquals(DriveStatus.PUBLISHED.name(), placementDriveDAO.findById(1).orElseThrow().getStatus());
    }

    @Test
    void publishDriveRejectsInvalidLifecycleTransition() {
        placementDriveDAO.save(drive(2, DriveStatus.PUBLISHED.name()));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> driveService.publishDrive(2));

        assertTrue(exception.getMessage().contains("Invalid drive transition"));
    }

    @Test
    void closeDriveRejectsWhenNotPublished() {
        placementDriveDAO.save(drive(3, DriveStatus.DRAFT.name()));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> driveService.closeDrive(3));

        assertTrue(exception.getMessage().contains("Invalid drive transition"));
    }

    @Test
    void completeDriveRejectsWhenNotClosed() {
        placementDriveDAO.save(drive(4, DriveStatus.PUBLISHED.name()));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> driveService.completeDrive(4));

        assertTrue(exception.getMessage().contains("Invalid drive transition"));
    }

    @Test
    void closeThenCompleteFollowsValidLifecycle() {
        placementDriveDAO.save(drive(5, DriveStatus.PUBLISHED.name()));

        PlacementDrive closed = driveService.closeDrive(5);
        assertEquals(DriveStatus.CLOSED.name(), closed.getStatus());

        PlacementDrive completed = driveService.completeDrive(5);
        assertEquals(DriveStatus.COMPLETED.name(), completed.getStatus());
    }

    private PlacementDrive drive(int id, String status) {
        PlacementDrive drive = new PlacementDrive();
        drive.setDriveId(id);
        drive.setCompanyId(1);
        drive.setJobTitle("Software Engineer");
        drive.setMinCgpa(new BigDecimal("7.00"));
        drive.setMaxBacklogs(0);
        drive.setAllowedBranches("CSE");
        drive.setStatus(status);
        drive.setApplicationDeadline(LocalDateTime.now().plusDays(7));
        drive.setCreatedBy(1);
        return drive;
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
            return new ArrayList<>();
        }

        @Override
        public List<PlacementDrive> findByStatus(String status) {
            return new ArrayList<>();
        }

        @Override
        public List<PlacementDrive> findPublishedDrives() {
            return new ArrayList<>();
        }

        @Override
        public List<PlacementDrive> findAll() {
            return new ArrayList<>(drives.values());
        }

        @Override
        public boolean update(PlacementDrive drive) {
            drives.put(drive.getDriveId(), drive);
            return true;
        }

        @Override
        public boolean deleteById(int driveId) {
            return false;
        }
    }

    private static final class EmptyStudentDAO implements StudentDAO {
        @Override
        public Student insert(Student student) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Student> findById(int studentId) {
            return Optional.empty();
        }

        @Override
        public Optional<Student> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<Student> findByRollNumber(String rollNumber) {
            return Optional.empty();
        }

        @Override
        public List<Student> findAllActive() {
            return new ArrayList<>();
        }

        @Override
        public List<Student> findAll() {
            return new ArrayList<>();
        }

        @Override
        public List<Student> searchByNameOrRollNumber(String keyword) {
            return new ArrayList<>();
        }

        @Override
        public List<com.placepro.dao.StudentSearchRow> searchStudents(
                com.placepro.dao.StudentSearchCriteria criteria, int offset, int limit) {
            return new ArrayList<>();
        }

        @Override
        public int countStudents(com.placepro.dao.StudentSearchCriteria criteria) {
            return 0;
        }

        @Override
        public boolean update(Student student) {
            return false;
        }

        @Override
        public boolean deactivate(int studentId) {
            return false;
        }

        @Override
        public boolean deleteById(int studentId) {
            return false;
        }
    }

    private static final class EmptyNotificationDAO implements NotificationDAO {
        @Override
        public Notification insert(Notification notification) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Notification insert(java.sql.Connection connection, Notification notification) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Notification> findById(int notificationId) {
            return Optional.empty();
        }

        @Override
        public List<Notification> findByStudentId(int studentId) {
            return new ArrayList<>();
        }

        @Override
        public List<Notification> findByOfficerId(int officerId) {
            return new ArrayList<>();
        }

        @Override
        public List<Notification> findByRecruiterId(int recruiterId) {
            return new ArrayList<>();
        }

        @Override
        public List<Notification> findUnreadByStudentId(int studentId) {
            return new ArrayList<>();
        }

        @Override
        public boolean update(Notification notification) {
            return false;
        }

        @Override
        public boolean deleteById(int notificationId) {
            return false;
        }
    }
}

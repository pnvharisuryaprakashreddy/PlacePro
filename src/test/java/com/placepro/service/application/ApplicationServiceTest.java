package com.placepro.service.application;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.CompanyDAO;
import com.placepro.dao.NotificationDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.ResumeDAO;
import com.placepro.model.Application;
import com.placepro.model.Notification;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Resume;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveStatus;
import com.placepro.service.drive.EligibilityResult;
import com.placepro.service.drive.EligibilityService;
import com.placepro.service.notification.NotificationService;
import com.placepro.util.TransactionCallback;
import com.placepro.util.TransactionRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {

    private SessionManager sessionManager;
    private StubApplicationDAO applicationDAO;
    private StubNotificationDAO notificationDAO;
    private NotificationService notificationService;
    private StubStudentDAO studentDAO;
    private StubPlacementDriveDAO placementDriveDAO;
    private StubResumeDAO resumeDAO;
    private StubEligibilityService eligibilityService;
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
        sessionManager.setSession(1, UserRole.STUDENT);

        applicationDAO = new StubApplicationDAO();
        notificationDAO = new StubNotificationDAO();
        notificationService = new NotificationService(notificationDAO, sessionManager);
        studentDAO = new StubStudentDAO();
        placementDriveDAO = new StubPlacementDriveDAO();
        resumeDAO = new StubResumeDAO();
        eligibilityService = new StubEligibilityService();

        placementDriveDAO.save(drive(100, "Graduate Engineer"));
        resumeDAO.saveCurrentResume(1, 500);

        applicationService = new ApplicationService(
                applicationDAO,
                notificationService,
                placementDriveDAO,
                new StubCompanyDAO(),
                resumeDAO,
                studentDAO,
                eligibilityService,
                sessionManager,
                new TransactionRunner() {
                    @Override
                    public <T> T execute(TransactionCallback<T> callback) {
                        try {
                            return callback.execute(null);
                        } catch (SQLException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
                });
    }

    @Test
    void eligibleStudentApplicationSucceeds() {
        eligibilityService.setResult(EligibilityResult.eligible());

        Application application = applicationService.submitApplication(1, 100);

        assertNotNull(application.getApplicationId());
        assertEquals(ApplicationStatus.APPLIED.name(), application.getStatus());
        assertEquals(500, application.getResumeId());
        assertEquals(1, applicationDAO.insertCount);
        assertEquals(1, notificationDAO.insertCount);
    }

    @Test
    void ineligibleStudentApplicationIsRejectedWithReasons() {
        eligibilityService.setResult(EligibilityResult.ineligible(List.of("CGPA too low.")));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> applicationService.submitApplication(1, 100));

        assertTrue(exception.getMessage().contains("CGPA too low."));
        assertEquals(0, applicationDAO.insertCount);
        assertEquals(0, notificationDAO.insertCount);
    }

    @Test
    void duplicateApplicationIsRejected() {
        eligibilityService.setResult(EligibilityResult.eligible());
        applicationDAO.save(existingApplication(1, 100));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> applicationService.submitApplication(1, 100));

        assertTrue(exception.getMessage().contains("already applied"));
        assertEquals(0, applicationDAO.insertCount);
        assertEquals(0, notificationDAO.insertCount);
    }

    @Test
    void transactionalInsertRollsBackWhenNotificationFails() {
        eligibilityService.setResult(EligibilityResult.eligible());
        TransactionalStubApplicationDAO transactionalApplicationDAO = new TransactionalStubApplicationDAO();
        StubNotificationDAO failingNotificationDAO = new StubNotificationDAO();
        failingNotificationDAO.failOnInsert = true;

        NotificationService failingNotificationService = new NotificationService(
                failingNotificationDAO, sessionManager);

        ApplicationService transactionalService = new ApplicationService(
                transactionalApplicationDAO,
                failingNotificationService,
                placementDriveDAO,
                new StubCompanyDAO(),
                resumeDAO,
                studentDAO,
                eligibilityService,
                sessionManager,
                new CommitAwareTransactionRunner(transactionalApplicationDAO));

        assertThrows(RuntimeException.class, () -> transactionalService.submitApplication(1, 100));
        assertEquals(1, transactionalApplicationDAO.insertAttempts);
        assertEquals(0, transactionalApplicationDAO.committedInserts);
        assertTrue(transactionalApplicationDAO.findByStudentAndDrive(1, 100).isEmpty());
    }

    private PlacementDrive drive(int id, String title) {
        PlacementDrive drive = new PlacementDrive();
        drive.setDriveId(id);
        drive.setCompanyId(1);
        drive.setJobTitle(title);
        drive.setMinCgpa(new BigDecimal("7.00"));
        drive.setMaxBacklogs(0);
        drive.setAllowedBranches("CSE");
        drive.setStatus(DriveStatus.PUBLISHED.name());
        drive.setApplicationDeadline(LocalDateTime.now().plusDays(5));
        return drive;
    }

    private Application existingApplication(int studentId, int driveId) {
        Application application = new Application();
        application.setApplicationId(999);
        application.setStudentId(studentId);
        application.setDriveId(driveId);
        application.setStatus(ApplicationStatus.APPLIED.name());
        return application;
    }

    private static final class StubEligibilityService extends EligibilityService {
        private EligibilityResult nextResult = EligibilityResult.eligible();

        StubEligibilityService() {
            super(null, null);
        }

        void setResult(EligibilityResult result) {
            this.nextResult = result;
        }

        @Override
        public EligibilityResult check(int studentId, int driveId) {
            return nextResult;
        }
    }

    private static final class CommitAwareTransactionRunner implements TransactionRunner {
        private final TransactionalStubApplicationDAO applicationDAO;

        CommitAwareTransactionRunner(TransactionalStubApplicationDAO applicationDAO) {
            this.applicationDAO = applicationDAO;
        }

        @Override
        public <T> T execute(TransactionCallback<T> callback) {
            try {
                T result = callback.execute(null);
                applicationDAO.commitPending();
                return result;
            } catch (RuntimeException exception) {
                applicationDAO.rollbackPending();
                throw exception;
            } catch (SQLException exception) {
                applicationDAO.rollbackPending();
                throw new RuntimeException(exception);
            }
        }
    }

    private static final class TransactionalStubApplicationDAO extends StubApplicationDAO {
        private Application pendingInsert;
        private int insertAttempts;
        private int committedInserts;

        void commitPending() {
            if (pendingInsert != null) {
                save(pendingInsert);
                committedInserts++;
                pendingInsert = null;
            }
        }

        void rollbackPending() {
            pendingInsert = null;
        }

        @Override
        public Application insert(Connection connection, Application application) {
            insertAttempts++;
            application.setApplicationId(idSequence.incrementAndGet());
            pendingInsert = application;
            return application;
        }
    }

    private static class StubApplicationDAO implements ApplicationDAO {
        private final Map<String, Application> applications = new HashMap<>();
        protected final AtomicInteger idSequence = new AtomicInteger(1000);
        protected int insertCount;

        protected void save(Application application) {
            applications.put(key(application.getStudentId(), application.getDriveId()), application);
        }

        @Override
        public Application insert(Application application) {
            return insert(null, application);
        }

        @Override
        public Application insert(Connection connection, Application application) {
            insertCount++;
            application.setApplicationId(idSequence.incrementAndGet());
            save(application);
            return application;
        }

        @Override
        public Optional<Application> findById(int applicationId) {
            return applications.values().stream()
                    .filter(application -> application.getApplicationId().equals(applicationId))
                    .findFirst();
        }

        @Override
        public Optional<Application> findByStudentAndDrive(int studentId, int driveId) {
            return Optional.ofNullable(applications.get(key(studentId, driveId)));
        }

        @Override
        public List<Application> findByStudentId(int studentId) {
            return new ArrayList<>();
        }

        @Override
        public List<Application> findByDriveId(int driveId) {
            return new ArrayList<>();
        }

        @Override
        public List<Application> findByStatus(String status) {
            return new ArrayList<>();
        }

        @Override
        public boolean update(Application application) {
            return true;
        }

        @Override
        public boolean update(Connection connection, Application application) {
            return true;
        }

        @Override
        public boolean deleteById(int applicationId) {
            return false;
        }

        private String key(int studentId, int driveId) {
            return studentId + ":" + driveId;
        }
    }

    private static class StubNotificationDAO implements NotificationDAO {
        private int insertCount;
        private boolean failOnInsert;

        @Override
        public Notification insert(Notification notification) {
            return insert(null, notification);
        }

        @Override
        public Notification insert(Connection connection, Notification notification) {
            insertCount++;
            if (failOnInsert) {
                throw new RuntimeException("Simulated notification insert failure.");
            }
            notification.setNotificationId(1);
            return notification;
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
            return new ArrayList<>();
        }

        @Override
        public boolean update(PlacementDrive drive) {
            return false;
        }

        @Override
        public boolean deleteById(int driveId) {
            return false;
        }
    }

    private static final class StubResumeDAO implements ResumeDAO {
        private final Map<Integer, Resume> currentResumes = new HashMap<>();

        void saveCurrentResume(int studentId, int resumeId) {
            Resume resume = new Resume();
            resume.setResumeId(resumeId);
            resume.setStudentId(studentId);
            resume.setIsCurrent(true);
            currentResumes.put(studentId, resume);
        }

        @Override
        public Resume insert(Resume resume) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Resume> findById(int resumeId) {
            return Optional.empty();
        }

        @Override
        public Optional<Resume> findCurrentByStudentId(int studentId) {
            return Optional.ofNullable(currentResumes.get(studentId));
        }

        @Override
        public List<Resume> findByStudentId(int studentId) {
            return new ArrayList<>();
        }

        @Override
        public boolean update(Resume resume) {
            return false;
        }

        @Override
        public boolean deleteById(int resumeId) {
            return false;
        }
    }

    private static final class StubCompanyDAO implements CompanyDAO {
        @Override
        public com.placepro.model.Company insert(com.placepro.model.Company company) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.placepro.model.Company> findById(int companyId) {
            com.placepro.model.Company company = new com.placepro.model.Company();
            company.setCompanyId(companyId);
            company.setCompanyName("Test Company");
            return Optional.of(company);
        }

        @Override
        public Optional<com.placepro.model.Company> findByCompanyName(String companyName) {
            return Optional.empty();
        }

        @Override
        public java.util.List<com.placepro.model.Company> findAllActive() {
            return new ArrayList<>();
        }

        @Override
        public java.util.List<com.placepro.model.Company> findAll() {
            return new ArrayList<>();
        }

        @Override
        public java.util.List<com.placepro.model.Company> searchCompanies(
                String nameKeyword, String industryKeyword, String activeFilter, String driveFilter) {
            return new ArrayList<>();
        }

        @Override
        public java.util.List<com.placepro.model.Company> searchByName(String keyword) {
            return new ArrayList<>();
        }

        @Override
        public boolean update(com.placepro.model.Company company) {
            return false;
        }

        @Override
        public boolean deactivate(int companyId) {
            return false;
        }

        @Override
        public boolean deleteById(int companyId) {
            return false;
        }
    }

    private static final class StubStudentDAO implements com.placepro.dao.StudentDAO {
        @Override
        public com.placepro.model.Student insert(com.placepro.model.Student student) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.placepro.model.Student> findById(int studentId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.placepro.model.Student> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<com.placepro.model.Student> findByRollNumber(String rollNumber) {
            return Optional.empty();
        }

        @Override
        public java.util.List<com.placepro.model.Student> findAll() {
            return new ArrayList<>();
        }

        @Override
        public java.util.List<com.placepro.model.Student> searchByNameOrRollNumber(String keyword) {
            return new ArrayList<>();
        }

        @Override
        public java.util.List<com.placepro.dao.StudentSearchRow> searchStudents(
                com.placepro.dao.StudentSearchCriteria criteria, int offset, int limit) {
            return new ArrayList<>();
        }

        @Override
        public int countStudents(com.placepro.dao.StudentSearchCriteria criteria) {
            return 0;
        }

        @Override
        public java.util.List<com.placepro.model.Student> findAllActive() {
            return new ArrayList<>();
        }

        @Override
        public boolean update(com.placepro.model.Student student) {
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
}

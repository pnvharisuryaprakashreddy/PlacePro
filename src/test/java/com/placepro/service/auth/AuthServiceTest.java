package com.placepro.service.auth;

import com.placepro.dao.PlacementOfficerDAO;
import com.placepro.dao.RecruiterDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class AuthServiceTest {

    private static final String PASSWORD = "Password@123";

    private StubStudentDAO studentDAO;
    private SessionManager sessionManager;
    private LoginAttemptTracker loginAttemptTracker;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        studentDAO = new StubStudentDAO();
        sessionManager = new SessionManager();
        loginAttemptTracker = new LoginAttemptTracker();
        authService = new AuthService(
                studentDAO,
                new EmptyPlacementOfficerDAO(),
                new EmptyRecruiterDAO(),
                sessionManager,
                loginAttemptTracker);
    }

    @Test
    void registerStudentSucceedsAndCreatesSession() {
        Student student = newStudent("21CSE001", "alice@campus.edu");

        Student saved = authService.registerStudent(student, PASSWORD);

        assertNotNull(saved.getStudentId());
        assertTrue(PasswordUtil.verifyPassword(PASSWORD, saved.getPasswordHash()));
        assertEquals(UserRole.STUDENT, sessionManager.getCurrentRole().orElseThrow());
        assertEquals(saved.getStudentId(), sessionManager.getCurrentUserId().orElseThrow());
    }

    @Test
    void registerStudentRejectsDuplicateRollNumber() {
        studentDAO.save(registeredStudent("21CSE001", "existing@campus.edu"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> authService.registerStudent(newStudent("21CSE001", "new@campus.edu"), PASSWORD));

        assertTrue(exception.getMessage().contains("roll number"));
        assertTrue(sessionManager.getCurrentRole().isEmpty());
    }

    @Test
    void registerStudentRejectsDuplicateEmail() {
        studentDAO.save(registeredStudent("21CSE002", "alice@campus.edu"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> authService.registerStudent(newStudent("21CSE099", "alice@campus.edu"), PASSWORD));

        assertTrue(exception.getMessage().contains("email"));
        assertTrue(sessionManager.getCurrentRole().isEmpty());
    }

    @Test
    void loginStudentSucceedsWithValidCredentials() {
        studentDAO.save(registeredStudent("21CSE001", "alice@campus.edu"));

        Student loggedIn = authService.loginStudent("alice@campus.edu", PASSWORD);

        assertEquals("alice@campus.edu", loggedIn.getEmail());
        assertEquals(UserRole.STUDENT, sessionManager.getCurrentRole().orElseThrow());
    }

    @Test
    void loginStudentFailsWithWrongPassword() {
        studentDAO.save(registeredStudent("21CSE001", "alice@campus.edu"));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> authService.loginStudent("alice@campus.edu", "WrongPassword1"));

        assertTrue(exception.getMessage().contains("Invalid email or password"));
        assertTrue(sessionManager.getCurrentRole().isEmpty());
    }

    @Test
    void loginStudentFailsWithUnknownEmail() {
        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> authService.loginStudent("nobody@campus.edu", PASSWORD));

        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    @Test
    void accountLocksAfterFiveFailedLoginAttempts() {
        studentDAO.save(registeredStudent("21CSE001", "alice@campus.edu"));
        String email = "alice@campus.edu";

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(ServiceException.class, () -> authService.loginStudent(email, "wrong"));
        }

        ServiceException locked = assertThrows(
                ServiceException.class,
                () -> authService.loginStudent(email, PASSWORD));

        assertTrue(locked.getMessage().contains("locked"));
    }

    private Student newStudent(String rollNumber, String email) {
        Student student = new Student();
        student.setRollNumber(rollNumber);
        student.setFullName("Test Student");
        student.setEmail(email);
        student.setBranch("CSE");
        student.setIsActive(true);
        return student;
    }

    private Student registeredStudent(String rollNumber, String email) {
        Student student = newStudent(rollNumber, email);
        student.setStudentId(1);
        student.setPasswordHash(PasswordUtil.hashPassword(PASSWORD));
        return student;
    }

    private static final class StubStudentDAO implements StudentDAO {
        private final Map<Integer, Student> byId = new HashMap<>();
        private final Map<String, Student> byEmail = new HashMap<>();
        private final Map<String, Student> byRoll = new HashMap<>();
        private final AtomicInteger idSequence = new AtomicInteger(1);

        void save(Student student) {
            byId.put(student.getStudentId(), student);
            byEmail.put(student.getEmail().toLowerCase(), student);
            byRoll.put(student.getRollNumber(), student);
        }

        @Override
        public Student insert(Student student) {
            student.setStudentId(idSequence.getAndIncrement());
            save(student);
            return student;
        }

        @Override
        public Optional<Student> findById(int studentId) {
            return Optional.ofNullable(byId.get(studentId));
        }

        @Override
        public Optional<Student> findByEmail(String email) {
            return Optional.ofNullable(byEmail.get(email.toLowerCase()));
        }

        @Override
        public Optional<Student> findByRollNumber(String rollNumber) {
            return Optional.ofNullable(byRoll.get(rollNumber));
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

    private static final class EmptyPlacementOfficerDAO implements PlacementOfficerDAO {
        @Override
        public com.placepro.model.PlacementOfficer insert(com.placepro.model.PlacementOfficer officer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.placepro.model.PlacementOfficer> findById(int officerId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.placepro.model.PlacementOfficer> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<com.placepro.model.PlacementOfficer> findByEmployeeId(String employeeId) {
            return Optional.empty();
        }

        @Override
        public List<com.placepro.model.PlacementOfficer> findAllActive() {
            return new ArrayList<>();
        }

        @Override
        public List<com.placepro.model.PlacementOfficer> findAll() {
            return new ArrayList<>();
        }

        @Override
        public List<com.placepro.model.PlacementOfficer> findByRole(String role) {
            return new ArrayList<>();
        }

        @Override
        public boolean update(com.placepro.model.PlacementOfficer officer) {
            return false;
        }

        @Override
        public boolean deactivate(int officerId) {
            return false;
        }

        @Override
        public boolean deleteById(int officerId) {
            return false;
        }
    }

    private static final class EmptyRecruiterDAO implements RecruiterDAO {
        @Override
        public com.placepro.model.Recruiter insert(com.placepro.model.Recruiter recruiter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<com.placepro.model.Recruiter> findById(int recruiterId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.placepro.model.Recruiter> findByEmail(String email) {
            return Optional.empty();
        }

        @Override
        public List<com.placepro.model.Recruiter> findAllActive() {
            return new ArrayList<>();
        }

        @Override
        public List<com.placepro.model.Recruiter> findAll() {
            return new ArrayList<>();
        }

        @Override
        public List<com.placepro.model.Recruiter> findByCompanyId(int companyId) {
            return new ArrayList<>();
        }

        @Override
        public boolean update(com.placepro.model.Recruiter recruiter) {
            return false;
        }

        @Override
        public boolean deactivate(int recruiterId) {
            return false;
        }

        @Override
        public boolean deleteById(int recruiterId) {
            return false;
        }
    }
}

package com.placepro.service.auth;

import com.placepro.model.PlacementOfficer;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;

public class AuthService {

    private final com.placepro.dao.StudentDAO studentDAO;
    private final com.placepro.dao.PlacementOfficerDAO placementOfficerDAO;
    private final com.placepro.dao.RecruiterDAO recruiterDAO;
    private final SessionManager sessionManager;
    private final LoginAttemptTracker loginAttemptTracker;

    public AuthService(com.placepro.dao.StudentDAO studentDAO,
                       com.placepro.dao.PlacementOfficerDAO placementOfficerDAO,
                       com.placepro.dao.RecruiterDAO recruiterDAO,
                       SessionManager sessionManager) {
        this(studentDAO, placementOfficerDAO, recruiterDAO, sessionManager, new LoginAttemptTracker());
    }

    AuthService(com.placepro.dao.StudentDAO studentDAO,
                com.placepro.dao.PlacementOfficerDAO placementOfficerDAO,
                com.placepro.dao.RecruiterDAO recruiterDAO,
                SessionManager sessionManager,
                LoginAttemptTracker loginAttemptTracker) {
        this.studentDAO = studentDAO;
        this.placementOfficerDAO = placementOfficerDAO;
        this.recruiterDAO = recruiterDAO;
        this.sessionManager = sessionManager;
        this.loginAttemptTracker = loginAttemptTracker;
    }

    public Student registerStudent(Student student, String plainPassword) {
        if (studentDAO.findByRollNumber(student.getRollNumber()).isPresent()) {
            throw new com.placepro.service.ServiceException("A student with this roll number already exists.");
        }
        if (studentDAO.findByEmail(student.getEmail()).isPresent()) {
            throw new com.placepro.service.ServiceException("A student with this email already exists.");
        }

        student.setPasswordHash(com.placepro.util.PasswordUtil.hashPassword(plainPassword));
        if (student.getIsActive() == null) {
            student.setIsActive(true);
        }
        if (student.getBacklogCount() == null) {
            student.setBacklogCount(0);
        }
        Student savedStudent = studentDAO.insert(student);
        sessionManager.setSession(savedStudent.getStudentId(), com.placepro.service.UserRole.STUDENT);
        return savedStudent;
    }

    public Student loginStudent(String email, String plainPassword) {
        String accountKey = "student:" + email.toLowerCase();
        loginAttemptTracker.ensureNotLocked(accountKey);

        Student student = studentDAO.findByEmail(email)
                .filter(found -> Boolean.TRUE.equals(found.getIsActive()))
                .orElseThrow(() -> handleFailedLogin(accountKey, "Invalid email or password."));

        if (!com.placepro.util.PasswordUtil.verifyPassword(plainPassword, student.getPasswordHash())) {
            throw handleFailedLogin(accountKey, "Invalid email or password.");
        }

        loginAttemptTracker.clearAttempts(accountKey);
        sessionManager.setSession(student.getStudentId(), com.placepro.service.UserRole.STUDENT);
        return student;
    }

    public PlacementOfficer loginOfficerOrAdmin(String email, String plainPassword) {
        String accountKey = "officer:" + email.toLowerCase();
        loginAttemptTracker.ensureNotLocked(accountKey);

        PlacementOfficer officer = placementOfficerDAO.findByEmail(email)
                .filter(found -> Boolean.TRUE.equals(found.getIsActive()))
                .orElseThrow(() -> handleFailedLogin(accountKey, "Invalid email or password."));

        if (!com.placepro.util.PasswordUtil.verifyPassword(plainPassword, officer.getPasswordHash())) {
            throw handleFailedLogin(accountKey, "Invalid email or password.");
        }

        loginAttemptTracker.clearAttempts(accountKey);
        com.placepro.service.UserRole role = "ADMIN".equals(officer.getRole())
                ? com.placepro.service.UserRole.ADMIN
                : com.placepro.service.UserRole.OFFICER;
        sessionManager.setSession(officer.getOfficerId(), role);
        return officer;
    }

    public Recruiter loginRecruiter(String email, String plainPassword) {
        String accountKey = "recruiter:" + email.toLowerCase();
        loginAttemptTracker.ensureNotLocked(accountKey);

        Recruiter recruiter = recruiterDAO.findByEmail(email)
                .filter(found -> Boolean.TRUE.equals(found.getIsActive()))
                .orElseThrow(() -> handleFailedLogin(accountKey, "Invalid email or password."));

        if (!com.placepro.util.PasswordUtil.verifyPassword(plainPassword, recruiter.getPasswordHash())) {
            throw handleFailedLogin(accountKey, "Invalid email or password.");
        }

        loginAttemptTracker.clearAttempts(accountKey);
        sessionManager.setSession(recruiter.getRecruiterId(), com.placepro.service.UserRole.RECRUITER);
        return recruiter;
    }

    public void logout() {
        sessionManager.logout();
    }

    public String resetStudentPassword(int studentId) {
        com.placepro.service.AuthorizationHelper.requireRole(sessionManager, com.placepro.service.UserRole.ADMIN);
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new com.placepro.service.ServiceException("Student not found."));
        String temporaryPassword = generateTemporaryPassword();
        student.setPasswordHash(com.placepro.util.PasswordUtil.hashPassword(temporaryPassword));
        studentDAO.update(student);
        return temporaryPassword;
    }

    public String resetOfficerPassword(int officerId) {
        com.placepro.service.AuthorizationHelper.requireRole(sessionManager, com.placepro.service.UserRole.ADMIN);
        PlacementOfficer officer = placementOfficerDAO.findById(officerId)
                .orElseThrow(() -> new com.placepro.service.ServiceException("Officer not found."));
        String temporaryPassword = generateTemporaryPassword();
        officer.setPasswordHash(com.placepro.util.PasswordUtil.hashPassword(temporaryPassword));
        placementOfficerDAO.update(officer);
        return temporaryPassword;
    }

    public String resetRecruiterPassword(int recruiterId) {
        com.placepro.service.AuthorizationHelper.requireRole(sessionManager, com.placepro.service.UserRole.ADMIN);
        Recruiter recruiter = recruiterDAO.findById(recruiterId)
                .orElseThrow(() -> new com.placepro.service.ServiceException("Recruiter not found."));
        String temporaryPassword = generateTemporaryPassword();
        recruiter.setPasswordHash(com.placepro.util.PasswordUtil.hashPassword(temporaryPassword));
        recruiterDAO.update(recruiter);
        return temporaryPassword;
    }

    private String generateTemporaryPassword() {
        return "Temp@" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private com.placepro.service.ServiceException handleFailedLogin(String accountKey, String message) {
        loginAttemptTracker.recordFailedAttempt(accountKey);
        return new com.placepro.service.ServiceException(message);
    }
}

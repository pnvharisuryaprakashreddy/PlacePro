package com.placepro.service.admin;

import com.placepro.dao.PlacementOfficerDAO;
import com.placepro.dao.RecruiterDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.PlacementOfficer;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;

import java.util.List;

public class UserManagementService {

    private final StudentDAO studentDAO;
    private final PlacementOfficerDAO placementOfficerDAO;
    private final RecruiterDAO recruiterDAO;
    private final AuthService authService;
    private final SessionManager sessionManager;

    public UserManagementService(StudentDAO studentDAO,
                                 PlacementOfficerDAO placementOfficerDAO,
                                 RecruiterDAO recruiterDAO,
                                 AuthService authService,
                                 SessionManager sessionManager) {
        this.studentDAO = studentDAO;
        this.placementOfficerDAO = placementOfficerDAO;
        this.recruiterDAO = recruiterDAO;
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    public List<Student> listStudents() {
        requireAdmin();
        return studentDAO.findAll();
    }

    public List<PlacementOfficer> listOfficers() {
        requireAdmin();
        return placementOfficerDAO.findAll();
    }

    public List<Recruiter> listRecruiters() {
        requireAdmin();
        return recruiterDAO.findAll();
    }

    public void deactivateStudent(int studentId) {
        requireAdmin();
        if (!studentDAO.deactivate(studentId)) {
            throw new ServiceException("Unable to deactivate student.");
        }
    }

    public void reactivateStudent(int studentId) {
        requireAdmin();
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new ServiceException("Student not found."));
        student.setIsActive(true);
        studentDAO.update(student);
    }

    public void deactivateOfficer(int officerId) {
        requireAdmin();
        if (!placementOfficerDAO.deactivate(officerId)) {
            throw new ServiceException("Unable to deactivate officer.");
        }
    }

    public void reactivateOfficer(int officerId) {
        requireAdmin();
        PlacementOfficer officer = placementOfficerDAO.findById(officerId)
                .orElseThrow(() -> new ServiceException("Officer not found."));
        officer.setIsActive(true);
        placementOfficerDAO.update(officer);
    }

    public void deactivateRecruiter(int recruiterId) {
        requireAdmin();
        if (!recruiterDAO.deactivate(recruiterId)) {
            throw new ServiceException("Unable to deactivate recruiter.");
        }
    }

    public void reactivateRecruiter(int recruiterId) {
        requireAdmin();
        Recruiter recruiter = recruiterDAO.findById(recruiterId)
                .orElseThrow(() -> new ServiceException("Recruiter not found."));
        recruiter.setIsActive(true);
        recruiterDAO.update(recruiter);
    }

    public String resetStudentPassword(int studentId) {
        requireAdmin();
        return authService.resetStudentPassword(studentId);
    }

    public String resetOfficerPassword(int officerId) {
        requireAdmin();
        return authService.resetOfficerPassword(officerId);
    }

    public String resetRecruiterPassword(int recruiterId) {
        requireAdmin();
        return authService.resetRecruiterPassword(recruiterId);
    }

    private void requireAdmin() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.ADMIN);
    }
}

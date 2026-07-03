package com.placepro.service.student;

import com.placepro.dao.ResumeDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.dao.StudentSearchCriteria;
import com.placepro.model.Resume;
import com.placepro.model.Student;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;

import java.util.Optional;

/**
 * Officer/admin-facing student directory: paginated search plus profile and
 * resume lookups for the quick-link actions.
 */
public class StudentDirectoryService {

    private final StudentDAO studentDAO;
    private final ResumeDAO resumeDAO;
    private final SessionManager sessionManager;

    public StudentDirectoryService(StudentDAO studentDAO, ResumeDAO resumeDAO, SessionManager sessionManager) {
        this.studentDAO = studentDAO;
        this.resumeDAO = resumeDAO;
        this.sessionManager = sessionManager;
    }

    public StudentSearchPage searchStudents(StudentSearchCriteria criteria, int page, int pageSize) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (page < 0 || pageSize < 1) {
            throw new ServiceException("Invalid pagination request.");
        }
        int totalCount = studentDAO.countStudents(criteria);
        return new StudentSearchPage(
                studentDAO.searchStudents(criteria, page * pageSize, pageSize),
                totalCount,
                page,
                pageSize);
    }

    public Student getStudent(int studentId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return studentDAO.findById(studentId)
                .orElseThrow(() -> new ServiceException("Student not found."));
    }

    public Optional<Resume> getCurrentResume(int studentId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return resumeDAO.findCurrentByStudentId(studentId);
    }
}

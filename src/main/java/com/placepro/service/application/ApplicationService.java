package com.placepro.service.application;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.CompanyDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.ResumeDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.Application;
import com.placepro.model.Company;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Resume;
import com.placepro.model.Student;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.EligibilityResult;
import com.placepro.service.drive.EligibilityService;
import com.placepro.service.notification.NotificationService;
import com.placepro.util.AppLog;
import com.placepro.util.TransactionExecutor;
import com.placepro.util.TransactionRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final NotificationService notificationService;
    private final PlacementDriveDAO placementDriveDAO;
    private final CompanyDAO companyDAO;
    private final ResumeDAO resumeDAO;
    private final StudentDAO studentDAO;
    private final EligibilityService eligibilityService;
    private final SessionManager sessionManager;
    private final TransactionRunner transactionRunner;

    public ApplicationService(ApplicationDAO applicationDAO,
                              NotificationService notificationService,
                              PlacementDriveDAO placementDriveDAO,
                              CompanyDAO companyDAO,
                              ResumeDAO resumeDAO,
                              StudentDAO studentDAO,
                              EligibilityService eligibilityService,
                              SessionManager sessionManager) {
        this(applicationDAO, notificationService, placementDriveDAO, companyDAO, resumeDAO, studentDAO,
                eligibilityService, sessionManager, TransactionExecutor::run);
    }

    ApplicationService(ApplicationDAO applicationDAO,
                       NotificationService notificationService,
                       PlacementDriveDAO placementDriveDAO,
                       CompanyDAO companyDAO,
                       ResumeDAO resumeDAO,
                       StudentDAO studentDAO,
                       EligibilityService eligibilityService,
                       SessionManager sessionManager,
                       TransactionRunner transactionRunner) {
        this.applicationDAO = applicationDAO;
        this.notificationService = notificationService;
        this.placementDriveDAO = placementDriveDAO;
        this.companyDAO = companyDAO;
        this.resumeDAO = resumeDAO;
        this.studentDAO = studentDAO;
        this.eligibilityService = eligibilityService;
        this.sessionManager = sessionManager;
        this.transactionRunner = transactionRunner;
    }

    public Application submitApplication(int studentId, int driveId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);

        EligibilityResult eligibilityResult = eligibilityService.check(studentId, driveId);
        if (!eligibilityResult.isEligible()) {
            throw new ServiceException("Application rejected: " + String.join(" ", eligibilityResult.getReasons()));
        }

        if (applicationDAO.findByStudentAndDrive(studentId, driveId).isPresent()) {
            throw new ServiceException("You have already applied to this drive.");
        }

        PlacementDrive drive = placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new ServiceException("Placement drive not found."));

        Application application = new Application();
        application.setStudentId(studentId);
        application.setDriveId(driveId);
        application.setStatus(ApplicationStatus.APPLIED.name());
        resumeDAO.findCurrentByStudentId(studentId)
                .ifPresent(resume -> application.setResumeId(resume.getResumeId()));

        Application savedApplication = transactionRunner.execute(connection -> {
            Application inserted = applicationDAO.insert(connection, application);
            notificationService.notifyStudent(
                    connection,
                    studentId,
                    "Application Submitted",
                    "Your application to " + drive.getJobTitle() + " was submitted successfully.",
                    "GENERAL",
                    inserted.getApplicationId());
            return inserted;
        });
        com.placepro.monitoring.MetricsRegistry.get().recordApplicationSubmitted();
        return savedApplication;
    }

    public Application updateStatus(int applicationId, ApplicationStatus newStatus, int updatedByOfficerId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (sessionManager.getCurrentUserId().orElse(-1) != updatedByOfficerId) {
            throw new ServiceException("Officer identity does not match the active session.");
        }
        return applyStatusChange(applicationId, newStatus);
    }

    public List<Application> updateStatusBulk(List<Integer> applicationIds,
                                              ApplicationStatus newStatus,
                                              int updatedByOfficerId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (sessionManager.getCurrentUserId().orElse(-1) != updatedByOfficerId) {
            throw new ServiceException("Officer identity does not match the active session.");
        }
        List<Application> updated = new ArrayList<>();
        for (Integer applicationId : applicationIds) {
            updated.add(applyStatusChange(applicationId, newStatus));
        }
        return updated;
    }

    public List<ApplicationReviewRow> listApplicationReviewRowsForDrive(int driveId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return buildReviewRows(applicationDAO.findByDriveId(driveId));
    }

    public List<Application> listApplicationsForStudent(int studentId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);
        return applicationDAO.findByStudentId(studentId);
    }

    public List<Application> listApplicationsForDrive(int driveId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return applicationDAO.findByDriveId(driveId);
    }

    public java.util.Optional<Application> findApplicationForStudent(int studentId, int driveId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);
        return applicationDAO.findByStudentAndDrive(studentId, driveId);
    }

    public Application findApplicationById(int applicationId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);
        return applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));
    }

    private Application applyStatusChange(int applicationId, ApplicationStatus newStatus) {
        Application application = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));

        String previousStatus = application.getStatus();
        if (previousStatus.equals(newStatus.name())) {
            return application;
        }
        application.setStatus(newStatus.name());

        Application updated = transactionRunner.execute(connection -> {
            applicationDAO.update(connection, application);
            notificationService.notifyStudent(
                    connection,
                    application.getStudentId(),
                    "Application Status Updated",
                    String.format(
                            "Your application status changed from %s to %s.",
                            previousStatus,
                            newStatus.name()),
                    "STATUS_CHANGE",
                    applicationId);
            return application;
        });
        com.placepro.monitoring.MetricsRegistry.get().recordStatusChange(newStatus.name());
        AppLog.applicationStatusTransition(applicationId, previousStatus, newStatus.name());
        return updated;
    }

    private List<ApplicationReviewRow> buildReviewRows(List<Application> applications) {
        return applications.stream()
                .map(application -> {
                    Student student = studentDAO.findById(application.getStudentId())
                            .orElseThrow(() -> new ServiceException("Student not found."));
                    Resume resume = null;
                    if (application.getResumeId() != null) {
                        resume = resumeDAO.findById(application.getResumeId()).orElse(null);
                    }
                    if (resume == null) {
                        resume = resumeDAO.findCurrentByStudentId(application.getStudentId()).orElse(null);
                    }
                    String resumeFileName = resume == null ? "-" : resume.getFileName();
                    String resumeFilePath = resume == null ? null : resume.getFilePath();
                    PlacementDrive drive = placementDriveDAO.findById(application.getDriveId())
                            .orElseThrow(() -> new ServiceException("Placement drive not found."));
                    String companyName = companyDAO.findById(drive.getCompanyId())
                            .map(Company::getCompanyName)
                            .orElse("Unknown");
                    return new ApplicationReviewRow(
                            application,
                            student.getFullName(),
                            student.getCgpa(),
                            student.getBranch(),
                            resumeFileName,
                            resumeFilePath,
                            companyName,
                            drive.getJobTitle());
                })
                .collect(Collectors.toList());
    }
}

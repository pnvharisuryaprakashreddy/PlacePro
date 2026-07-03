package com.placepro.service.application;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.NotificationDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.ResumeDAO;
import com.placepro.model.Application;
import com.placepro.model.Notification;
import com.placepro.model.PlacementDrive;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.EligibilityResult;
import com.placepro.service.drive.EligibilityService;
import com.placepro.util.TransactionExecutor;
import com.placepro.util.TransactionRunner;

public class ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final NotificationDAO notificationDAO;
    private final PlacementDriveDAO placementDriveDAO;
    private final ResumeDAO resumeDAO;
    private final EligibilityService eligibilityService;
    private final SessionManager sessionManager;
    private final TransactionRunner transactionRunner;

    public ApplicationService(ApplicationDAO applicationDAO,
                              NotificationDAO notificationDAO,
                              PlacementDriveDAO placementDriveDAO,
                              ResumeDAO resumeDAO,
                              EligibilityService eligibilityService,
                              SessionManager sessionManager) {
        this(applicationDAO, notificationDAO, placementDriveDAO, resumeDAO, eligibilityService, sessionManager, TransactionExecutor::run);
    }

    ApplicationService(ApplicationDAO applicationDAO,
                       NotificationDAO notificationDAO,
                       PlacementDriveDAO placementDriveDAO,
                       ResumeDAO resumeDAO,
                       EligibilityService eligibilityService,
                       SessionManager sessionManager,
                       TransactionRunner transactionRunner) {
        this.applicationDAO = applicationDAO;
        this.notificationDAO = notificationDAO;
        this.placementDriveDAO = placementDriveDAO;
        this.resumeDAO = resumeDAO;
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

        Notification notification = new Notification();
        notification.setStudentId(studentId);
        notification.setTitle("Application Submitted");
        notification.setMessage("Your application to " + drive.getJobTitle() + " was submitted successfully.");
        notification.setNotificationType("GENERAL");
        notification.setReferenceId(driveId);
        notification.setIsRead(false);

        return transactionRunner.execute(connection -> {
            Application savedApplication = applicationDAO.insert(connection, application);
            notification.setReferenceId(savedApplication.getApplicationId());
            notificationDAO.insert(connection, notification);
            return savedApplication;
        });
    }

    public Application updateStatus(int applicationId, ApplicationStatus newStatus, int updatedByOfficerId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (sessionManager.getCurrentUserId().orElse(-1) != updatedByOfficerId) {
            throw new ServiceException("Officer identity does not match the active session.");
        }

        Application application = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));

        String previousStatus = application.getStatus();
        application.setStatus(newStatus.name());

        Notification notification = new Notification();
        notification.setStudentId(application.getStudentId());
        notification.setTitle("Application Status Updated");
        notification.setMessage(String.format(
                "Your application status changed from %s to %s.",
                previousStatus,
                newStatus.name()));
        notification.setNotificationType("STATUS_CHANGE");
        notification.setReferenceId(applicationId);
        notification.setIsRead(false);

        return transactionRunner.execute(connection -> {
            applicationDAO.update(connection, application);
            notificationDAO.insert(connection, notification);
            return application;
        });
    }

    public java.util.List<Application> listApplicationsForStudent(int studentId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);
        return applicationDAO.findByStudentId(studentId);
    }

    public java.util.Optional<Application> findApplicationForStudent(int studentId, int driveId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);
        return applicationDAO.findByStudentAndDrive(studentId, driveId);
    }
}

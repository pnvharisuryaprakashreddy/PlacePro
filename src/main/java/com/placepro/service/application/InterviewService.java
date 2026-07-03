package com.placepro.service.application;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.InterviewScheduleDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.RecruiterDAO;
import com.placepro.model.Application;
import com.placepro.model.InterviewSchedule;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Recruiter;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.notification.NotificationService;
import com.placepro.util.AppLog;
import com.placepro.util.DateUtil;
import com.placepro.util.TransactionExecutor;
import com.placepro.util.TransactionRunner;

import java.util.List;

public class InterviewService {

    private final InterviewScheduleDAO interviewScheduleDAO;
    private final ApplicationDAO applicationDAO;
    private final PlacementDriveDAO placementDriveDAO;
    private final RecruiterDAO recruiterDAO;
    private final NotificationService notificationService;
    private final SessionManager sessionManager;
    private final TransactionRunner transactionRunner;

    public InterviewService(InterviewScheduleDAO interviewScheduleDAO,
                            ApplicationDAO applicationDAO,
                            PlacementDriveDAO placementDriveDAO,
                            RecruiterDAO recruiterDAO,
                            NotificationService notificationService,
                            SessionManager sessionManager) {
        this(interviewScheduleDAO, applicationDAO, placementDriveDAO, recruiterDAO, notificationService,
                sessionManager, TransactionExecutor::run);
    }

    InterviewService(InterviewScheduleDAO interviewScheduleDAO,
                     ApplicationDAO applicationDAO,
                     PlacementDriveDAO placementDriveDAO,
                     RecruiterDAO recruiterDAO,
                     NotificationService notificationService,
                     SessionManager sessionManager,
                     TransactionRunner transactionRunner) {
        this.interviewScheduleDAO = interviewScheduleDAO;
        this.applicationDAO = applicationDAO;
        this.placementDriveDAO = placementDriveDAO;
        this.recruiterDAO = recruiterDAO;
        this.notificationService = notificationService;
        this.sessionManager = sessionManager;
        this.transactionRunner = transactionRunner;
    }

    public InterviewSchedule scheduleInterview(InterviewSchedule interviewSchedule) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);

        Application application = applicationDAO.findById(interviewSchedule.getApplicationId())
                .orElseThrow(() -> new ServiceException("Application not found."));
        verifyAccessToApplication(application);

        if (interviewScheduleDAO.findByApplicationAndRound(
                interviewSchedule.getApplicationId(),
                interviewSchedule.getRoundNumber()).isPresent()) {
            throw new ServiceException("An interview round already exists for this application.");
        }

        if (interviewSchedule.getOutcome() == null) {
            interviewSchedule.setOutcome("PENDING");
        }
        validateScheduleFields(interviewSchedule);
        applyCreatorFromSession(interviewSchedule);

        PlacementDrive drive = placementDriveDAO.findById(application.getDriveId())
                .orElseThrow(() -> new ServiceException("Placement drive not found."));

        String previousStatus = application.getStatus();
        InterviewSchedule scheduled = transactionRunner.execute(connection -> {
            InterviewSchedule savedInterview = interviewScheduleDAO.insert(connection, interviewSchedule);
            application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED.name());
            applicationDAO.update(connection, application);
            notificationService.notifyStudent(
                    connection,
                    application.getStudentId(),
                    "Interview Scheduled",
                    String.format(
                            "Your %s interview for %s is scheduled on %s at %s at %s.",
                            savedInterview.getRoundType(),
                            drive.getJobTitle(),
                            DateUtil.formatDate(savedInterview.getScheduledDate()),
                            savedInterview.getScheduledTime(),
                            savedInterview.getVenue()),
                    "INTERVIEW_SCHEDULED",
                    savedInterview.getInterviewId());
            return savedInterview;
        });
        com.placepro.monitoring.MetricsRegistry.get()
                .recordStatusChange(ApplicationStatus.INTERVIEW_SCHEDULED.name());
        AppLog.applicationStatusTransition(
                application.getApplicationId(),
                previousStatus,
                ApplicationStatus.INTERVIEW_SCHEDULED.name());
        return scheduled;
    }

    public InterviewSchedule recordOutcome(int interviewId, String outcome, String notes) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);

        InterviewSchedule interviewSchedule = interviewScheduleDAO.findById(interviewId)
                .orElseThrow(() -> new ServiceException("Interview schedule not found."));

        Application application = applicationDAO.findById(interviewSchedule.getApplicationId())
                .orElseThrow(() -> new ServiceException("Application not found."));
        verifyAccessToApplication(application);

        validateOutcome(outcome);
        interviewSchedule.setOutcome(outcome);
        interviewSchedule.setNotes(notes);

        ApplicationStatus newStatus = mapOutcomeToApplicationStatus(outcome);
        String previousStatus = application.getStatus();
        if (newStatus != null) {
            application.setStatus(newStatus.name());
        }

        InterviewSchedule updatedSchedule = transactionRunner.execute(connection -> {
            interviewScheduleDAO.update(connection, interviewSchedule);
            if (newStatus != null) {
                applicationDAO.update(connection, application);
                notificationService.notifyStudent(
                        connection,
                        application.getStudentId(),
                        "Application Status Updated",
                        String.format(
                                "Your application status changed from %s to %s after interview round %d.",
                                previousStatus,
                                newStatus.name(),
                                interviewSchedule.getRoundNumber()),
                        "STATUS_CHANGE",
                        application.getApplicationId());
            }
            return interviewSchedule;
        });
        if (newStatus != null) {
            com.placepro.monitoring.MetricsRegistry.get().recordStatusChange(newStatus.name());
            AppLog.applicationStatusTransition(
                    application.getApplicationId(), previousStatus, newStatus.name());
        }
        return updatedSchedule;
    }

    public List<InterviewSchedule> getInterviewsForApplication(int applicationId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);
        Application application = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));
        verifyAccessToApplication(application);
        return interviewScheduleDAO.findByApplicationId(applicationId);
    }

    public int getNextRoundNumber(int applicationId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);
        Application application = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));
        verifyAccessToApplication(application);
        return interviewScheduleDAO.findByApplicationId(applicationId).stream()
                .mapToInt(InterviewSchedule::getRoundNumber)
                .max()
                .orElse(0) + 1;
    }

    private void validateScheduleFields(InterviewSchedule interviewSchedule) {
        if (interviewSchedule.getRoundNumber() == null || interviewSchedule.getRoundNumber() < 1) {
            throw new ServiceException("Round number must be at least 1.");
        }
        if (interviewSchedule.getRoundType() == null || interviewSchedule.getRoundType().isBlank()) {
            throw new ServiceException("Round type is required.");
        }
        if (interviewSchedule.getScheduledDate() == null) {
            throw new ServiceException("Interview date is required.");
        }
        if (interviewSchedule.getScheduledTime() == null) {
            throw new ServiceException("Interview time is required.");
        }
        if (interviewSchedule.getVenue() == null || interviewSchedule.getVenue().isBlank()) {
            throw new ServiceException("Venue is required.");
        }
    }

    private void validateOutcome(String outcome) {
        if (!"PENDING".equals(outcome) && !"SELECTED".equals(outcome)
                && !"REJECTED".equals(outcome) && !"ON_HOLD".equals(outcome)) {
            throw new ServiceException("Invalid outcome. Allowed values: PENDING, SELECTED, REJECTED, ON_HOLD.");
        }
    }

    private ApplicationStatus mapOutcomeToApplicationStatus(String outcome) {
        if ("SELECTED".equals(outcome)) {
            return ApplicationStatus.SELECTED;
        }
        if ("REJECTED".equals(outcome)) {
            return ApplicationStatus.REJECTED;
        }
        if ("ON_HOLD".equals(outcome)) {
            return ApplicationStatus.ON_HOLD;
        }
        return null;
    }

    private void applyCreatorFromSession(InterviewSchedule interviewSchedule) {
        UserRole role = sessionManager.getCurrentRole()
                .orElseThrow(() -> new ServiceException("You must be logged in."));
        int userId = sessionManager.getCurrentUserId()
                .orElseThrow(() -> new ServiceException("You must be logged in."));
        if (role == UserRole.RECRUITER) {
            interviewSchedule.setCreatedByRecruiterId(userId);
            interviewSchedule.setCreatedByOfficerId(null);
        } else {
            interviewSchedule.setCreatedByOfficerId(userId);
            interviewSchedule.setCreatedByRecruiterId(null);
        }
    }

    private void verifyAccessToApplication(Application application) {
        UserRole role = sessionManager.getCurrentRole()
                .orElseThrow(() -> new ServiceException("You must be logged in."));
        if (role == UserRole.RECRUITER) {
            int recruiterId = sessionManager.getCurrentUserId()
                    .orElseThrow(() -> new ServiceException("You must be logged in."));
            Recruiter recruiter = recruiterDAO.findById(recruiterId)
                    .orElseThrow(() -> new ServiceException("Recruiter not found."));
            PlacementDrive drive = placementDriveDAO.findById(application.getDriveId())
                    .orElseThrow(() -> new ServiceException("Placement drive not found."));
            if (recruiter.getCompanyId() == null
                    || !recruiter.getCompanyId().equals(drive.getCompanyId())) {
                throw new ServiceException("You are not authorized to access this application.");
            }
        }
    }
}

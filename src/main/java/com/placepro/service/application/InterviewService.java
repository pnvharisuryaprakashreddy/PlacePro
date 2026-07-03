package com.placepro.service.application;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.InterviewScheduleDAO;
import com.placepro.model.Application;
import com.placepro.model.InterviewSchedule;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;

import java.util.List;

public class InterviewService {

    private final InterviewScheduleDAO interviewScheduleDAO;
    private final ApplicationDAO applicationDAO;
    private final SessionManager sessionManager;

    public InterviewService(InterviewScheduleDAO interviewScheduleDAO,
                            ApplicationDAO applicationDAO,
                            SessionManager sessionManager) {
        this.interviewScheduleDAO = interviewScheduleDAO;
        this.applicationDAO = applicationDAO;
        this.sessionManager = sessionManager;
    }

    public InterviewSchedule scheduleInterview(InterviewSchedule interviewSchedule) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);

        Application application = applicationDAO.findById(interviewSchedule.getApplicationId())
                .orElseThrow(() -> new ServiceException("Application not found."));

        if (interviewScheduleDAO.findByApplicationAndRound(
                interviewSchedule.getApplicationId(),
                interviewSchedule.getRoundNumber()).isPresent()) {
            throw new ServiceException("An interview round already exists for this application.");
        }

        if (interviewSchedule.getOutcome() == null) {
            interviewSchedule.setOutcome("PENDING");
        }

        InterviewSchedule savedInterview = interviewScheduleDAO.insert(interviewSchedule);
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED.name());
        applicationDAO.update(application);
        return savedInterview;
    }

    public InterviewSchedule recordOutcome(int interviewId, String outcome, String notes) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);

        InterviewSchedule interviewSchedule = interviewScheduleDAO.findById(interviewId)
                .orElseThrow(() -> new ServiceException("Interview schedule not found."));

        interviewSchedule.setOutcome(outcome);
        interviewSchedule.setNotes(notes);
        interviewScheduleDAO.update(interviewSchedule);

        Application application = applicationDAO.findById(interviewSchedule.getApplicationId())
                .orElseThrow(() -> new ServiceException("Application not found."));

        if ("SELECTED".equals(outcome)) {
            application.setStatus(ApplicationStatus.SELECTED.name());
        } else if ("REJECTED".equals(outcome)) {
            application.setStatus(ApplicationStatus.REJECTED.name());
        } else if ("ON_HOLD".equals(outcome)) {
            application.setStatus(ApplicationStatus.ON_HOLD.name());
        }
        applicationDAO.update(application);
        return interviewSchedule;
    }

    public List<InterviewSchedule> getInterviewsForApplication(int applicationId) {
        return interviewScheduleDAO.findByApplicationId(applicationId);
    }
}

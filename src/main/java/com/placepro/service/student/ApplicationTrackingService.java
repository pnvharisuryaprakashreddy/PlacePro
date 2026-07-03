package com.placepro.service.student;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.CompanyDAO;
import com.placepro.dao.InterviewScheduleDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.model.Application;
import com.placepro.model.Company;
import com.placepro.model.InterviewSchedule;
import com.placepro.model.PlacementDrive;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ApplicationTrackingService {

    private final ApplicationDAO applicationDAO;
    private final PlacementDriveDAO placementDriveDAO;
    private final CompanyDAO companyDAO;
    private final InterviewScheduleDAO interviewScheduleDAO;
    private final SessionManager sessionManager;

    public ApplicationTrackingService(ApplicationDAO applicationDAO,
                                      PlacementDriveDAO placementDriveDAO,
                                      CompanyDAO companyDAO,
                                      InterviewScheduleDAO interviewScheduleDAO,
                                      SessionManager sessionManager) {
        this.applicationDAO = applicationDAO;
        this.placementDriveDAO = placementDriveDAO;
        this.companyDAO = companyDAO;
        this.interviewScheduleDAO = interviewScheduleDAO;
        this.sessionManager = sessionManager;
    }

    public List<ApplicationTrackingRow> listTrackingForStudent(int studentId) {
        // A student may view their own applications; officers and admins may view any student's.
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId,
                UserRole.OFFICER, UserRole.ADMIN);

        List<ApplicationTrackingRow> rows = new ArrayList<>();
        for (Application application : applicationDAO.findByStudentId(studentId)) {
            PlacementDrive drive = placementDriveDAO.findById(application.getDriveId()).orElse(null);
            if (drive == null) {
                continue;
            }
            String companyName = companyDAO.findById(drive.getCompanyId())
                    .map(Company::getCompanyName)
                    .orElse("Unknown");
            Optional<InterviewSchedule> scheduledInterview = findUpcomingInterview(application.getApplicationId());
            rows.add(new ApplicationTrackingRow(
                    application.getApplicationId(),
                    companyName,
                    drive.getJobTitle(),
                    application.getStatus(),
                    application.getAppliedAt(),
                    scheduledInterview.map(InterviewSchedule::getScheduledDate).orElse(null),
                    scheduledInterview.map(InterviewSchedule::getScheduledTime).orElse(null),
                    scheduledInterview.map(InterviewSchedule::getVenue).orElse(null)));
        }
        return rows;
    }

    private Optional<InterviewSchedule> findUpcomingInterview(int applicationId) {
        return interviewScheduleDAO.findByApplicationId(applicationId).stream()
                .filter(interview -> "PENDING".equals(interview.getOutcome()))
                .min(Comparator.comparing(InterviewSchedule::getScheduledDate)
                        .thenComparing(InterviewSchedule::getScheduledTime)
                        .thenComparing(InterviewSchedule::getRoundNumber));
    }
}

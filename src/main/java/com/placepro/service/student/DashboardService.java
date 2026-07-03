package com.placepro.service.student;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.CompanyDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.model.Application;
import com.placepro.model.Company;
import com.placepro.model.PlacementDrive;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardService {

    private static final int RECENT_DRIVE_LIMIT = 5;

    private final ApplicationDAO applicationDAO;
    private final PlacementDriveDAO placementDriveDAO;
    private final CompanyDAO companyDAO;
    private final SessionManager sessionManager;

    public DashboardService(ApplicationDAO applicationDAO,
                            PlacementDriveDAO placementDriveDAO,
                            CompanyDAO companyDAO,
                            SessionManager sessionManager) {
        this.applicationDAO = applicationDAO;
        this.placementDriveDAO = placementDriveDAO;
        this.companyDAO = companyDAO;
        this.sessionManager = sessionManager;
    }

    public StudentDashboardSummary getSummary(int studentId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);

        List<PlacementDrive> publishedDrives = placementDriveDAO.findPublishedDrives();
        List<Application> applications = applicationDAO.findByStudentId(studentId);
        Map<Integer, String> companyNames = loadCompanyNames();

        Map<String, Integer> applicationCounts = new HashMap<>();
        for (Application application : applications) {
            applicationCounts.merge(application.getStatus(), 1, Integer::sum);
        }

        LocalDateTime now = LocalDateTime.now();
        int upcomingDeadlines = (int) publishedDrives.stream()
                .filter(drive -> drive.getApplicationDeadline().isAfter(now))
                .count();

        List<StudentDriveSummary> recentDrives = publishedDrives.stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(RECENT_DRIVE_LIMIT)
                .map(drive -> new StudentDriveSummary(
                        drive,
                        companyNames.getOrDefault(drive.getCompanyId(), "Unknown")))
                .collect(Collectors.toList());

        return new StudentDashboardSummary(
                publishedDrives.size(),
                applicationCounts,
                upcomingDeadlines,
                recentDrives);
    }

    public List<StudentDriveSummary> listPublishedDrivesForStudent() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.STUDENT);
        Map<Integer, String> companyNames = loadCompanyNames();
        return placementDriveDAO.findPublishedDrives().stream()
                .map(drive -> new StudentDriveSummary(
                        drive,
                        companyNames.getOrDefault(drive.getCompanyId(), "Unknown")))
                .collect(Collectors.toList());
    }

    public StudentDriveSummary getPublishedDriveSummary(int driveId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.STUDENT);
        PlacementDrive drive = placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new com.placepro.service.ServiceException("Placement drive not found."));
        if (!DriveStatus.PUBLISHED.name().equals(drive.getStatus())) {
            throw new com.placepro.service.ServiceException("This drive is not available to students.");
        }
        String companyName = companyDAO.findById(drive.getCompanyId())
                .map(Company::getCompanyName)
                .orElse("Unknown");
        return new StudentDriveSummary(drive, companyName);
    }

    private Map<Integer, String> loadCompanyNames() {
        Map<Integer, String> companyNames = new HashMap<>();
        for (Company company : companyDAO.findAllActive()) {
            companyNames.put(company.getCompanyId(), company.getCompanyName());
        }
        return companyNames;
    }
}

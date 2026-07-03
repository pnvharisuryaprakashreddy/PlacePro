package com.placepro.service.report;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.Application;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Student;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.UserRole;
import com.placepro.service.application.ApplicationStatus;
import com.placepro.service.auth.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReportService {

    private final PlacementDriveDAO placementDriveDAO;
    private final ApplicationDAO applicationDAO;
    private final StudentDAO studentDAO;
    private final SessionManager sessionManager;

    public ReportService(PlacementDriveDAO placementDriveDAO,
                         ApplicationDAO applicationDAO,
                         StudentDAO studentDAO,
                         SessionManager sessionManager) {
        this.placementDriveDAO = placementDriveDAO;
        this.applicationDAO = applicationDAO;
        this.studentDAO = studentDAO;
        this.sessionManager = sessionManager;
    }

    public List<DrivePlacementSummary> getDriveWiseSummary() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);

        List<DrivePlacementSummary> summaries = new ArrayList<>();
        for (PlacementDrive drive : placementDriveDAO.findPublishedDrives()) {
            List<Application> applications = applicationDAO.findByDriveId(drive.getDriveId());
            summaries.add(new DrivePlacementSummary(
                    drive.getDriveId(),
                    drive.getJobTitle(),
                    countByStatus(applications, ApplicationStatus.APPLIED),
                    countByStatus(applications, ApplicationStatus.SHORTLISTED),
                    countByStatus(applications, ApplicationStatus.SELECTED),
                    countByStatus(applications, ApplicationStatus.REJECTED)));
        }
        return summaries;
    }

    public List<DepartmentPlacementSummary> getDepartmentWiseSummary() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);

        Map<String, DepartmentPlacementSummary> summaries = new HashMap<>();
        List<Application> selectedApplications = applicationDAO.findByStatus(ApplicationStatus.SELECTED.name());

        for (Application application : selectedApplications) {
            Optional<Student> student = studentDAO.findById(application.getStudentId());
            if (!student.isPresent()) {
                continue;
            }
            String branch = student.get().getBranch();
            DepartmentPlacementSummary summary = summaries.computeIfAbsent(
                    branch,
                    key -> new DepartmentPlacementSummary(key, 0, 0));
            summary.incrementSelectedCount();
        }

        for (Student student : studentDAO.findAllActive()) {
            DepartmentPlacementSummary summary = summaries.computeIfAbsent(
                    student.getBranch(),
                    key -> new DepartmentPlacementSummary(key, 0, 0));
            summary.incrementRegisteredStudents();
        }

        return new ArrayList<>(summaries.values());
    }

    private int countByStatus(List<Application> applications, ApplicationStatus status) {
        return (int) applications.stream()
                .filter(application -> status.name().equals(application.getStatus()))
                .count();
    }
}

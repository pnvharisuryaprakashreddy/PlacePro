package com.placepro.service.drive;

import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Student;
import com.placepro.service.ServiceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EligibilityService {

    private final StudentDAO studentDAO;
    private final PlacementDriveDAO placementDriveDAO;

    public EligibilityService(StudentDAO studentDAO, PlacementDriveDAO placementDriveDAO) {
        this.studentDAO = studentDAO;
        this.placementDriveDAO = placementDriveDAO;
    }

    public EligibilityResult check(int studentId, int driveId) {
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new ServiceException("Student not found."));
        PlacementDrive drive = placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new ServiceException("Placement drive not found."));

        List<String> reasons = new ArrayList<>();

        if (student.getCgpa().compareTo(drive.getMinCgpa()) < 0) {
            reasons.add(String.format(
                    Locale.ENGLISH,
                    "CGPA %.2f is below the required minimum of %.2f.",
                    student.getCgpa(),
                    drive.getMinCgpa()));
        }

        if (student.getBacklogCount() > drive.getMaxBacklogs()) {
            reasons.add(String.format(
                    Locale.ENGLISH,
                    "Backlog count %d exceeds the maximum allowed %d.",
                    student.getBacklogCount(),
                    drive.getMaxBacklogs()));
        }

        if (!isBranchAllowed(student.getBranch(), drive.getAllowedBranches())) {
            reasons.add(String.format(
                    Locale.ENGLISH,
                    "Branch %s is not eligible for this drive.",
                    student.getBranch()));
        }

        if (!DriveStatus.PUBLISHED.name().equals(drive.getStatus())) {
            reasons.add("This drive is not open for applications.");
        }

        return reasons.isEmpty() ? EligibilityResult.eligible() : EligibilityResult.ineligible(reasons);
    }

    private boolean isBranchAllowed(String studentBranch, String allowedBranches) {
        List<String> branches = Arrays.stream(allowedBranches.split(","))
                .map(String::trim)
                .filter(branch -> !branch.isEmpty())
                .map(branch -> branch.toUpperCase(Locale.ENGLISH))
                .collect(Collectors.toList());
        return branches.contains(studentBranch.trim().toUpperCase(Locale.ENGLISH));
    }
}

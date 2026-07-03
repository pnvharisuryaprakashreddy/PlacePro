package com.placepro.service.student;

import com.placepro.model.PlacementDrive;

import java.time.LocalDateTime;

public class StudentDriveSummary {

    private final PlacementDrive drive;
    private final String companyName;

    public StudentDriveSummary(PlacementDrive drive, String companyName) {
        this.drive = drive;
        this.companyName = companyName;
    }

    public PlacementDrive getDrive() {
        return drive;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getDriveId() {
        return drive.getDriveId();
    }

    public String getJobTitle() {
        return drive.getJobTitle();
    }

    public LocalDateTime getApplicationDeadline() {
        return drive.getApplicationDeadline();
    }
}

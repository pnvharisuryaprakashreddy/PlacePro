package com.placepro.service.report;

public class DrivePlacementSummary {

    private final int driveId;
    private final String jobTitle;
    private final int appliedCount;
    private final int shortlistedCount;
    private final int selectedCount;
    private final int rejectedCount;

    public DrivePlacementSummary(int driveId,
                                 String jobTitle,
                                 int appliedCount,
                                 int shortlistedCount,
                                 int selectedCount,
                                 int rejectedCount) {
        this.driveId = driveId;
        this.jobTitle = jobTitle;
        this.appliedCount = appliedCount;
        this.shortlistedCount = shortlistedCount;
        this.selectedCount = selectedCount;
        this.rejectedCount = rejectedCount;
    }

    public int getDriveId() {
        return driveId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public int getAppliedCount() {
        return appliedCount;
    }

    public int getShortlistedCount() {
        return shortlistedCount;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }
}

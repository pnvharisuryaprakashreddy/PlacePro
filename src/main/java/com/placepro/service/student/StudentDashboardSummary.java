package com.placepro.service.student;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class StudentDashboardSummary {

    private final int activeDrivesCount;
    private final Map<String, Integer> applicationCountsByStatus;
    private final int upcomingDeadlinesCount;
    private final List<StudentDriveSummary> recentPublishedDrives;

    public StudentDashboardSummary(int activeDrivesCount,
                                   Map<String, Integer> applicationCountsByStatus,
                                   int upcomingDeadlinesCount,
                                   List<StudentDriveSummary> recentPublishedDrives) {
        this.activeDrivesCount = activeDrivesCount;
        this.applicationCountsByStatus = applicationCountsByStatus;
        this.upcomingDeadlinesCount = upcomingDeadlinesCount;
        this.recentPublishedDrives = recentPublishedDrives;
    }

    public int getActiveDrivesCount() {
        return activeDrivesCount;
    }

    public Map<String, Integer> getApplicationCountsByStatus() {
        return applicationCountsByStatus;
    }

    public int getUpcomingDeadlinesCount() {
        return upcomingDeadlinesCount;
    }

    public List<StudentDriveSummary> getRecentPublishedDrives() {
        return recentPublishedDrives;
    }
}

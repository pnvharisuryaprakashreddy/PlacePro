package com.placepro.service.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ApplicationTrackingRow {

    private final int applicationId;
    private final String companyName;
    private final String jobTitle;
    private final String status;
    private final LocalDateTime appliedAt;
    private final LocalDate interviewDate;
    private final LocalTime interviewTime;
    private final String venue;

    public ApplicationTrackingRow(int applicationId,
                                  String companyName,
                                  String jobTitle,
                                  String status,
                                  LocalDateTime appliedAt,
                                  LocalDate interviewDate,
                                  LocalTime interviewTime,
                                  String venue) {
        this.applicationId = applicationId;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.status = status;
        this.appliedAt = appliedAt;
        this.interviewDate = interviewDate;
        this.interviewTime = interviewTime;
        this.venue = venue;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public LocalTime getInterviewTime() {
        return interviewTime;
    }

    public String getVenue() {
        return venue;
    }
}

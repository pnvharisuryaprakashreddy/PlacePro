package com.placepro.service.recruiter;

import com.placepro.model.InterviewSchedule;

public class RecruiterInterviewRow {

    private final InterviewSchedule interview;
    private final String studentName;
    private final String jobTitle;
    private final int applicationId;

    public RecruiterInterviewRow(InterviewSchedule interview,
                                 String studentName,
                                 String jobTitle,
                                 int applicationId) {
        this.interview = interview;
        this.studentName = studentName;
        this.jobTitle = jobTitle;
        this.applicationId = applicationId;
    }

    public InterviewSchedule getInterview() {
        return interview;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public int getApplicationId() {
        return applicationId;
    }
}

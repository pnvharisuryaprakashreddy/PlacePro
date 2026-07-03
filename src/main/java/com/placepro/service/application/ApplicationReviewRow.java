package com.placepro.service.application;

import com.placepro.model.Application;

import java.math.BigDecimal;

public class ApplicationReviewRow {

    private final Application application;
    private final String studentName;
    private final BigDecimal cgpa;
    private final String branch;
    private final String resumeFileName;
    private final String resumeFilePath;
    private final String companyName;
    private final String jobTitle;

    public ApplicationReviewRow(Application application,
                                String studentName,
                                BigDecimal cgpa,
                                String branch,
                                String resumeFileName,
                                String resumeFilePath,
                                String companyName,
                                String jobTitle) {
        this.application = application;
        this.studentName = studentName;
        this.cgpa = cgpa;
        this.branch = branch;
        this.resumeFileName = resumeFileName;
        this.resumeFilePath = resumeFilePath;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
    }

    public Application getApplication() {
        return application;
    }

    public String getStudentName() {
        return studentName;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public String getBranch() {
        return branch;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public String getResumeFilePath() {
        return resumeFilePath;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }
}

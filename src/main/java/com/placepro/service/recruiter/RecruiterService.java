package com.placepro.service.recruiter;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.CompanyDAO;
import com.placepro.dao.InterviewScheduleDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.RecruiterDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.model.Application;
import com.placepro.model.Company;
import com.placepro.model.InterviewSchedule;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.application.ApplicationReviewRow;
import com.placepro.service.application.ApplicationStatus;
import com.placepro.service.auth.SessionManager;
import com.placepro.dao.ResumeDAO;
import com.placepro.model.Resume;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecruiterService {

    private final RecruiterDAO recruiterDAO;
    private final PlacementDriveDAO placementDriveDAO;
    private final CompanyDAO companyDAO;
    private final ApplicationDAO applicationDAO;
    private final StudentDAO studentDAO;
    private final ResumeDAO resumeDAO;
    private final InterviewScheduleDAO interviewScheduleDAO;
    private final SessionManager sessionManager;

    public RecruiterService(RecruiterDAO recruiterDAO,
                            PlacementDriveDAO placementDriveDAO,
                            CompanyDAO companyDAO,
                            ApplicationDAO applicationDAO,
                            StudentDAO studentDAO,
                            ResumeDAO resumeDAO,
                            InterviewScheduleDAO interviewScheduleDAO,
                            SessionManager sessionManager) {
        this.recruiterDAO = recruiterDAO;
        this.placementDriveDAO = placementDriveDAO;
        this.companyDAO = companyDAO;
        this.applicationDAO = applicationDAO;
        this.studentDAO = studentDAO;
        this.resumeDAO = resumeDAO;
        this.interviewScheduleDAO = interviewScheduleDAO;
        this.sessionManager = sessionManager;
    }

    public List<PlacementDrive> listDrivesForCurrentRecruiter() {
        int companyId = requireRecruiterCompanyId();
        return placementDriveDAO.findByCompanyId(companyId);
    }

    public List<ApplicationReviewRow> listApplicationsForDrive(int driveId) {
        int companyId = requireRecruiterCompanyId();
        verifyDriveBelongsToCompany(driveId, companyId);
        return buildReviewRows(applicationDAO.findByDriveId(driveId));
    }

    public List<ApplicationReviewRow> listShortlistedApplicationsForCurrentRecruiter() {
        int companyId = requireRecruiterCompanyId();
        List<ApplicationReviewRow> rows = new ArrayList<>();
        for (PlacementDrive drive : placementDriveDAO.findByCompanyId(companyId)) {
            for (Application application : applicationDAO.findByDriveId(drive.getDriveId())) {
                if (ApplicationStatus.SHORTLISTED.name().equals(application.getStatus())
                        || ApplicationStatus.INTERVIEW_SCHEDULED.name().equals(application.getStatus())) {
                    rows.addAll(buildReviewRows(List.of(application)));
                }
            }
        }
        return rows;
    }

    public List<RecruiterInterviewRow> listScheduledInterviewsForCurrentRecruiter() {
        int companyId = requireRecruiterCompanyId();
        List<RecruiterInterviewRow> rows = new ArrayList<>();
        for (InterviewSchedule interview : interviewScheduleDAO.findByCompanyId(companyId)) {
            Application application = applicationDAO.findById(interview.getApplicationId())
                    .orElse(null);
            if (application == null) {
                continue;
            }
            String studentName = studentDAO.findById(application.getStudentId())
                    .map(Student::getFullName)
                    .orElse("Unknown");
            String jobTitle = placementDriveDAO.findById(application.getDriveId())
                    .map(PlacementDrive::getJobTitle)
                    .orElse("Unknown");
            rows.add(new RecruiterInterviewRow(interview, studentName, jobTitle, application.getApplicationId()));
        }
        return rows;
    }

    public void verifyApplicationBelongsToRecruiterCompany(int applicationId) {
        int companyId = requireRecruiterCompanyId();
        Application application = applicationDAO.findById(applicationId)
                .orElseThrow(() -> new ServiceException("Application not found."));
        verifyDriveBelongsToCompany(application.getDriveId(), companyId);
    }

    private List<ApplicationReviewRow> buildReviewRows(List<Application> applications) {
        return applications.stream()
                .map(application -> {
                    Student student = studentDAO.findById(application.getStudentId())
                            .orElseThrow(() -> new ServiceException("Student not found."));
                    Resume resume = null;
                    if (application.getResumeId() != null) {
                        resume = resumeDAO.findById(application.getResumeId()).orElse(null);
                    }
                    if (resume == null) {
                        resume = resumeDAO.findCurrentByStudentId(application.getStudentId()).orElse(null);
                    }
                    String resumeFileName = resume == null ? "-" : resume.getFileName();
                    String resumeFilePath = resume == null ? null : resume.getFilePath();
                    PlacementDrive drive = placementDriveDAO.findById(application.getDriveId())
                            .orElseThrow(() -> new ServiceException("Placement drive not found."));
                    String companyName = companyDAO.findById(drive.getCompanyId())
                            .map(Company::getCompanyName)
                            .orElse("Unknown");
                    return new ApplicationReviewRow(
                            application,
                            student.getFullName(),
                            student.getCgpa(),
                            student.getBranch(),
                            resumeFileName,
                            resumeFilePath,
                            companyName,
                            drive.getJobTitle());
                })
                .collect(Collectors.toList());
    }

    private int requireRecruiterCompanyId() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.RECRUITER);
        int recruiterId = sessionManager.getCurrentUserId()
                .orElseThrow(() -> new ServiceException("You must be logged in."));
        Recruiter recruiter = recruiterDAO.findById(recruiterId)
                .orElseThrow(() -> new ServiceException("Recruiter not found."));
        if (recruiter.getCompanyId() == null) {
            throw new ServiceException("Recruiter is not linked to a company.");
        }
        return recruiter.getCompanyId();
    }

    private void verifyDriveBelongsToCompany(int driveId, int companyId) {
        PlacementDrive drive = placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new ServiceException("Placement drive not found."));
        if (drive.getCompanyId() == null || drive.getCompanyId() != companyId) {
            throw new ServiceException("You are not authorized to access this drive.");
        }
    }
}

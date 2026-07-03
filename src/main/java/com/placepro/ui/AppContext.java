package com.placepro.ui;

import com.placepro.dao.ApplicationDAO;
import com.placepro.dao.CompanyDAO;
import com.placepro.dao.InterviewScheduleDAO;
import com.placepro.dao.NotificationDAO;
import com.placepro.dao.PlacementDriveDAO;
import com.placepro.dao.PlacementOfficerDAO;
import com.placepro.dao.RecruiterDAO;
import com.placepro.dao.ReportDAO;
import com.placepro.dao.ResumeDAO;
import com.placepro.dao.StudentDAO;
import com.placepro.dao.impl.ApplicationDAOImpl;
import com.placepro.dao.impl.CompanyDAOImpl;
import com.placepro.dao.impl.InterviewScheduleDAOImpl;
import com.placepro.dao.impl.NotificationDAOImpl;
import com.placepro.dao.impl.PlacementDriveDAOImpl;
import com.placepro.dao.impl.PlacementOfficerDAOImpl;
import com.placepro.dao.impl.RecruiterDAOImpl;
import com.placepro.dao.impl.ReportDAOImpl;
import com.placepro.dao.impl.ResumeDAOImpl;
import com.placepro.dao.impl.StudentDAOImpl;
import com.placepro.monitoring.DaoMetrics;
import com.placepro.service.CompanyService;
import com.placepro.service.ResumeService;
import com.placepro.service.admin.UserManagementService;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.application.InterviewService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;
import com.placepro.service.drive.EligibilityService;
import com.placepro.service.notification.NotificationService;
import com.placepro.service.recruiter.RecruiterService;
import com.placepro.service.report.ReportService;
import com.placepro.service.student.ApplicationTrackingService;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDirectoryService;

public final class AppContext {

    private static final SessionManager SESSION_MANAGER = new SessionManager();

    // Every DAO is wrapped in a thin metrics proxy that times each call for
    // the placepro_db_query_duration_seconds histogram.
    private static final StudentDAO STUDENT_DAO =
            DaoMetrics.instrument(StudentDAO.class, new StudentDAOImpl());
    private static final PlacementOfficerDAO PLACEMENT_OFFICER_DAO =
            DaoMetrics.instrument(PlacementOfficerDAO.class, new PlacementOfficerDAOImpl());
    private static final RecruiterDAO RECRUITER_DAO =
            DaoMetrics.instrument(RecruiterDAO.class, new RecruiterDAOImpl());
    private static final CompanyDAO COMPANY_DAO =
            DaoMetrics.instrument(CompanyDAO.class, new CompanyDAOImpl());
    private static final PlacementDriveDAO PLACEMENT_DRIVE_DAO =
            DaoMetrics.instrument(PlacementDriveDAO.class, new PlacementDriveDAOImpl());
    private static final ApplicationDAO APPLICATION_DAO =
            DaoMetrics.instrument(ApplicationDAO.class, new ApplicationDAOImpl());
    private static final NotificationDAO NOTIFICATION_DAO =
            DaoMetrics.instrument(NotificationDAO.class, new NotificationDAOImpl());
    private static final ResumeDAO RESUME_DAO =
            DaoMetrics.instrument(ResumeDAO.class, new ResumeDAOImpl());
    private static final InterviewScheduleDAO INTERVIEW_SCHEDULE_DAO =
            DaoMetrics.instrument(InterviewScheduleDAO.class, new InterviewScheduleDAOImpl());
    private static final ReportDAO REPORT_DAO =
            DaoMetrics.instrument(ReportDAO.class, new ReportDAOImpl());

    private static final AuthService AUTH_SERVICE = new AuthService(
            STUDENT_DAO,
            PLACEMENT_OFFICER_DAO,
            RECRUITER_DAO,
            SESSION_MANAGER);

    private static final NotificationService NOTIFICATION_SERVICE = new NotificationService(
            NOTIFICATION_DAO,
            SESSION_MANAGER);
    private static final CompanyService COMPANY_SERVICE = new CompanyService(COMPANY_DAO, SESSION_MANAGER);
    private static final DriveService DRIVE_SERVICE = new DriveService(
            PLACEMENT_DRIVE_DAO,
            STUDENT_DAO,
            NOTIFICATION_SERVICE,
            SESSION_MANAGER);
    private static final EligibilityService ELIGIBILITY_SERVICE = new EligibilityService(STUDENT_DAO, PLACEMENT_DRIVE_DAO);
    private static final ApplicationService APPLICATION_SERVICE = new ApplicationService(
            APPLICATION_DAO,
            NOTIFICATION_SERVICE,
            PLACEMENT_DRIVE_DAO,
            COMPANY_DAO,
            RESUME_DAO,
            STUDENT_DAO,
            ELIGIBILITY_SERVICE,
            SESSION_MANAGER);
    private static final InterviewService INTERVIEW_SERVICE = new InterviewService(
            INTERVIEW_SCHEDULE_DAO,
            APPLICATION_DAO,
            PLACEMENT_DRIVE_DAO,
            RECRUITER_DAO,
            NOTIFICATION_SERVICE,
            SESSION_MANAGER);
    private static final ResumeService RESUME_SERVICE = new ResumeService(RESUME_DAO, SESSION_MANAGER);
    private static final DashboardService DASHBOARD_SERVICE = new DashboardService(
            APPLICATION_DAO,
            PLACEMENT_DRIVE_DAO,
            COMPANY_DAO,
            SESSION_MANAGER);
    private static final ApplicationTrackingService APPLICATION_TRACKING_SERVICE = new ApplicationTrackingService(
            APPLICATION_DAO,
            PLACEMENT_DRIVE_DAO,
            COMPANY_DAO,
            INTERVIEW_SCHEDULE_DAO,
            SESSION_MANAGER);
    private static final RecruiterService RECRUITER_SERVICE = new RecruiterService(
            RECRUITER_DAO,
            PLACEMENT_DRIVE_DAO,
            COMPANY_DAO,
            APPLICATION_DAO,
            STUDENT_DAO,
            RESUME_DAO,
            INTERVIEW_SCHEDULE_DAO,
            SESSION_MANAGER);
    private static final StudentDirectoryService STUDENT_DIRECTORY_SERVICE = new StudentDirectoryService(
            STUDENT_DAO,
            RESUME_DAO,
            SESSION_MANAGER);
    private static final ReportService REPORT_SERVICE = new ReportService(
            PLACEMENT_DRIVE_DAO,
            APPLICATION_DAO,
            STUDENT_DAO,
            REPORT_DAO,
            SESSION_MANAGER);
    private static final UserManagementService USER_MANAGEMENT_SERVICE = new UserManagementService(
            STUDENT_DAO,
            PLACEMENT_OFFICER_DAO,
            RECRUITER_DAO,
            AUTH_SERVICE,
            SESSION_MANAGER);

    private AppContext() {
    }

    public static AuthService getAuthService() {
        return AUTH_SERVICE;
    }

    public static SessionManager getSessionManager() {
        return SESSION_MANAGER;
    }

    public static CompanyService getCompanyService() {
        return COMPANY_SERVICE;
    }

    public static DriveService getDriveService() {
        return DRIVE_SERVICE;
    }

    public static EligibilityService getEligibilityService() {
        return ELIGIBILITY_SERVICE;
    }

    public static ApplicationService getApplicationService() {
        return APPLICATION_SERVICE;
    }

    public static InterviewService getInterviewService() {
        return INTERVIEW_SERVICE;
    }

    public static NotificationService getNotificationService() {
        return NOTIFICATION_SERVICE;
    }

    public static ResumeService getResumeService() {
        return RESUME_SERVICE;
    }

    public static DashboardService getDashboardService() {
        return DASHBOARD_SERVICE;
    }

    public static ApplicationTrackingService getApplicationTrackingService() {
        return APPLICATION_TRACKING_SERVICE;
    }

    public static RecruiterService getRecruiterService() {
        return RECRUITER_SERVICE;
    }

    public static UserManagementService getUserManagementService() {
        return USER_MANAGEMENT_SERVICE;
    }

    public static ReportService getReportService() {
        return REPORT_SERVICE;
    }

    public static StudentDirectoryService getStudentDirectoryService() {
        return STUDENT_DIRECTORY_SERVICE;
    }
}

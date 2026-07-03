package com.placepro.ui;

import com.placepro.dao.impl.CompanyDAOImpl;
import com.placepro.dao.impl.PlacementDriveDAOImpl;
import com.placepro.dao.impl.PlacementOfficerDAOImpl;
import com.placepro.dao.impl.RecruiterDAOImpl;
import com.placepro.dao.impl.StudentDAOImpl;
import com.placepro.service.CompanyService;
import com.placepro.service.admin.UserManagementService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;

public final class AppContext {

    private static final SessionManager SESSION_MANAGER = new SessionManager();
    private static final StudentDAOImpl STUDENT_DAO = new StudentDAOImpl();
    private static final PlacementOfficerDAOImpl PLACEMENT_OFFICER_DAO = new PlacementOfficerDAOImpl();
    private static final RecruiterDAOImpl RECRUITER_DAO = new RecruiterDAOImpl();
    private static final CompanyDAOImpl COMPANY_DAO = new CompanyDAOImpl();
    private static final PlacementDriveDAOImpl PLACEMENT_DRIVE_DAO = new PlacementDriveDAOImpl();

    private static final AuthService AUTH_SERVICE = new AuthService(
            STUDENT_DAO,
            PLACEMENT_OFFICER_DAO,
            RECRUITER_DAO,
            SESSION_MANAGER);

    private static final CompanyService COMPANY_SERVICE = new CompanyService(COMPANY_DAO, SESSION_MANAGER);
    private static final DriveService DRIVE_SERVICE = new DriveService(PLACEMENT_DRIVE_DAO, SESSION_MANAGER);
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

    public static UserManagementService getUserManagementService() {
        return USER_MANAGEMENT_SERVICE;
    }
}

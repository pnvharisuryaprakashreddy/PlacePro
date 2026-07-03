package com.placepro.ui;

import com.placepro.dao.impl.PlacementOfficerDAOImpl;
import com.placepro.dao.impl.RecruiterDAOImpl;
import com.placepro.dao.impl.StudentDAOImpl;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;

public final class AppContext {

    private static final SessionManager SESSION_MANAGER = new SessionManager();
    private static final AuthService AUTH_SERVICE = new AuthService(
            new StudentDAOImpl(),
            new PlacementOfficerDAOImpl(),
            new RecruiterDAOImpl(),
            SESSION_MANAGER);

    private AppContext() {
    }

    public static AuthService getAuthService() {
        return AUTH_SERVICE;
    }

    public static SessionManager getSessionManager() {
        return SESSION_MANAGER;
    }
}

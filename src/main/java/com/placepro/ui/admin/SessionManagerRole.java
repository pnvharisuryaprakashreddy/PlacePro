package com.placepro.ui.admin;

import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;

final class SessionManagerRole {

    private SessionManagerRole() {
    }

    static boolean isAdmin(SessionManager sessionManager) {
        return sessionManager.getCurrentRole().map(role -> role == UserRole.ADMIN).orElse(false);
    }
}

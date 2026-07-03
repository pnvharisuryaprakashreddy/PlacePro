package com.placepro.service;

import com.placepro.service.auth.SessionManager;

public final class AuthorizationHelper {

    private AuthorizationHelper() {
    }

    public static void requireRole(SessionManager sessionManager, UserRole... allowedRoles) {
        UserRole currentRole = sessionManager.getCurrentRole()
                .orElseThrow(() -> new UnauthorizedException("You must be logged in to perform this action."));

        for (UserRole allowedRole : allowedRoles) {
            if (currentRole == allowedRole) {
                return;
            }
        }

        throw new UnauthorizedException("You do not have permission to perform this action.");
    }

    public static void requireSelfOrRole(SessionManager sessionManager, int userId, UserRole... allowedRoles) {
        if (sessionManager.getCurrentUserId().orElse(-1) == userId) {
            return;
        }
        requireRole(sessionManager, allowedRoles);
    }
}

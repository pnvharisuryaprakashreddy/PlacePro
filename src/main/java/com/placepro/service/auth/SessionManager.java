package com.placepro.service.auth;

import com.placepro.service.UserRole;

import java.util.Optional;

public final class SessionManager {

    private Integer currentUserId;
    private UserRole currentRole;

    public void setSession(int userId, UserRole role) {
        this.currentUserId = userId;
        this.currentRole = role;
    }

    public Optional<Integer> getCurrentUserId() {
        return Optional.ofNullable(currentUserId);
    }

    public Optional<UserRole> getCurrentRole() {
        return Optional.ofNullable(currentRole);
    }

    public boolean isLoggedIn() {
        return currentUserId != null && currentRole != null;
    }

    public void logout() {
        currentUserId = null;
        currentRole = null;
    }
}

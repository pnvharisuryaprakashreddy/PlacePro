package com.placepro.ui.login;

import com.placepro.service.auth.AuthService;
import com.placepro.service.ServiceException;

public class AdminLoginPanel extends BaseLoginPanel {

    public AdminLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Administrator Login", authService, navigator);
    }

    @Override
    protected void performLogin(String email, String password) {
        var admin = authService.loginOfficerOrAdmin(email, password);
        if (admin.getRole() == null || !"ADMIN".equals(admin.getRole())) {
            authService.logout();
            throw new ServiceException("This account is not authorized for administrator access.");
        }
        navigator.showAdminDashboard(admin);
    }
}

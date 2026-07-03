package com.placepro.ui.login;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.auth.AuthService;
import com.placepro.service.ServiceException;

public class AdminLoginPanel extends BaseLoginPanel<PlacementOfficer> {

    public AdminLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Administrator Login", authService, navigator);
    }

    @Override
    protected PlacementOfficer authenticate(String email, String password) {
        PlacementOfficer admin = authService.loginOfficerOrAdmin(email, password);
        if (admin.getRole() == null || !"ADMIN".equals(admin.getRole())) {
            authService.logout();
            throw new ServiceException("This account is not authorized for administrator access.");
        }
        return admin;
    }

    @Override
    protected void onLoginSuccess(PlacementOfficer admin) {
        navigator.showAdminDashboard(admin);
    }
}

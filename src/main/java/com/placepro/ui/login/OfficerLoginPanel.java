package com.placepro.ui.login;

import com.placepro.service.auth.AuthService;
import com.placepro.service.ServiceException;

public class OfficerLoginPanel extends BaseLoginPanel {

    public OfficerLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Placement Officer Login", authService, navigator);
    }

    @Override
    protected void performLogin(String email, String password) {
        var officer = authService.loginOfficerOrAdmin(email, password);
        if (officer.getRole() == null || !"OFFICER".equals(officer.getRole())) {
            authService.logout();
            throw new ServiceException("This account is not authorized for placement officer access.");
        }
        navigator.showOfficerDashboard(officer);
    }
}

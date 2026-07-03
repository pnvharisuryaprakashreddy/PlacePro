package com.placepro.ui.login;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.auth.AuthService;
import com.placepro.service.ServiceException;

public class OfficerLoginPanel extends BaseLoginPanel<PlacementOfficer> {

    public OfficerLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Placement Officer Login", authService, navigator);
    }

    @Override
    protected PlacementOfficer authenticate(String email, String password) {
        PlacementOfficer officer = authService.loginOfficerOrAdmin(email, password);
        if (officer.getRole() == null || !"OFFICER".equals(officer.getRole())) {
            authService.logout();
            throw new ServiceException("This account is not authorized for placement officer access.");
        }
        return officer;
    }

    @Override
    protected void onLoginSuccess(PlacementOfficer officer) {
        navigator.showOfficerDashboard(officer);
    }
}

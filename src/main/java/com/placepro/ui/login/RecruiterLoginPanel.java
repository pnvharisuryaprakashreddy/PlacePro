package com.placepro.ui.login;

import com.placepro.service.auth.AuthService;

public class RecruiterLoginPanel extends BaseLoginPanel {

    public RecruiterLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Recruiter Login", authService, navigator);
    }

    @Override
    protected void performLogin(String email, String password) {
        navigator.showRecruiterDashboard(authService.loginRecruiter(email, password));
    }
}

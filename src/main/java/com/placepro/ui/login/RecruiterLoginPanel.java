package com.placepro.ui.login;

import com.placepro.model.Recruiter;
import com.placepro.service.auth.AuthService;

public class RecruiterLoginPanel extends BaseLoginPanel<Recruiter> {

    public RecruiterLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Recruiter Login", authService, navigator);
    }

    @Override
    protected Recruiter authenticate(String email, String password) {
        return authService.loginRecruiter(email, password);
    }

    @Override
    protected void onLoginSuccess(Recruiter recruiter) {
        navigator.showRecruiterDashboard(recruiter);
    }
}

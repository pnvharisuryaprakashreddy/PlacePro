package com.placepro.ui.login;

import com.placepro.service.auth.AuthService;
import com.placepro.ui.common.UiStyles;

import java.awt.GridBagConstraints;

public class StudentLoginPanel extends BaseLoginPanel {

    public StudentLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Student Login", authService, navigator);
    }

    @Override
    protected void addRoleSpecificLinks(GridBagConstraints constraints) {
        constraints.gridy++;
        add(UiStyles.createLinkLabel("New student? Register here", navigator::showStudentRegistration), constraints);
    }

    @Override
    protected void performLogin(String email, String password) {
        navigator.showStudentDashboard(authService.loginStudent(email, password));
    }
}

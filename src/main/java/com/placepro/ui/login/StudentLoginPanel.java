package com.placepro.ui.login;

import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.common.UiStyles;

import java.awt.GridBagConstraints;

public class StudentLoginPanel extends BaseLoginPanel<Student> {

    public StudentLoginPanel(AuthService authService, LoginNavigator navigator) {
        super("Student Login", authService, navigator);
    }

    @Override
    protected void addRoleSpecificLinks(GridBagConstraints constraints) {
        constraints.gridy++;
        add(UiStyles.createLinkLabel("New student? Register here", navigator::showStudentRegistration), constraints);
    }

    @Override
    protected Student authenticate(String email, String password) {
        return authService.loginStudent(email, password);
    }

    @Override
    protected void onLoginSuccess(Student student) {
        navigator.showStudentDashboard(student);
    }
}

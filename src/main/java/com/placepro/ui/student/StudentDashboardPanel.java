package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.UiStyles;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class StudentDashboardPanel extends JPanel {

    public StudentDashboardPanel(Student student, AuthService authService, Runnable onLogout) {
        setLayout(new BorderLayout(16, 16));

        JLabel welcomeLabel = new JLabel(
                "Welcome, " + student.getFullName() + ", role: STUDENT",
                JLabel.CENTER);
        welcomeLabel.setFont(UiStyles.TITLE_FONT);
        add(welcomeLabel, BorderLayout.CENTER);

        LogoutButton logoutButton = new LogoutButton(authService, onLogout);
        JPanel southPanel = new JPanel();
        southPanel.add(logoutButton);
        add(southPanel, BorderLayout.SOUTH);
    }
}

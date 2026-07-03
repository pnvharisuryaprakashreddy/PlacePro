package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.AppContext;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class StudentDashboardPanel extends JPanel {

    public StudentDashboardPanel(Student student, AuthService authService, Runnable onLogout) {
        setLayout(new BorderLayout());
        add(new StudentConsolePanel(
                student,
                authService,
                AppContext.getDashboardService(),
                AppContext.getApplicationService(),
                AppContext.getApplicationTrackingService(),
                AppContext.getEligibilityService(),
                AppContext.getResumeService(),
                onLogout), BorderLayout.CENTER);
    }
}

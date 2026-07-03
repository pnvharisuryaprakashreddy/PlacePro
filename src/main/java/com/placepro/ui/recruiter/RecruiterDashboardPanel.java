package com.placepro.ui.recruiter;

import com.placepro.model.Recruiter;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.AppContext;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class RecruiterDashboardPanel extends JPanel {

    public RecruiterDashboardPanel(Recruiter recruiter, AuthService authService, Runnable onLogout) {
        setLayout(new BorderLayout());
        add(new RecruiterConsolePanel(
                recruiter,
                authService,
                AppContext.getRecruiterService(),
                AppContext.getInterviewService(),
                onLogout), BorderLayout.CENTER);
    }
}

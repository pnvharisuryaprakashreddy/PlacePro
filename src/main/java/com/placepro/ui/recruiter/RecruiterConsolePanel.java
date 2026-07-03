package com.placepro.ui.recruiter;

import com.placepro.model.Recruiter;
import com.placepro.service.application.InterviewService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.recruiter.RecruiterService;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.UiStyles;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class RecruiterConsolePanel extends JPanel {

    public RecruiterConsolePanel(Recruiter recruiter,
                                 AuthService authService,
                                 RecruiterService recruiterService,
                                 InterviewService interviewService,
                                 Runnable onLogout) {
        setLayout(new BorderLayout(8, 8));

        JPanel header = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + recruiter.getFullName() + ", role: RECRUITER");
        welcomeLabel.setFont(UiStyles.TITLE_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(new LogoutButton(authService, onLogout), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Drives & Shortlist", new RecruiterDrivesPanel(recruiterService));
        tabs.addTab("Interviews", new RecruiterInterviewPanel(recruiterService, interviewService));
        add(tabs, BorderLayout.CENTER);
    }
}

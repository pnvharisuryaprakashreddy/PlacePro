package com.placepro.ui.admin;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.CompanyService;
import com.placepro.service.admin.UserManagementService;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.application.InterviewService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;
import com.placepro.ui.AppContext;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.NotificationBellComponent;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.officer.CompanyListPanel;
import com.placepro.ui.officer.DriveListPanel;
import com.placepro.ui.officer.OfficerApplicationsPanel;
import com.placepro.ui.officer.ReportsPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class AdminConsolePanel extends JPanel {

    public AdminConsolePanel(PlacementOfficer admin,
                             AuthService authService,
                             CompanyService companyService,
                             DriveService driveService,
                             UserManagementService userManagementService,
                             ApplicationService applicationService,
                             InterviewService interviewService,
                             SessionManager sessionManager,
                             Runnable onLogout) {
        setLayout(new BorderLayout(8, 8));

        JPanel header = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getFullName() + ", role: ADMIN");
        welcomeLabel.setFont(UiStyles.TITLE_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.add(new NotificationBellComponent(AppContext.getNotificationService()));
        headerActions.add(new LogoutButton(authService, onLogout));
        header.add(headerActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Companies", new CompanyListPanel(companyService));
        tabs.addTab("Drives", new DriveListPanel(driveService, companyService, sessionManager));
        tabs.addTab("Applications", new OfficerApplicationsPanel(
                admin, driveService, applicationService, interviewService));
        tabs.addTab("Reports", new ReportsPanel(AppContext.getReportService(), companyService));
        tabs.addTab("Analytics", new AnalyticsDashboardPanel(AppContext.getReportService()));

        if (SessionManagerRole.isAdmin(sessionManager)) {
            tabs.addTab("User Management", new UserManagementPanel(userManagementService));
        }

        add(tabs, BorderLayout.CENTER);
    }
}

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
import com.placepro.ui.officer.StudentListPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

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
        setLayout(new BorderLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);

        // Dark Slate Top Header
        JPanel header = new UiStyles.DarkHeaderPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 4));
        titleGroup.setOpaque(false);
        JLabel welcomeLabel = new JLabel("System Administrator Console — " + admin.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Global System Metrics, Analytics, User Management & Campus Operations");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(203, 213, 225));
        titleGroup.add(welcomeLabel);
        titleGroup.add(subtitleLabel);
        header.add(titleGroup, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerActions.setOpaque(false);
        headerActions.add(new NotificationBellComponent(AppContext.getNotificationService()));
        headerActions.add(new LogoutButton(authService, onLogout));
        header.add(headerActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabbed Workspaces
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📊 Live Analytics", new AnalyticsDashboardPanel(AppContext.getReportService()));
        tabs.addTab("🏢 Corporate Companies", new CompanyListPanel(companyService));
        tabs.addTab("🎓 Student Directory", new StudentListPanel(
                AppContext.getStudentDirectoryService(),
                AppContext.getApplicationTrackingService()));
        tabs.addTab("⚡ Placement Drives", new DriveListPanel(driveService, companyService, sessionManager));
        tabs.addTab("📋 Applications & Interviews", new OfficerApplicationsPanel(
                admin, driveService, applicationService, interviewService));
        tabs.addTab("📄 Custom Reports", new ReportsPanel(AppContext.getReportService(), companyService));

        if (SessionManagerRole.isAdmin(sessionManager)) {
            tabs.addTab("⚡ User Management", new UserManagementPanel(userManagementService));
        }

        UiStyles.styleTabs(tabs);

        JPanel content = new UiStyles.RoundedPanel(16, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.add(tabs, BorderLayout.CENTER);

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(UiStyles.BACKGROUND_COLOR);
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainWrapper.add(content, BorderLayout.CENTER);

        add(mainWrapper, BorderLayout.CENTER);
    }
}

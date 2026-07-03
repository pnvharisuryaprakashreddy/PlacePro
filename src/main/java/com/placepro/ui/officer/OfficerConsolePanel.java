package com.placepro.ui.officer;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.CompanyService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.PlaceholderPanel;
import com.placepro.ui.common.UiStyles;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class OfficerConsolePanel extends JPanel {

    public OfficerConsolePanel(PlacementOfficer officer,
                               AuthService authService,
                               CompanyService companyService,
                               DriveService driveService,
                               SessionManager sessionManager,
                               Runnable onLogout) {
        setLayout(new BorderLayout(8, 8));

        JPanel header = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + officer.getFullName() + ", role: OFFICER");
        welcomeLabel.setFont(UiStyles.TITLE_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(new LogoutButton(authService, onLogout), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Companies", new CompanyListPanel(companyService));
        tabs.addTab("Drives", new DriveListPanel(driveService, companyService, sessionManager));
        tabs.addTab("Applications", new PlaceholderPanel("Application review will be available in a later release."));
        tabs.addTab("Reports", new PlaceholderPanel("Placement reports will be available in a later release."));
        add(tabs, BorderLayout.CENTER);
    }
}

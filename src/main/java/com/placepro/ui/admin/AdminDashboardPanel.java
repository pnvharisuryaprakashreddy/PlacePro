package com.placepro.ui.admin;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.CompanyService;
import com.placepro.service.admin.UserManagementService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;

import javax.swing.JPanel;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(PlacementOfficer admin,
                               AuthService authService,
                               CompanyService companyService,
                               DriveService driveService,
                               UserManagementService userManagementService,
                               SessionManager sessionManager,
                               Runnable onLogout) {
        setLayout(new java.awt.BorderLayout());
        add(new AdminConsolePanel(
                admin,
                authService,
                companyService,
                driveService,
                userManagementService,
                sessionManager,
                onLogout), java.awt.BorderLayout.CENTER);
    }
}

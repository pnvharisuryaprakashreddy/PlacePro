package com.placepro.ui.officer;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.CompanyService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;

import javax.swing.JPanel;

public class OfficerDashboardPanel extends JPanel {

    public OfficerDashboardPanel(PlacementOfficer officer,
                                 AuthService authService,
                                 CompanyService companyService,
                                 DriveService driveService,
                                 SessionManager sessionManager,
                                 Runnable onLogout) {
        setLayout(new java.awt.BorderLayout());
        add(new OfficerConsolePanel(
                officer,
                authService,
                companyService,
                driveService,
                sessionManager,
                onLogout), java.awt.BorderLayout.CENTER);
    }
}

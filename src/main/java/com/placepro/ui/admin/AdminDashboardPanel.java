package com.placepro.ui.admin;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.UiStyles;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel(PlacementOfficer admin, AuthService authService, Runnable onLogout) {
        setLayout(new BorderLayout(16, 16));

        JLabel welcomeLabel = new JLabel(
                "Welcome, " + admin.getFullName() + ", role: ADMIN",
                JLabel.CENTER);
        welcomeLabel.setFont(UiStyles.TITLE_FONT);
        add(welcomeLabel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.add(new LogoutButton(authService, onLogout));
        add(southPanel, BorderLayout.SOUTH);
    }
}

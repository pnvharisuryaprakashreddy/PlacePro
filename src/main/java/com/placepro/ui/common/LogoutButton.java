package com.placepro.ui.common;

import com.placepro.service.auth.AuthService;

import javax.swing.JButton;

public class LogoutButton extends JButton {

    public LogoutButton(AuthService authService, Runnable onLogout) {
        super("Logout");
        addActionListener(event -> {
            authService.logout();
            onLogout.run();
        });
    }
}

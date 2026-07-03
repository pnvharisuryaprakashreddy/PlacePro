package com.placepro.ui.common;

import com.placepro.service.UserRole;
import com.placepro.service.auth.AuthService;
import com.placepro.service.auth.SessionManager;
import com.placepro.util.AppLog;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

/**
 * Logs out the user after prolonged inactivity on shared lab machines.
 * After {@link #IDLE_TIMEOUT_MS} without input, shows a confirmation dialog;
 * if unanswered within {@link #PROMPT_TIMEOUT_MS}, the session ends automatically.
 */
public final class SessionIdleTimeoutManager implements AWTEventListener {

    static final int IDLE_TIMEOUT_MS = 15 * 60 * 1000;
    static final int PROMPT_TIMEOUT_MS = 60 * 1000;
    private static final int CHECK_INTERVAL_MS = 30_000;

    private final java.awt.Window hostWindow;
    private final SessionManager sessionManager;
    private final AuthService authService;
    private final Runnable returnToLogin;

    private volatile long lastActivityMs = System.currentTimeMillis();
    private volatile boolean promptVisible;
    private Timer idleChecker;
    private JDialog promptDialog;

    public SessionIdleTimeoutManager(java.awt.Window hostWindow,
                                     SessionManager sessionManager,
                                     AuthService authService,
                                     Runnable returnToLogin) {
        this.hostWindow = hostWindow;
        this.sessionManager = sessionManager;
        this.authService = authService;
        this.returnToLogin = returnToLogin;
    }

    public void start() {
        Toolkit.getDefaultToolkit().addAWTEventListener(
                this,
                AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        idleChecker = new Timer(CHECK_INTERVAL_MS, event -> checkIdle());
        idleChecker.start();
    }

    public void stop() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if (idleChecker != null) {
            idleChecker.stop();
        }
        dismissPrompt();
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (!sessionManager.isLoggedIn() || promptVisible) {
            return;
        }
        if (event.getID() == WindowEvent.WINDOW_DEACTIVATED) {
            return;
        }
        lastActivityMs = System.currentTimeMillis();
    }

    public void resetActivityClock() {
        lastActivityMs = System.currentTimeMillis();
    }

    private void checkIdle() {
        if (!sessionManager.isLoggedIn() || promptVisible) {
            return;
        }
        if (System.currentTimeMillis() - lastActivityMs >= IDLE_TIMEOUT_MS) {
            SwingUtilities.invokeLater(this::showStillThereDialog);
        }
    }

    private Timer countdownTimer;

    private void showStillThereDialog() {
        if (!sessionManager.isLoggedIn() || promptVisible) {
            return;
        }
        promptVisible = true;

        promptDialog = new JDialog((java.awt.Frame) hostWindow, "PlacePro", true);
        promptDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JLabel messageLabel = new JLabel(
                "Are you still there? Your session will end in 60 seconds.",
                JLabel.CENTER);
        promptDialog.add(messageLabel, BorderLayout.CENTER);

        JButton stayButton = new JButton("Yes, I'm here");
        stayButton.addActionListener(this::stayLoggedIn);
        promptDialog.add(stayButton, BorderLayout.SOUTH);

        promptDialog.setSize(420, 160);
        promptDialog.setLocationRelativeTo(hostWindow);

        int[] secondsRemaining = {PROMPT_TIMEOUT_MS / 1000};
        countdownTimer = new Timer(1000, event -> {
            secondsRemaining[0]--;
            messageLabel.setText("Are you still there? Your session will end in "
                    + secondsRemaining[0] + " seconds.");
            if (secondsRemaining[0] <= 0) {
                countdownTimer.stop();
                performIdleLogout();
            }
        });
        countdownTimer.start();

        promptDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                if (countdownTimer != null) {
                    countdownTimer.stop();
                }
            }
        });

        promptDialog.setVisible(true);
    }

    private void stayLoggedIn(ActionEvent event) {
        resetActivityClock();
        dismissPrompt();
    }

    private void performIdleLogout() {
        if (!sessionManager.isLoggedIn()) {
            dismissPrompt();
            return;
        }
        UserRole role = sessionManager.getCurrentRole().orElse(null);
        dismissPrompt();
        authService.logout();
        AppLog.sessionIdleLogout(role == null ? "UNKNOWN" : role.name());
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    hostWindow,
                    "Your session ended due to inactivity.",
                    "PlacePro",
                    JOptionPane.INFORMATION_MESSAGE);
            returnToLogin.run();
        });
    }

    private void dismissPrompt() {
        promptVisible = false;
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        if (promptDialog != null) {
            promptDialog.dispose();
            promptDialog = null;
        }
    }
}

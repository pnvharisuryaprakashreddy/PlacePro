package com.placepro.ui.login;

import com.placepro.service.auth.AuthService;
import com.placepro.service.ServiceException;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

abstract class BaseLoginPanel<T> extends JPanel {

    protected final AuthService authService;
    protected final LoginNavigator navigator;
    protected final JTextField emailField;
    protected final JPasswordField passwordField;
    protected final JLabel errorLabel;
    private final JButton loginButton;

    BaseLoginPanel(String title, AuthService authService, LoginNavigator navigator) {
        this.authService = authService;
        this.navigator = navigator;

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiStyles.TITLE_FONT);
        add(titleLabel, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        add(new JLabel("Email"), constraints);

        constraints.gridx = 1;
        emailField = new JTextField(24);
        add(emailField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        add(new JLabel("Password"), constraints);

        constraints.gridx = 1;
        passwordField = new JPasswordField(24);
        add(passwordField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        errorLabel = UiStyles.createErrorLabel();
        add(errorLabel, constraints);

        constraints.gridy++;
        loginButton = new JButton("Login");
        loginButton.addActionListener(event -> attemptLogin());
        add(loginButton, constraints);

        constraints.gridy++;
        add(UiStyles.createLinkLabel("Back to role selection", navigator::showSelection), constraints);

        addRoleSpecificLinks(constraints);
    }

    protected void addRoleSpecificLinks(GridBagConstraints constraints) {
    }

    private void attemptLogin() {
        clearError();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        loginButton.setEnabled(false);
        UiTasks.run(
                () -> authenticate(email, password),
                principal -> {
                    loginButton.setEnabled(true);
                    onLoginSuccess(principal);
                },
                exception -> {
                    loginButton.setEnabled(true);
                    showError(extractMessage(exception));
                });
    }

    /** Runs on a background thread; must not touch Swing components. */
    protected abstract T authenticate(String email, String password);

    /** Runs on the EDT after a successful login. */
    protected abstract void onLoginSuccess(T principal);

    private String extractMessage(Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        if (cause instanceof ServiceException && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return "Login failed, please try again.";
    }

    protected void clearError() {
        errorLabel.setText(" ");
    }

    protected void showError(String message) {
        errorLabel.setText(message);
    }
}

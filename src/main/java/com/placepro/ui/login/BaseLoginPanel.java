package com.placepro.ui.login;

import com.placepro.service.auth.AuthService;
import com.placepro.service.ServiceException;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

abstract class BaseLoginPanel<T> extends JPanel {

    protected final AuthService authService;
    protected final LoginNavigator navigator;
    protected final JTextField emailField;
    protected final JPasswordField passwordField;
    protected final JLabel errorLabel;
    protected final JPanel formCard;
    private final JButton loginButton;

    BaseLoginPanel(String title, AuthService authService, LoginNavigator navigator) {
        this.authService = authService;
        this.navigator = navigator;
        setLayout(new GridBagLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);

        formCard = new UiStyles.RoundedPanel(20, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(440, 480));
        formCard.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 6, 8, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;

        JLabel titleLabel = UiStyles.createTitleLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        formCard.add(titleLabel, constraints);

        constraints.gridy++;
        JLabel subtitleLabel = UiStyles.createMutedLabel("Sign in to your enterprise workspace");
        formCard.add(subtitleLabel, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(16, 6, 4, 6);
        JLabel emailLabel = new JLabel("Email Address");
        UiStyles.styleLabel(emailLabel);
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formCard.add(emailLabel, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 6, 8, 6);
        emailField = new JTextField(24);
        UiStyles.styleInput(emailField);
        formCard.add(emailField, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 6, 4, 6);
        JLabel passwordLabel = new JLabel("Password");
        UiStyles.styleLabel(passwordLabel);
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formCard.add(passwordLabel, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 6, 8, 6);
        passwordField = new JPasswordField(24);
        UiStyles.styleInput(passwordField);
        formCard.add(passwordField, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(4, 6, 8, 6);
        errorLabel = UiStyles.createErrorLabel();
        formCard.add(errorLabel, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 6, 12, 6);
        loginButton = new JButton("Sign In");
        UiStyles.stylePrimaryButton(loginButton);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(0, 42));
        loginButton.addActionListener(event -> attemptLogin());
        formCard.add(loginButton, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(4, 6, 4, 6);
        formCard.add(UiStyles.createLinkLabel("← Return to Role Selection", navigator::showSelection), constraints);

        addRoleSpecificLinks(constraints);

        add(formCard);
    }

    protected void addRoleSpecificLinks(GridBagConstraints constraints) {
    }

    protected void addToForm(Component component, GridBagConstraints constraints) {
        formCard.add(component, constraints);
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
        return "Login failed, please check credentials.";
    }

    protected void clearError() {
        errorLabel.setText(" ");
    }

    protected void showError(String message) {
        errorLabel.setText("⚠️ " + message);
    }
}

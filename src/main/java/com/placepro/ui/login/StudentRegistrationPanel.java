package com.placepro.ui.login;

import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StudentRegistrationPanel extends JPanel {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    private final AuthService authService;
    private final LoginNavigator navigator;

    private final JTextField nameField = new JTextField(18);
    private final JTextField rollNumberField = new JTextField(18);
    private final JTextField branchField = new JTextField(18);
    private final JTextField cgpaField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmPasswordField = new JPasswordField(18);
    private final JLabel generalErrorLabel = UiStyles.createErrorLabel();
    private final Map<String, JLabel> fieldErrors = new HashMap<>();

    public StudentRegistrationPanel(AuthService authService, LoginNavigator navigator) {
        this.authService = authService;
        this.navigator = navigator;
        buildLayout();
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);

        JPanel card = new UiStyles.RoundedPanel(20, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        card.setPreferredSize(new Dimension(640, 540));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;

        JLabel titleLabel = new JLabel("Student Registration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UiStyles.TEXT_COLOR);
        card.add(titleLabel, constraints);

        constraints.gridy++;
        JLabel subLabel = UiStyles.createMutedLabel("Create your placement profile to start applying for corporate drives.");
        card.add(subLabel, constraints);

        constraints.gridy++;
        generalErrorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(generalErrorLabel, constraints);

        // 2-Column form grid
        int y = constraints.gridy + 1;

        addFormField(card, "Full Name", nameField, "name", 0, y);
        addFormField(card, "Roll Number", rollNumberField, "rollNumber", 2, y);

        y += 2;
        addFormField(card, "Branch / Dept", branchField, "branch", 0, y);
        addFormField(card, "Current CGPA (0-10)", cgpaField, "cgpa", 2, y);

        y += 2;
        addFormField(card, "Email Address", emailField, "email", 0, y);
        addFormField(card, "Phone Number", phoneField, "phone", 2, y);

        y += 2;
        addFormField(card, "Account Password", passwordField, "password", 0, y);
        addFormField(card, "Confirm Password", confirmPasswordField, "confirmPassword", 2, y);

        y += 2;
        constraints.gridy = y;
        constraints.gridx = 0;
        constraints.gridwidth = 4;
        constraints.insets = new Insets(16, 6, 8, 6);

        JButton registerButton = UiStyles.stylePrimaryButton(new JButton("Complete Registration & Proceed →"));
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(0, 42));
        registerButton.addActionListener(event -> attemptRegistration());
        card.add(registerButton, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(4, 6, 4, 6);
        JPanel linksPanel = new JPanel();
        linksPanel.setOpaque(false);
        linksPanel.add(UiStyles.createLinkLabel("Already registered? Login here", navigator::showStudentLogin));
        linksPanel.add(new JLabel("  •  "));
        linksPanel.add(UiStyles.createLinkLabel("Return to Role Selection", navigator::showSelection));
        card.add(linksPanel, constraints);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(card);
    }

    private void addFormField(JPanel parent, String labelText, JTextField field, String key, int col, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 6, 2, 6);
        c.gridx = col;
        c.gridy = row;
        c.gridwidth = 2;

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UiStyles.TEXT_COLOR);
        parent.add(lbl, c);

        c.gridy = row + 1;
        UiStyles.styleInput(field);
        parent.add(field, c);

        c.gridy = row + 1; // place error label right below
        JLabel err = UiStyles.createErrorLabel();
        err.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fieldErrors.put(key, err);
    }

    private void attemptRegistration() {
        clearFieldErrors();
        generalErrorLabel.setText(" ");

        String name = nameField.getText().trim();
        String rollNumber = rollNumberField.getText().trim();
        String branch = branchField.getText().trim();
        String cgpaText = cgpaField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        boolean valid = true;
        valid &= requireNonEmpty("name", name, "Full name is required.");
        valid &= requireNonEmpty("rollNumber", rollNumber, "Roll number is required.");
        valid &= requireNonEmpty("branch", branch, "Branch is required.");
        valid &= requireNonEmpty("email", email, "Email is required.");
        valid &= requireNonEmpty("phone", phone, "Phone number is required.");
        valid &= requireNonEmpty("password", password, "Password is required.");
        valid &= requireNonEmpty("confirmPassword", confirmPassword, "Please confirm password.");

        BigDecimal cgpa = null;
        if (cgpaText.isEmpty()) {
            setFieldError("cgpa", "CGPA is required.");
            valid = false;
        } else {
            try {
                cgpa = new BigDecimal(cgpaText);
                if (cgpa.compareTo(BigDecimal.ZERO) < 0 || cgpa.compareTo(new BigDecimal("10.00")) > 0) {
                    setFieldError("cgpa", "CGPA must be 0.00 - 10.00.");
                    valid = false;
                }
            } catch (NumberFormatException exception) {
                setFieldError("cgpa", "Invalid number.");
                valid = false;
            }
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            setFieldError("email", "Invalid email address.");
            valid = false;
        }

        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            setFieldError("phone", "10-15 digits required.");
            valid = false;
        }

        if (!password.isEmpty() && password.length() < 8) {
            setFieldError("password", "Min 8 characters.");
            valid = false;
        }

        if (!password.isEmpty() && !confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            setFieldError("confirmPassword", "Passwords do not match.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        Student student = new Student();
        student.setFullName(name);
        student.setRollNumber(rollNumber);
        student.setBranch(branch);
        student.setCgpa(cgpa);
        student.setEmail(email);
        student.setPhone(phone);
        student.setBacklogCount(0);
        student.setGraduationYear(Year.now().getValue() + 1);
        student.setIsActive(true);

        UiTasks.run(
                () -> authService.registerStudent(student, password),
                registeredStudent -> navigator.showStudentDashboard(registeredStudent),
                exception -> {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    if (cause instanceof ServiceException && cause.getMessage() != null) {
                        generalErrorLabel.setText("⚠️ " + cause.getMessage());
                    } else {
                        generalErrorLabel.setText("⚠️ Registration failed, please try again.");
                    }
                });
    }

    private boolean requireNonEmpty(String key, String value, String message) {
        if (value.isEmpty()) {
            setFieldError(key, message);
            return false;
        }
        return true;
    }

    private void setFieldError(String key, String message) {
        JLabel label = fieldErrors.get(key);
        if (label != null) {
            label.setText(message);
        }
    }

    private void clearFieldErrors() {
        for (JLabel label : fieldErrors.values()) {
            label.setText(" ");
        }
    }
}

package com.placepro.ui.login;

import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.common.UiStyles;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
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

    private final JTextField nameField = new JTextField(24);
    private final JTextField rollNumberField = new JTextField(24);
    private final JTextField branchField = new JTextField(24);
    private final JTextField cgpaField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);
    private final JPasswordField confirmPasswordField = new JPasswordField(24);
    private final JLabel generalErrorLabel = UiStyles.createErrorLabel();
    private final Map<String, JLabel> fieldErrors = new HashMap<>();

    public StudentRegistrationPanel(AuthService authService, LoginNavigator navigator) {
        this.authService = authService;
        this.navigator = navigator;
        buildLayout();
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 8, 6, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;

        JLabel titleLabel = new JLabel("Student Registration");
        titleLabel.setFont(UiStyles.TITLE_FONT);
        add(titleLabel, constraints);

        constraints.gridy++;
        add(generalErrorLabel, constraints);

        addField(constraints, "Full Name", nameField, "name");
        addField(constraints, "Roll Number", rollNumberField, "rollNumber");
        addField(constraints, "Branch", branchField, "branch");
        addField(constraints, "CGPA", cgpaField, "cgpa");
        addField(constraints, "Email", emailField, "email");
        addField(constraints, "Phone", phoneField, "phone");
        addField(constraints, "Password", passwordField, "password");
        addField(constraints, "Confirm Password", confirmPasswordField, "confirmPassword");

        constraints.gridy++;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(event -> attemptRegistration());
        add(registerButton, constraints);

        constraints.gridy++;
        add(UiStyles.createLinkLabel("Already registered? Back to student login", navigator::showStudentLogin), constraints);

        constraints.gridy++;
        add(UiStyles.createLinkLabel("Back to role selection", navigator::showSelection), constraints);
    }

    private void addField(GridBagConstraints constraints, String label, JTextField field, String key) {
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        add(new JLabel(label), constraints);

        constraints.gridx = 1;
        add(field, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        JLabel errorLabel = UiStyles.createErrorLabel();
        fieldErrors.put(key, errorLabel);
        add(errorLabel, constraints);
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
        valid &= requireNonEmpty("confirmPassword", confirmPassword, "Please confirm your password.");

        BigDecimal cgpa = null;
        if (cgpaText.isEmpty()) {
            setFieldError("cgpa", "CGPA is required.");
            valid = false;
        } else {
            try {
                cgpa = new BigDecimal(cgpaText);
                if (cgpa.compareTo(BigDecimal.ZERO) < 0 || cgpa.compareTo(new BigDecimal("10.00")) > 0) {
                    setFieldError("cgpa", "CGPA must be between 0.00 and 10.00.");
                    valid = false;
                }
            } catch (NumberFormatException exception) {
                setFieldError("cgpa", "CGPA must be a valid number.");
                valid = false;
            }
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            setFieldError("email", "Enter a valid email address.");
            valid = false;
        }

        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            setFieldError("phone", "Phone number must contain 10 to 15 digits.");
            valid = false;
        }

        if (!password.isEmpty() && password.length() < 8) {
            setFieldError("password", "Password must be at least 8 characters.");
            valid = false;
        }

        if (!password.isEmpty() && !confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            setFieldError("confirmPassword", "Passwords do not match.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        try {
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

            Student registeredStudent = authService.registerStudent(student, password);
            navigator.showStudentDashboard(registeredStudent);
        } catch (ServiceException exception) {
            generalErrorLabel.setText(exception.getMessage());
        }
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

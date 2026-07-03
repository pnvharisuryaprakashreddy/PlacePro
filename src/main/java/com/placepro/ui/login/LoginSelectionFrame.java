package com.placepro.ui.login;

import com.placepro.model.PlacementOfficer;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.admin.AdminDashboardPanel;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.officer.OfficerDashboardPanel;
import com.placepro.ui.officer.RecruiterDashboardPanel;
import com.placepro.ui.student.StudentDashboardPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

public class LoginSelectionFrame extends JFrame implements LoginNavigator {

    public static final String CARD_SELECTION = "selection";
    public static final String CARD_STUDENT_LOGIN = "student_login";
    public static final String CARD_STUDENT_REGISTER = "student_register";
    public static final String CARD_OFFICER_LOGIN = "officer_login";
    public static final String CARD_ADMIN_LOGIN = "admin_login";
    public static final String CARD_RECRUITER_LOGIN = "recruiter_login";
    public static final String CARD_STUDENT_DASHBOARD = "student_dashboard";
    public static final String CARD_OFFICER_DASHBOARD = "officer_dashboard";
    public static final String CARD_ADMIN_DASHBOARD = "admin_dashboard";
    public static final String CARD_RECRUITER_DASHBOARD = "recruiter_dashboard";

    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Map<String, JPanel> dashboardCards = new HashMap<>();

    public LoginSelectionFrame(AuthService authService) {
        super("PlacePro");
        this.authService = authService;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        cardPanel.add(buildSelectionPanel(), CARD_SELECTION);
        cardPanel.add(new StudentLoginPanel(authService, this), CARD_STUDENT_LOGIN);
        cardPanel.add(new StudentRegistrationPanel(authService, this), CARD_STUDENT_REGISTER);
        cardPanel.add(new OfficerLoginPanel(authService, this), CARD_OFFICER_LOGIN);
        cardPanel.add(new AdminLoginPanel(authService, this), CARD_ADMIN_LOGIN);
        cardPanel.add(new RecruiterLoginPanel(authService, this), CARD_RECRUITER_LOGIN);

        setContentPane(cardPanel);
        setSize(720, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        showSelection();
    }

    private JPanel buildSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));

        JLabel titleLabel = new JLabel("PlacePro", JLabel.CENTER);
        titleLabel.setFont(UiStyles.TITLE_FONT);
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel("Select your role to continue", JLabel.CENTER);
        panel.add(subtitleLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 12, 12));
        JButton studentButton = new JButton("Student");
        studentButton.addActionListener(event -> showStudentLogin());
        JButton officerButton = new JButton("Placement Officer");
        officerButton.addActionListener(event -> showOfficerLogin());
        JButton adminButton = new JButton("Admin");
        adminButton.addActionListener(event -> showAdminLogin());
        JButton recruiterButton = new JButton("Recruiter");
        recruiterButton.addActionListener(event -> showRecruiterLogin());

        buttonPanel.add(studentButton);
        buttonPanel.add(officerButton);
        buttonPanel.add(adminButton);
        buttonPanel.add(recruiterButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void showSelection() {
        cardLayout.show(cardPanel, CARD_SELECTION);
    }

    @Override
    public void showStudentLogin() {
        cardLayout.show(cardPanel, CARD_STUDENT_LOGIN);
    }

    @Override
    public void showStudentRegistration() {
        cardLayout.show(cardPanel, CARD_STUDENT_REGISTER);
    }

    @Override
    public void showOfficerLogin() {
        cardLayout.show(cardPanel, CARD_OFFICER_LOGIN);
    }

    @Override
    public void showAdminLogin() {
        cardLayout.show(cardPanel, CARD_ADMIN_LOGIN);
    }

    @Override
    public void showRecruiterLogin() {
        cardLayout.show(cardPanel, CARD_RECRUITER_LOGIN);
    }

    @Override
    public void showStudentDashboard(Student student) {
        showDashboard(
                CARD_STUDENT_DASHBOARD,
                new StudentDashboardPanel(student, authService, this::showSelection));
    }

    @Override
    public void showOfficerDashboard(PlacementOfficer officer) {
        showDashboard(
                CARD_OFFICER_DASHBOARD,
                new OfficerDashboardPanel(officer, authService, this::showSelection));
    }

    @Override
    public void showAdminDashboard(PlacementOfficer admin) {
        showDashboard(
                CARD_ADMIN_DASHBOARD,
                new AdminDashboardPanel(admin, authService, this::showSelection));
    }

    @Override
    public void showRecruiterDashboard(Recruiter recruiter) {
        showDashboard(
                CARD_RECRUITER_DASHBOARD,
                new RecruiterDashboardPanel(recruiter, authService, this::showSelection));
    }

    private void showDashboard(String cardName, JPanel dashboardPanel) {
        JPanel existing = dashboardCards.get(cardName);
        if (existing != null) {
            cardPanel.remove(existing);
        }
        dashboardCards.put(cardName, dashboardPanel);
        cardPanel.add(dashboardPanel, cardName);
        cardLayout.show(cardPanel, cardName);
        cardPanel.revalidate();
        cardPanel.repaint();
    }
}

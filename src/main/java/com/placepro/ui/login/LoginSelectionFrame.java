package com.placepro.ui.login;

import com.placepro.model.PlacementOfficer;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.ui.AppContext;
import com.placepro.ui.admin.AdminDashboardPanel;
import com.placepro.ui.common.SessionIdleTimeoutManager;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.officer.OfficerDashboardPanel;
import com.placepro.ui.recruiter.RecruiterDashboardPanel;
import com.placepro.ui.student.StudentDashboardPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Main Frame & Role Selection Portal.
 */
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
    private final SessionIdleTimeoutManager idleTimeoutManager;

    public LoginSelectionFrame(AuthService authService) {
        super("PlacePro Enterprise - Campus Placement Platform");
        this.authService = authService;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.idleTimeoutManager = new SessionIdleTimeoutManager(
                this,
                AppContext.getSessionManager(),
                authService,
                this::returnToLoginAfterIdleTimeout);

        cardPanel.add(buildSelectionPanel(), CARD_SELECTION);
        cardPanel.add(new StudentLoginPanel(authService, this), CARD_STUDENT_LOGIN);
        cardPanel.add(new StudentRegistrationPanel(authService, this), CARD_STUDENT_REGISTER);
        cardPanel.add(new OfficerLoginPanel(authService, this), CARD_OFFICER_LOGIN);
        cardPanel.add(new AdminLoginPanel(authService, this), CARD_ADMIN_LOGIN);
        cardPanel.add(new RecruiterLoginPanel(authService, this), CARD_RECRUITER_LOGIN);

        setContentPane(cardPanel);
        setSize(960, 680);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        idleTimeoutManager.start();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                idleTimeoutManager.stop();
            }
        });
        showSelection();
    }

    private JPanel buildSelectionPanel() {
        JPanel main = UiStyles.createPagePanel();
        main.setLayout(new BorderLayout());

        // Hero Banner Header
        JPanel heroHeader = new UiStyles.DarkHeaderPanel();
        heroHeader.setLayout(new BorderLayout());
        heroHeader.setBorder(BorderFactory.createEmptyBorder(36, 48, 36, 48));

        JPanel brandTitleGroup = new JPanel(new GridLayout(2, 1, 0, 8));
        brandTitleGroup.setOpaque(false);
        JLabel titleLabel = new JLabel("PlacePro Enterprise");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Unified Placement & Campus Recruitment Intelligence Portal");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(203, 213, 225)); // Slate 300

        brandTitleGroup.add(titleLabel);
        brandTitleGroup.add(subtitleLabel);
        heroHeader.add(brandTitleGroup, BorderLayout.WEST);

        JLabel badge = new JLabel("● SYSTEM ACTIVE");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(UiStyles.SUCCESS_COLOR);
        heroHeader.add(badge, BorderLayout.EAST);

        main.add(heroHeader, BorderLayout.NORTH);

        // Role Cards Container
        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(UiStyles.BACKGROUND_COLOR);
        cardsWrapper.setBorder(BorderFactory.createEmptyBorder(36, 48, 36, 48));

        JLabel selectPrompt = new JLabel("Select your workspace role to log in");
        selectPrompt.setFont(UiStyles.SECTION_FONT);
        selectPrompt.setForeground(UiStyles.TEXT_COLOR);
        selectPrompt.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        cardsWrapper.add(selectPrompt, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(UiStyles.BACKGROUND_COLOR);

        grid.add(createRoleCard("Student Portal", "🎓", "Apply for drives, upload resumes, and track interviews.", UiStyles.PRIMARY_COLOR, this::showStudentLogin));
        grid.add(createRoleCard("Placement Officer", "🏛️", "Manage campus drives, approvals, and student records.", new Color(13, 148, 136), this::showOfficerLogin));
        grid.add(createRoleCard("System Admin", "⚡", "Configure users, system metrics, and live analytics.", new Color(124, 58, 237), this::showAdminLogin));
        grid.add(createRoleCard("Corporate Recruiter", "💼", "Post job profiles, shortlist candidates, & schedule interviews.", new Color(225, 29, 72), this::showRecruiterLogin));

        cardsWrapper.add(grid, BorderLayout.CENTER);
        main.add(cardsWrapper, BorderLayout.CENTER);

        return main;
    }

    private JPanel createRoleCard(String roleTitle, String iconSymbol, String description, Color accentColor, Runnable onSelect) {
        RoleCardPanel card = new RoleCardPanel(accentColor);
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel icon = new JLabel(iconSymbol);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        JLabel title = new JLabel(roleTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UiStyles.TEXT_COLOR);
        top.add(icon, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JLabel desc = new JLabel("<html><body style='width: 180px'>" + description + "</body></html>");
        desc.setFont(UiStyles.BODY_FONT);
        desc.setForeground(UiStyles.MUTED_TEXT_COLOR);

        JButton actionButton = UiStyles.stylePrimaryButton(new JButton("Enter Portal →"));
        actionButton.addActionListener(e -> onSelect.run());

        card.add(top, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        card.add(actionButton, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.run();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setHover(true);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setHover(false);
            }
        });

        return card;
    }

    private static class RoleCardPanel extends JPanel {
        private final Color accentColor;
        private boolean isHover = false;

        public RoleCardPanel(Color accent) {
            this.accentColor = accent;
            setOpaque(false);
        }

        public void setHover(boolean hover) {
            this.isHover = hover;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(isHover ? UiStyles.PRIMARY_SOFT_COLOR : UiStyles.SURFACE_COLOR);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

            // Top accent bar
            g2.setColor(accentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, 6, 16, 16));

            // Border outline
            g2.setColor(isHover ? accentColor : UiStyles.BORDER_COLOR);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 16, 16));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    @Override
    public void showSelection() {
        idleTimeoutManager.resetActivityClock();
        cardLayout.show(cardPanel, CARD_SELECTION);
    }

    private void returnToLoginAfterIdleTimeout() {
        dashboardCards.values().forEach(cardPanel::remove);
        dashboardCards.clear();
        cardLayout.show(cardPanel, CARD_SELECTION);
        cardPanel.revalidate();
        cardPanel.repaint();
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
                new OfficerDashboardPanel(
                        officer,
                        authService,
                        AppContext.getCompanyService(),
                        AppContext.getDriveService(),
                        AppContext.getSessionManager(),
                        this::showSelection));
    }

    @Override
    public void showAdminDashboard(PlacementOfficer admin) {
        showDashboard(
                CARD_ADMIN_DASHBOARD,
                new AdminDashboardPanel(
                        admin,
                        authService,
                        AppContext.getCompanyService(),
                        AppContext.getDriveService(),
                        AppContext.getUserManagementService(),
                        AppContext.getSessionManager(),
                        this::showSelection));
    }

    @Override
    public void showRecruiterDashboard(Recruiter recruiter) {
        showDashboard(
                CARD_RECRUITER_DASHBOARD,
                new RecruiterDashboardPanel(recruiter, authService, this::showSelection));
    }

    private void showDashboard(String cardName, JPanel dashboardPanel) {
        idleTimeoutManager.resetActivityClock();
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

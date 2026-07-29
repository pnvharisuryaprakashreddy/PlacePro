package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDashboardSummary;
import com.placepro.service.student.StudentDriveSummary;
import com.placepro.ui.AppContext;
import com.placepro.ui.common.ApplicationStatusRenderer;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.NotificationBellComponent;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

public class StudentHomePanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final DashboardService dashboardService;

    private final JLabel activeDrivesValue = createValueLabel();
    private final JLabel upcomingDeadlinesValue = createValueLabel();
    private final JLabel appliedValue = createValueLabel();
    private final JLabel shortlistedValue = createValueLabel();
    private final JLabel interviewsValue = createValueLabel();
    private final JLabel selectedValue = createValueLabel();
    private final JLabel statusLabel = UiStyles.createStatusLabel();

    private final java.util.List<StudentDriveSummary> recentDrives = new java.util.ArrayList<>();
    private final DefaultTableModel recentDrivesModel = new DefaultTableModel(
            new String[]{"Company", "Job Title", "Deadline", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable recentDrivesTable = new JTable(recentDrivesModel);

    public StudentHomePanel(Student student,
                            StudentNavigator navigator,
                            AuthService authService,
                            DashboardService dashboardService,
                            Runnable onLogout) {
        this.student = student;
        this.navigator = navigator;
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);
        buildLayout(authService, onLogout);
        loadDashboard();
    }

    public void refresh() {
        loadDashboard();
    }

    private void buildLayout(AuthService authService, Runnable onLogout) {
        // Dark Slate Top Header
        JPanel header = new UiStyles.DarkHeaderPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 4));
        titleGroup.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Welcome back, " + student.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Branch: " + student.getBranch() + "  |  CGPA: " + student.getCgpa() + "  |  Roll No: " + student.getRollNumber());
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(203, 213, 225));
        titleGroup.add(welcomeLabel);
        titleGroup.add(subtitleLabel);
        header.add(titleGroup, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerActions.setOpaque(false);
        headerActions.add(new NotificationBellComponent(AppContext.getNotificationService()));
        headerActions.add(new LogoutButton(authService, onLogout));
        header.add(headerActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center Content Body
        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setBackground(UiStyles.BACKGROUND_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // KPI Stat Cards Grid (2x3)
        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setBackground(UiStyles.BACKGROUND_COLOR);
        cards.add(createKpiCard("Active Drives", activeDrivesValue, "⚡ Currently open for registration", UiStyles.PRIMARY_COLOR));
        cards.add(createKpiCard("Upcoming Deadlines", upcomingDeadlinesValue, "⏳ Closing within 48 hours", UiStyles.WARNING_COLOR));
        cards.add(createKpiCard("Total Applied", appliedValue, "📄 Submitted drive applications", new Color(14, 165, 233)));
        cards.add(createKpiCard("Shortlisted", shortlistedValue, "⭐ Advanced to technical evaluation", new Color(168, 85, 247)));
        cards.add(createKpiCard("Interviews Scheduled", interviewsValue, "📅 Confirmed interview slots", new Color(234, 88, 12)));
        cards.add(createKpiCard("Selected / Offers", selectedValue, "🎉 Offers received!", UiStyles.SUCCESS_COLOR));
        center.add(cards, BorderLayout.NORTH);

        // Recent Drives Table Card
        JPanel tableCard = new UiStyles.RoundedPanel(16, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        tableCard.setLayout(new BorderLayout(14, 14));
        tableCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel sectionTitle = new JLabel("Recent Published Corporate Drives");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionTitle.setForeground(UiStyles.TEXT_COLOR);

        JLabel hint = UiStyles.createMutedLabel("💡 Double-click any drive to view eligibility & application details.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        tableHeader.add(sectionTitle, BorderLayout.WEST);
        tableHeader.add(hint, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        recentDrivesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        recentDrivesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    openSelectedRecentDrive();
                }
            }
        });
        UiStyles.styleTable(recentDrivesTable);
        recentDrivesTable.getColumnModel().getColumn(3).setCellRenderer(new ApplicationStatusRenderer());

        JScrollPane scrollPane = UiStyles.createScrollPane(recentDrivesTable);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        center.add(tableCard, BorderLayout.CENTER);

        // Bottom Navigation Toolbar
        JPanel footerShell = new JPanel(new BorderLayout(16, 16));
        footerShell.setOpaque(false);

        JPanel quickNav = new UiStyles.RoundedPanel(12, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        quickNav.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));

        JButton browseButton = UiStyles.stylePrimaryButton(new JButton("🔍 Browse Open Drives"));
        browseButton.addActionListener(event -> navigator.showBrowseDrives());

        JButton applicationsButton = UiStyles.styleSecondaryButton(new JButton("📋 My Applications Tracker"));
        applicationsButton.addActionListener(event -> navigator.showMyApplications());

        JButton profileButton = UiStyles.styleSecondaryButton(new JButton("📄 Profile / Resume Vault"));
        profileButton.addActionListener(event -> navigator.showProfileResume());

        quickNav.add(browseButton);
        quickNav.add(applicationsButton);
        quickNav.add(profileButton);

        footerShell.add(quickNav, BorderLayout.WEST);

        JPanel statusPanel = new UiStyles.RoundedPanel(12, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        statusPanel.add(statusLabel);
        footerShell.add(statusPanel, BorderLayout.CENTER);

        center.add(footerShell, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtext, Color accentColor) {
        JPanel card = new UiStyles.RoundedPanel(14, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Top accent border bar
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 5, 14, 14));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(4, 6));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(UiStyles.MUTED_TEXT_COLOR);

        JLabel hint = new JLabel(subtext);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(UiStyles.MUTED_TEXT_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    private static JLabel createValueLabel() {
        JLabel label = new JLabel("-", SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(UiStyles.TEXT_COLOR);
        return label;
    }

    private void loadDashboard() {
        statusLabel.setText("Refreshing dashboard...");
        UiTasks.run(
                () -> dashboardService.getSummary(student.getStudentId()),
                this::populateSummary,
                exception -> statusLabel.setText("⚠️ Unable to load dashboard."));
    }

    private void populateSummary(StudentDashboardSummary summary) {
        activeDrivesValue.setText(String.valueOf(summary.getActiveDrivesCount()));
        upcomingDeadlinesValue.setText(String.valueOf(summary.getUpcomingDeadlinesCount()));

        Map<String, Integer> counts = summary.getApplicationCountsByStatus();
        appliedValue.setText(String.valueOf(counts.getOrDefault("APPLIED", 0)));
        shortlistedValue.setText(String.valueOf(counts.getOrDefault("SHORTLISTED", 0)));
        interviewsValue.setText(String.valueOf(counts.getOrDefault("INTERVIEW_SCHEDULED", 0)));
        selectedValue.setText(String.valueOf(counts.getOrDefault("SELECTED", 0)));
        statusLabel.setText("✅ System synced");

        recentDrives.clear();
        recentDrives.addAll(summary.getRecentPublishedDrives());
        recentDrivesModel.setRowCount(0);
        for (StudentDriveSummary driveSummary : summary.getRecentPublishedDrives()) {
            recentDrivesModel.addRow(new Object[]{
                    driveSummary.getCompanyName(),
                    driveSummary.getJobTitle(),
                    driveSummary.getApplicationDeadline(),
                    "ACTIVE"
            });
        }
    }

    private void openSelectedRecentDrive() {
        int viewRow = recentDrivesTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = recentDrivesTable.convertRowIndexToModel(viewRow);
        if (modelRow < recentDrives.size()) {
            navigator.showDriveDetail(recentDrives.get(modelRow));
        }
    }
}

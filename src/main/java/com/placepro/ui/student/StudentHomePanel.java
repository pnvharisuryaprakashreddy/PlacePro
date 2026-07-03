package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.auth.AuthService;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDashboardSummary;
import com.placepro.service.student.StudentDriveSummary;
import com.placepro.ui.common.LogoutButton;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Map;

public class StudentHomePanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final DashboardService dashboardService;

    private final JLabel activeDrivesValue = new JLabel("-");
    private final JLabel upcomingDeadlinesValue = new JLabel("-");
    private final JLabel appliedValue = new JLabel("-");
    private final JLabel shortlistedValue = new JLabel("-");
    private final JLabel interviewsValue = new JLabel("-");
    private final JLabel selectedValue = new JLabel("-");
    private final JLabel statusLabel = new JLabel("Loading dashboard...");
    private final java.util.List<StudentDriveSummary> recentDrives = new java.util.ArrayList<>();
    private final DefaultTableModel recentDrivesModel = new DefaultTableModel(
            new String[]{"Company", "Job Title", "Deadline"}, 0) {
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
        setLayout(new BorderLayout(12, 12));
        buildLayout(authService, onLogout);
        loadDashboard();
    }

    public void refresh() {
        loadDashboard();
    }

    private void buildLayout(AuthService authService, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + student.getFullName());
        welcomeLabel.setFont(UiStyles.TITLE_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(new LogoutButton(authService, onLogout), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(12, 12));

        JPanel cards = new JPanel(new GridLayout(2, 3, 12, 12));
        cards.add(createCard("Active Drives", activeDrivesValue));
        cards.add(createCard("Upcoming Deadlines", upcomingDeadlinesValue));
        cards.add(createCard("Applied", appliedValue));
        cards.add(createCard("Shortlisted", shortlistedValue));
        cards.add(createCard("Interviews", interviewsValue));
        cards.add(createCard("Selected", selectedValue));
        center.add(cards, BorderLayout.NORTH);

        recentDrivesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        recentDrivesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    openSelectedRecentDrive();
                }
            }
        });
        center.add(new JScrollPane(recentDrivesTable), BorderLayout.CENTER);

        JPanel quickNav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton browseButton = new JButton("Browse Drives");
        browseButton.addActionListener(event -> navigator.showBrowseDrives());
        JButton applicationsButton = new JButton("My Applications");
        applicationsButton.addActionListener(event -> navigator.showMyApplications());
        JButton profileButton = new JButton("Profile / Resume");
        profileButton.addActionListener(event -> navigator.showProfileResume());
        quickNav.add(browseButton);
        quickNav.add(applicationsButton);
        quickNav.add(profileButton);
        quickNav.add(statusLabel);
        center.add(quickNav, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        valueLabel.setFont(UiStyles.TITLE_FONT);
        card.add(new JLabel(title), BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadDashboard() {
        statusLabel.setText("Loading...");
        UiTasks.run(
                () -> dashboardService.getSummary(student.getStudentId()),
                this::populateSummary,
                exception -> statusLabel.setText("Unable to load dashboard."));
    }

    private void populateSummary(StudentDashboardSummary summary) {
        activeDrivesValue.setText(String.valueOf(summary.getActiveDrivesCount()));
        upcomingDeadlinesValue.setText(String.valueOf(summary.getUpcomingDeadlinesCount()));

        Map<String, Integer> counts = summary.getApplicationCountsByStatus();
        appliedValue.setText(String.valueOf(counts.getOrDefault("APPLIED", 0)));
        shortlistedValue.setText(String.valueOf(counts.getOrDefault("SHORTLISTED", 0)));
        interviewsValue.setText(String.valueOf(counts.getOrDefault("INTERVIEW_SCHEDULED", 0)));
        selectedValue.setText(String.valueOf(counts.getOrDefault("SELECTED", 0)));
        statusLabel.setText(" ");

        recentDrives.clear();
        recentDrives.addAll(summary.getRecentPublishedDrives());
        recentDrivesModel.setRowCount(0);
        for (StudentDriveSummary driveSummary : summary.getRecentPublishedDrives()) {
            recentDrivesModel.addRow(new Object[]{
                    driveSummary.getCompanyName(),
                    driveSummary.getJobTitle(),
                    driveSummary.getApplicationDeadline()
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

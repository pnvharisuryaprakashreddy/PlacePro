package com.placepro.ui.student;

import com.placepro.model.Application;
import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDriveSummary;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class MyApplicationsPanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final ApplicationService applicationService;
    private final DashboardService dashboardService;
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Ref #", "Company", "Job Title", "Status", "Applied At"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable applicationsTable = new JTable(tableModel);

    public MyApplicationsPanel(Student student,
                               StudentNavigator navigator,
                               ApplicationService applicationService,
                               DashboardService dashboardService) {
        this.student = student;
        this.navigator = navigator;
        this.applicationService = applicationService;
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout(12, 12));
        buildLayout();
        loadApplications();
    }

    public void refresh() {
        loadApplications();
    }

    private void buildLayout() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(event -> navigator.showDashboard());
        header.add(backButton);
        header.add(new JLabel("My Applications"));
        add(header, BorderLayout.NORTH);

        applicationsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(applicationsTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> loadApplications());
        footer.add(refreshButton);
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadApplications() {
        statusLabel.setText("Loading applications...");
        UiTasks.run(
                () -> applicationService.listApplicationsForStudent(student.getStudentId()),
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load applications.");
                });
    }

    private void populateTable(List<Application> applications) {
        tableModel.setRowCount(0);
        List<String> loadErrors = new ArrayList<>();
        for (Application application : applications) {
            String companyName = "Drive #" + application.getDriveId();
            String jobTitle = "-";
            try {
                StudentDriveSummary summary = dashboardService.getPublishedDriveSummary(application.getDriveId());
                companyName = summary.getCompanyName();
                jobTitle = summary.getJobTitle();
            } catch (ServiceException exception) {
                loadErrors.add(application.getDriveId().toString());
            }
            tableModel.addRow(new Object[]{
                    application.getApplicationId(),
                    companyName,
                    jobTitle,
                    application.getStatus(),
                    application.getAppliedAt() == null ? "-" : DateUtil.formatDateTime(application.getAppliedAt())
            });
        }
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        if (loadErrors.isEmpty()) {
            statusLabel.setText(applications.size() + " application(s) found.");
        } else {
            statusLabel.setText(applications.size() + " application(s) found. Some drive details are unavailable.");
        }
    }
}

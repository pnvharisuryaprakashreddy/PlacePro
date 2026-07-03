package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDriveSummary;
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
import java.util.ArrayList;
import java.util.List;

public class DriveBrowsePanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final DashboardService dashboardService;
    private final JLabel statusLabel = new JLabel(" ");
    private final List<StudentDriveSummary> driveList = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Company", "Job Title", "Min CGPA", "Deadline", "Branches"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable driveTable = new JTable(tableModel);

    public DriveBrowsePanel(Student student, StudentNavigator navigator, DashboardService dashboardService) {
        this.student = student;
        this.navigator = navigator;
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout(12, 12));
        buildLayout();
        loadDrives();
    }

    public void refresh() {
        loadDrives();
    }

    private void buildLayout() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(event -> navigator.showDashboard());
        header.add(backButton);
        header.add(new JLabel("Browse Published Drives"));
        add(header, BorderLayout.NORTH);

        driveTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        driveTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    openSelectedDrive();
                }
            }
        });
        add(new JScrollPane(driveTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewButton = new JButton("View Details");
        viewButton.addActionListener(event -> openSelectedDrive());
        footer.add(viewButton);
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadDrives() {
        statusLabel.setText("Loading drives...");
        UiTasks.run(
                dashboardService::listPublishedDrivesForStudent,
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load drives.");
                });
    }

    private void populateTable(List<StudentDriveSummary> drives) {
        driveList.clear();
        driveList.addAll(drives);
        tableModel.setRowCount(0);
        for (StudentDriveSummary summary : drives) {
            tableModel.addRow(new Object[]{
                    summary.getCompanyName(),
                    summary.getJobTitle(),
                    summary.getDrive().getMinCgpa(),
                    summary.getApplicationDeadline(),
                    summary.getDrive().getAllowedBranches()
            });
        }
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText(drives.size() + " published drives available.");
    }

    private void openSelectedDrive() {
        int viewRow = driveTable.getSelectedRow();
        if (viewRow < 0) {
            statusLabel.setText("Select a drive to view details.");
            return;
        }
        int modelRow = driveTable.convertRowIndexToModel(viewRow);
        if (modelRow >= driveList.size()) {
            return;
        }
        navigator.showDriveDetail(driveList.get(modelRow));
    }
}

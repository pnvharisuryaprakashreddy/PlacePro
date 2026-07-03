package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.student.ApplicationTrackingRow;
import com.placepro.service.student.ApplicationTrackingService;
import com.placepro.ui.common.ApplicationStatusRenderer;
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
import java.util.List;

public class ApplicationTrackingPanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final ApplicationTrackingService applicationTrackingService;
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Company", "Role", "Status", "Applied", "Interview Date", "Interview Time", "Venue"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable trackingTable = new JTable(tableModel);

    public ApplicationTrackingPanel(Student student,
                                    StudentNavigator navigator,
                                    ApplicationTrackingService applicationTrackingService) {
        this.student = student;
        this.navigator = navigator;
        this.applicationTrackingService = applicationTrackingService;
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

        trackingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        trackingTable.getColumnModel().getColumn(2).setCellRenderer(new ApplicationStatusRenderer());
        add(new JScrollPane(trackingTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> loadApplications());
        footer.add(refreshButton);
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadApplications() {
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Loading applications...");
        UiTasks.run(
                () -> applicationTrackingService.listTrackingForStudent(student.getStudentId()),
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load applications.");
                });
    }

    private void populateTable(List<ApplicationTrackingRow> rows) {
        tableModel.setRowCount(0);
        for (ApplicationTrackingRow row : rows) {
            tableModel.addRow(new Object[]{
                    row.getCompanyName(),
                    row.getJobTitle(),
                    row.getStatus(),
                    row.getAppliedAt() == null ? "-" : DateUtil.formatDateTime(row.getAppliedAt()),
                    row.getInterviewDate() == null ? "-" : DateUtil.formatDate(row.getInterviewDate()),
                    row.getInterviewTime() == null ? "-" : row.getInterviewTime().toString(),
                    row.getVenue() == null || row.getVenue().isBlank() ? "-" : row.getVenue()
            });
        }
        statusLabel.setText(rows.size() + " application(s) found.");
    }
}

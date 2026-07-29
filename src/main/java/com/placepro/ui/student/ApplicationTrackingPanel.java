package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.student.ApplicationTrackingRow;
import com.placepro.service.student.ApplicationTrackingService;
import com.placepro.ui.common.ApplicationStatusRenderer;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class ApplicationTrackingPanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final ApplicationTrackingService applicationTrackingService;
    private final JLabel statusLabel = UiStyles.createStatusLabel();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Company Name", "Role / Job Title", "Status", "Applied Timestamp", "Interview Date", "Time Slot", "Location / Link"}, 0) {
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
        setLayout(new BorderLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);
        buildLayout();
        loadApplications();
    }

    public void refresh() {
        loadApplications();
    }

    private void buildLayout() {
        // Dark Slate Top Header
        JPanel header = new UiStyles.DarkHeaderPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 14));

        JButton backButton = UiStyles.styleSecondaryButton(new JButton("← Dashboard"));
        backButton.addActionListener(event -> navigator.showDashboard());
        header.add(backButton);

        JLabel title = new JLabel("Application Status & Interview Schedule Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title);

        add(header, BorderLayout.NORTH);

        // Center Content Card
        JPanel card = new UiStyles.RoundedPanel(16, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        trackingTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        UiStyles.styleTable(trackingTable);
        trackingTable.getColumnModel().getColumn(2).setCellRenderer(new ApplicationStatusRenderer());

        JScrollPane scrollPane = UiStyles.createScrollPane(trackingTable);
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(UiStyles.BACKGROUND_COLOR);
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainWrapper.add(card, BorderLayout.CENTER);

        JPanel footer = new UiStyles.RoundedPanel(12, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        footer.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));

        JButton refreshButton = UiStyles.styleSecondaryButton(new JButton("🔄 Sync Status"));
        refreshButton.addActionListener(event -> loadApplications());

        footer.add(refreshButton);
        footer.add(statusLabel);
        mainWrapper.add(footer, BorderLayout.SOUTH);

        add(mainWrapper, BorderLayout.CENTER);
    }

    private void loadApplications() {
        statusLabel.setText("Syncing applications with database...");
        UiTasks.run(
                () -> applicationTrackingService.listTrackingForStudent(student.getStudentId()),
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("⚠️ Unable to load application records.");
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
                    row.getInterviewDate() == null ? "TBD" : DateUtil.formatDate(row.getInterviewDate()),
                    row.getInterviewTime() == null ? "TBD" : row.getInterviewTime().toString(),
                    row.getVenue() == null || row.getVenue().isBlank() ? "To Be Announced" : row.getVenue()
            });
        }
        statusLabel.setForeground(UiStyles.TEXT_COLOR);
        statusLabel.setText("Total " + rows.size() + " active application(s) tracked.");
    }
}

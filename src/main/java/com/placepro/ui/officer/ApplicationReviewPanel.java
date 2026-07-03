package com.placepro.ui.officer;

import com.placepro.model.PlacementDrive;
import com.placepro.model.PlacementOfficer;
import com.placepro.service.ServiceException;
import com.placepro.service.application.ApplicationReviewRow;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.application.ApplicationStatus;
import com.placepro.service.drive.DriveService;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationReviewPanel extends JPanel {

    private final PlacementOfficer officer;
    private final DriveService driveService;
    private final ApplicationService applicationService;
    private final JComboBox<DriveOption> driveComboBox = new JComboBox<>();
    private final JComboBox<ApplicationStatus> bulkStatusComboBox = new JComboBox<>(ApplicationStatus.values());
    private final JLabel statusLabel = new JLabel(" ");
    private final Map<Integer, ApplicationReviewRow> rowByApplicationId = new HashMap<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"App ID", "Student", "CGPA", "Branch", "Resume", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 5;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 5) {
                return ApplicationStatus.class;
            }
            return Object.class;
        }
    };
    private final JTable applicationTable = new JTable(tableModel);

    public ApplicationReviewPanel(PlacementOfficer officer,
                                  DriveService driveService,
                                  ApplicationService applicationService) {
        this.officer = officer;
        this.driveService = driveService;
        this.applicationService = applicationService;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        loadDrives();
    }

    public void refresh() {
        loadDrives();
        loadApplicationsForSelectedDrive();
    }

    private void buildLayout() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Drive:"));
        driveComboBox.addActionListener(event -> loadApplicationsForSelectedDrive());
        top.add(driveComboBox);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());
        top.add(refreshButton);
        add(top, BorderLayout.NORTH);

        applicationTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        applicationTable.getColumnModel().getColumn(5).setCellEditor(
                new javax.swing.DefaultCellEditor(new JComboBox<>(ApplicationStatus.values())));
        add(new JScrollPane(applicationTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openResumeButton = new JButton("Open Resume");
        openResumeButton.addActionListener(event -> openSelectedResume());
        JButton updateSelectedButton = new JButton("Update Selected");
        updateSelectedButton.addActionListener(event -> updateSelectedRows(false));
        JButton bulkUpdateButton = new JButton("Bulk Set Status");
        bulkUpdateButton.addActionListener(event -> updateSelectedRows(true));
        actions.add(openResumeButton);
        actions.add(new JLabel("Bulk status:"));
        actions.add(bulkStatusComboBox);
        actions.add(updateSelectedButton);
        actions.add(bulkUpdateButton);
        actions.add(statusLabel);
        add(actions, BorderLayout.SOUTH);
    }

    private void loadDrives() {
        UiTasks.run(
                () -> driveService.listDrives("ALL"),
                drives -> {
                    driveComboBox.removeAllItems();
                    for (PlacementDrive drive : drives) {
                        driveComboBox.addItem(new DriveOption(drive.getDriveId(), drive.getJobTitle()));
                    }
                    if (driveComboBox.getItemCount() > 0) {
                        loadApplicationsForSelectedDrive();
                    }
                },
                exception -> statusLabel.setText("Unable to load drives."));
    }

    private void loadApplicationsForSelectedDrive() {
        DriveOption selected = (DriveOption) driveComboBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        statusLabel.setText("Loading applications...");
        UiTasks.run(
                () -> applicationService.listApplicationReviewRowsForDrive(selected.driveId),
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load applications.");
                });
    }

    private void populateTable(List<ApplicationReviewRow> rows) {
        rowByApplicationId.clear();
        tableModel.setRowCount(0);
        for (ApplicationReviewRow row : rows) {
            rowByApplicationId.put(row.getApplication().getApplicationId(), row);
            tableModel.addRow(new Object[]{
                    row.getApplication().getApplicationId(),
                    row.getStudentName(),
                    row.getCgpa(),
                    row.getBranch(),
                    row.getResumeFileName(),
                    safeStatus(row.getApplication().getStatus())
            });
        }
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText(rows.size() + " application(s) loaded.");
    }

    private Object safeStatus(String rawStatus) {
        try {
            return ApplicationStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return rawStatus == null ? "-" : rawStatus;
        }
    }

    private void openSelectedResume() {
        ApplicationReviewRow row = getSelectedRow();
        if (row == null) {
            statusLabel.setText("Select an application to open the resume.");
            return;
        }
        if (row.getResumeFilePath() == null || row.getResumeFilePath().isBlank()) {
            JOptionPane.showMessageDialog(this, "No resume is attached to this application.", "PlacePro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (!Files.exists(Paths.get(row.getResumeFilePath()))) {
                JOptionPane.showMessageDialog(this, "Resume file is not available on disk.", "PlacePro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Desktop.getDesktop().open(Paths.get(row.getResumeFilePath()).toFile());
        } catch (IOException exception) {
            UiExceptionHandler.handle(this, exception);
        }
    }

    private void updateSelectedRows(boolean useBulkStatus) {
        int[] selectedRows = applicationTable.getSelectedRows();
        if (selectedRows.length == 0) {
            statusLabel.setText("Select one or more applications to update.");
            return;
        }

        List<Integer> applicationIds = new ArrayList<>();
        if (useBulkStatus) {
            ApplicationStatus bulkStatus = (ApplicationStatus) bulkStatusComboBox.getSelectedItem();
            if (bulkStatus == null) {
                return;
            }
            for (int rowIndex : selectedRows) {
                applicationIds.add((Integer) tableModel.getValueAt(rowIndex, 0));
            }
            ApplicationStatus finalBulkStatus = bulkStatus;
            statusLabel.setText("Updating applications...");
            UiTasks.run(
                    () -> applicationService.updateStatusBulk(applicationIds, finalBulkStatus, officer.getOfficerId()),
                    updated -> {
                        statusLabel.setText(updated.size() + " application(s) updated.");
                        loadApplicationsForSelectedDrive();
                    },
                    exception -> UiExceptionHandler.handleServiceFailure(ApplicationReviewPanel.this, exception));
            return;
        }

        statusLabel.setText("Updating applications...");
        UiTasks.run(
                () -> {
                    for (int rowIndex : selectedRows) {
                        int applicationId = (Integer) tableModel.getValueAt(rowIndex, 0);
                        ApplicationStatus rowStatus = (ApplicationStatus) tableModel.getValueAt(rowIndex, 5);
                        applicationService.updateStatus(applicationId, rowStatus, officer.getOfficerId());
                    }
                    return selectedRows.length;
                },
                count -> {
                    statusLabel.setText(count + " application(s) updated.");
                    loadApplicationsForSelectedDrive();
                },
                exception -> UiExceptionHandler.handleServiceFailure(ApplicationReviewPanel.this, exception));
    }

    private ApplicationReviewRow getSelectedRow() {
        int selectedRow = applicationTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        Integer applicationId = (Integer) tableModel.getValueAt(selectedRow, 0);
        return rowByApplicationId.get(applicationId);
    }

    private static final class DriveOption {
        private final int driveId;
        private final String label;

        private DriveOption(int driveId, String label) {
            this.driveId = driveId;
            this.label = label;
        }

        @Override
        public String toString() {
            return label + " (#" + driveId + ")";
        }
    }
}

package com.placepro.ui.officer;

import com.placepro.model.Company;
import com.placepro.model.PlacementDrive;
import com.placepro.service.CompanyService;
import com.placepro.service.ServiceException;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriveListPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {
            "ID", "Company", "Job Title", "Status", "Deadline", "Min CGPA"
    };

    private final DriveService driveService;
    private final CompanyService companyService;
    private final SessionManager sessionManager;
    private final JComboBox<String> statusFilterCombo = new JComboBox<>(new String[]{
            "ALL", "DRAFT", "PUBLISHED", "CLOSED", "COMPLETED"
    });
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable driveTable = new JTable(tableModel);
    private final Map<Integer, PlacementDrive> driveById = new HashMap<>();
    private final Map<Integer, String> companyNames = new HashMap<>();

    public DriveListPanel(DriveService driveService, CompanyService companyService, SessionManager sessionManager) {
        this.driveService = driveService;
        this.companyService = companyService;
        this.sessionManager = sessionManager;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        loadDrives();
    }

    private void buildLayout() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(statusFilterCombo);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> loadDrives());
        filterPanel.add(searchButton);
        add(filterPanel, BorderLayout.NORTH);

        driveTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(driveTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        addButton.addActionListener(event -> openDriveForm(null));
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(event -> editSelectedDrive());
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(statusLabel);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void loadDrives() {
        statusLabel.setText("Loading drives...");
        UiTasks.run(
                () -> {
                    List<PlacementDrive> drives = driveService.listDrives((String) statusFilterCombo.getSelectedItem());
                    Map<Integer, String> names = new HashMap<>();
                    for (Company company : companyService.listActiveCompanies()) {
                        names.put(company.getCompanyId(), company.getCompanyName());
                    }
                    return new DriveLoadResult(drives, names);
                },
                result -> {
                    driveById.clear();
                    companyNames.clear();
                    companyNames.putAll(result.companyNames);
                    populateTable(result.drives);
                },
                this::showError);
    }

    private void populateTable(List<PlacementDrive> drives) {
        tableModel.setRowCount(0);
        for (PlacementDrive drive : drives) {
            driveById.put(drive.getDriveId(), drive);
            tableModel.addRow(new Object[]{
                    drive.getDriveId(),
                    companyNames.getOrDefault(drive.getCompanyId(), "Unknown"),
                    drive.getJobTitle(),
                    drive.getStatus(),
                    drive.getApplicationDeadline(),
                    drive.getMinCgpa()
            });
        }
        statusLabel.setText(drives.size() + " drives loaded.");
    }

    private void openDriveForm(PlacementDrive drive) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                drive == null ? "Create Drive" : "Edit Drive",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        DriveFormPanel formPanel = new DriveFormPanel(
                driveService,
                companyService,
                sessionManager,
                drive,
                saved -> {
                    driveById.put(saved.getDriveId(), saved);
                    dialog.dispose();
                    loadDrives();
                });
        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editSelectedDrive() {
        PlacementDrive selected = getSelectedDrive();
        if (selected == null) {
            statusLabel.setText("Select a drive to edit.");
            return;
        }
        openDriveForm(selected);
    }

    private PlacementDrive getSelectedDrive() {
        int selectedRow = driveTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int driveId = (Integer) tableModel.getValueAt(selectedRow, 0);
        return driveById.get(driveId);
    }

    private void showError(Exception exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        statusLabel.setForeground(UiStyles.ERROR_COLOR);
        statusLabel.setText(cause.getMessage());
    }

    private static final class DriveLoadResult {
        private final List<PlacementDrive> drives;
        private final Map<Integer, String> companyNames;

        private DriveLoadResult(List<PlacementDrive> drives, Map<Integer, String> companyNames) {
            this.drives = drives;
            this.companyNames = companyNames;
        }
    }
}

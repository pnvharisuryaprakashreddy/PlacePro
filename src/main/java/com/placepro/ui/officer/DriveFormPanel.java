package com.placepro.ui.officer;

import com.placepro.model.Company;
import com.placepro.model.PlacementDrive;
import com.placepro.service.CompanyService;
import com.placepro.service.ServiceException;
import com.placepro.service.auth.SessionManager;
import com.placepro.service.drive.DriveService;
import com.placepro.service.drive.DriveStatus;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DriveFormPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DriveService driveService;
    private final CompanyService companyService;
    private final SessionManager sessionManager;
    private final PlacementDrive existingDrive;
    private final Consumer<PlacementDrive> onSaved;

    private final JLabel errorLabel = UiStyles.createErrorLabel();
    private final JComboBox<CompanyOption> companyCombo = new JComboBox<>();
    private final JTextField jobTitleField = new JTextField(24);
    private final JTextArea descriptionArea = new JTextArea(4, 24);
    private final JTextField packageMinField = new JTextField(8);
    private final JTextField packageMaxField = new JTextField(8);
    private final JTextField minCgpaField = new JTextField(8);
    private final JTextField maxBacklogsField = new JTextField(8);
    private final JTextField visitDateField = new JTextField(12);
    private final JTextField deadlineField = new JTextField(16);
    private final Map<String, JCheckBox> branchCheckboxes = new HashMap<>();
    private final JButton publishButton = new JButton("Publish");
    private final JButton closeButton = new JButton("Close Drive");
    private final JButton completeButton = new JButton("Complete");

    public DriveFormPanel(DriveService driveService,
                          CompanyService companyService,
                          SessionManager sessionManager,
                          PlacementDrive existingDrive,
                          Consumer<PlacementDrive> onSaved) {
        this.driveService = driveService;
        this.companyService = companyService;
        this.sessionManager = sessionManager;
        this.existingDrive = existingDrive;
        this.onSaved = onSaved;
        buildForm();
        loadCompanies();
        if (existingDrive != null) {
            populate(existingDrive);
            updateLifecycleButtons(existingDrive.getStatus());
        } else {
            updateLifecycleButtons(DriveStatus.DRAFT.name());
        }
    }

    private void buildForm() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 8, 6, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;

        add(new JLabel(existingDrive == null ? "Create Drive" : "Edit Drive"), constraints);
        constraints.gridy++;
        add(errorLabel, constraints);

        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        add(new JLabel("Company"), constraints);
        constraints.gridx = 1;
        add(companyCombo, constraints);

        addField(constraints, "Job Title", jobTitleField);
        constraints.gridy++;
        constraints.gridx = 0;
        add(new JLabel("Description"), constraints);
        constraints.gridx = 1;
        descriptionArea.setLineWrap(true);
        add(new JScrollPane(descriptionArea), constraints);

        addField(constraints, "Package Min (LPA)", packageMinField);
        addField(constraints, "Package Max (LPA)", packageMaxField);
        addField(constraints, "Min CGPA", minCgpaField);
        addField(constraints, "Max Backlogs", maxBacklogsField);
        addField(constraints, "Visit Date (yyyy-MM-dd)", visitDateField);
        addField(constraints, "Application Deadline (yyyy-MM-dd HH:mm)", deadlineField);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        JPanel branchPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        branchPanel.add(new JLabel("Allowed Branches:"));
        for (String branch : BranchConstants.BRANCHES) {
            JCheckBox checkBox = new JCheckBox(branch);
            branchCheckboxes.put(branch, checkBox);
            branchPanel.add(checkBox);
        }
        add(branchPanel, constraints);

        constraints.gridy++;
        JPanel savePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(event -> saveDrive());
        savePanel.add(saveButton);
        add(savePanel, constraints);

        constraints.gridy++;
        JPanel lifecyclePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        publishButton.addActionListener(event -> transition(driveService::publishDrive));
        closeButton.addActionListener(event -> transition(driveService::closeDrive));
        completeButton.addActionListener(event -> transition(driveService::completeDrive));
        lifecyclePanel.add(publishButton);
        lifecyclePanel.add(closeButton);
        lifecyclePanel.add(completeButton);
        add(lifecyclePanel, constraints);
    }

    private void addField(GridBagConstraints constraints, String label, JTextField field) {
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        add(new JLabel(label), constraints);
        constraints.gridx = 1;
        add(field, constraints);
    }

    private void loadCompanies() {
        UiTasks.run(
                companyService::listActiveCompanies,
                companies -> {
                    companyCombo.removeAllItems();
                    for (Company company : companies) {
                        companyCombo.addItem(new CompanyOption(company.getCompanyId(), company.getCompanyName()));
                    }
                },
                exception -> errorLabel.setText(exception.getMessage()));
    }

    private void populate(PlacementDrive drive) {
        selectCompany(drive.getCompanyId());
        jobTitleField.setText(drive.getJobTitle());
        descriptionArea.setText(drive.getJobDescription());
        if (drive.getPackageMin() != null) {
            packageMinField.setText(drive.getPackageMin().toPlainString());
        }
        if (drive.getPackageMax() != null) {
            packageMaxField.setText(drive.getPackageMax().toPlainString());
        }
        minCgpaField.setText(drive.getMinCgpa().toPlainString());
        maxBacklogsField.setText(String.valueOf(drive.getMaxBacklogs()));
        if (drive.getVisitDate() != null) {
            visitDateField.setText(drive.getVisitDate().format(DATE_FORMAT));
        }
        deadlineField.setText(drive.getApplicationDeadline().format(DATE_TIME_FORMAT));
        selectBranches(drive.getAllowedBranches());
    }

    private void selectCompany(int companyId) {
        for (int index = 0; index < companyCombo.getItemCount(); index++) {
            CompanyOption option = companyCombo.getItemAt(index);
            if (option.getCompanyId() == companyId) {
                companyCombo.setSelectedIndex(index);
                return;
            }
        }
    }

    private void selectBranches(String allowedBranches) {
        for (JCheckBox checkBox : branchCheckboxes.values()) {
            checkBox.setSelected(false);
        }
        if (allowedBranches == null) {
            return;
        }
        for (String branch : allowedBranches.split(",")) {
            JCheckBox checkBox = branchCheckboxes.get(branch.trim());
            if (checkBox != null) {
                checkBox.setSelected(true);
            }
        }
    }

    private void saveDrive() {
        errorLabel.setText(" ");
        try {
            PlacementDrive drive = existingDrive == null ? new PlacementDrive() : existingDrive;
            CompanyOption selectedCompany = (CompanyOption) companyCombo.getSelectedItem();
            if (selectedCompany == null) {
                throw new ServiceException("Select a company.");
            }

            drive.setCompanyId(selectedCompany.getCompanyId());
            drive.setJobTitle(jobTitleField.getText().trim());
            drive.setJobDescription(descriptionArea.getText().trim());
            drive.setPackageMin(parseDecimal(packageMinField.getText().trim(), true));
            drive.setPackageMax(parseDecimal(packageMaxField.getText().trim(), true));
            drive.setMinCgpa(parseDecimal(minCgpaField.getText().trim(), false));
            drive.setMaxBacklogs(Integer.parseInt(maxBacklogsField.getText().trim()));
            drive.setAllowedBranches(buildAllowedBranches());
            drive.setVisitDate(parseDate(visitDateField.getText().trim()));
            drive.setApplicationDeadline(parseDateTime(deadlineField.getText().trim()));

            if (existingDrive == null) {
                drive.setStatus(DriveStatus.DRAFT.name());
                drive.setCreatedBy(sessionManager.getCurrentUserId()
                        .orElseThrow(() -> new ServiceException("Session expired.")));
                PlacementDrive saved = driveService.createDrive(drive);
                onSaved.accept(saved);
            } else {
                PlacementDrive saved = driveService.updateDrive(drive);
                onSaved.accept(saved);
            }
        } catch (ServiceException | NumberFormatException | DateTimeParseException exception) {
            errorLabel.setText(exception.getMessage());
        }
    }

    private void transition(java.util.function.IntFunction<PlacementDrive> transitionFunction) {
        if (existingDrive == null) {
            errorLabel.setText("Save the drive before changing lifecycle status.");
            return;
        }
        try {
            PlacementDrive updated = transitionFunction.apply(existingDrive.getDriveId());
            updateLifecycleButtons(updated.getStatus());
            onSaved.accept(updated);
        } catch (ServiceException exception) {
            errorLabel.setText(exception.getMessage());
        }
    }

    private void updateLifecycleButtons(String status) {
        publishButton.setEnabled(DriveStatus.DRAFT.name().equals(status));
        closeButton.setEnabled(DriveStatus.PUBLISHED.name().equals(status));
        completeButton.setEnabled(DriveStatus.CLOSED.name().equals(status));
    }

    private String buildAllowedBranches() {
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : branchCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }
        if (selected.isEmpty()) {
            throw new ServiceException("Select at least one eligible branch.");
        }
        return String.join(",", selected);
    }

    private BigDecimal parseDecimal(String value, boolean allowEmpty) {
        if (value.isEmpty()) {
            if (allowEmpty) {
                return null;
            }
            throw new ServiceException("Required numeric field is missing.");
        }
        return new BigDecimal(value);
    }

    private LocalDate parseDate(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMAT);
    }

    private LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value, DATE_TIME_FORMAT);
    }

    private static final class CompanyOption {
        private final int companyId;
        private final String companyName;

        private CompanyOption(int companyId, String companyName) {
            this.companyId = companyId;
            this.companyName = companyName;
        }

        private int getCompanyId() {
            return companyId;
        }

        @Override
        public String toString() {
            return companyName;
        }
    }
}

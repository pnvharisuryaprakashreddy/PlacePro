package com.placepro.ui.officer;

import com.placepro.model.Company;
import com.placepro.service.CompanyService;
import com.placepro.service.ServiceException;
import com.placepro.service.report.ReportFilter;
import com.placepro.service.report.ReportTable;
import com.placepro.ui.AppContext;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class CompanyListPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {
            "ID", "Name", "Industry", "Contact", "Email", "Phone", "Active"
    };

    private final CompanyService companyService;
    private final JTextField nameFilterField = new JTextField(12);
    private final JTextField industryFilterField = new JTextField(12);
    private final JComboBox<String> activeFilterCombo = new JComboBox<>(new String[]{"ALL", "ACTIVE", "INACTIVE"});
    private final JComboBox<String> driveFilterCombo = new JComboBox<>(
            new String[]{"ALL", "WITH ACTIVE DRIVES", "WITHOUT ACTIVE DRIVES"});
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable companyTable = new JTable(tableModel);
    private List<Company> loadedCompanies = List.of();

    public CompanyListPanel(CompanyService companyService) {
        this.companyService = companyService;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        loadCompanies();
    }

    private void buildLayout() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Name"));
        filterPanel.add(nameFilterField);
        filterPanel.add(new JLabel("Industry"));
        filterPanel.add(industryFilterField);
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(activeFilterCombo);
        filterPanel.add(new JLabel("Drives"));
        filterPanel.add(driveFilterCombo);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> loadCompanies());
        filterPanel.add(searchButton);
        add(filterPanel, BorderLayout.NORTH);

        companyTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(companyTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        addButton.addActionListener(event -> openCompanyForm(null));
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(event -> editSelectedCompany());
        JButton deactivateButton = new JButton("Deactivate");
        deactivateButton.addActionListener(event -> deactivateSelectedCompany());
        JButton drillDownButton = new JButton("View Drives & Outcomes");
        drillDownButton.addActionListener(event -> showDriveDrillDown());

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deactivateButton);
        actionPanel.add(drillDownButton);
        actionPanel.add(statusLabel);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void loadCompanies() {
        statusLabel.setText("Loading companies...");
        String driveFilter = ((String) driveFilterCombo.getSelectedItem()).replace(' ', '_');
        UiTasks.run(
                () -> companyService.searchCompanies(
                        nameFilterField.getText(),
                        industryFilterField.getText(),
                        (String) activeFilterCombo.getSelectedItem(),
                        driveFilter),
                this::populateTable,
                this::showError);
    }

    private void populateTable(List<Company> companies) {
        loadedCompanies = companies;
        tableModel.setRowCount(0);
        for (Company company : companies) {
            tableModel.addRow(new Object[]{
                    company.getCompanyId(),
                    company.getCompanyName(),
                    company.getIndustry(),
                    company.getContactPerson(),
                    company.getEmail(),
                    company.getPhone(),
                    Boolean.TRUE.equals(company.getIsActive()) ? "Yes" : "No"
            });
        }
        statusLabel.setText(companies.size() + " companies loaded.");
    }

    private void openCompanyForm(Company company) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                company == null ? "Add Company" : "Edit Company",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        CompanyFormPanel formPanel = new CompanyFormPanel(companyService, company, saved -> {
            statusLabel.setText("Company saved successfully.");
            dialog.dispose();
            loadCompanies();
        });
        dialog.setContentPane(formPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void editSelectedCompany() {
        Company selected = getSelectedCompany();
        if (selected == null) {
            statusLabel.setText("Select a company to edit.");
            return;
        }
        openCompanyForm(selected);
    }

    private void deactivateSelectedCompany() {
        Company selected = getSelectedCompany();
        if (selected == null) {
            statusLabel.setText("Select a company to deactivate.");
            return;
        }
        UiTasks.run(
                () -> {
                    companyService.deactivateCompany(selected.getCompanyId());
                    return true;
                },
                ignored -> {
                    statusLabel.setText("Company deactivated.");
                    loadCompanies();
                },
                this::showError);
    }

    private Company getSelectedCompany() {
        int selectedRow = companyTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int companyId = (Integer) tableModel.getValueAt(selectedRow, 0);
        return loadedCompanies.stream()
                .filter(company -> company.getCompanyId() == companyId)
                .findFirst()
                .orElse(null);
    }

    private void showDriveDrillDown() {
        Company selected = getSelectedCompany();
        if (selected == null) {
            statusLabel.setText("Select a company to view its drives.");
            return;
        }
        statusLabel.setText("Loading drives for " + selected.getCompanyName() + "...");
        ReportFilter filter = new ReportFilter(null, null, selected.getCompanyId(), null, null);
        UiTasks.run(
                () -> AppContext.getReportService().getDriveApplicantFunnel(filter),
                report -> {
                    statusLabel.setText(" ");
                    showDrillDownDialog(selected, report);
                },
                this::showError);
    }

    private void showDrillDownDialog(Company company, ReportTable report) {
        DefaultTableModel model = new DefaultTableModel(report.getColumns().toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (java.util.List<Object> row : report.getRows()) {
            model.addRow(row.toArray());
        }

        javax.swing.JDialog dialog = new javax.swing.JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                "Drives & Placement Outcomes - " + company.getCompanyName(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.add(new JLabel(report.getRows().isEmpty()
                ? "This company has no placement drives yet."
                : report.getRows().size() + " drive(s). Counts show applied / shortlisted / interviewed / selected."),
                BorderLayout.NORTH);
        content.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        dialog.setContentPane(content);
        dialog.setSize(760, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showError(Exception exception) {
        String message = exception.getCause() instanceof ServiceException
                ? exception.getCause().getMessage()
                : exception.getMessage();
        statusLabel.setForeground(UiStyles.ERROR_COLOR);
        statusLabel.setText(message);
    }
}

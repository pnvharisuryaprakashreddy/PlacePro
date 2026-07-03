package com.placepro.ui.officer;

import com.placepro.model.Company;
import com.placepro.service.CompanyService;
import com.placepro.service.report.ReportFilter;
import com.placepro.service.report.ReportService;
import com.placepro.service.report.ReportTable;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Reporting screen for officers and admins: pick a report type, apply optional
 * filters, view the result in a table, and export to CSV or PDF.
 */
public class ReportsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ALL = "All";

    private final ReportService reportService;
    private final CompanyService companyService;

    private final JComboBox<ReportType> reportTypeCombo = new JComboBox<>();
    private final JTextField graduationYearField = new JTextField(6);
    private final JComboBox<String> branchCombo = new JComboBox<>();
    private final JComboBox<CompanyOption> companyCombo = new JComboBox<>();
    private final JTextField fromDateField = new JTextField(9);
    private final JTextField toDateField = new JTextField(9);
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton exportCsvButton = new JButton("Export to CSV");
    private final JButton exportPdfButton = new JButton("Export to PDF");

    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resultTable = new JTable(tableModel);

    private ReportTable currentReport;

    public ReportsPanel(ReportService reportService, CompanyService companyService) {
        this.reportService = reportService;
        this.companyService = companyService;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        loadCompanies();
    }

    private void buildLayout() {
        reportTypeCombo.addItem(new ReportType(
                "Placement Summary by Department", reportService::getPlacementSummaryByDepartment));
        reportTypeCombo.addItem(new ReportType(
                "Company-wise Selection Statistics", reportService::getCompanySelectionStatistics));
        reportTypeCombo.addItem(new ReportType(
                "Drive-wise Applicant Funnel", reportService::getDriveApplicantFunnel));
        reportTypeCombo.addItem(new ReportType(
                "Individual Student Placement Record", reportService::getStudentPlacementRecords));

        branchCombo.addItem(ALL);
        for (String branch : BranchConstants.BRANCHES) {
            branchCombo.addItem(branch);
        }
        companyCombo.addItem(new CompanyOption(null, ALL));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filters.add(new JLabel("Report:"));
        filters.add(reportTypeCombo);
        filters.add(new JLabel("Grad Year:"));
        filters.add(graduationYearField);
        filters.add(new JLabel("Branch:"));
        filters.add(branchCombo);
        filters.add(new JLabel("Company:"));
        filters.add(companyCombo);
        filters.add(new JLabel("From (yyyy-MM-dd):"));
        filters.add(fromDateField);
        filters.add(new JLabel("To:"));
        filters.add(toDateField);
        JButton runButton = new JButton("Run Report");
        runButton.addActionListener(event -> runReport());
        filters.add(runButton);
        add(filters, BorderLayout.NORTH);

        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        exportCsvButton.setEnabled(false);
        exportPdfButton.setEnabled(false);
        exportCsvButton.addActionListener(event -> exportReport("csv"));
        exportPdfButton.addActionListener(event -> exportReport("pdf"));
        footer.add(exportCsvButton);
        footer.add(exportPdfButton);
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadCompanies() {
        UiTasks.run(
                companyService::listActiveCompanies,
                companies -> {
                    for (Company company : companies) {
                        companyCombo.addItem(new CompanyOption(company.getCompanyId(), company.getCompanyName()));
                    }
                },
                exception -> showError("Unable to load the company list."));
    }

    private void runReport() {
        ReportType reportType = (ReportType) reportTypeCombo.getSelectedItem();
        if (reportType == null) {
            return;
        }

        ReportFilter filter;
        try {
            filter = buildFilter();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Running report...");
        exportCsvButton.setEnabled(false);
        exportPdfButton.setEnabled(false);
        UiTasks.run(
                () -> reportType.query.apply(filter),
                this::showReport,
                exception -> showError("Unable to run the report."));
    }

    private ReportFilter buildFilter() {
        Integer graduationYear = null;
        String yearText = graduationYearField.getText().trim();
        if (!yearText.isEmpty()) {
            try {
                graduationYear = Integer.parseInt(yearText);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Graduation year must be a number, e.g. 2026.");
            }
        }

        String branch = (String) branchCombo.getSelectedItem();
        if (ALL.equals(branch)) {
            branch = null;
        }

        CompanyOption company = (CompanyOption) companyCombo.getSelectedItem();
        Integer companyId = company == null ? null : company.companyId;

        LocalDate fromDate = parseDate(fromDateField.getText().trim(), "From date");
        LocalDate toDate = parseDate(toDateField.getText().trim(), "To date");
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("The To date must not be before the From date.");
        }

        return new ReportFilter(graduationYear, branch, companyId, fromDate, toDate);
    }

    private LocalDate parseDate(String text, String fieldName) {
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " must be in yyyy-MM-dd format.");
        }
    }

    private void showReport(ReportTable report) {
        currentReport = report;
        tableModel.setColumnIdentifiers(report.getColumns().toArray());
        tableModel.setRowCount(0);
        for (List<Object> row : report.getRows()) {
            tableModel.addRow(row.toArray());
        }
        boolean hasRows = !report.getRows().isEmpty();
        exportCsvButton.setEnabled(hasRows);
        exportPdfButton.setEnabled(hasRows);
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText(hasRows
                ? report.getTitle() + ": " + report.getRows().size() + " row(s)."
                : "No data found.");
    }

    private void exportReport(String format) {
        if (currentReport == null || currentReport.getRows().isEmpty()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export report as " + format.toUpperCase());
        String defaultName = currentReport.getTitle().replaceAll("[^A-Za-z0-9]+", "_") + "." + format;
        chooser.setSelectedFile(new File(defaultName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith("." + format)) {
            target = new File(target.getParentFile(), target.getName() + "." + format);
        }

        File destination = target;
        ReportTable report = currentReport;
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Exporting...");
        exportCsvButton.setEnabled(false);
        exportPdfButton.setEnabled(false);
        UiTasks.run(
                () -> {
                    try {
                        if ("csv".equals(format)) {
                            ReportExporter.toCsv(destination, report);
                        } else {
                            ReportExporter.toPdf(destination, report);
                        }
                        return destination.getAbsolutePath();
                    } catch (IOException exception) {
                        throw new RuntimeException("Could not write the export file: " + exception.getMessage(), exception);
                    }
                },
                path -> {
                    reenableExportButtons();
                    statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
                    statusLabel.setText("Exported to " + path);
                },
                exception -> {
                    reenableExportButtons();
                    showError("Export failed. Check the file location and try again.");
                });
    }

    private void reenableExportButtons() {
        boolean hasRows = currentReport != null && !currentReport.getRows().isEmpty();
        exportCsvButton.setEnabled(hasRows);
        exportPdfButton.setEnabled(hasRows);
    }

    private void showError(String message) {
        statusLabel.setForeground(UiStyles.ERROR_COLOR);
        statusLabel.setText(message);
    }

    private static final class ReportType {
        private final String label;
        private final Function<ReportFilter, ReportTable> query;

        private ReportType(String label, Function<ReportFilter, ReportTable> query) {
            this.label = label;
            this.query = query;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class CompanyOption {
        private final Integer companyId;
        private final String name;

        private CompanyOption(Integer companyId, String name) {
            this.companyId = companyId;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

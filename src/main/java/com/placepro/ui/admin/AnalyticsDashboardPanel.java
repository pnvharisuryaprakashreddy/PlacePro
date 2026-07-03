package com.placepro.ui.admin;

import com.placepro.service.report.AnalyticsSnapshot;
import com.placepro.service.report.DepartmentPlacementSummary;
import com.placepro.service.report.ReportService;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Visual KPI dashboard for admins. All numbers are pulled live from MySQL on
 * load/refresh via a background worker; nothing is cached.
 */
public class AnalyticsDashboardPanel extends JPanel {

    private final ReportService reportService;

    private final JLabel totalPlacementsValue = createKpiValueLabel();
    private final JLabel placementPercentValue = createKpiValueLabel();
    private final JLabel averagePackageValue = createKpiValueLabel();
    private final JLabel conversionRateValue = createKpiValueLabel();
    private final JLabel statusLabel = new JLabel(" ");
    private final JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 12, 12));

    public AnalyticsDashboardPanel(ReportService reportService) {
        this.reportService = reportService;
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildLayout();
        refresh();
    }

    private void buildLayout() {
        JPanel north = new JPanel(new BorderLayout(8, 8));

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 12));
        kpiRow.add(createKpiCard("Total Placements", totalPlacementsValue));
        kpiRow.add(createKpiCard("Overall Placement %", placementPercentValue));
        kpiRow.add(createKpiCard("Average Package (LPA)", averagePackageValue));
        kpiRow.add(createKpiCard("Application → Selection %", conversionRateValue));
        north.add(kpiRow, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new BorderLayout());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());
        toolbar.add(refreshButton, BorderLayout.EAST);
        toolbar.add(statusLabel, BorderLayout.WEST);
        north.add(toolbar, BorderLayout.NORTH);

        add(north, BorderLayout.NORTH);
        add(chartsPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Loading analytics from database...");
        UiTasks.run(
                reportService::getAnalyticsSnapshot,
                this::showSnapshot,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load analytics.");
                });
    }

    private void showSnapshot(AnalyticsSnapshot snapshot) {
        totalPlacementsValue.setText(String.valueOf(snapshot.getTotalPlacements()));
        placementPercentValue.setText(String.format(Locale.ENGLISH, "%.1f%%", snapshot.getOverallPlacementPercentage()));
        averagePackageValue.setText(snapshot.getAveragePackage() == null
                ? "-"
                : snapshot.getAveragePackage().setScale(2, RoundingMode.HALF_UP).toPlainString());
        conversionRateValue.setText(String.format(Locale.ENGLISH, "%.1f%%", snapshot.getConversionRate()));

        chartsPanel.removeAll();
        chartsPanel.add(new ChartPanel(buildDepartmentChart(snapshot)));
        chartsPanel.add(new ChartPanel(buildTopCompaniesChart(snapshot)));
        chartsPanel.revalidate();
        chartsPanel.repaint();

        statusLabel.setText("Analytics loaded (" + snapshot.getTotalActiveStudents() + " active students, "
                + snapshot.getTotalApplications() + " applications).");
    }

    private JFreeChart buildDepartmentChart(AnalyticsSnapshot snapshot) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (DepartmentPlacementSummary summary : snapshot.getDepartmentSummaries()) {
            double percent = summary.getRegisteredStudents() == 0
                    ? 0.0
                    : 100.0 * summary.getSelectedCount() / summary.getRegisteredStudents();
            dataset.addValue(percent, "Placement %", summary.getBranch());
        }
        return ChartFactory.createBarChart(
                "Placement % by Department",
                "Department",
                "Placed (%)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);
    }

    private JFreeChart buildTopCompaniesChart(AnalyticsSnapshot snapshot) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (snapshot.getTopCompanies().isEmpty()) {
            dataset.setValue("No selections yet", 1);
        } else {
            for (AnalyticsSnapshot.CompanySelectionCount company : snapshot.getTopCompanies()) {
                dataset.setValue(company.getCompanyName() + " (" + company.getSelectedCount() + ")",
                        company.getSelectedCount());
            }
        }
        return ChartFactory.createPieChart(
                "Top Recruiting Companies (by selections)",
                dataset,
                true,
                true,
                false);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel createKpiValueLabel() {
        JLabel label = new JLabel("-", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        return label;
    }
}

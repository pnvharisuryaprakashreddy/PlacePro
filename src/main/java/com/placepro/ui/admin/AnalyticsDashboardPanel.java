package com.placepro.ui.admin;

import com.placepro.service.report.AnalyticsSnapshot;
import com.placepro.service.report.DepartmentPlacementSummary;
import com.placepro.service.report.ReportService;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Visual KPI & Analytics Dashboard for Admins with Enterprise Styled JFreeCharts.
 */
public class AnalyticsDashboardPanel extends JPanel {

    private final ReportService reportService;

    private final JLabel totalPlacementsValue = createKpiValueLabel();
    private final JLabel placementPercentValue = createKpiValueLabel();
    private final JLabel averagePackageValue = createKpiValueLabel();
    private final JLabel conversionRateValue = createKpiValueLabel();
    private final JLabel statusLabel = UiStyles.createStatusLabel();
    private final JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 16, 16));

    public AnalyticsDashboardPanel(ReportService reportService) {
        this.reportService = reportService;
        setLayout(new BorderLayout(16, 16));
        setBackground(UiStyles.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        buildLayout();
        refresh();
    }

    private void buildLayout() {
        JPanel north = new JPanel(new BorderLayout(12, 12));
        north.setOpaque(false);

        JPanel toolbar = new UiStyles.RoundedPanel(12, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        toolbar.setLayout(new BorderLayout());
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        toolbar.add(statusLabel, BorderLayout.WEST);
        JButton refreshButton = UiStyles.stylePrimaryButton(new JButton("🔄 Sync Live Metrics"));
        refreshButton.addActionListener(event -> refresh());
        toolbar.add(refreshButton, BorderLayout.EAST);
        north.add(toolbar, BorderLayout.NORTH);

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 16));
        kpiRow.setOpaque(false);
        kpiRow.add(createKpiCard("Total Placements Secured", totalPlacementsValue, UiStyles.PRIMARY_COLOR));
        kpiRow.add(createKpiCard("Overall Campus Placement %", placementPercentValue, UiStyles.SUCCESS_COLOR));
        kpiRow.add(createKpiCard("Average Salary Package (LPA)", averagePackageValue, new Color(168, 85, 247)));
        kpiRow.add(createKpiCard("Selection Conversion Rate", conversionRateValue, UiStyles.WARNING_COLOR));
        north.add(kpiRow, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        chartsPanel.setOpaque(false);
        add(chartsPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        statusLabel.setText("Loading live placement analytics from database...");
        UiTasks.run(
                reportService::getAnalyticsSnapshot,
                this::showSnapshot,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("⚠️ Unable to load live analytics.");
                });
    }

    private void showSnapshot(AnalyticsSnapshot snapshot) {
        totalPlacementsValue.setText(String.valueOf(snapshot.getTotalPlacements()));
        placementPercentValue.setText(String.format(Locale.ENGLISH, "%.1f%%", snapshot.getOverallPlacementPercentage()));
        averagePackageValue.setText(snapshot.getAveragePackage() == null
                ? "-"
                : snapshot.getAveragePackage().setScale(2, RoundingMode.HALF_UP).toPlainString() + " LPA");
        conversionRateValue.setText(String.format(Locale.ENGLISH, "%.1f%%", snapshot.getConversionRate()));

        chartsPanel.removeAll();

        JFreeChart deptChart = buildDepartmentChart(snapshot);
        styleBarChart(deptChart);
        ChartPanel p1 = new ChartPanel(deptChart);
        p1.setBorder(BorderFactory.createLineBorder(UiStyles.BORDER_COLOR, 1));
        chartsPanel.add(p1);

        JFreeChart companyChart = buildTopCompaniesChart(snapshot);
        stylePieChart(companyChart);
        ChartPanel p2 = new ChartPanel(companyChart);
        p2.setBorder(BorderFactory.createLineBorder(UiStyles.BORDER_COLOR, 1));
        chartsPanel.add(p2);

        chartsPanel.revalidate();
        chartsPanel.repaint();

        statusLabel.setText("✅ Analytics synced (" + snapshot.getTotalActiveStudents() + " active students, "
                + snapshot.getTotalApplications() + " total applications).");
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
                "Department Placement Rate (%)",
                "Department Branch",
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
            dataset.setValue("No selections recorded yet", 1);
        } else {
            for (AnalyticsSnapshot.CompanySelectionCount company : snapshot.getTopCompanies()) {
                dataset.setValue(company.getCompanyName() + " (" + company.getSelectedCount() + ")",
                        company.getSelectedCount());
            }
        }
        return ChartFactory.createPieChart(
                "Top Recruiting Companies by Selections",
                dataset,
                true,
                true,
                false);
    }

    private void styleBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(UiStyles.SURFACE_COLOR);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 15));
        chart.getTitle().setPaint(UiStyles.TEXT_COLOR);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(UiStyles.SURFACE_ALT_COLOR);
        plot.setOutlinePaint(UiStyles.BORDER_COLOR);
        plot.setRangeGridlinePaint(UiStyles.BORDER_COLOR);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UiStyles.PRIMARY_COLOR);
        renderer.setDrawBarOutline(false);
    }

    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(UiStyles.SURFACE_COLOR);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 15));
        chart.getTitle().setPaint(UiStyles.TEXT_COLOR);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(UiStyles.SURFACE_COLOR);
        plot.setOutlinePaint(UiStyles.BORDER_COLOR);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new UiStyles.RoundedPanel(14, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 5, 14, 14));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(4, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(UiStyles.MUTED_TEXT_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel createKpiValueLabel() {
        JLabel label = new JLabel("-", SwingConstants.LEFT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(UiStyles.TEXT_COLOR);
        return label;
    }
}

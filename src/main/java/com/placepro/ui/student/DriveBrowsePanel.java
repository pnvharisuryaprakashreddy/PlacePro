package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDriveSummary;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class DriveBrowsePanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final DashboardService dashboardService;
    private final JLabel statusLabel = UiStyles.createStatusLabel();
    private final List<StudentDriveSummary> driveList = new ArrayList<>();
    private final JTextField searchField = new JTextField(20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Company Name", "Job Title / Role", "Min CGPA", "Deadline", "Eligible Branches"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable driveTable = new JTable(tableModel);
    private TableRowSorter<DefaultTableModel> rowSorter;

    public DriveBrowsePanel(Student student, StudentNavigator navigator, DashboardService dashboardService) {
        this.student = student;
        this.navigator = navigator;
        this.dashboardService = dashboardService;
        setLayout(new BorderLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);
        buildLayout();
        loadDrives();
    }

    public void refresh() {
        loadDrives();
    }

    private void buildLayout() {
        // Dark Slate Top Header
        JPanel header = new UiStyles.DarkHeaderPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JButton backButton = UiStyles.styleSecondaryButton(new JButton("← Dashboard"));
        backButton.addActionListener(event -> navigator.showDashboard());
        left.add(backButton);

        JLabel title = new JLabel("Corporate Placement Drive Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        left.add(title);
        header.add(left, BorderLayout.WEST);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchGroup.setOpaque(false);
        JLabel searchLbl = new JLabel("🔍 Quick Filter:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLbl.setForeground(Color.WHITE);
        UiStyles.styleInput(searchField);
        searchGroup.add(searchLbl);
        searchGroup.add(searchField);
        header.add(searchGroup, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Center Table Card
        JPanel card = new UiStyles.RoundedPanel(16, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        card.setLayout(new BorderLayout(14, 14));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        rowSorter = new TableRowSorter<>(tableModel);
        driveTable.setRowSorter(rowSorter);
        driveTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        UiStyles.styleTable(driveTable);

        driveTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    openSelectedDrive();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + PatternQuote(text)));
                }
            }
        });

        JScrollPane scrollPane = UiStyles.createScrollPane(driveTable);
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(UiStyles.BACKGROUND_COLOR);
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainWrapper.add(card, BorderLayout.CENTER);

        JPanel footer = new UiStyles.RoundedPanel(12, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        footer.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));

        JButton viewButton = UiStyles.stylePrimaryButton(new JButton("Inspect Drive Eligibility & Details →"));
        viewButton.addActionListener(event -> openSelectedDrive());

        footer.add(viewButton);
        footer.add(statusLabel);
        mainWrapper.add(footer, BorderLayout.SOUTH);

        add(mainWrapper, BorderLayout.CENTER);
    }

    private void loadDrives() {
        statusLabel.setText("Loading active placement drives...");
        UiTasks.run(
                dashboardService::listPublishedDrivesForStudent,
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("⚠️ Unable to load corporate drives.");
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
                    summary.getDrive().getMinCgpa() != null ? summary.getDrive().getMinCgpa() + " CGPA" : "None",
                    summary.getApplicationDeadline(),
                    summary.getDrive().getAllowedBranches() != null ? summary.getDrive().getAllowedBranches() : "All Branches"
            });
        }
        statusLabel.setForeground(UiStyles.TEXT_COLOR);
        statusLabel.setText("Showing " + drives.size() + " active corporate placement drives.");
    }

    private void openSelectedDrive() {
        int viewRow = driveTable.getSelectedRow();
        if (viewRow < 0) {
            statusLabel.setText("⚠️ Please select a drive row to view details.");
            return;
        }
        int modelRow = driveTable.convertRowIndexToModel(viewRow);
        if (modelRow >= driveList.size()) {
            return;
        }
        navigator.showDriveDetail(driveList.get(modelRow));
    }

    private String PatternQuote(String s) {
        return java.util.regex.Pattern.quote(s);
    }
}

package com.placepro.ui.officer;

import com.placepro.dao.StudentSearchCriteria;
import com.placepro.dao.StudentSearchRow;
import com.placepro.model.Resume;
import com.placepro.model.Student;
import com.placepro.service.student.ApplicationTrackingRow;
import com.placepro.service.student.ApplicationTrackingService;
import com.placepro.service.student.StudentDirectoryService;
import com.placepro.service.student.StudentSearchPage;
import com.placepro.ui.common.ApplicationStatusRenderer;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Officer/admin student directory: server-side search with pagination and
 * quick links to a student's profile, applications, and current resume.
 */
public class StudentListPanel extends JPanel {

    private static final int PAGE_SIZE = 25;
    private static final String ALL = "All";

    private final StudentDirectoryService directoryService;
    private final ApplicationTrackingService applicationTrackingService;

    private final JTextField nameField = new JTextField(10);
    private final JTextField rollNumberField = new JTextField(8);
    private final JComboBox<String> branchCombo = new JComboBox<>();
    private final JTextField minCgpaField = new JTextField(4);
    private final JTextField maxCgpaField = new JTextField(4);
    private final JComboBox<String> placementCombo = new JComboBox<>(
            new String[]{ALL, "Placed", "Not Placed"});
    private final JLabel statusLabel = new JLabel(" ");
    private final JLabel pageLabel = new JLabel("Page 1 of 1");
    private final JButton previousButton = new JButton("< Prev");
    private final JButton nextButton = new JButton("Next >");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Roll Number", "Name", "Branch", "CGPA", "Backlogs", "Grad Year", "Placement"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable studentTable = new JTable(tableModel);

    private final List<StudentSearchRow> currentRows = new ArrayList<>();
    private StudentSearchCriteria currentCriteria = new StudentSearchCriteria(null, null, null, null, null, null);
    private int currentPage;
    private int totalPages = 1;

    public StudentListPanel(StudentDirectoryService directoryService,
                            ApplicationTrackingService applicationTrackingService) {
        this.directoryService = directoryService;
        this.applicationTrackingService = applicationTrackingService;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        runSearch(0);
    }

    private void buildLayout() {
        branchCombo.addItem(ALL);
        for (String branch : BranchConstants.BRANCHES) {
            branchCombo.addItem(branch);
        }

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filters.add(new JLabel("Name:"));
        filters.add(nameField);
        filters.add(new JLabel("Roll No:"));
        filters.add(rollNumberField);
        filters.add(new JLabel("Branch:"));
        filters.add(branchCombo);
        filters.add(new JLabel("CGPA:"));
        filters.add(minCgpaField);
        filters.add(new JLabel("to"));
        filters.add(maxCgpaField);
        filters.add(new JLabel("Placement:"));
        filters.add(placementCombo);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> applyFiltersAndSearch());
        filters.add(searchButton);
        add(filters, BorderLayout.NORTH);

        studentTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton profileButton = new JButton("View Profile");
        profileButton.addActionListener(event -> viewProfile());
        JButton applicationsButton = new JButton("View Applications");
        applicationsButton.addActionListener(event -> viewApplications());
        JButton resumeButton = new JButton("Open Resume");
        resumeButton.addActionListener(event -> openResume());
        actions.add(profileButton);
        actions.add(applicationsButton);
        actions.add(resumeButton);
        actions.add(statusLabel);
        south.add(actions, BorderLayout.WEST);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        previousButton.addActionListener(event -> runSearch(currentPage - 1));
        nextButton.addActionListener(event -> runSearch(currentPage + 1));
        pagination.add(previousButton);
        pagination.add(pageLabel);
        pagination.add(nextButton);
        south.add(pagination, BorderLayout.EAST);

        add(south, BorderLayout.SOUTH);
    }

    private void applyFiltersAndSearch() {
        BigDecimal minCgpa;
        BigDecimal maxCgpa;
        try {
            minCgpa = parseCgpa(minCgpaField.getText().trim(), "Minimum CGPA");
            maxCgpa = parseCgpa(maxCgpaField.getText().trim(), "Maximum CGPA");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (minCgpa != null && maxCgpa != null && maxCgpa.compareTo(minCgpa) < 0) {
            JOptionPane.showMessageDialog(this, "Maximum CGPA must not be below minimum CGPA.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String branch = (String) branchCombo.getSelectedItem();
        String placement = (String) placementCombo.getSelectedItem();
        currentCriteria = new StudentSearchCriteria(
                blankToNull(nameField.getText()),
                blankToNull(rollNumberField.getText()),
                ALL.equals(branch) ? null : branch,
                minCgpa,
                maxCgpa,
                "Placed".equals(placement) ? StudentSearchCriteria.PLACED
                        : "Not Placed".equals(placement) ? StudentSearchCriteria.NOT_PLACED
                        : null);
        runSearch(0);
    }

    private BigDecimal parseCgpa(String text, String fieldName) {
        if (text.isEmpty()) {
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(text);
            if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("10")) > 0) {
                throw new IllegalArgumentException(fieldName + " must be between 0 and 10.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }

    private static String blankToNull(String text) {
        String trimmed = text == null ? "" : text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void runSearch(int page) {
        StudentSearchCriteria criteria = currentCriteria;
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Searching...");
        previousButton.setEnabled(false);
        nextButton.setEnabled(false);
        UiTasks.run(
                () -> directoryService.searchStudents(criteria, Math.max(0, page), PAGE_SIZE),
                this::showPage,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to search students.");
                });
    }

    private void showPage(StudentSearchPage result) {
        currentRows.clear();
        currentRows.addAll(result.getRows());
        currentPage = result.getPage();
        totalPages = result.getTotalPages();

        tableModel.setRowCount(0);
        for (StudentSearchRow row : result.getRows()) {
            Student student = row.getStudent();
            tableModel.addRow(new Object[]{
                    student.getRollNumber(),
                    student.getFullName(),
                    student.getBranch(),
                    student.getCgpa(),
                    student.getBacklogCount(),
                    student.getGraduationYear(),
                    row.getPlacedCompany() == null ? "Not Placed" : "Placed - " + row.getPlacedCompany()
            });
        }

        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
        statusLabel.setText(result.getTotalCount() == 0
                ? "No students found."
                : result.getTotalCount() + " student(s) found.");
    }

    private StudentSearchRow getSelectedRow() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentRows.size()) {
            statusLabel.setText("Select a student first.");
            return null;
        }
        return currentRows.get(studentTable.convertRowIndexToModel(selectedRow));
    }

    private void viewProfile() {
        StudentSearchRow row = getSelectedRow();
        if (row == null) {
            return;
        }
        Student student = row.getStudent();
        String profile = String.format(
                "Roll Number: %s%nName: %s%nEmail: %s%nPhone: %s%nBranch: %s%nCGPA: %s%n"
                        + "Backlogs: %d%nGraduation Year: %d%nPlacement: %s",
                student.getRollNumber(),
                student.getFullName(),
                student.getEmail(),
                student.getPhone(),
                student.getBranch(),
                student.getCgpa(),
                student.getBacklogCount(),
                student.getGraduationYear(),
                row.getPlacedCompany() == null ? "Not Placed" : "Placed at " + row.getPlacedCompany());
        JOptionPane.showMessageDialog(this, profile,
                "Student Profile - " + student.getFullName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewApplications() {
        StudentSearchRow row = getSelectedRow();
        if (row == null) {
            return;
        }
        Student student = row.getStudent();
        statusLabel.setText("Loading applications...");
        UiTasks.run(
                () -> applicationTrackingService.listTrackingForStudent(student.getStudentId()),
                rows -> {
                    statusLabel.setText(" ");
                    showApplicationsDialog(student, rows);
                },
                exception -> {
                    statusLabel.setText(" ");
                    UiExceptionHandler.handleServiceFailure(this, exception);
                });
    }

    private void showApplicationsDialog(Student student, List<ApplicationTrackingRow> rows) {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Company", "Job Title", "Status", "Applied", "Interview Date", "Time", "Venue"}, 0) {
            @Override
            public boolean isCellEditable(int rowIndex, int column) {
                return false;
            }
        };
        for (ApplicationTrackingRow row : rows) {
            model.addRow(new Object[]{
                    row.getCompanyName(),
                    row.getJobTitle(),
                    row.getStatus(),
                    row.getAppliedAt() == null ? "-" : DateUtil.formatDateTime(row.getAppliedAt()),
                    row.getInterviewDate() == null ? "-" : DateUtil.formatDate(row.getInterviewDate()),
                    row.getInterviewTime() == null ? "-" : row.getInterviewTime().toString(),
                    row.getVenue() == null || row.getVenue().isBlank() ? "-" : row.getVenue()
            });
        }
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(2).setCellRenderer(new ApplicationStatusRenderer());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Applications - " + student.getFullName(), JDialog.ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.add(new JLabel(rows.isEmpty()
                ? "This student has not applied to any drives yet."
                : rows.size() + " application(s)."), BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.setContentPane(content);
        dialog.setSize(720, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openResume() {
        StudentSearchRow row = getSelectedRow();
        if (row == null) {
            return;
        }
        Student student = row.getStudent();
        statusLabel.setText("Looking up resume...");
        UiTasks.run(
                () -> directoryService.getCurrentResume(student.getStudentId()),
                resume -> {
                    statusLabel.setText(" ");
                    openResumeFile(student, resume);
                },
                exception -> {
                    statusLabel.setText(" ");
                    UiExceptionHandler.handleServiceFailure(this, exception);
                });
    }

    private void openResumeFile(Student student, Optional<Resume> resume) {
        if (!resume.isPresent()) {
            JOptionPane.showMessageDialog(this,
                    student.getFullName() + " has not uploaded a resume yet.",
                    "PlacePro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String filePath = resume.get().getFilePath();
        try {
            if (filePath == null || !Files.exists(Paths.get(filePath))) {
                JOptionPane.showMessageDialog(this, "The resume file is not available on disk.",
                        "PlacePro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Desktop.getDesktop().open(Paths.get(filePath).toFile());
        } catch (IOException exception) {
            UiExceptionHandler.handle(this, exception);
        }
    }
}

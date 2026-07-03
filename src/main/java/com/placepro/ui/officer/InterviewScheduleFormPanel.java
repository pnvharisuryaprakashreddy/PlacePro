package com.placepro.ui.officer;

import com.placepro.model.InterviewSchedule;
import com.placepro.model.PlacementOfficer;
import com.placepro.service.application.ApplicationReviewRow;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.application.InterviewService;
import com.placepro.service.drive.DriveService;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterviewScheduleFormPanel extends JPanel {

    private static final String[] INTERVIEW_TYPES = {
            "Aptitude", "Technical", "HR", "Group Discussion", "Coding", "Managerial"
    };
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final PlacementOfficer officer;
    private final DriveService driveService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final Runnable onInterviewScheduled;

    private final JComboBox<DriveOption> driveComboBox = new JComboBox<>();
    private final JComboBox<ApplicationOption> applicationComboBox = new JComboBox<>();
    private final JLabel noApplicationsLabel = new JLabel(" ");
    private final JLabel studentValueLabel = new JLabel("-");
    private final JLabel companyValueLabel = new JLabel("-");
    private final JLabel jobTitleValueLabel = new JLabel("-");
    private final JLabel statusValueLabel = new JLabel("-");
    private final JSpinner roundNumberSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JComboBox<String> interviewTypeComboBox = new JComboBox<>(INTERVIEW_TYPES);
    private final JTextField dateField = new JTextField(12);
    private final JTextField timeField = new JTextField(8);
    private final JTextField venueField = new JTextField(24);
    private final JTextArea notesArea = new JTextArea(3, 24);
    private final JLabel statusLabel = new JLabel(" ");
    private final Map<Integer, ApplicationReviewRow> reviewRowByApplicationId = new HashMap<>();
    private boolean suppressComboEvents;

    private final DefaultTableModel roundsModel = new DefaultTableModel(
            new String[]{"Round", "Type", "Date", "Time", "Venue", "Outcome"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable roundsTable = new JTable(roundsModel);

    public InterviewScheduleFormPanel(PlacementOfficer officer,
                                      DriveService driveService,
                                      ApplicationService applicationService,
                                      InterviewService interviewService,
                                      Runnable onInterviewScheduled) {
        this.officer = officer;
        this.driveService = driveService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.onInterviewScheduled = onInterviewScheduled;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        loadDrives();
    }

    public void refresh() {
        loadDrives();
    }

    private void buildLayout() {
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selectors.add(new JLabel("Drive:"));
        driveComboBox.addActionListener(event -> {
            if (!suppressComboEvents) {
                loadApplicationsForDrive();
            }
        });
        selectors.add(driveComboBox);
        selectors.add(new JLabel("Application:"));
        applicationComboBox.addActionListener(event -> {
            if (!suppressComboEvents) {
                updateSelectedApplicationDetails();
                loadRoundsForSelectedApplication();
            }
        });
        selectors.add(applicationComboBox);
        selectors.add(noApplicationsLabel);
        add(selectors, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildDetailsPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildFormPanel());
        content.add(Box.createVerticalStrut(8));
        content.add(buildRoundsPanel());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Application Details"));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(4, 8, 4, 8);

        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.gridx = 1;
        valueConstraints.gridy = 0;
        valueConstraints.weightx = 1.0;
        valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        valueConstraints.anchor = GridBagConstraints.WEST;
        valueConstraints.insets = new Insets(4, 8, 4, 8);

        addDetailRow(panel, labelConstraints, valueConstraints, 0, "Student Name", studentValueLabel);
        addDetailRow(panel, labelConstraints, valueConstraints, 1, "Company", companyValueLabel);
        addDetailRow(panel, labelConstraints, valueConstraints, 2, "Job Title", jobTitleValueLabel);
        addDetailRow(panel, labelConstraints, valueConstraints, 3, "Current Status", statusValueLabel);
        return panel;
    }

    private void addDetailRow(JPanel panel,
                              GridBagConstraints labelConstraints,
                              GridBagConstraints valueConstraints,
                              int row,
                              String labelText,
                              JLabel valueLabel) {
        labelConstraints.gridy = row;
        valueConstraints.gridy = row;
        panel.add(new JLabel(labelText), labelConstraints);
        panel.add(valueLabel, valueConstraints);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Schedule Interview"));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(6, 8, 6, 8);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.WEST;
        fieldConstraints.insets = new Insets(6, 8, 6, 8);

        int row = 0;
        addFormRow(panel, labelConstraints, fieldConstraints, row++, "Round Number", roundNumberSpinner);
        addFormRow(panel, labelConstraints, fieldConstraints, row++, "Interview Type", interviewTypeComboBox);
        addFormRow(panel, labelConstraints, fieldConstraints, row++, "Interview Date (yyyy-MM-dd)", dateField);
        addFormRow(panel, labelConstraints, fieldConstraints, row++, "Interview Time (HH:mm)", timeField);
        addFormRow(panel, labelConstraints, fieldConstraints, row++, "Venue", venueField);

        labelConstraints.gridy = row;
        fieldConstraints.gridy = row;
        panel.add(new JLabel("Notes (optional)"), labelConstraints);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(200, 60));
        fieldConstraints.gridy = row++;
        panel.add(notesScroll, fieldConstraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton scheduleButton = new JButton("Schedule Interview");
        scheduleButton.addActionListener(event -> scheduleInterview());
        actions.add(scheduleButton);
        actions.add(statusLabel);

        GridBagConstraints actionConstraints = new GridBagConstraints();
        actionConstraints.gridx = 0;
        actionConstraints.gridy = row;
        actionConstraints.gridwidth = 2;
        actionConstraints.anchor = GridBagConstraints.WEST;
        actionConstraints.insets = new Insets(8, 8, 8, 8);
        panel.add(actions, actionConstraints);

        return panel;
    }

    private void addFormRow(JPanel panel,
                            GridBagConstraints labelConstraints,
                            GridBagConstraints fieldConstraints,
                            int row,
                            String labelText,
                            java.awt.Component field) {
        labelConstraints.gridy = row;
        fieldConstraints.gridy = row;
        panel.add(new JLabel(labelText), labelConstraints);
        panel.add(field, fieldConstraints);
    }

    private JPanel buildRoundsPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Existing Interview Rounds"));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(0, 140));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        panel.add(new JScrollPane(roundsTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadDrives() {
        UiTasks.run(
                () -> driveService.listDrives("ALL"),
                drives -> {
                    suppressComboEvents = true;
                    driveComboBox.removeAllItems();
                    for (com.placepro.model.PlacementDrive drive : drives) {
                        driveComboBox.addItem(new DriveOption(drive.getDriveId(), drive.getJobTitle()));
                    }
                    suppressComboEvents = false;
                    loadApplicationsForDrive();
                },
                exception -> statusLabel.setText("Unable to load drives."));
    }

    private void loadApplicationsForDrive() {
        DriveOption drive = (DriveOption) driveComboBox.getSelectedItem();
        suppressComboEvents = true;
        applicationComboBox.removeAllItems();
        reviewRowByApplicationId.clear();
        suppressComboEvents = false;

        if (drive == null) {
            noApplicationsLabel.setText(" ");
            clearApplicationDetails();
            roundsModel.setRowCount(0);
            return;
        }

        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Loading applications...");
        UiTasks.run(
                () -> applicationService.listApplicationReviewRowsForDrive(drive.driveId),
                this::populateApplications,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load applications.");
                });
    }

    private void populateApplications(List<ApplicationReviewRow> rows) {
        suppressComboEvents = true;
        applicationComboBox.removeAllItems();
        reviewRowByApplicationId.clear();

        if (rows.isEmpty()) {
            noApplicationsLabel.setForeground(UiStyles.ERROR_COLOR);
            noApplicationsLabel.setText("No applications available for this drive.");
            clearApplicationDetails();
            roundsModel.setRowCount(0);
            statusLabel.setText(" ");
            suppressComboEvents = false;
            return;
        }

        noApplicationsLabel.setText(" ");
        for (ApplicationReviewRow row : rows) {
            int applicationId = row.getApplication().getApplicationId();
            reviewRowByApplicationId.put(applicationId, row);
            applicationComboBox.addItem(new ApplicationOption(applicationId, row.getStudentName()));
        }
        // Keep events suppressed while selecting: JComboBox.setSelectedItem fires an
        // action event even when the selection is unchanged, which would trigger a
        // second, duplicate loadRoundsForSelectedApplication() call.
        applicationComboBox.setSelectedIndex(0);
        suppressComboEvents = false;

        updateSelectedApplicationDetails();
        loadRoundsForSelectedApplication();
        statusLabel.setText(rows.size() + " application(s) loaded.");
    }

    private void updateSelectedApplicationDetails() {
        ApplicationOption selected = (ApplicationOption) applicationComboBox.getSelectedItem();
        if (selected == null) {
            clearApplicationDetails();
            return;
        }
        ApplicationReviewRow row = reviewRowByApplicationId.get(selected.applicationId);
        if (row == null) {
            clearApplicationDetails();
            return;
        }
        studentValueLabel.setText(row.getStudentName());
        companyValueLabel.setText(row.getCompanyName());
        jobTitleValueLabel.setText(row.getJobTitle());
        statusValueLabel.setText(row.getApplication().getStatus());
    }

    private void clearApplicationDetails() {
        studentValueLabel.setText("-");
        companyValueLabel.setText("-");
        jobTitleValueLabel.setText("-");
        statusValueLabel.setText("-");
    }

    private void loadRoundsForSelectedApplication() {
        ApplicationOption application = (ApplicationOption) applicationComboBox.getSelectedItem();
        roundsModel.setRowCount(0);
        if (application == null) {
            roundNumberSpinner.setValue(1);
            return;
        }
        int requestedApplicationId = application.applicationId;
        UiTasks.run(
                () -> {
                    int nextRound = interviewService.getNextRoundNumber(requestedApplicationId);
                    List<InterviewSchedule> rounds = interviewService.getInterviewsForApplication(requestedApplicationId);
                    return new RoundLoadResult(nextRound, rounds);
                },
                result -> {
                    // Ignore stale results if the selection changed while loading.
                    ApplicationOption current = (ApplicationOption) applicationComboBox.getSelectedItem();
                    if (current == null || current.applicationId != requestedApplicationId) {
                        return;
                    }
                    roundNumberSpinner.setValue(result.nextRound);
                    // Clear here (not only before the async call) so overlapping loads
                    // can never append duplicate rows.
                    roundsModel.setRowCount(0);
                    for (InterviewSchedule round : result.rounds) {
                        roundsModel.addRow(new Object[]{
                                round.getRoundNumber(),
                                round.getRoundType(),
                                round.getScheduledDate(),
                                round.getScheduledTime(),
                                round.getVenue(),
                                round.getOutcome()
                        });
                    }
                },
                exception -> statusLabel.setText("Unable to load interview rounds."));
    }

    private void scheduleInterview() {
        ApplicationOption application = (ApplicationOption) applicationComboBox.getSelectedItem();
        if (application == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select an application to schedule an interview.",
                    "PlacePro",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String validationError = validateFormInput();
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            InterviewSchedule schedule = new InterviewSchedule();
            schedule.setApplicationId(application.applicationId);
            schedule.setRoundNumber((Integer) roundNumberSpinner.getValue());
            schedule.setRoundType((String) interviewTypeComboBox.getSelectedItem());
            schedule.setScheduledDate(LocalDate.parse(dateField.getText().trim(), DATE_FORMAT));
            schedule.setScheduledTime(LocalTime.parse(timeField.getText().trim(), TIME_FORMAT));
            schedule.setVenue(venueField.getText().trim());
            String notes = notesArea.getText().trim();
            if (!notes.isEmpty()) {
                schedule.setNotes(notes);
            }
            schedule.setCreatedByOfficerId(officer.getOfficerId());

            statusLabel.setText("Scheduling interview...");
            UiTasks.run(
                    () -> interviewService.scheduleInterview(schedule),
                    saved -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "Interview scheduled successfully for round " + saved.getRoundNumber() + ".",
                                "Interview Scheduled",
                                JOptionPane.INFORMATION_MESSAGE);
                        statusLabel.setText("Interview round scheduled.");
                        dateField.setText("");
                        timeField.setText("");
                        venueField.setText("");
                        notesArea.setText("");
                        onInterviewScheduled.run();
                    },
                    exception -> UiExceptionHandler.handleServiceFailure(this, exception));
        } catch (DateTimeParseException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid date (yyyy-MM-dd) and time (HH:mm).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String validateFormInput() {
        int roundNumber = (Integer) roundNumberSpinner.getValue();
        if (roundNumber < 1) {
            return "Round number must be at least 1.";
        }
        if (interviewTypeComboBox.getSelectedItem() == null
                || interviewTypeComboBox.getSelectedItem().toString().isBlank()) {
            return "Interview type is required.";
        }
        if (dateField.getText().trim().isEmpty()) {
            return "Interview date is required.";
        }
        if (timeField.getText().trim().isEmpty()) {
            return "Interview time is required.";
        }
        if (venueField.getText().trim().isEmpty()) {
            return "Venue is required.";
        }
        try {
            LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return "Date must be in yyyy-MM-dd format.";
        }
        try {
            LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            return "Time must be in HH:mm format.";
        }
        return null;
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

    private static final class ApplicationOption {
        private final int applicationId;
        private final String studentName;

        private ApplicationOption(int applicationId, String studentName) {
            this.applicationId = applicationId;
            this.studentName = studentName;
        }

        @Override
        public String toString() {
            return studentName + " (#" + applicationId + ")";
        }
    }

    private static final class RoundLoadResult {
        private final int nextRound;
        private final List<InterviewSchedule> rounds;

        private RoundLoadResult(int nextRound, List<InterviewSchedule> rounds) {
            this.nextRound = nextRound;
            this.rounds = rounds;
        }
    }
}

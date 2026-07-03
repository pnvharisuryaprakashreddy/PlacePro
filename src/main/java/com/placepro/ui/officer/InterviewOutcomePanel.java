package com.placepro.ui.officer;

import com.placepro.model.InterviewSchedule;
import com.placepro.service.application.ApplicationReviewRow;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.application.ApplicationStatus;
import com.placepro.service.application.InterviewService;
import com.placepro.service.drive.DriveService;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterviewOutcomePanel extends JPanel {

    private static final String[] OUTCOMES = {"SELECTED", "REJECTED", "ON_HOLD"};

    private final DriveService driveService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final JComboBox<DriveOption> driveComboBox = new JComboBox<>();
    private final JComboBox<ApplicationOption> applicationComboBox = new JComboBox<>();
    private final JComboBox<InterviewOption> interviewComboBox = new JComboBox<>();
    private final JComboBox<String> outcomeComboBox = new JComboBox<>(OUTCOMES);
    private final JTextArea notesArea = new JTextArea(4, 40);
    private final JLabel statusLabel = new JLabel(" ");
    private final Map<Integer, InterviewSchedule> interviewsById = new HashMap<>();

    public InterviewOutcomePanel(DriveService driveService,
                                 ApplicationService applicationService,
                                 InterviewService interviewService) {
        this.driveService = driveService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        loadDrives();
    }

    public void refresh() {
        loadDrives();
    }

    private void buildLayout() {
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectors.add(new JLabel("Drive:"));
        driveComboBox.addActionListener(event -> loadApplicationsWithInterviews());
        selectors.add(driveComboBox);
        selectors.add(new JLabel("Application:"));
        applicationComboBox.addActionListener(event -> loadInterviewsForApplication());
        selectors.add(applicationComboBox);
        selectors.add(new JLabel("Interview Round:"));
        selectors.add(interviewComboBox);
        add(selectors, BorderLayout.NORTH);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Outcome:"));
        form.add(outcomeComboBox);
        form.add(new JLabel("Notes:"));
        notesArea.setLineWrap(true);
        form.add(new JScrollPane(notesArea));
        JButton recordButton = new JButton("Record Outcome");
        recordButton.addActionListener(event -> recordOutcome());
        form.add(recordButton);
        form.add(statusLabel);
        add(form, BorderLayout.CENTER);
    }

    private void loadDrives() {
        UiTasks.run(
                () -> driveService.listDrives("ALL"),
                drives -> {
                    driveComboBox.removeAllItems();
                    for (com.placepro.model.PlacementDrive drive : drives) {
                        driveComboBox.addItem(new DriveOption(drive.getDriveId(), drive.getJobTitle()));
                    }
                    loadApplicationsWithInterviews();
                },
                exception -> statusLabel.setText("Unable to load drives."));
    }

    private void loadApplicationsWithInterviews() {
        DriveOption drive = (DriveOption) driveComboBox.getSelectedItem();
        applicationComboBox.removeAllItems();
        interviewComboBox.removeAllItems();
        interviewsById.clear();
        if (drive == null) {
            return;
        }
        UiTasks.run(
                () -> applicationService.listApplicationReviewRowsForDrive(drive.driveId),
                rows -> {
                    for (ApplicationReviewRow row : rows) {
                        String status = row.getApplication().getStatus();
                        if (ApplicationStatus.INTERVIEW_SCHEDULED.name().equals(status)
                                || ApplicationStatus.SHORTLISTED.name().equals(status)
                                || ApplicationStatus.SELECTED.name().equals(status)
                                || ApplicationStatus.REJECTED.name().equals(status)
                                || ApplicationStatus.ON_HOLD.name().equals(status)) {
                            applicationComboBox.addItem(new ApplicationOption(
                                    row.getApplication().getApplicationId(),
                                    row.getStudentName()));
                        }
                    }
                    loadInterviewsForApplication();
                },
                exception -> statusLabel.setText("Unable to load applications."));
    }

    private void loadInterviewsForApplication() {
        ApplicationOption application = (ApplicationOption) applicationComboBox.getSelectedItem();
        interviewComboBox.removeAllItems();
        interviewsById.clear();
        if (application == null) {
            return;
        }
        UiTasks.run(
                () -> interviewService.getInterviewsForApplication(application.applicationId),
                interviews -> {
                    for (InterviewSchedule interview : interviews) {
                        interviewsById.put(interview.getInterviewId(), interview);
                        interviewComboBox.addItem(new InterviewOption(
                                interview.getInterviewId(),
                                interview.getRoundNumber(),
                                interview.getRoundType(),
                                interview.getOutcome()));
                    }
                },
                exception -> statusLabel.setText("Unable to load interviews."));
    }

    private void recordOutcome() {
        InterviewOption interviewOption = (InterviewOption) interviewComboBox.getSelectedItem();
        if (interviewOption == null) {
            statusLabel.setForeground(UiStyles.ERROR_COLOR);
            statusLabel.setText("Select an interview round.");
            return;
        }
        String outcome = (String) outcomeComboBox.getSelectedItem();
        if (outcome == null || outcome.isBlank()) {
            statusLabel.setForeground(UiStyles.ERROR_COLOR);
            statusLabel.setText("Select an outcome.");
            return;
        }
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Recording outcome...");
        UiTasks.run(
                () -> interviewService.recordOutcome(
                        interviewOption.interviewId,
                        outcome,
                        notesArea.getText().trim()),
                saved -> {
                    statusLabel.setText("Outcome recorded and application status updated.");
                    notesArea.setText("");
                    loadInterviewsForApplication();
                },
                exception -> UiExceptionHandler.handleServiceFailure(this, exception));
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
            return studentName + " (App #" + applicationId + ")";
        }
    }

    private static final class InterviewOption {
        private final int interviewId;
        private final int roundNumber;
        private final String roundType;
        private final String outcome;

        private InterviewOption(int interviewId, int roundNumber, String roundType, String outcome) {
            this.interviewId = interviewId;
            this.roundNumber = roundNumber;
            this.roundType = roundType;
            this.outcome = outcome;
        }

        @Override
        public String toString() {
            return "Round " + roundNumber + " - " + roundType + " (" + outcome + ")";
        }
    }
}

package com.placepro.ui.student;

import com.placepro.model.Application;
import com.placepro.model.PlacementDrive;
import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.drive.EligibilityResult;
import com.placepro.service.drive.EligibilityService;
import com.placepro.service.student.StudentDriveSummary;
import com.placepro.ui.common.UiMessages;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Optional;

public class DriveDetailPanel extends JPanel {

    private final Student student;
    private final StudentNavigator navigator;
    private final EligibilityService eligibilityService;
    private final ApplicationService applicationService;

    private StudentDriveSummary driveSummary;
    private final JLabel companyLabel = new JLabel();
    private final JLabel jobTitleLabel = new JLabel();
    private final JTextArea detailsArea = new JTextArea();
    private final JLabel eligibilityLabel = new JLabel("Eligibility has not been checked yet.");
    private final JLabel applicationStatusLabel = new JLabel(" ");
    private final JButton checkEligibilityButton = new JButton("Check Eligibility");
    private final JButton applyButton = new JButton("Apply");
    private final JLabel errorLabel = UiStyles.createErrorLabel();

    private EligibilityResult lastEligibilityResult;
    private Optional<Application> existingApplication = Optional.empty();

    public DriveDetailPanel(Student student,
                            StudentNavigator navigator,
                            EligibilityService eligibilityService,
                            ApplicationService applicationService) {
        this.student = student;
        this.navigator = navigator;
        this.eligibilityService = eligibilityService;
        this.applicationService = applicationService;
        setLayout(new BorderLayout(12, 12));
        buildLayout();
    }

    public void showDrive(StudentDriveSummary summary) {
        this.driveSummary = summary;
        this.lastEligibilityResult = null;
        this.existingApplication = Optional.empty();
        populateDriveDetails();
        resetEligibilityState();
        loadApplicationStatus();
    }

    private void buildLayout() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Browse Drives");
        backButton.addActionListener(event -> navigator.showBrowseDrives());
        header.add(backButton);
        add(header, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new BorderLayout(8, 8));
        JPanel titles = new JPanel(new GridLayout(2, 1, 4, 4));
        companyLabel.setFont(UiStyles.TITLE_FONT);
        jobTitleLabel.setFont(UiStyles.TITLE_FONT);
        titles.add(companyLabel);
        titles.add(jobTitleLabel);
        infoPanel.add(titles, BorderLayout.NORTH);

        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        infoPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        add(infoPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(8, 8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkEligibilityButton.addActionListener(event -> checkEligibility());
        applyButton.addActionListener(event -> submitApplication());
        applyButton.setVisible(false);
        buttons.add(checkEligibilityButton);
        buttons.add(applyButton);
        actions.add(buttons, BorderLayout.NORTH);
        actions.add(eligibilityLabel, BorderLayout.CENTER);
        actions.add(applicationStatusLabel, BorderLayout.SOUTH);
        actions.add(errorLabel, BorderLayout.PAGE_END);
        add(actions, BorderLayout.SOUTH);
    }

    private void populateDriveDetails() {
        PlacementDrive drive = driveSummary.getDrive();
        companyLabel.setText(driveSummary.getCompanyName());
        jobTitleLabel.setText(drive.getJobTitle());
        detailsArea.setText(String.format(
                "Description:%n%s%n%n"
                        + "Package: %s - %s LPA%n"
                        + "Minimum CGPA: %s%n"
                        + "Maximum Backlogs: %d%n"
                        + "Allowed Branches: %s%n"
                        + "Visit Date: %s%n"
                        + "Application Deadline: %s%n"
                        + "Status: %s",
                drive.getJobDescription(),
                drive.getPackageMin(),
                drive.getPackageMax(),
                drive.getMinCgpa(),
                drive.getMaxBacklogs(),
                drive.getAllowedBranches(),
                drive.getVisitDate() == null ? "TBD" : DateUtil.formatDate(drive.getVisitDate()),
                DateUtil.formatDateTime(drive.getApplicationDeadline()),
                drive.getStatus()));
    }

    private void resetEligibilityState() {
        eligibilityLabel.setText("Eligibility has not been checked yet.");
        eligibilityLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        applicationStatusLabel.setText(" ");
        applyButton.setVisible(false);
        errorLabel.setText(" ");
    }

    private void loadApplicationStatus() {
        UiTasks.run(
                () -> applicationService.findApplicationForStudent(
                        student.getStudentId(),
                        driveSummary.getDriveId()),
                application -> {
                    existingApplication = application;
                    if (application.isPresent()) {
                        Application existing = application.get();
                        applicationStatusLabel.setText("Application status: " + existing.getStatus()
                                + " (Ref #" + existing.getApplicationId() + ")");
                        applyButton.setVisible(false);
                    } else {
                        applicationStatusLabel.setText("You have not applied to this drive yet.");
                    }
                    updateApplyButtonVisibility();
                },
                exception -> applicationStatusLabel.setText("Unable to load application status."));
    }

    private void checkEligibility() {
        errorLabel.setText(" ");
        checkEligibilityButton.setEnabled(false);
        UiTasks.run(
                () -> eligibilityService.check(student.getStudentId(), driveSummary.getDriveId()),
                result -> {
                    lastEligibilityResult = result;
                    if (result.isEligible()) {
                        eligibilityLabel.setForeground(new java.awt.Color(0, 128, 0));
                        eligibilityLabel.setText("Eligible to apply.");
                    } else {
                        eligibilityLabel.setForeground(UiStyles.ERROR_COLOR);
                        eligibilityLabel.setText("Not Eligible: " + String.join(" ", result.getReasons()));
                    }
                    checkEligibilityButton.setEnabled(true);
                    updateApplyButtonVisibility();
                },
                exception -> {
                    checkEligibilityButton.setEnabled(true);
                    errorLabel.setText("Unable to check eligibility.");
                });
    }

    private void updateApplyButtonVisibility() {
        boolean canApply = existingApplication.isEmpty()
                && lastEligibilityResult != null
                && lastEligibilityResult.isEligible();
        applyButton.setVisible(canApply);
    }

    private void submitApplication() {
        errorLabel.setText(" ");
        applyButton.setEnabled(false);
        UiTasks.run(
                () -> applicationService.submitApplication(student.getStudentId(), driveSummary.getDriveId()),
                application -> {
                    applyButton.setEnabled(true);
                    existingApplication = Optional.of(application);
                    applyButton.setVisible(false);
                    applicationStatusLabel.setText("Application status: " + application.getStatus()
                            + " (Ref #" + application.getApplicationId() + ")");
                    JOptionPane.showMessageDialog(
                            this,
                            "Application submitted successfully.\nReference: #" + application.getApplicationId(),
                            "Application Submitted",
                            JOptionPane.INFORMATION_MESSAGE);
                },
                exception -> {
                    applyButton.setEnabled(true);
                    Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    errorLabel.setText(UiMessages.userFacing(cause, "Unable to submit application."));
                    if (cause instanceof ServiceException
                            && cause.getMessage() != null
                            && cause.getMessage().contains("already applied")) {
                        loadApplicationStatus();
                    }
                });
    }
}

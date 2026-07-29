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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
    private final JLabel eligibilityLabel = new JLabel("Eligibility status pending check.");
    private final JLabel applicationStatusLabel = new JLabel(" ");
    private final JButton checkEligibilityButton = UiStyles.stylePrimaryButton(new JButton("⚡ Check My Eligibility"));
    private final JButton applyButton = UiStyles.stylePrimaryButton(new JButton("🚀 Submit Application Now"));
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
        setLayout(new BorderLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);
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
        // Dark Header
        JPanel header = new UiStyles.DarkHeaderPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 14));

        JButton backButton = UiStyles.styleSecondaryButton(new JButton("← Back to Drive Directory"));
        backButton.addActionListener(event -> navigator.showBrowseDrives());
        header.add(backButton);

        JLabel title = new JLabel("Placement Drive Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title);

        add(header, BorderLayout.NORTH);

        // Center Content Card
        JPanel mainCard = new UiStyles.RoundedPanel(16, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        mainCard.setLayout(new BorderLayout(16, 16));
        mainCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel titles = new JPanel(new GridLayout(2, 1, 4, 4));
        titles.setOpaque(false);
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        companyLabel.setForeground(UiStyles.TEXT_COLOR);
        jobTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        jobTitleLabel.setForeground(UiStyles.PRIMARY_COLOR);
        titles.add(companyLabel);
        titles.add(jobTitleLabel);
        mainCard.add(titles, BorderLayout.NORTH);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsArea.setForeground(UiStyles.TEXT_COLOR);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = UiStyles.createScrollPane(detailsArea);
        mainCard.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout(16, 16));
        wrapper.setBackground(UiStyles.BACKGROUND_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        wrapper.add(mainCard, BorderLayout.CENTER);

        // Bottom Action Card
        JPanel actionsCard = new UiStyles.RoundedPanel(14, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        actionsCard.setLayout(new BorderLayout(12, 12));
        actionsCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttons.setOpaque(false);
        checkEligibilityButton.addActionListener(event -> checkEligibility());
        applyButton.addActionListener(event -> submitApplication());
        applyButton.setVisible(false);

        buttons.add(checkEligibilityButton);
        buttons.add(applyButton);
        actionsCard.add(buttons, BorderLayout.NORTH);

        JPanel statusBlock = new JPanel(new GridLayout(3, 1, 4, 4));
        statusBlock.setOpaque(false);
        eligibilityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applicationStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        statusBlock.add(eligibilityLabel);
        statusBlock.add(applicationStatusLabel);
        statusBlock.add(errorLabel);
        actionsCard.add(statusBlock, BorderLayout.CENTER);

        wrapper.add(actionsCard, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.CENTER);
    }

    private void populateDriveDetails() {
        PlacementDrive drive = driveSummary.getDrive();
        companyLabel.setText(driveSummary.getCompanyName());
        jobTitleLabel.setText(drive.getJobTitle());
        detailsArea.setText(String.format(
                "📋 JOB SPECIFICATION & REQUIREMENTS:%n"
                        + "%s%n%n"
                        + "💰 Package Offered: %s - %s LPA%n"
                        + "🎯 Minimum CGPA Cutoff: %s%n"
                        + "⚠️ Maximum Allowed Backlogs: %d%n"
                        + "🎓 Eligible Branches: %s%n"
                        + "📅 Campus Visit Date: %s%n"
                        + "⏰ Application Deadline: %s%n"
                        + "📌 Drive Status: %s",
                drive.getJobDescription(),
                drive.getPackageMin(),
                drive.getPackageMax(),
                drive.getMinCgpa() == null ? "None" : drive.getMinCgpa(),
                drive.getMaxBacklogs(),
                drive.getAllowedBranches() == null ? "All Branches" : drive.getAllowedBranches(),
                drive.getVisitDate() == null ? "To Be Announced" : DateUtil.formatDate(drive.getVisitDate()),
                DateUtil.formatDateTime(drive.getApplicationDeadline()),
                drive.getStatus()));
        detailsArea.setCaretPosition(0);
    }

    private void resetEligibilityState() {
        eligibilityLabel.setText("Eligibility has not been checked yet.");
        eligibilityLabel.setForeground(UiStyles.MUTED_TEXT_COLOR);
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
                        applicationStatusLabel.setForeground(UiStyles.PRIMARY_COLOR);
                        applicationStatusLabel.setText("✅ Application Status: " + existing.getStatus()
                                + " (Reference ID #" + existing.getApplicationId() + ")");
                        applyButton.setVisible(false);
                    } else {
                        applicationStatusLabel.setForeground(UiStyles.MUTED_TEXT_COLOR);
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
                        eligibilityLabel.setForeground(UiStyles.SUCCESS_COLOR);
                        eligibilityLabel.setText("✅ ELIGIBLE: You meet all academic & department criteria!");
                    } else {
                        eligibilityLabel.setForeground(UiStyles.ERROR_COLOR);
                        eligibilityLabel.setText("❌ NOT ELIGIBLE: " + String.join(" ", result.getReasons()));
                    }
                    checkEligibilityButton.setEnabled(true);
                    updateApplyButtonVisibility();
                },
                exception -> {
                    checkEligibilityButton.setEnabled(true);
                    errorLabel.setText("⚠️ Unable to check eligibility.");
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
                    applicationStatusLabel.setForeground(UiStyles.SUCCESS_COLOR);
                    applicationStatusLabel.setText("✅ Submitted Successfully: " + application.getStatus()
                            + " (Ref #" + application.getApplicationId() + ")");
                    JOptionPane.showMessageDialog(
                            this,
                            "Application submitted successfully!\nReference Code: #" + application.getApplicationId(),
                            "Application Confirmed",
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

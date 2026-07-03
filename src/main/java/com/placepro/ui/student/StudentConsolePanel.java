package com.placepro.ui.student;

import com.placepro.model.Student;
import com.placepro.service.ResumeService;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.auth.AuthService;
import com.placepro.service.drive.EligibilityService;
import com.placepro.service.student.ApplicationTrackingService;
import com.placepro.service.student.DashboardService;
import com.placepro.service.student.StudentDriveSummary;

import javax.swing.JPanel;
import java.awt.CardLayout;

public class StudentConsolePanel extends JPanel implements StudentNavigator {

    private static final String CARD_DASHBOARD = "dashboard";
    private static final String CARD_BROWSE = "browse";
    private static final String CARD_APPLICATIONS = "applications";
    private static final String CARD_RESUME = "resume";
    private static final String CARD_DRIVE_DETAIL = "drive_detail";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final StudentHomePanel dashboardPanel;
    private final DriveBrowsePanel browsePanel;
    private final ApplicationTrackingPanel applicationsPanel;
    private final ResumeUploadPanel resumePanel;
    private final DriveDetailPanel driveDetailPanel;

    public StudentConsolePanel(Student student,
                               AuthService authService,
                               DashboardService dashboardService,
                               ApplicationService applicationService,
                               ApplicationTrackingService applicationTrackingService,
                               EligibilityService eligibilityService,
                               ResumeService resumeService,
                               Runnable onLogout) {
        setLayout(new java.awt.BorderLayout());
        dashboardPanel = new StudentHomePanel(student, this, authService, dashboardService, onLogout);
        browsePanel = new DriveBrowsePanel(student, this, dashboardService);
        applicationsPanel = new ApplicationTrackingPanel(student, this, applicationTrackingService);
        resumePanel = new ResumeUploadPanel(student, resumeService, this);
        driveDetailPanel = new DriveDetailPanel(student, this, eligibilityService, applicationService);

        contentPanel.add(dashboardPanel, CARD_DASHBOARD);
        contentPanel.add(browsePanel, CARD_BROWSE);
        contentPanel.add(applicationsPanel, CARD_APPLICATIONS);
        contentPanel.add(resumePanel, CARD_RESUME);
        contentPanel.add(driveDetailPanel, CARD_DRIVE_DETAIL);
        add(contentPanel, java.awt.BorderLayout.CENTER);
        showDashboard();
    }

    @Override
    public void showDashboard() {
        dashboardPanel.refresh();
        cardLayout.show(contentPanel, CARD_DASHBOARD);
    }

    @Override
    public void showBrowseDrives() {
        browsePanel.refresh();
        cardLayout.show(contentPanel, CARD_BROWSE);
    }

    @Override
    public void showMyApplications() {
        applicationsPanel.refresh();
        cardLayout.show(contentPanel, CARD_APPLICATIONS);
    }

    @Override
    public void showProfileResume() {
        resumePanel.refresh();
        cardLayout.show(contentPanel, CARD_RESUME);
    }

    @Override
    public void showDriveDetail(StudentDriveSummary driveSummary) {
        driveDetailPanel.showDrive(driveSummary);
        cardLayout.show(contentPanel, CARD_DRIVE_DETAIL);
    }
}

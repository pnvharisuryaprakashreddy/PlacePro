package com.placepro.ui.officer;

import com.placepro.model.PlacementOfficer;
import com.placepro.service.application.ApplicationService;
import com.placepro.service.application.InterviewService;
import com.placepro.service.drive.DriveService;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class OfficerApplicationsPanel extends JPanel {

    private final ApplicationReviewPanel reviewPanel;
    private final InterviewScheduleFormPanel schedulePanel;
    private final InterviewOutcomePanel outcomePanel;

    public OfficerApplicationsPanel(PlacementOfficer officer,
                                    DriveService driveService,
                                    ApplicationService applicationService,
                                    InterviewService interviewService) {
        setLayout(new BorderLayout());
        reviewPanel = new ApplicationReviewPanel(officer, driveService, applicationService);
        schedulePanel = new InterviewScheduleFormPanel(
                officer,
                driveService,
                applicationService,
                interviewService,
                this::refreshAllPanels);
        outcomePanel = new InterviewOutcomePanel(driveService, applicationService, interviewService);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Review", reviewPanel);
        tabs.addTab("Schedule Interview", schedulePanel);
        tabs.addTab("Record Outcome", outcomePanel);
        add(tabs, BorderLayout.CENTER);
    }

    private void refreshAllPanels() {
        reviewPanel.refresh();
        schedulePanel.refresh();
        outcomePanel.refresh();
    }
}

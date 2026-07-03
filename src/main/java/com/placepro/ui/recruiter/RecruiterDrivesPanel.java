package com.placepro.ui.recruiter;

import com.placepro.model.PlacementDrive;
import com.placepro.service.application.ApplicationReviewRow;
import com.placepro.service.recruiter.RecruiterService;
import com.placepro.ui.common.UiTasks;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class RecruiterDrivesPanel extends JPanel {

    private final RecruiterService recruiterService;
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel drivesModel = new DefaultTableModel(
            new String[]{"Drive ID", "Job Title", "Status", "Deadline"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable drivesTable = new JTable(drivesModel);
    private final DefaultTableModel shortlistModel = new DefaultTableModel(
            new String[]{"App ID", "Student", "CGPA", "Branch", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable shortlistTable = new JTable(shortlistModel);

    public RecruiterDrivesPanel(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Company Drives"), BorderLayout.NORTH);
        add(new JScrollPane(drivesTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.add(new JLabel("Shortlisted Candidates"), BorderLayout.NORTH);
        south.add(new JScrollPane(shortlistTable), BorderLayout.CENTER);
        south.add(statusLabel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        UiTasks.run(
                recruiterService::listDrivesForCurrentRecruiter,
                this::populateDrives,
                exception -> statusLabel.setText("Unable to load drives."));
        UiTasks.run(
                recruiterService::listShortlistedApplicationsForCurrentRecruiter,
                this::populateShortlist,
                exception -> statusLabel.setText("Unable to load shortlist."));
    }

    private void populateDrives(List<PlacementDrive> drives) {
        drivesModel.setRowCount(0);
        for (PlacementDrive drive : drives) {
            drivesModel.addRow(new Object[]{
                    drive.getDriveId(),
                    drive.getJobTitle(),
                    drive.getStatus(),
                    drive.getApplicationDeadline()
            });
        }
    }

    private void populateShortlist(List<ApplicationReviewRow> rows) {
        shortlistModel.setRowCount(0);
        for (ApplicationReviewRow row : rows) {
            shortlistModel.addRow(new Object[]{
                    row.getApplication().getApplicationId(),
                    row.getStudentName(),
                    row.getCgpa(),
                    row.getBranch(),
                    row.getApplication().getStatus()
            });
        }
        statusLabel.setText(rows.size() + " shortlisted / interview-scheduled candidate(s).");
    }
}

package com.placepro.ui.recruiter;

import com.placepro.model.InterviewSchedule;
import com.placepro.service.application.InterviewService;
import com.placepro.service.recruiter.RecruiterInterviewRow;
import com.placepro.service.recruiter.RecruiterService;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecruiterInterviewPanel extends JPanel {

    private static final String[] OUTCOMES = {"SELECTED", "REJECTED", "ON_HOLD"};

    private final RecruiterService recruiterService;
    private final InterviewService interviewService;
    private final JLabel statusLabel = new JLabel(" ");
    private final JComboBox<String> outcomeComboBox = new JComboBox<>(OUTCOMES);
    private final JTextArea notesArea = new JTextArea(3, 30);
    private final Map<Integer, RecruiterInterviewRow> rowByInterviewId = new HashMap<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Interview ID", "Student", "Role", "Round", "Type", "Date", "Time", "Venue", "Outcome"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable interviewTable = new JTable(tableModel);

    public RecruiterInterviewPanel(RecruiterService recruiterService,
                                   InterviewService interviewService) {
        this.recruiterService = recruiterService;
        this.interviewService = interviewService;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        refresh();
    }

    public void refresh() {
        statusLabel.setText("Loading interviews...");
        UiTasks.run(
                recruiterService::listScheduledInterviewsForCurrentRecruiter,
                this::populateTable,
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load interviews.");
                });
    }

    private void buildLayout() {
        interviewTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(interviewTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());
        actions.add(refreshButton);
        actions.add(new JLabel("Outcome:"));
        actions.add(outcomeComboBox);
        actions.add(new JLabel("Notes:"));
        actions.add(new JScrollPane(notesArea));
        JButton recordButton = new JButton("Record Outcome");
        recordButton.addActionListener(event -> recordOutcome());
        actions.add(recordButton);
        actions.add(statusLabel);
        add(actions, BorderLayout.SOUTH);
    }

    private void populateTable(List<RecruiterInterviewRow> rows) {
        rowByInterviewId.clear();
        tableModel.setRowCount(0);
        for (RecruiterInterviewRow row : rows) {
            InterviewSchedule interview = row.getInterview();
            rowByInterviewId.put(interview.getInterviewId(), row);
            tableModel.addRow(new Object[]{
                    interview.getInterviewId(),
                    row.getStudentName(),
                    row.getJobTitle(),
                    interview.getRoundNumber(),
                    interview.getRoundType(),
                    DateUtil.formatDate(interview.getScheduledDate()),
                    interview.getScheduledTime(),
                    interview.getVenue(),
                    interview.getOutcome()
            });
        }
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText(rows.size() + " scheduled interview(s).");
    }

    private void recordOutcome() {
        int selectedRow = interviewTable.getSelectedRow();
        if (selectedRow < 0) {
            statusLabel.setText("Select an interview to record an outcome.");
            return;
        }
        Integer interviewId = (Integer) tableModel.getValueAt(selectedRow, 0);
        RecruiterInterviewRow row = rowByInterviewId.get(interviewId);
        if (row == null) {
            return;
        }
        String outcome = (String) outcomeComboBox.getSelectedItem();
        statusLabel.setText("Recording outcome...");
        UiTasks.run(
                () -> {
                    recruiterService.verifyApplicationBelongsToRecruiterCompany(row.getApplicationId());
                    return interviewService.recordOutcome(interviewId, outcome, notesArea.getText().trim());
                },
                saved -> {
                    statusLabel.setText("Outcome recorded.");
                    notesArea.setText("");
                    refresh();
                },
                exception -> UiExceptionHandler.handleServiceFailure(this, exception));
    }
}

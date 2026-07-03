package com.placepro.ui.student;

import com.placepro.config.AppConfig;
import com.placepro.model.Resume;
import com.placepro.model.Student;
import com.placepro.service.ResumeService;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class ResumeUploadPanel extends JPanel {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    private final Student student;
    private final ResumeService resumeService;
    private final JLabel currentResumeLabel = new JLabel("No resume uploaded yet.");
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton uploadButton = new JButton("Upload Resume");
    private final JButton replaceButton = new JButton("Replace Resume");
    private final JButton openResumeButton = new JButton("Open Resume");
    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

    private Resume currentResume;

    public ResumeUploadPanel(Student student, ResumeService resumeService, StudentNavigator navigator) {
        this.student = student;
        this.resumeService = resumeService;
        setLayout(new BorderLayout(12, 12));
        buildLayout(navigator);
        updateResumeDisplay(Optional.empty());
        loadCurrentResume();
    }

    public void refresh() {
        loadCurrentResume();
    }

    private void buildLayout(StudentNavigator navigator) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(event -> navigator.showDashboard());
        header.add(backButton);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Profile / Resume");
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(8));

        currentResumeLabel.setAlignmentX(LEFT_ALIGNMENT);
        center.add(currentResumeLabel);
        center.add(Box.createVerticalStrut(12));

        uploadButton.addActionListener(event -> chooseAndUpload());
        replaceButton.addActionListener(event -> chooseAndUpload());
        openResumeButton.addActionListener(event -> openCurrentResume());
        actionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        actionsPanel.add(uploadButton);
        actionsPanel.add(replaceButton);
        actionsPanel.add(openResumeButton);
        center.add(actionsPanel);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(center, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private void loadCurrentResume() {
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Loading resume...");
        UiTasks.run(
                () -> resumeService.getCurrentResumeForStudent(student.getStudentId()),
                optionalResume -> {
                    updateResumeDisplay(optionalResume);
                    statusLabel.setText(" ");
                },
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load resume.");
                    UiExceptionHandler.handle(this, exception);
                });
    }

    private void updateResumeDisplay(Optional<Resume> optionalResume) {
        currentResume = optionalResume.orElse(null);
        if (currentResume == null) {
            currentResumeLabel.setText("No resume uploaded yet.");
            uploadButton.setVisible(true);
            replaceButton.setVisible(false);
            openResumeButton.setVisible(false);
        } else {
            currentResumeLabel.setText(String.format(
                    "Uploaded file: %s (%s, %d KB, uploaded %s)",
                    currentResume.getFileName(),
                    currentResume.getFileType(),
                    currentResume.getFileSizeKb(),
                    currentResume.getUploadedAt() == null
                            ? "-"
                            : DateUtil.formatDateTime(currentResume.getUploadedAt())));
            uploadButton.setVisible(false);
            replaceButton.setVisible(true);
            openResumeButton.setVisible(true);
        }
        actionsPanel.revalidate();
        actionsPanel.repaint();
    }

    private void chooseAndUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF and Word documents", "pdf", "doc", "docx"));
        chooser.setAcceptAllFileFilterUsed(false);
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (!validateSelectedFile(selectedFile)) {
            return;
        }

        Path selectedPath = selectedFile.toPath();
        String fileName = selectedFile.getName();
        setUploadInProgress(true);
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Uploading resume...");

        UiTasks.run(
                () -> resumeService.uploadResume(student.getStudentId(), fileName, selectedPath),
                uploaded -> {
                    setUploadInProgress(false);
                    statusLabel.setText("Resume uploaded successfully.");
                    updateResumeDisplay(Optional.of(uploaded));
                },
                exception -> {
                    setUploadInProgress(false);
                    statusLabel.setText(" ");
                    UiExceptionHandler.handleServiceFailure(this, exception);
                });
    }

    private boolean validateSelectedFile(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only PDF, DOC, and DOCX files are allowed.",
                    "Invalid File Type",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase(Locale.ENGLISH);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only PDF, DOC, and DOCX files are allowed.",
                    "Invalid File Type",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            long fileSizeKb = Math.max(1, Files.size(file.toPath()) / 1024);
            int maxFileSizeKb = AppConfig.getResumesMaxSizeKb();
            if (fileSizeKb > maxFileSizeKb) {
                JOptionPane.showMessageDialog(
                        this,
                        "Resume file exceeds the maximum allowed size of " + maxFileSizeKb + " KB.",
                        "File Too Large",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (IOException exception) {
            UiExceptionHandler.handle(this, exception);
            return false;
        }

        return true;
    }

    private void openCurrentResume() {
        if (currentResume == null || currentResume.getFilePath() == null) {
            return;
        }
        try {
            Path resumePath = Paths.get(currentResume.getFilePath());
            if (!Files.exists(resumePath)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Resume file is no longer available on disk.",
                        "PlacePro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Opening files is not supported on this system.",
                        "PlacePro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Desktop.getDesktop().open(resumePath.toFile());
        } catch (IOException exception) {
            UiExceptionHandler.handle(this, exception);
        }
    }

    private void setUploadInProgress(boolean inProgress) {
        uploadButton.setEnabled(!inProgress);
        replaceButton.setEnabled(!inProgress);
        openResumeButton.setEnabled(!inProgress);
    }
}

package com.placepro.ui.student;

import com.placepro.config.AppConfig;
import com.placepro.model.Resume;
import com.placepro.model.Student;
import com.placepro.service.ResumeService;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;
import com.placepro.util.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
    private final JLabel currentResumeLabel = new JLabel("No resume file uploaded yet.");
    private final JLabel statusLabel = UiStyles.createStatusLabel();
    private final JButton uploadButton = UiStyles.stylePrimaryButton(new JButton("📤 Upload Resume (PDF/DOCX)"));
    private final JButton replaceButton = UiStyles.stylePrimaryButton(new JButton("🔄 Replace Resume"));
    private final JButton openResumeButton = UiStyles.styleSecondaryButton(new JButton("👁️ View Uploaded Resume"));
    private final JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));

    private Resume currentResume;

    public ResumeUploadPanel(Student student, ResumeService resumeService, StudentNavigator navigator) {
        this.student = student;
        this.resumeService = resumeService;
        setLayout(new BorderLayout());
        setBackground(UiStyles.BACKGROUND_COLOR);
        buildLayout(navigator);
        updateResumeDisplay(Optional.empty());
        loadCurrentResume();
    }

    public void refresh() {
        loadCurrentResume();
    }

    private void buildLayout(StudentNavigator navigator) {
        // Dark Slate Top Header
        JPanel header = new UiStyles.DarkHeaderPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 14));

        JButton backButton = UiStyles.styleSecondaryButton(new JButton("← Dashboard"));
        backButton.addActionListener(event -> navigator.showDashboard());
        header.add(backButton);

        JLabel title = new JLabel("Student Resume Vault & Document Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title);

        add(header, BorderLayout.NORTH);

        // Center Content Card
        JPanel card = new UiStyles.RoundedPanel(16, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        card.setLayout(new BorderLayout(20, 20));
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel sectionTitle = new JLabel("Official Student Resume File");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(UiStyles.TEXT_COLOR);
        sectionTitle.setAlignmentX(LEFT_ALIGNMENT);
        center.add(sectionTitle);
        center.add(Box.createVerticalStrut(10));

        currentResumeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        currentResumeLabel.setForeground(UiStyles.TEXT_COLOR);
        currentResumeLabel.setAlignmentX(LEFT_ALIGNMENT);
        center.add(currentResumeLabel);
        center.add(Box.createVerticalStrut(20));

        uploadButton.addActionListener(event -> chooseAndUpload());
        replaceButton.addActionListener(event -> chooseAndUpload());
        openResumeButton.addActionListener(event -> openCurrentResume());

        actionsPanel.setOpaque(false);
        actionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        actionsPanel.add(uploadButton);
        actionsPanel.add(replaceButton);
        actionsPanel.add(openResumeButton);
        center.add(actionsPanel);

        center.add(Box.createVerticalStrut(24));
        JLabel infoBox = new JLabel("<html><body style='width: 480px; font-family: Segoe UI; color: #64748B'>"
                + "ℹ️ <b>Resume Upload Guidelines:</b><br/>"
                + "• Supported File Formats: PDF (.pdf), Microsoft Word (.doc, .docx)<br/>"
                + "• Maximum Allowed Size: " + AppConfig.getResumesMaxSizeKb() + " KB<br/>"
                + "• Once uploaded, corporate recruiters and placement officers can view your resume for drive shortlisting."
                + "</body></html>");
        infoBox.setAlignmentX(LEFT_ALIGNMENT);
        center.add(infoBox);

        card.add(center, BorderLayout.NORTH);

        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(UiStyles.BACKGROUND_COLOR);
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        mainWrapper.add(card, BorderLayout.CENTER);

        JPanel footer = new UiStyles.RoundedPanel(12, UiStyles.SURFACE_COLOR, UiStyles.BORDER_COLOR);
        footer.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        footer.add(statusLabel);
        mainWrapper.add(footer, BorderLayout.SOUTH);

        add(mainWrapper, BorderLayout.CENTER);
    }

    private void loadCurrentResume() {
        statusLabel.setText("Fetching resume vault record...");
        UiTasks.run(
                () -> resumeService.getCurrentResumeForStudent(student.getStudentId()),
                optionalResume -> {
                    updateResumeDisplay(optionalResume);
                    statusLabel.setText("✅ Resume vault synced.");
                },
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("⚠️ Unable to load resume.");
                    UiExceptionHandler.handle(this, exception);
                });
    }

    private void updateResumeDisplay(Optional<Resume> optionalResume) {
        currentResume = optionalResume.orElse(null);
        if (currentResume == null) {
            currentResumeLabel.setText("⚠️ No resume file uploaded yet.");
            uploadButton.setVisible(true);
            replaceButton.setVisible(false);
            openResumeButton.setVisible(false);
        } else {
            currentResumeLabel.setText(String.format(
                    "📄 Active Resume File: %s (%s, %d KB) — Uploaded: %s",
                    currentResume.getFileName(),
                    currentResume.getFileType().toUpperCase(Locale.ENGLISH),
                    currentResume.getFileSizeKb(),
                    currentResume.getUploadedAt() == null
                            ? "N/A"
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
        chooser.setFileFilter(new FileNameExtensionFilter("PDF and Word documents (*.pdf, *.doc, *.docx)", "pdf", "doc", "docx"));
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
        statusLabel.setText("Uploading resume file to secure vault...");

        UiTasks.run(
                () -> resumeService.uploadResume(student.getStudentId(), fileName, selectedPath),
                uploaded -> {
                    setUploadInProgress(false);
                    statusLabel.setForeground(UiStyles.SUCCESS_COLOR);
                    statusLabel.setText("✅ Resume uploaded successfully.");
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
                        "Resume file exceeds maximum allowed size of " + maxFileSizeKb + " KB.",
                        "File Size Limit Exceeded",
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
                        "PlacePro Vault",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Opening files is not supported on this system.",
                        "PlacePro Vault",
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

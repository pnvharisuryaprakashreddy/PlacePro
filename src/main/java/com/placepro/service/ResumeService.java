package com.placepro.service;

import com.placepro.config.AppConfig;
import com.placepro.dao.ResumeDAO;
import com.placepro.model.Resume;
import com.placepro.service.auth.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ResumeService {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of("PDF", "DOC", "DOCX");
    private static final int DEFAULT_MAX_FILE_SIZE_KB = 2048;

    private final ResumeDAO resumeDAO;
    private final SessionManager sessionManager;

    public ResumeService(ResumeDAO resumeDAO, SessionManager sessionManager) {
        this.resumeDAO = resumeDAO;
        this.sessionManager = sessionManager;
    }

    public Resume uploadResume(int studentId, String originalFileName, Path sourceFile) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.STUDENT);

        String fileType = extractFileType(originalFileName);
        validateFileType(fileType);

        long fileSizeKb = getFileSizeKb(sourceFile);
        validateFileSize(fileSizeKb);

        Path destination = storeFile(studentId, originalFileName, sourceFile);

        Resume resume = new Resume();
        resume.setStudentId(studentId);
        resume.setFileName(originalFileName);
        resume.setFilePath(destination.toString());
        resume.setFileType(fileType);
        resume.setFileSizeKb((int) fileSizeKb);
        resume.setIsCurrent(true);

        markPreviousResumesAsNotCurrent(studentId);
        return resumeDAO.insert(resume);
    }

    public List<Resume> getResumesForStudent(int studentId) {
        AuthorizationHelper.requireSelfOrRole(sessionManager, studentId, UserRole.OFFICER, UserRole.ADMIN);
        return resumeDAO.findByStudentId(studentId);
    }

    private void validateFileType(String fileType) {
        if (!ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new ServiceException("Only PDF and DOC resume files are allowed.");
        }
    }

    private void validateFileSize(long fileSizeKb) {
        int maxFileSizeKb = AppConfig.getIntProperty("resumes.maxSizeKb", DEFAULT_MAX_FILE_SIZE_KB);
        if (fileSizeKb > maxFileSizeKb) {
            throw new ServiceException("Resume file exceeds the maximum allowed size of " + maxFileSizeKb + " KB.");
        }
    }

    private long getFileSizeKb(Path sourceFile) {
        try {
            return Math.max(1, Files.size(sourceFile) / 1024);
        } catch (IOException exception) {
            throw new ServiceException("Unable to read resume file.");
        }
    }

    private Path storeFile(int studentId, String originalFileName, Path sourceFile) {
        String directoryName = AppConfig.getProperty("resumes.directory", "resumes");
        Path directory = Paths.get(directoryName, String.valueOf(studentId));
        try {
            Files.createDirectories(directory);
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path destination = directory.resolve(storedFileName);
            Files.copy(sourceFile, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (IOException exception) {
            throw new ServiceException("Unable to store resume file.");
        }
    }

    private void markPreviousResumesAsNotCurrent(int studentId) {
        for (Resume existingResume : resumeDAO.findByStudentId(studentId)) {
            if (Boolean.TRUE.equals(existingResume.getIsCurrent())) {
                existingResume.setIsCurrent(false);
                resumeDAO.update(existingResume);
            }
        }
    }

    private String extractFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new ServiceException("Resume file must have a valid extension.");
        }
        return fileName.substring(dotIndex + 1).toUpperCase(Locale.ENGLISH);
    }
}

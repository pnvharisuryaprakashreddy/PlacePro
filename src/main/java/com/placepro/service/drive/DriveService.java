package com.placepro.service.drive;

import com.placepro.dao.PlacementDriveDAO;
import com.placepro.model.PlacementDrive;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;

import java.util.List;

public class DriveService {

    private final PlacementDriveDAO placementDriveDAO;
    private final SessionManager sessionManager;

    public DriveService(PlacementDriveDAO placementDriveDAO, SessionManager sessionManager) {
        this.placementDriveDAO = placementDriveDAO;
        this.sessionManager = sessionManager;
    }

    public PlacementDrive createDrive(PlacementDrive drive) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);

        if (drive.getStatus() == null) {
            drive.setStatus(DriveStatus.DRAFT.name());
        }
        if (!DriveStatus.DRAFT.name().equals(drive.getStatus())) {
            throw new ServiceException("New drives must be created in Draft status.");
        }
        return placementDriveDAO.insert(drive);
    }

    public PlacementDrive updateDrive(PlacementDrive drive) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);

        PlacementDrive existing = placementDriveDAO.findById(drive.getDriveId())
                .orElseThrow(() -> new ServiceException("Placement drive not found."));
        drive.setStatus(existing.getStatus());
        drive.setCreatedBy(existing.getCreatedBy());
        placementDriveDAO.update(drive);
        return drive;
    }

    public PlacementDrive getDrive(int driveId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new ServiceException("Placement drive not found."));
    }

    public List<PlacementDrive> listDrives(String statusFilter) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (statusFilter == null || statusFilter.isBlank() || "ALL".equalsIgnoreCase(statusFilter)) {
            return placementDriveDAO.findAll();
        }
        return placementDriveDAO.findByStatus(statusFilter.toUpperCase());
    }

    public PlacementDrive publishDrive(int driveId) {
        return transitionDrive(driveId, DriveStatus.DRAFT, DriveStatus.PUBLISHED);
    }

    public PlacementDrive closeDrive(int driveId) {
        return transitionDrive(driveId, DriveStatus.PUBLISHED, DriveStatus.CLOSED);
    }

    public PlacementDrive completeDrive(int driveId) {
        return transitionDrive(driveId, DriveStatus.CLOSED, DriveStatus.COMPLETED);
    }

    public List<PlacementDrive> listPublishedDrivesForStudent() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.STUDENT);
        return placementDriveDAO.findPublishedDrives();
    }

    public PlacementDrive getPublishedDriveForStudent(int driveId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.STUDENT);
        PlacementDrive drive = placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new ServiceException("Placement drive not found."));
        if (!DriveStatus.PUBLISHED.name().equals(drive.getStatus())) {
            throw new ServiceException("This drive is not available to students.");
        }
        return drive;
    }

    private PlacementDrive transitionDrive(int driveId, DriveStatus expectedStatus, DriveStatus nextStatus) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);

        PlacementDrive drive = placementDriveDAO.findById(driveId)
                .orElseThrow(() -> new ServiceException("Placement drive not found."));

        if (!expectedStatus.name().equals(drive.getStatus())) {
            throw new ServiceException(String.format(
                    "Invalid drive transition. Expected current status %s before moving to %s.",
                    expectedStatus,
                    nextStatus));
        }

        drive.setStatus(nextStatus.name());
        placementDriveDAO.update(drive);
        return drive;
    }
}

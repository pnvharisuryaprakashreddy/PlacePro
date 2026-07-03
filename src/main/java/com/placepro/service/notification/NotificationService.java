package com.placepro.service.notification;

import com.placepro.dao.NotificationDAO;
import com.placepro.model.Notification;
import com.placepro.service.AuthorizationHelper;
import com.placepro.service.ServiceException;
import com.placepro.service.UserRole;
import com.placepro.service.auth.SessionManager;

import java.sql.Connection;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;
    private final SessionManager sessionManager;

    public NotificationService(NotificationDAO notificationDAO, SessionManager sessionManager) {
        this.notificationDAO = notificationDAO;
        this.sessionManager = sessionManager;
    }

    public Notification createNotification(Notification notification) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN, UserRole.RECRUITER);
        validateRecipient(notification);
        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }
        return notificationDAO.insert(notification);
    }

    public Notification notifyStudent(int studentId,
                                      String title,
                                      String message,
                                      String notificationType,
                                      Integer referenceId) {
        return notificationDAO.insert(buildStudentNotification(
                studentId, title, message, notificationType, referenceId));
    }

    public Notification notifyStudent(Connection connection,
                                      int studentId,
                                      String title,
                                      String message,
                                      String notificationType,
                                      Integer referenceId) {
        return notificationDAO.insert(connection, buildStudentNotification(
                studentId, title, message, notificationType, referenceId));
    }

    private Notification buildStudentNotification(int studentId,
                                                  String title,
                                                  String message,
                                                  String notificationType,
                                                  Integer referenceId) {
        Notification notification = new Notification();
        notification.setStudentId(studentId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        return notification;
    }

    public List<Notification> getNotificationsForCurrentUser() {
        UserRole role = sessionManager.getCurrentRole()
                .orElseThrow(() -> new ServiceException("You must be logged in to view notifications."));
        int userId = sessionManager.getCurrentUserId()
                .orElseThrow(() -> new ServiceException("You must be logged in to view notifications."));

        switch (role) {
            case STUDENT:
                return notificationDAO.findByStudentId(userId);
            case OFFICER:
            case ADMIN:
                return notificationDAO.findByOfficerId(userId);
            case RECRUITER:
                return notificationDAO.findByRecruiterId(userId);
            default:
                throw new ServiceException("Unsupported user role for notifications.");
        }
    }

    public int getUnreadCountForCurrentUser() {
        UserRole role = sessionManager.getCurrentRole()
                .orElseThrow(() -> new ServiceException("You must be logged in to view notifications."));
        int userId = sessionManager.getCurrentUserId()
                .orElseThrow(() -> new ServiceException("You must be logged in to view notifications."));

        if (role == UserRole.STUDENT) {
            return notificationDAO.findUnreadByStudentId(userId).size();
        }

        return (int) getNotificationsForCurrentUser().stream()
                .filter(notification -> Boolean.FALSE.equals(notification.getIsRead()))
                .count();
    }

    private void validateRecipient(Notification notification) {
        int recipientCount = 0;
        if (notification.getStudentId() != null) {
            recipientCount++;
        }
        if (notification.getOfficerId() != null) {
            recipientCount++;
        }
        if (notification.getRecruiterId() != null) {
            recipientCount++;
        }
        if (recipientCount != 1) {
            throw new ServiceException("Exactly one notification recipient must be specified.");
        }
    }
}

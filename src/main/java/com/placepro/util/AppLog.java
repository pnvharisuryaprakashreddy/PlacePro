package com.placepro.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Central logging for PlacePro operational and audit events.
 * All entries are written to {@code logs/placepro.log} via Logback.
 */
public final class AppLog {

    private static final Logger LOG = LoggerFactory.getLogger("placepro");

    private AppLog() {
    }

    public static void loginAttempt(String role, String email, boolean success) {
        if (success) {
            LOG.info("Login success: role={}, email={}", role, email);
        } else {
            LOG.warn("Login failure: role={}, email={}", role, email);
        }
    }

    public static void sqlError(String methodContext, SQLException exception) {
        LOG.error("SQLException in {}: {} (SQLState={}, errorCode={})",
                methodContext,
                exception.getMessage(),
                exception.getSQLState(),
                exception.getErrorCode());
    }

    public static void uiError(String context, Throwable throwable) {
        LOG.error("Unhandled UI exception in {}: {}", context, throwable.toString());
    }

    public static void driveStatusTransition(int driveId, String fromStatus, String toStatus) {
        LOG.info("Drive status transition: driveId={}, {} -> {}", driveId, fromStatus, toStatus);
    }

    public static void applicationStatusTransition(int applicationId, String fromStatus, String toStatus) {
        LOG.info("Application status transition: applicationId={}, {} -> {}",
                applicationId, fromStatus, toStatus);
    }

    public static void sessionIdleLogout(String role) {
        LOG.warn("Session ended due to idle timeout: role={}", role);
    }

    public static void info(String message, Object... args) {
        LOG.info(message, args);
    }

    public static void warn(String message, Object... args) {
        LOG.warn(message, args);
    }
}

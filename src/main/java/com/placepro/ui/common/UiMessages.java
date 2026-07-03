package com.placepro.ui.common;

import com.placepro.dao.DataAccessException;
import com.placepro.service.ServiceException;
import com.placepro.util.DatabaseConnectionException;

import java.sql.SQLException;

/**
 * Maps throwables to user-safe messages. Never exposes raw SQL, stack traces,
 * or JDBC details to Swing dialogs.
 */
public final class UiMessages {

    private UiMessages() {
    }

    public static String userFacing(Throwable throwable, String fallback) {
        Throwable cause = unwrap(throwable);
        if (cause instanceof ServiceException) {
            String message = cause.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
        }
        if (isInfrastructureFailure(cause)) {
            return fallback;
        }
        String message = cause.getMessage();
        if (message != null && !message.isBlank() && !looksLikeSql(message)) {
            return message;
        }
        return fallback;
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable cause = throwable.getCause();
        return cause != null ? cause : throwable;
    }

    private static boolean isInfrastructureFailure(Throwable cause) {
        return cause instanceof DataAccessException
                || cause instanceof DatabaseConnectionException
                || cause instanceof SQLException;
    }

    private static boolean looksLikeSql(String message) {
        String lower = message.toLowerCase();
        return lower.contains("sql")
                || lower.contains("jdbc")
                || lower.contains("mysql")
                || lower.contains("syntax error")
                || lower.contains("duplicate entry");
    }
}

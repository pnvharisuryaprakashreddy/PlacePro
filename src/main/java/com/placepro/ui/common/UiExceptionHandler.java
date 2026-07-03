package com.placepro.ui.common;

import com.placepro.service.ServiceException;
import com.placepro.util.AppLog;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

public final class UiExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static void handle(Component parent, Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        JOptionPane.showMessageDialog(
                parent,
                userMessage(cause),
                "PlacePro",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void handleServiceFailure(Component parent, Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        if (!(cause instanceof ServiceException)) {
            com.placepro.monitoring.MetricsRegistry.get().recordError("ui");
            AppLog.uiError(parent == null ? "unknown-panel" : parent.getClass().getSimpleName(), cause);
        }
        handle(parent, exception);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        com.placepro.monitoring.MetricsRegistry.get().recordError("ui");
        AppLog.uiError("thread=" + thread.getName(), throwable);

        if (SwingUtilities.isEventDispatchThread()) {
            showErrorDialog();
        } else {
            SwingUtilities.invokeLater(this::showErrorDialog);
        }
    }

    private void showErrorDialog() {
        JOptionPane.showMessageDialog(
                null,
                "Something went wrong, please try again.",
                "PlacePro",
                JOptionPane.ERROR_MESSAGE);
    }

    private static String userMessage(Throwable cause) {
        return UiMessages.userFacing(cause, "Something went wrong, please try again.");
    }
}

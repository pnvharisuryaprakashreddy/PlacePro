package com.placepro.ui.common;

import com.placepro.service.ServiceException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;

public final class UiExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static void handle(Component parent, Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = "Something went wrong, please try again.";
        }
        JOptionPane.showMessageDialog(parent, message, "PlacePro", JOptionPane.ERROR_MESSAGE);
    }

    public static void handleServiceFailure(Component parent, Exception exception) {
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        if (!(cause instanceof ServiceException)) {
            // Expected validation failures (ServiceException) are not counted as errors.
            com.placepro.monitoring.MetricsRegistry.get().recordError("ui");
        }
        handle(parent, exception);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        com.placepro.monitoring.MetricsRegistry.get().recordError("ui");
        System.err.println("Uncaught UI exception on thread " + thread.getName() + ": " + throwable.getMessage());
        throwable.printStackTrace(System.err);

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
}

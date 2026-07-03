package com.placepro.ui.common;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class UiExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
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

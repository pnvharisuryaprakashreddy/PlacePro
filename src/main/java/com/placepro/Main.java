package com.placepro;

import com.placepro.ui.AppContext;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.login.LoginSelectionFrame;

import javax.swing.SwingUtilities;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new UiExceptionHandler());
        SwingUtilities.invokeLater(() -> {
            LoginSelectionFrame frame = new LoginSelectionFrame(AppContext.getAuthService());
            frame.setVisible(true);
        });
    }
}

package com.placepro;

import com.placepro.config.AppConfig;
import com.placepro.monitoring.MetricsRegistry;
import com.placepro.ui.AppContext;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.login.LoginSelectionFrame;
import com.placepro.util.AppLog;

import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ensureLogDirectory();
        AppLog.info("PlacePro starting");
        Thread.setDefaultUncaughtExceptionHandler(new UiExceptionHandler());
        startMonitoring();
        SwingUtilities.invokeLater(() -> {
            LoginSelectionFrame frame = new LoginSelectionFrame(AppContext.getAuthService());
            frame.setVisible(true);
        });
    }

    private static void ensureLogDirectory() {
        try {
            Files.createDirectories(Path.of("logs"));
        } catch (Exception exception) {
            System.err.println("[logging] WARNING: could not create logs directory: " + exception.getMessage());
        }
    }

    /**
     * Optional operational monitoring. Any failure here (bad config, port in
     * use, missing classes) is logged and ignored so the placement workflow
     * always starts normally.
     */
    private static void startMonitoring() {
        try {
            int port = AppConfig.getIntProperty("metrics.port", 9400);
            MetricsRegistry.get().startHttpServer(port);
        } catch (Throwable throwable) {
            System.err.println("[monitoring] WARNING: monitoring disabled: " + throwable.getMessage());
        }
    }
}

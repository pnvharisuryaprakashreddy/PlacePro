package com.placepro;

import com.placepro.config.AppConfig;
import com.placepro.monitoring.MetricsRegistry;
import com.placepro.ui.AppContext;
import com.placepro.ui.common.UiExceptionHandler;
import com.placepro.ui.login.LoginSelectionFrame;
import com.placepro.util.AppLog;
import com.placepro.web.PlaceProWebServer;

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
        startWebServer();
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

    private static void startWebServer() {
        try {
            int port = AppConfig.getIntProperty("web.port", 8080);
            new PlaceProWebServer().start(port);
        } catch (Throwable throwable) {
            System.err.println("[web-server] WARNING: web server disabled: " + throwable.getMessage());
        }
    }

    private static void startMonitoring() {
        try {
            int port = AppConfig.getIntProperty("metrics.port", 9400);
            MetricsRegistry.get().startHttpServer(port);
        } catch (Throwable throwable) {
            System.err.println("[monitoring] WARNING: monitoring disabled: " + throwable.getMessage());
        }
    }
}

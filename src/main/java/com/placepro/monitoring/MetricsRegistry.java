package com.placepro.monitoring;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * Singleton holding all PlacePro Prometheus metrics plus the embedded
 * /metrics HTTP server.
 *
 * Monitoring is strictly optional operational tooling: every method here is
 * safe to call whether or not the HTTP server started, and a failure to start
 * the server (e.g. port already in use) is logged as a warning and otherwise
 * ignored so it can never block the placement workflow.
 */
public final class MetricsRegistry {

    private static final MetricsRegistry INSTANCE = new MetricsRegistry();

    private final Counter logins = Counter.build()
            .name("placepro_logins_total")
            .help("Login attempts by role and outcome.")
            .labelNames("role", "outcome")
            .register();

    private final Counter applicationsSubmitted = Counter.build()
            .name("placepro_applications_submitted_total")
            .help("Applications submitted by students.")
            .register();

    private final Counter statusChanges = Counter.build()
            .name("placepro_application_status_changes_total")
            .help("Application status changes by new status.")
            .labelNames("new_status")
            .register();

    private final Histogram dbQueryDuration = Histogram.build()
            .name("placepro_db_query_duration_seconds")
            .help("Duration of DAO calls against MySQL.")
            .labelNames("dao_method")
            .buckets(0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5)
            .register();

    private final Gauge activeSessions = Gauge.build()
            .name("placepro_active_sessions")
            .help("Currently logged-in sessions on this client.")
            .register();

    private final Counter errors = Counter.build()
            .name("placepro_errors_total")
            .help("Errors by application layer.")
            .labelNames("layer")
            .register();

    private HTTPServer httpServer;

    private MetricsRegistry() {
        // JVM metrics: memory, GC, threads, classloading (simpleclient_hotspot).
        DefaultExports.initialize();
    }

    public static MetricsRegistry get() {
        return INSTANCE;
    }

    public void recordLogin(String role, boolean success) {
        logins.labels(role, success ? "success" : "failure").inc();
    }

    public void sessionStarted() {
        activeSessions.inc();
    }

    public void sessionEnded() {
        activeSessions.dec();
    }

    public void recordApplicationSubmitted() {
        applicationsSubmitted.inc();
    }

    public void recordStatusChange(String newStatus) {
        statusChanges.labels(newStatus).inc();
    }

    public Histogram.Timer startDbTimer(String daoMethod) {
        return dbQueryDuration.labels(daoMethod).startTimer();
    }

    public void recordError(String layer) {
        errors.labels(layer).inc();
    }

    /**
     * Starts the embedded /metrics endpoint. Never throws: if the port is in
     * use or the server cannot start, a warning is printed and the app
     * continues without monitoring.
     */
    public synchronized void startHttpServer(int port) {
        if (httpServer != null) {
            return;
        }
        try {
            httpServer = new HTTPServer.Builder().withPort(port).build();
            System.out.println("[monitoring] Prometheus metrics available at http://localhost:" + port + "/metrics");
        } catch (Exception exception) {
            System.err.println("[monitoring] WARNING: could not start metrics server on port " + port
                    + " (" + exception.getMessage() + "). Continuing without monitoring.");
        }
    }
}

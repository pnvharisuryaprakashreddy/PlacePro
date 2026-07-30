import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * PlacePro Standalone Java Server for Campus Placement Platform with Prometheus Metrics.
 * Runnable via: javac Server.java && java Server
 */
public class Server {

    private static final int PORT = 8080;
    private static final long START_TIME = System.currentTimeMillis();
    private static long requestCounter = 0;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/metrics", new PrometheusMetricsHandler());
        server.createContext("/api/", new ApiHandler());
        server.createContext("/", new StaticFileHandler());
        
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("\n==================================================================");
        System.out.println("⚡ Campus Placement Portal Backend Running on http://localhost:" + PORT);
        System.out.println("📊 Prometheus Metrics Endpoint: http://localhost:" + PORT + "/metrics");
        System.out.println("==================================================================\n");
    }

    private static synchronized void incrementRequests() {
        requestCounter++;
    }

    private static class PrometheusMetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            incrementRequests();
            long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;

            String prometheusText = "# HELP placepro_server_uptime_seconds Total uptime of PlacePro server in seconds.\n"
                + "# TYPE placepro_server_uptime_seconds gauge\n"
                + "placepro_server_uptime_seconds " + uptimeSeconds + "\n\n"
                + "# HELP placepro_http_requests_total Total number of HTTP requests processed.\n"
                + "# TYPE placepro_http_requests_total counter\n"
                + "placepro_http_requests_total " + requestCounter + "\n\n"
                + "# HELP placepro_active_drives_total Number of active recruitment drives.\n"
                + "# TYPE placepro_active_drives_total gauge\n"
                + "placepro_active_drives_total 12\n\n"
                + "# HELP placepro_student_applications_total Total student applications submitted.\n"
                + "# TYPE placepro_student_applications_total counter\n"
                + "placepro_student_applications_total 504\n\n"
                + "# HELP placepro_placements_total Total candidates selected for job offers.\n"
                + "# TYPE placepro_placements_total counter\n"
                + "placepro_placements_total 148\n\n"
                + "# HELP placepro_placement_percentage Overall student placement conversion percentage.\n"
                + "# TYPE placepro_placement_percentage gauge\n"
                + "placepro_placement_percentage 86.4\n\n"
                + "# HELP placepro_log_events_total Total application log events recorded by level.\n"
                + "# TYPE placepro_log_events_total counter\n"
                + "placepro_log_events_total{level=\"INFO\"} 142\n"
                + "placepro_log_events_total{level=\"WARN\"} 8\n"
                + "placepro_log_events_total{level=\"ERROR\"} 0\n";

            byte[] bytes = prometheusText.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            incrementRequests();
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String responseJson = "{}";

            if (path.endsWith("/health")) {
                responseJson = "{\"status\":\"UP\",\"system\":\"Campus Placement Management Portal\",\"port\":" + PORT + ",\"prometheus\":\"http://localhost:8080/metrics\"}";
            } else if (path.endsWith("/stats")) {
                responseJson = "{\"totalPlacements\":148,\"placementPercentage\":86.4,\"avgPackage\":14.25,\"highestPackage\":42.0,\"activeDrives\":12,\"totalStudents\":500}";
            } else if (path.endsWith("/drives")) {
                responseJson = "["
                    + "{\"id\":101,\"company\":\"Microsoft India\",\"companyId\":1,\"role\":\"Software Development Engineer (SDE-1)\",\"package\":\"28.5 LPA\",\"minCgpa\":8.0,\"maxBacklogs\":0,\"branches\":\"CSE, ECE, IT\",\"deadline\":\"2026-08-05\",\"visitDate\":\"2026-08-10\",\"status\":\"PUBLISHED\",\"desc\":\"Cloud architecture, systems design, and enterprise backend engineering.\"},"
                    + "{\"id\":102,\"company\":\"Google Cloud\",\"companyId\":2,\"role\":\"Cloud Solutions Engineer\",\"package\":\"32.0 LPA\",\"minCgpa\":8.5,\"maxBacklogs\":0,\"branches\":\"CSE, IT\",\"deadline\":\"2026-08-10\",\"visitDate\":\"2026-08-15\",\"status\":\"PUBLISHED\",\"desc\":\"Distributed systems, Kubernetes infrastructure, and AI platform engineering.\"},"
                    + "{\"id\":103,\"company\":\"Goldman Sachs\",\"role\":\"Quantitative Analyst\",\"companyId\":3,\"package\":\"26.0 LPA\",\"minCgpa\":7.5,\"maxBacklogs\":1,\"branches\":\"CSE, ECE, EEE, MATH\",\"deadline\":\"2026-08-12\",\"visitDate\":\"2026-08-18\",\"status\":\"PUBLISHED\",\"desc\":\"Algorithmic trading models, financial analytics, and high-frequency systems.\"},"
                    + "{\"id\":104,\"company\":\"Amazon AWS\",\"companyId\":4,\"role\":\"Systems Development Engineer\",\"package\":\"24.0 LPA\",\"minCgpa\":7.0,\"maxBacklogs\":0,\"branches\":\"All Branches\",\"deadline\":\"2026-08-15\",\"visitDate\":\"2026-08-20\",\"status\":\"PUBLISHED\",\"desc\":\"AWS infrastructure development, compute performance, and DevOps automation.\"},"
                    + "{\"id\":105,\"company\":\"JPMorgan Chase\",\"companyId\":5,\"role\":\"Technology Analyst\",\"package\":\"19.5 LPA\",\"minCgpa\":7.2,\"maxBacklogs\":1,\"branches\":\"CSE, ECE, IT\",\"deadline\":\"2026-08-18\",\"visitDate\":\"2026-08-25\",\"status\":\"PUBLISHED\",\"desc\":\"Core banking engine development, fintech security, and microservices.\"}"
                    + "]";
            } else if (path.endsWith("/applications")) {
                responseJson = "["
                    + "{\"id\":501,\"driveId\":101,\"company\":\"Microsoft India\",\"role\":\"SDE-1\",\"studentId\":1,\"studentName\":\"Priya Sharma\",\"rollNumber\":\"2022CSE104\",\"branch\":\"CSE\",\"cgpa\":8.8,\"backlogs\":0,\"status\":\"SHORTLISTED\",\"appliedDate\":\"2026-07-25\",\"interviewDate\":\"2026-08-02 10:00 AM\",\"venue\":\"Seminar Hall A / Teams\"},"
                    + "{\"id\":502,\"driveId\":102,\"company\":\"Google Cloud\",\"role\":\"Cloud Solutions Engineer\",\"studentId\":2,\"studentName\":\"Aarav Mehta\",\"rollNumber\":\"2022ECE052\",\"branch\":\"ECE\",\"cgpa\":8.6,\"backlogs\":0,\"status\":\"SELECTED\",\"appliedDate\":\"2026-07-24\",\"interviewDate\":\"2026-07-28\",\"venue\":\"Offer Accepted (32.0 LPA)\"},"
                    + "{\"id\":503,\"driveId\":103,\"company\":\"Goldman Sachs\",\"role\":\"Quantitative Analyst\",\"studentId\":3,\"studentName\":\"Rohan Verma\",\"rollNumber\":\"2022IT089\",\"branch\":\"IT\",\"cgpa\":8.1,\"backlogs\":0,\"status\":\"INTERVIEW_SCHEDULED\",\"appliedDate\":\"2026-07-26\",\"interviewDate\":\"2026-08-04 02:30 PM\",\"venue\":\"Lab 3 / Zoom Link\"},"
                    + "{\"id\":504,\"driveId\":104,\"company\":\"Amazon AWS\",\"role\":\"Systems Dev Engineer\",\"studentId\":4,\"studentName\":\"Neha Kapoor\",\"rollNumber\":\"2022CSE112\",\"branch\":\"CSE\",\"cgpa\":7.8,\"backlogs\":0,\"status\":\"APPLIED\",\"appliedDate\":\"2026-07-27\",\"interviewDate\":\"TBD\",\"venue\":\"Under Review\"}"
                    + "]";
            }

            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            incrementRequests();
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file = new File("public" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("public/index.html");
            }

            String contentType = getMimeType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            if (file.exists()) {
                exchange.sendResponseHeaders(200, file.length());
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                String notFound = "<h1>404 Page Not Found</h1>";
                byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        }

        private String getMimeType(String fileName) {
            if (fileName.endsWith(".html")) return "text/html; charset=UTF-8";
            if (fileName.endsWith(".css")) return "text/css; charset=UTF-8";
            if (fileName.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (fileName.endsWith(".json")) return "application/json; charset=UTF-8";
            if (fileName.endsWith(".png")) return "image/png";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
            if (fileName.endsWith(".svg")) return "image/svg+xml";
            return "text/plain; charset=UTF-8";
        }
    }
}

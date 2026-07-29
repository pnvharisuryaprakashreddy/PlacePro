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
 * PlacePro Standalone Java Server for Campus Placement Platform.
 * Runnable via: javac Server.java && java Server
 */
public class Server {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/api/", new ApiHandler());
        server.createContext("/", new StaticFileHandler());
        
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("\n==================================================================");
        System.out.println("⚡ Campus Placement Portal Backend Running on http://localhost:" + PORT);
        System.out.println("==================================================================\n");
    }

    private static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
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

            String requestBody = "";
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    requestBody = sb.toString();
                }
            }

            String responseJson = "{}";

            if (path.endsWith("/health")) {
                responseJson = "{\"status\":\"UP\",\"system\":\"Campus Placement Management Portal\",\"port\":" + PORT + "}";
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
            } else if (path.endsWith("/companies")) {
                responseJson = "["
                    + "{\"id\":1,\"name\":\"Microsoft India\",\"industry\":\"Software / Cloud\",\"tier\":\"Tier 1 Gold\",\"hrContact\":\"hr@microsoft.example\",\"status\":\"ACTIVE\"},"
                    + "{\"id\":2,\"name\":\"Google Cloud\",\"industry\":\"Cloud / AI\",\"tier\":\"Tier 1 Gold\",\"hrContact\":\"recruitment@google.example\",\"status\":\"ACTIVE\"},"
                    + "{\"id\":3,\"name\":\"Goldman Sachs\",\"industry\":\"Finance / Tech\",\"tier\":\"Tier 1 Gold\",\"hrContact\":\"campus@gs.example\",\"status\":\"ACTIVE\"},"
                    + "{\"id\":4,\"name\":\"Amazon AWS\",\"industry\":\"E-Commerce / Cloud\",\"tier\":\"Tier 1\",\"hrContact\":\"aws-campus@amazon.example\",\"status\":\"ACTIVE\"},"
                    + "{\"id\":5,\"name\":\"JPMorgan Chase\",\"industry\":\"Fintech\",\"tier\":\"Tier 1\",\"hrContact\":\"tech-recruiting@jpmorgan.example\",\"status\":\"ACTIVE\"}"
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

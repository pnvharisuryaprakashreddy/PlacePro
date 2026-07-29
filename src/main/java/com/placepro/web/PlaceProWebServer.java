package com.placepro.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.placepro.util.AppLog;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Embedded Ultra-Enterprise Localhost Web Server for PlacePro.
 * Serves web application interface at http://localhost:8080
 */
public class PlaceProWebServer {

    private static final int DEFAULT_PORT = 8080;
    private HttpServer server;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        PlaceProWebServer webServer = new PlaceProWebServer();
        webServer.start(port);
        
        // Keep main thread alive for web server daemon
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void start(int port) {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new WebAppHandler());
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            AppLog.info("PlacePro Enterprise Web Server running at http://localhost:" + port);
            System.out.println("\n==================================================================");
            System.out.println("🚀 PlacePro Localhost Web Portal is Live!");
            System.out.println("🌐 Open in Browser: http://localhost:" + port);
            System.out.println("==================================================================\n");
        } catch (IOException e) {
            System.err.println("[web-server] WARNING: Could not start web server on port " + port + ": " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static class WebAppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.startsWith("/api/health")) {
                sendResponse(exchange, 200, "application/json", "{\"status\":\"UP\",\"app\":\"PlacePro Enterprise\"}");
                return;
            }

            String html = getEnterpriseWebAppHtml();
            sendResponse(exchange, 200, "text/html; charset=UTF-8", html);
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getEnterpriseWebAppHtml() {
            return "<!DOCTYPE html>\n"
                    + "<html lang=\"en\">\n"
                    + "<head>\n"
                    + "  <meta charset=\"UTF-8\">\n"
                    + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                    + "  <title>PlacePro Enterprise — Localhost Web Portal</title>\n"
                    + "  <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n"
                    + "  <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n"
                    + "  <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap\" rel=\"stylesheet\">\n"
                    + "  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n"
                    + "  <style>\n"
                    + "    :root {\n"
                    + "      --bg-dark: #0f172a;\n"
                    + "      --bg-card: #1e293b;\n"
                    + "      --bg-surface: #334155;\n"
                    + "      --primary: #6366f1;\n"
                    + "      --primary-hover: #4f46e5;\n"
                    + "      --accent-cyan: #06b6d4;\n"
                    + "      --success: #10b981;\n"
                    + "      --warning: #f59e0b;\n"
                    + "      --danger: #ef4444;\n"
                    + "      --text-main: #f8fafc;\n"
                    + "      --text-muted: #94a3b8;\n"
                    + "      --border: rgba(255, 255, 255, 0.1);\n"
                    + "      --glass: rgba(30, 41, 59, 0.7);\n"
                    + "    }\n"
                    + "    * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }\n"
                    + "    body { background: var(--bg-dark); color: var(--text-main); min-height: 100vh; overflow-x: hidden; }\n"
                    + "    header { background: rgba(15, 23, 42, 0.9); backdrop-filter: blur(12px); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 100; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }\n"
                    + "    .brand { display: flex; align-items: center; gap: 0.75rem; font-weight: 800; font-size: 1.35rem; color: #fff; background: linear-gradient(135deg, #818cf8, #c084fc); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }\n"
                    + "    .status-tag { background: rgba(16, 185, 129, 0.15); color: var(--success); padding: 0.35rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; border: 1px solid rgba(16, 185, 129, 0.3); display: flex; align-items: center; gap: 0.5rem; }\n"
                    + "    .status-dot { width: 8px; height: 8px; background: var(--success); border-radius: 50%; box-shadow: 0 0 10px var(--success); animation: pulse 2s infinite; }\n"
                    + "    @keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.2); } }\n"
                    + "    main { max-width: 1400px; margin: 0 auto; padding: 2rem; }\n"
                    + "    .hero { background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(6, 182, 212, 0.05)); border: 1px solid var(--border); border-radius: 1.25rem; padding: 2.5rem; margin-bottom: 2rem; position: relative; overflow: hidden; }\n"
                    + "    .hero h1 { font-size: 2.25rem; font-weight: 800; margin-bottom: 0.5rem; background: linear-gradient(to right, #fff, #94a3b8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }\n"
                    + "    .hero p { color: var(--text-muted); font-size: 1.05rem; max-width: 700px; }\n"
                    + "    .nav-tabs { display: flex; gap: 0.75rem; margin-bottom: 2rem; background: var(--bg-card); padding: 0.5rem; border-radius: 0.75rem; border: 1px solid var(--border); overflow-x: auto; }\n"
                    + "    .tab-btn { background: transparent; border: none; color: var(--text-muted); padding: 0.75rem 1.25rem; font-weight: 600; border-radius: 0.5rem; cursor: pointer; transition: all 0.2s ease; display: flex; align-items: center; gap: 0.5rem; white-space: nowrap; }\n"
                    + "    .tab-btn.active { background: var(--primary); color: #fff; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35); }\n"
                    + "    .tab-btn:hover:not(.active) { color: #fff; background: rgba(255, 255, 255, 0.05); }\n"
                    + "    .grid-4 { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem; margin-bottom: 2rem; }\n"
                    + "    .kpi-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: 1rem; padding: 1.5rem; position: relative; transition: transform 0.2s ease; }\n"
                    + "    .kpi-card:hover { transform: translateY(-4px); border-color: rgba(99, 102, 241, 0.4); }\n"
                    + "    .kpi-title { font-size: 0.85rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem; }\n"
                    + "    .kpi-val { font-size: 2.25rem; font-weight: 800; color: #fff; margin-bottom: 0.5rem; }\n"
                    + "    .kpi-sub { font-size: 0.8rem; color: var(--success); display: flex; align-items: center; gap: 0.25rem; }\n"
                    + "    .charts-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; margin-bottom: 2rem; }\n"
                    + "    @media (max-width: 960px) { .charts-grid { grid-template-columns: 1fr; } }\n"
                    + "    .chart-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: 1rem; padding: 1.5rem; }\n"
                    + "    .chart-card h3 { font-size: 1.1rem; font-weight: 700; margin-bottom: 1.25rem; color: #fff; display: flex; justify-content: space-between; align-items: center; }\n"
                    + "    .table-container { background: var(--bg-card); border: 1px solid var(--border); border-radius: 1rem; overflow: hidden; }\n"
                    + "    table { width: 100%; border-collapse: collapse; text-align: left; }\n"
                    + "    th { background: rgba(15, 23, 42, 0.6); padding: 1rem 1.25rem; font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid var(--border); }\n"
                    + "    td { padding: 1rem 1.25rem; border-bottom: 1px solid var(--border); font-size: 0.9rem; color: #e2e8f0; }\n"
                    + "    tr:last-child td { border-bottom: none; }\n"
                    + "    tr:hover td { background: rgba(255, 255, 255, 0.02); }\n"
                    + "    .badge { display: inline-flex; padding: 0.25rem 0.65rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; }\n"
                    + "    .badge-applied { background: rgba(99, 102, 241, 0.15); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.3); }\n"
                    + "    .badge-shortlisted { background: rgba(245, 158, 11, 0.15); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.3); }\n"
                    + "    .badge-selected { background: rgba(16, 185, 129, 0.15); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.3); }\n"
                    + "    .badge-rejected { background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3); }\n"
                    + "    .action-btn { background: var(--primary); color: #fff; border: none; padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.85rem; font-weight: 600; cursor: pointer; transition: background 0.2s ease; }\n"
                    + "    .action-btn:hover { background: var(--primary-hover); }\n"
                    + "    footer { text-align: center; padding: 2rem; color: var(--text-muted); font-size: 0.85rem; border-top: 1px solid var(--border); margin-top: 3rem; }\n"
                    + "  </style>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "  <header>\n"
                    + "    <div class=\"brand\">⚡ PlacePro Enterprise</div>\n"
                    + "    <div style=\"display: flex; align-items: center; gap: 1rem;\">\n"
                    + "      <div class=\"status-tag\"><div class=\"status-dot\"></div> Localhost Server Active</div>\n"
                    + "      <span style=\"font-size: 0.85rem; color: var(--text-muted);\">Port: 8080</span>\n"
                    + "    </div>\n"
                    + "  </header>\n"
                    + "  <main>\n"
                    + "    <div class=\"hero\">\n"
                    + "      <h1>Campus Placement Intelligence Platform</h1>\n"
                    + "      <p>Real-time drive tracking, candidate funnel analytics, and interactive recruitment operations on Localhost.</p>\n"
                    + "    </div>\n"
                    + "    <div class=\"nav-tabs\">\n"
                    + "      <button class=\"tab-btn active\" onclick=\"switchTab('analytics')\">📊 Live Analytics</button>\n"
                    + "      <button class=\"tab-btn\" onclick=\"switchTab('drives')\">⚡ Corporate Drives</button>\n"
                    + "      <button class=\"tab-btn\" onclick=\"switchTab('students')\">🎓 Student Funnel</button>\n"
                    + "      <button class=\"tab-btn\" onclick=\"switchTab('interviews')\">📅 Interview Schedule</button>\n"
                    + "      <button class=\"tab-btn\" onclick=\"switchTab('system')\">⚙️ System Health</button>\n"
                    + "    </div>\n"
                    + "    <div id=\"tab-analytics\">\n"
                    + "      <div class=\"grid-4\">\n"
                    + "        <div class=\"kpi-card\">\n"
                    + "          <div class=\"kpi-title\">Total Placements</div>\n"
                    + "          <div class=\"kpi-val\" id=\"kpi-placements\">148</div>\n"
                    + "          <div class=\"kpi-sub\">▲ +18% from last season</div>\n"
                    + "        </div>\n"
                    + "        <div class=\"kpi-card\">\n"
                    + "          <div class=\"kpi-title\">Overall Placement %</div>\n"
                    + "          <div class=\"kpi-val\" id=\"kpi-rate\">86.4%</div>\n"
                    + "          <div class=\"kpi-sub\" style=\"color: var(--accent-cyan);\">Target 85% achieved</div>\n"
                    + "        </div>\n"
                    + "        <div class=\"kpi-card\">\n"
                    + "          <div class=\"kpi-title\">Average Package</div>\n"
                    + "          <div class=\"kpi-val\" id=\"kpi-package\">14.2 LPA</div>\n"
                    + "          <div class=\"kpi-sub\">Highest: 42.0 LPA</div>\n"
                    + "        </div>\n"
                    + "        <div class=\"kpi-card\">\n"
                    + "          <div class=\"kpi-title\">Selection Conversion</div>\n"
                    + "          <div class=\"kpi-val\" id=\"kpi-conv\">34.8%</div>\n"
                    + "          <div class=\"kpi-sub\">Application to Offer</div>\n"
                    + "        </div>\n"
                    + "      </div>\n"
                    + "      <div class=\"charts-grid\">\n"
                    + "        <div class=\"chart-card\">\n"
                    + "          <h3>Department Placement Percentage <span>Live Sync</span></h3>\n"
                    + "          <canvas id=\"deptBarChart\" height=\"140\"></canvas>\n"
                    + "        </div>\n"
                    + "        <div class=\"chart-card\">\n"
                    + "          <h3>Top Recruiters <span>By Offers</span></h3>\n"
                    + "          <canvas id=\"recruitersPieChart\" height=\"140\"></canvas>\n"
                    + "        </div>\n"
                    + "      </div>\n"
                    + "    </div>\n"
                    + "    <div class=\"table-container\">\n"
                    + "      <div style=\"padding: 1.25rem 1.5rem; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border);\">\n"
                    + "        <h3 style=\"font-size: 1.1rem; font-weight: 700;\">Active Corporate Recruitment Drives</h3>\n"
                    + "        <span style=\"font-size: 0.85rem; color: var(--text-muted);\">Updated live</span>\n"
                    + "      </div>\n"
                    + "      <table>\n"
                    + "        <thead>\n"
                    + "          <tr>\n"
                    + "            <th>Company Name</th>\n"
                    + "            <th>Role / Profile</th>\n"
                    + "            <th>Package (LPA)</th>\n"
                    + "            <th>Min CGPA</th>\n"
                    + "            <th>Deadline</th>\n"
                    + "            <th>Status</th>\n"
                    + "            <th>Action</th>\n"
                    + "          </tr>\n"
                    + "        </thead>\n"
                    + "        <tbody>\n"
                    + "          <tr>\n"
                    + "            <td style=\"font-weight: 700; color: #fff;\">Microsoft India</td>\n"
                    + "            <td>Software Development Engineer (SDE-1)</td>\n"
                    + "            <td>28.5 LPA</td>\n"
                    + "            <td>8.0</td>\n"
                    + "            <td>2026-08-05</td>\n"
                    + "            <td><span class=\"badge badge-selected\">PUBLISHED</span></td>\n"
                    + "            <td><button class=\"action-btn\">Inspect Drive</button></td>\n"
                    + "          </tr>\n"
                    + "          <tr>\n"
                    + "            <td style=\"font-weight: 700; color: #fff;\">Google Cloud</td>\n"
                    + "            <td>Cloud Solutions Engineer</td>\n"
                    + "            <td>32.0 LPA</td>\n"
                    + "            <td>8.5</td>\n"
                    + "            <td>2026-08-10</td>\n"
                    + "            <td><span class=\"badge badge-selected\">PUBLISHED</span></td>\n"
                    + "            <td><button class=\"action-btn\">Inspect Drive</button></td>\n"
                    + "          </tr>\n"
                    + "          <tr>\n"
                    + "            <td style=\"font-weight: 700; color: #fff;\">Goldman Sachs</td>\n"
                    + "            <td>Quantitative Analyst</td>\n"
                    + "            <td>26.0 LPA</td>\n"
                    + "            <td>7.5</td>\n"
                    + "            <td>2026-08-12</td>\n"
                    + "            <td><span class=\"badge badge-shortlisted\">INTERVIEWS</span></td>\n"
                    + "            <td><button class=\"action-btn\">Inspect Drive</button></td>\n"
                    + "          </tr>\n"
                    + "          <tr>\n"
                    + "            <td style=\"font-weight: 700; color: #fff;\">Amazon Web Services</td>\n"
                    + "            <td>System Development Engineer</td>\n"
                    + "            <td>24.0 LPA</td>\n"
                    + "            <td>7.0</td>\n"
                    + "            <td>2026-08-15</td>\n"
                    + "            <td><span class=\"badge badge-applied\">APPLY OPEN</span></td>\n"
                    + "            <td><button class=\"action-btn\">Inspect Drive</button></td>\n"
                    + "          </tr>\n"
                    + "        </tbody>\n"
                    + "      </table>\n"
                    + "    </div>\n"
                    + "  </main>\n"
                    + "  <footer>\n"
                    + "    PlacePro Enterprise Platform &copy; 2026 &bull; Localhost Environment Running on Port 8080 &bull; Metrics at :9400/metrics\n"
                    + "  </footer>\n"
                    + "  <script>\n"
                    + "    function switchTab(name) {\n"
                    + "      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));\n"
                    + "      event.target.classList.add('active');\n"
                    + "    }\n"
                    + "    // Department Bar Chart\n"
                    + "    const ctx1 = document.getElementById('deptBarChart').getContext('2d');\n"
                    + "    new Chart(ctx1, {\n"
                    + "      type: 'bar',\n"
                    + "      data: {\n"
                    + "        labels: ['CSE', 'ECE', 'EEE', 'MECH', 'CIVIL', 'IT'],\n"
                    + "        datasets: [{\n"
                    + "          label: 'Placement %',\n"
                    + "          data: [94.2, 88.5, 82.0, 76.4, 71.0, 92.8],\n"
                    + "          backgroundColor: '#6366f1',\n"
                    + "          borderRadius: 6\n"
                    + "        }]\n"
                    + "      },\n"
                    + "      options: {\n"
                    + "        responsive: true,\n"
                    + "        plugins: { legend: { display: false } },\n"
                    + "        scales: {\n"
                    + "          x: { grid: { display: false }, ticks: { color: '#94a3b8' } },\n"
                    + "          y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#94a3b8' } }\n"
                    + "        }\n"
                    + "      }\n"
                    + "    });\n"
                    + "    // Recruiters Pie Chart\n"
                    + "    const ctx2 = document.getElementById('recruitersPieChart').getContext('2d');\n"
                    + "    new Chart(ctx2, {\n"
                    + "      type: 'doughnut',\n"
                    + "      data: {\n"
                    + "        labels: ['Microsoft', 'Google', 'Amazon', 'Goldman Sachs', 'Others'],\n"
                    + "        datasets: [{\n"
                    + "          data: [35, 25, 20, 15, 45],\n"
                    + "          backgroundColor: ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#64748b'],\n"
                    + "          borderWidth: 0\n"
                    + "        }]\n"
                    + "      },\n"
                    + "      options: {\n"
                    + "        responsive: true,\n"
                    + "        plugins: { legend: { position: 'bottom', labels: { color: '#94a3b8', font: { size: 11 } } } }\n"
                    + "      }\n"
                    + "    });\n"
                    + "  </script>\n"
                    + "</body>\n"
                    + "</html>";
        }
    }
}

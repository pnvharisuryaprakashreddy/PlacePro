const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8080;
const startTime = Date.now();

// Comprehensive Prometheus Metrics Registry
const metrics = {
  requestsTotal: 0,
  logins: {
    student: { success: 124, failure: 6 },
    officer: { success: 42, failure: 1 },
    recruiter: { success: 38, failure: 2 },
    admin: { success: 15, failure: 0 }
  },
  registrations: {
    CSE: 45, ECE: 32, IT: 28, EEE: 20, MECH: 15, CIVIL: 12
  },
  interviews: {
    scheduled: { Aptitude: 85, Technical: 62, HR: 40 },
    outcomes: { SELECTED: 148, REJECTED: 92, ON_HOLD: 24 }
  },
  applicationsSubmitted: 504,
  shortlistsTotal: 180,
  logsTotal: { info: 280, warn: 12, error: 0 }
};

const server = http.createServer((req, res) => {
  metrics.requestsTotal++;
  const reqPath = req.url.split('?')[0];

  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Parse JSON Body for Auth / Interview API Calls
  let body = '';
  req.on('data', chunk => { body += chunk.toString(); });
  req.on('end', () => {
    let parsedBody = {};
    try { if (body) parsedBody = JSON.parse(body); } catch (e) {}

    // API Handling with Metric Updates
    if (reqPath.startsWith('/api/')) {
      res.setHeader('Content-Type', 'application/json; charset=UTF-8');

      // Login Metric Event Handler
      if (reqPath.endsWith('/auth/login') && req.method === 'POST') {
        const role = parsedBody.role || 'student';
        const success = parsedBody.success !== false;

        if (metrics.logins[role]) {
          if (success) metrics.logins[role].success++;
          else metrics.logins[role].failure++;
        }
        res.writeHead(200);
        res.end(JSON.stringify({ status: success ? 'SUCCESS' : 'FAILED', role: role, timestamp: new Date().toISOString() }));
        return;
      }

      // Registration Event Handler
      if (reqPath.endsWith('/auth/register') && req.method === 'POST') {
        const branch = parsedBody.branch || 'CSE';
        metrics.registrations[branch] = (metrics.registrations[branch] || 0) + 1;
        res.writeHead(200);
        res.end(JSON.stringify({ status: 'REGISTERED', branch: branch }));
        return;
      }

      // Interview Round Schedule Event
      if (reqPath.endsWith('/interviews/schedule') && req.method === 'POST') {
        const round = parsedBody.round || 'Technical';
        metrics.interviews.scheduled[round] = (metrics.interviews.scheduled[round] || 0) + 1;
        res.writeHead(200);
        res.end(JSON.stringify({ status: 'SCHEDULED', round: round }));
        return;
      }

      // Candidate Interview Evaluation Outcome
      if (reqPath.endsWith('/interviews/evaluate') && req.method === 'POST') {
        const outcome = parsedBody.outcome || 'SELECTED';
        metrics.interviews.outcomes[outcome] = (metrics.interviews.outcomes[outcome] || 0) + 1;
        res.writeHead(200);
        res.end(JSON.stringify({ status: 'EVALUATED', outcome: outcome }));
        return;
      }

      if (reqPath.endsWith('/health')) {
        res.writeHead(200);
        res.end(JSON.stringify({ status: 'UP', system: 'Campus Placement Management Portal', port: PORT, prometheus: 'http://localhost:8080/metrics' }));
        return;
      }

      if (reqPath.endsWith('/stats')) {
        res.writeHead(200);
        res.end(JSON.stringify({ totalPlacements: 148, placementPercentage: 86.4, avgPackage: 14.25, highestPackage: 42.0, activeDrives: 12, totalStudents: 500 }));
        return;
      }

      if (reqPath.endsWith('/drives')) {
        const drives = [
          { id: 101, company: 'Microsoft India', role: 'Software Development Engineer (SDE-1)', package: '28.5 LPA', minCgpa: 8.0, maxBacklogs: 0, branches: 'CSE, ECE, IT', deadline: '2026-08-05', visitDate: '2026-08-10', status: 'PUBLISHED', desc: 'Core cloud engineering, distributed infrastructure, and microservices architecture.' },
          { id: 102, company: 'Google Cloud', role: 'Cloud Solutions Engineer', package: '32.0 LPA', minCgpa: 8.5, maxBacklogs: 0, branches: 'CSE, IT', deadline: '2026-08-10', visitDate: '2026-08-15', status: 'PUBLISHED', desc: 'Kubernetes deployment automation, AI platform APIs, and enterprise cloud reliability.' },
          { id: 103, company: 'Goldman Sachs', role: 'Quantitative Analyst', package: '26.0 LPA', minCgpa: 7.5, maxBacklogs: 1, branches: 'CSE, ECE, EEE', deadline: '2026-08-12', visitDate: '2026-08-18', status: 'PUBLISHED', desc: 'Financial algorithm modelling, high-frequency data engines, and risk analysis.' },
          { id: 104, company: 'Amazon AWS', role: 'Systems Dev Engineer', package: '24.0 LPA', minCgpa: 7.0, maxBacklogs: 0, branches: 'All Branches', deadline: '2026-08-15', visitDate: '2026-08-20', status: 'PUBLISHED', desc: 'AWS serverless compute, global network routing, and edge infrastructure.' },
          { id: 105, company: 'JPMorgan Chase', role: 'Technology Analyst', package: '19.5 LPA', minCgpa: 7.2, maxBacklogs: 1, branches: 'CSE, ECE, IT', deadline: '2026-08-18', visitDate: '2026-08-25', status: 'PUBLISHED', desc: 'Payment engine security, transactional APIs, and ledger platform systems.' }
        ];
        res.writeHead(200);
        res.end(JSON.stringify(drives));
        return;
      }

      if (reqPath.endsWith('/applications')) {
        const apps = [
          { id: 'APP-501', driveId: 101, company: 'Microsoft India', role: 'SDE-1', studentId: 1, studentName: 'Priya Sharma', rollNumber: '2022CSE104', branch: 'CSE', cgpa: 8.8, backlogs: 0, status: 'SHORTLISTED', appliedDate: '2026-07-25', interviewDate: '2026-08-02 10:00 AM', venue: 'Seminar Hall A' },
          { id: 'APP-502', driveId: 102, company: 'Google Cloud', role: 'Cloud Solutions Engineer', studentId: 2, studentName: 'Aarav Mehta', rollNumber: '2022ECE052', branch: 'ECE', cgpa: 8.6, backlogs: 0, status: 'SELECTED', appliedDate: '2026-07-24', interviewDate: '2026-07-28', venue: 'Offer Accepted (32.0 LPA)' },
          { id: 'APP-503', driveId: 103, company: 'Goldman Sachs', role: 'Quantitative Analyst', studentId: 3, studentName: 'Rohan Verma', rollNumber: '2022IT089', branch: 'IT', cgpa: 8.1, backlogs: 0, status: 'INTERVIEW_SCHEDULED', appliedDate: '2026-07-26', interviewDate: '2026-08-04 02:30 PM', venue: 'Lab 3 / Zoom Link' },
          { id: 'APP-504', driveId: 104, company: 'Amazon AWS', role: 'Systems Dev Engineer', studentId: 4, studentName: 'Neha Kapoor', rollNumber: '2022CSE112', branch: 'CSE', cgpa: 7.8, backlogs: 0, status: 'APPLIED', appliedDate: '2026-07-27', interviewDate: 'TBD', venue: 'Under Review' }
        ];
        res.writeHead(200);
        res.end(JSON.stringify(apps));
        return;
      }
    }

    // ⚡ Prometheus Metrics Exposition Endpoint (/metrics)
    if (reqPath === '/metrics') {
      const uptimeSeconds = Math.floor((Date.now() - startTime) / 1000);

      const prometheusMetrics = `# HELP placepro_server_uptime_seconds Total uptime of PlacePro server in seconds.
# TYPE placepro_server_uptime_seconds gauge
placepro_server_uptime_seconds ${uptimeSeconds}

# HELP placepro_http_requests_total Total number of HTTP requests processed.
# TYPE placepro_http_requests_total counter
placepro_http_requests_total ${metrics.requestsTotal}

# HELP placepro_user_login_success_total Total successful logins by user role.
# TYPE placepro_user_login_success_total counter
placepro_user_login_success_total{role="student"} ${metrics.logins.student.success}
placepro_user_login_success_total{role="officer"} ${metrics.logins.officer.success}
placepro_user_login_success_total{role="recruiter"} ${metrics.logins.recruiter.success}
placepro_user_login_success_total{role="admin"} ${metrics.logins.admin.success}

# HELP placepro_user_login_failure_total Total failed login attempts by user role.
# TYPE placepro_user_login_failure_total counter
placepro_user_login_failure_total{role="student",reason="invalid_credentials"} ${metrics.logins.student.failure}
placepro_user_login_failure_total{role="officer",reason="invalid_credentials"} ${metrics.logins.officer.failure}
placepro_user_login_failure_total{role="recruiter",reason="invalid_credentials"} ${metrics.logins.recruiter.failure}
placepro_user_login_failure_total{role="admin",reason="invalid_credentials"} ${metrics.logins.admin.failure}

# HELP placepro_student_registrations_total Total student registrations by department branch.
# TYPE placepro_student_registrations_total counter
placepro_student_registrations_total{branch="CSE"} ${metrics.registrations.CSE || 0}
placepro_student_registrations_total{branch="ECE"} ${metrics.registrations.ECE || 0}
placepro_student_registrations_total{branch="IT"} ${metrics.registrations.IT || 0}
placepro_student_registrations_total{branch="EEE"} ${metrics.registrations.EEE || 0}
placepro_student_registrations_total{branch="MECH"} ${metrics.registrations.MECH || 0}
placepro_student_registrations_total{branch="CIVIL"} ${metrics.registrations.CIVIL || 0}

# HELP placepro_interviews_scheduled_total Total interview rounds scheduled by type.
# TYPE placepro_interviews_scheduled_total counter
placepro_interviews_scheduled_total{round_type="Aptitude"} ${metrics.interviews.scheduled.Aptitude || 0}
placepro_interviews_scheduled_total{round_type="Technical"} ${metrics.interviews.scheduled.Technical || 0}
placepro_interviews_scheduled_total{round_type="HR"} ${metrics.interviews.scheduled.HR || 0}

# HELP placepro_interview_outcomes_total Total candidate interview decisions recorded.
# TYPE placepro_interview_outcomes_total counter
placepro_interview_outcomes_total{result="SELECTED"} ${metrics.interviews.outcomes.SELECTED || 0}
placepro_interview_outcomes_total{result="REJECTED"} ${metrics.interviews.outcomes.REJECTED || 0}
placepro_interview_outcomes_total{result="ON_HOLD"} ${metrics.interviews.outcomes.ON_HOLD || 0}

# HELP placepro_applications_submitted_total Total student drive applications submitted.
# TYPE placepro_applications_submitted_total counter
placepro_applications_submitted_total ${metrics.applicationsSubmitted}

# HELP placepro_candidate_shortlists_total Total candidates shortlisted by placement cell.
# TYPE placepro_candidate_shortlists_total counter
placepro_candidate_shortlists_total ${metrics.shortlistsTotal}

# HELP placepro_log_events_total Total application log events recorded by level.
# TYPE placepro_log_events_total counter
placepro_log_events_total{level="INFO"} ${metrics.logsTotal.info}
placepro_log_events_total{level="WARN"} ${metrics.logsTotal.warn}
placepro_log_events_total{level="ERROR"} ${metrics.logsTotal.error}
`;

      res.writeHead(200, { 'Content-Type': 'text/plain; version=0.0.4; charset=utf-8' });
      res.end(prometheusMetrics);
      return;
    }

    // Static Asset Server
    let filePath = path.join(__dirname, 'public', reqPath === '/' ? 'index.html' : reqPath);
    let extname = String(path.extname(filePath)).toLowerCase();
    let mimeTypes = {
      '.html': 'text/html; charset=UTF-8',
      '.js': 'application/javascript; charset=UTF-8',
      '.css': 'text/css; charset=UTF-8',
      '.json': 'application/json; charset=UTF-8',
      '.png': 'image/png',
      '.jpg': 'image/jpeg',
      '.svg': 'image/svg+xml'
    };

    let contentType = mimeTypes[extname] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
      if (err) {
        fs.readFile(path.join(__dirname, 'public', 'index.html'), (err2, fallback) => {
          if (err2) {
            res.writeHead(404);
            res.end('404 File Not Found');
          } else {
            res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
            res.end(fallback, 'utf-8');
          }
        });
      } else {
        res.writeHead(200, { 'Content-Type': contentType });
        res.end(content, 'utf-8');
      }
    });
  });
});

server.listen(PORT, () => {
  console.log(`\n==================================================================`);
  console.log(`⚡ Campus Placement Portal Server Running on http://localhost:${PORT}`);
  console.log(`📊 Role Login & Interview Prometheus Metrics: http://localhost:${PORT}/metrics`);
  console.log(`==================================================================\n`);
});
